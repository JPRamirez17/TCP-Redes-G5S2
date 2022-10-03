package servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import logger.Logger;

/**
 * Clase que representa un servidor TCP delegado para cada cliente
 */
public class ThreadServidor extends Thread {
    
    /**
     * Socket para intercambiar mensajes con el cliente
     */
    private Socket sktCliente;

    /**
     * Identificador del servidor TCP delegado
     */
    private int id;

    /**
     * Logger para almacenar el log del servidor.
     */
    private Logger logger;

    /**
     * Crea un nuevo servidor TCP delegado e inicializa sus atributos
     * @param pSocket Socket para intercambio de mensajes con el cliente. pSocket!=null.
     * @param pId Identificador del servidor delegado. pId>=0.
     */
    public ThreadServidor(Socket pSocket, int pId, Logger pLogger) {
        this.sktCliente = pSocket;
        this.id = pId;
        this.logger = pLogger;
    }

    /**
     * Servidor delegado se encarga de crear un lector y escritor para el flujo de mensajes.
     * Realiza la transferencia de archivo por medio de TCP.
     * Cierra los flujos de entrada y salida, junto al socket.
     */
    @Override
    public void run() {

        try {
            // Se conectan los flujos, tanto de entrada como de salida
            DataOutputStream escritor = new DataOutputStream(sktCliente.getOutputStream());
            DataInputStream lector = new DataInputStream(sktCliente.getInputStream());
                
            // Se ejecuta el protocolo en el lado del servidor
            logger.log("Servidor delegado " + (id+1) + " conectado");
            ProtocoloServidor.procesar(lector, escritor, id, logger);

            // Se cierran los flujos y el socket
            escritor.close();
            lector.close();
            sktCliente.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
