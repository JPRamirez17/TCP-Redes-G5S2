package servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import logger.Logger;

/**
 * Clase que representa el protocolo TCP del lado del servidor
 */
public class ProtocoloServidor {

    /**
     * Se encarga de transferir un archivo por medio del protocolo TCP.
     * Servidor - Cliente
     * id archivo <-
     * tamanio archivo ->
     * listo cliente <-
     * transferencia archivo ->
     * transferencia hash ->
     * @param pIn Flujo de entrada de mensajes. pIn!=null.
     * @param pOut Flujo de salida de mensajes. pOut!=null.
     * @throws IOException si existe algun error en los flujos de entrada o salida.
     * @throws NoSuchAlgorithmException si existe algun error en la creacion del MessageDigest.
     */
    public static void procesar(DataInputStream pIn, DataOutputStream pOut, int idThread, Logger logger)
            throws IOException, NoSuchAlgorithmException {
            
            // Recibe el id del archivo que el cliente quiere descargar
            String nomArchivo = (pIn.readLong() == 0) ? Servidor.ARCHIVO_0 : Servidor.ARCHIVO_1;

            // Crear el file handle para leer el archivo solicitado
            File archivo = new File(nomArchivo);
            FileInputStream lectorArchivo = new FileInputStream(archivo);
            
            // Envía al cliente el tamanio del archivo a transferir
            long size = archivo.length();
            pOut.writeLong(size);

            // Se usa el algoritmo SHA-256 para generar el hash del archivo
            MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
            // Crea el tamanio del payload de cada paquete a transferir 32KB
            byte[] buffer = new byte[32*1024];

            // Recibe el LISTO (id) del cliente para comenzar a transferir el archivo
            long idCliente = pIn.readLong();
            logger.log("Servidor delegado " + (idThread+1) + " preparado para enviar " + nomArchivo + " (" + (size/1000) + "KB) a Cliente " + (idCliente+1));
            
            // Envia el archivo por trozos
            int bytes = 0;
            long tInicio = System.currentTimeMillis();
            while (idCliente >= 0 && (bytes=lectorArchivo.read(buffer))!=-1){
                pOut.write(buffer,0,bytes); // Envia por la red
                pOut.flush();
                shaDigest.update(buffer, 0, bytes); // Actualiza el digest para crear el hash
            }
            long tTot = System.currentTimeMillis() - tInicio;
            logger.log("Servidor delegado " + (idThread+1) + " tiempo total de transferencia " + tTot + "ms a Cliente " + (idCliente+1));

            // Se genera el hash SHA-256 del archivo completo y se envia al cliente
            byte[] shaBytes = shaDigest.digest();
            pOut.write(shaBytes, 0, 32);
            
            // Recibe si el cliente recibe el archivo completo esperado
            boolean exito = pIn.readBoolean();
            logger.log("Servidor delegado " + (idThread+1) + " transferencia completa - transferencia exitosa (hash): " + exito + " a Cliente " + (idCliente+1));

            lectorArchivo.close();
    }
}
