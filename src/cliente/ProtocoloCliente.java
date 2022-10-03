package cliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import logger.Logger;

/**
 * Clase que representa el protocolo TCP del lado del cliente
 */
public class ProtocoloCliente {
    /**
     * Se encarga de recibir un archivo por medio del protocolo TCP.
     * Servidor - Cliente
     * id archivo <-
     * tamanio archivo ->
     * listo cliente <-
     * transferencia archivo ->
     * transferencia hash ->
     * Cliente crea su hash y lo compara con el recibido por el servidor
     * @param pIn Flujo de entrada de mensajes. pIn!=null.
     * @param pOut Flujo de salida de mensajes. pOut!=null.
     * @throws IOException si existe algun error en los flujos de entrada o salida.
     * @throws NoSuchAlgorithmException si existe algun error en la creacion del MessageDigest.
     */
    public static void procesar(DataInputStream  pIn, DataOutputStream pOut, int idCliente, long idArchivo, String nomArchivo, Logger logger) 
            throws IOException, NoSuchAlgorithmException {

        //Envia el id del archivo que quiere descargar
        pOut.writeLong(idArchivo);

        //Crear el file handle donde se va guardar el archivo recibido
        new File(nomArchivo);
        FileOutputStream escritorArchivo = new FileOutputStream(nomArchivo);

        //Recibe el tamanio del archivo requerido
        long size = pIn.readLong();

        // Inicializa el objeto que se encarga de realizar el hash con el algoritmo SHA-256
        MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
        // Crea el tamanio del payload de cada paquete a recibir 32KB
        byte[] buffer = new byte[32*1024];

        // Cliente LISTO (id del cliente) para la recepcion del archivo
        pOut.writeLong(idCliente);
        logger.log("Cliente " + (idCliente+1) + " preparado para recibir " + (size/1000) + "KB");

        // Recibe el archivo por trozos
        int bytes = 0;
        long tInicio = System.currentTimeMillis();
        while (size > 0 && (bytes = pIn.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1) {
            escritorArchivo.write(buffer,0,bytes); // Escribe el archivo recibido localmente
            size -= bytes;
            shaDigest.update(buffer, 0, bytes); // Actualiza el digest para crear el hash
        }
        long tTot = System.currentTimeMillis() - tInicio;
        logger.log("Cliente " + (idCliente+1) + " tiempo total de transferencia " + tTot + "ms");

        // Se genera el hash SHA-256 del archivo completo
        byte[] shaBytes = shaDigest.digest();

        //Recibe el hash del archivo por parte del servidor
        byte[] shaBytesOrg = new byte[32];
        pIn.read(shaBytesOrg, 0, shaBytesOrg.length);

        // Verificar si el hash del archivo recibido es el mismo que el que suministro el servidor
        boolean exito = Arrays.equals(shaBytes, shaBytesOrg);
        pOut.writeBoolean(exito);
        logger.log("Cliente " + (idCliente+1) + " transferencia completa - transferencia exitosa (hash): " + exito);

        escritorArchivo.close();
    }

}