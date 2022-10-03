package cliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;

import logger.Logger;

/**
 * Clase que representa un cliente especifico TCP 
 */
public class ThreadCliente extends Thread {
    
    /**
     * Directorio de los archivos recibidos
     */
    private final static String DIR_RECIBIDOS = "ArchivosRecibidos";
    
    /**
     * Extension de los archivos recibidos
     */
    private final static String EXT_ARCHIVO = ".txt";

    /**
     * Identificador del cliente
     */
    private int id;
    
    /**
     * Cantidad de clientes concurrentes
     */
    private int cantConexiones;
    
    /**
     * Socket para la conexion con el servidor.
     */
    private Socket socket;

    /**
     * Flujo de salida del socket de red.
     */
    private DataOutputStream escritor;

    /**
     * Flujo de entrada del socket de red.
     */
    private DataInputStream lector;

    /**
     * Identificador del archivo a solicitar al servidor.
     */
    private long idArchivo;

    /**
     * Logger para almacenar el log del usuario.
     */
    private Logger logger;

    /**
     * Crea un nuevo cliente TCP delegado e inicializa sus atributos.
     * @param pId Identificador del cliente delegado. pId>=0.
     * @param pCantConexiones Cantidad de clientes concurrentes.
     * @param pCantConexiones Servidor al cual se conecta.
     * @param puerto Puerto de la conexion.
     * @param idArchivo Identificador del archivo.
     */
    public ThreadCliente(int pId, int pCantConexiones, long pIdArchivo, Logger pLogger) {
        this.id = pId;
        this.idArchivo = pIdArchivo;
        this.cantConexiones = pCantConexiones;
        this.logger = pLogger;
    }
    
    /**
     * CLiente delegado se encarga de crear un lector y escritor para el flujo de mensajes.
     * Recibe la transferencia de archivo por medio de TCP y se encarga de escribirlo localmente.
     * Cierra los flujos de entrada y salida, junto al socket.
     */
    public void run() {
        try {

            // Crea el socket en el lado cliente
            socket = new Socket(Cliente.SERVIDOR, Cliente.PUERTO);
            // Se conectan los flujos, tanto de salida como de entrada
            escritor = new DataOutputStream(socket.getOutputStream());
            lector = new DataInputStream(socket.getInputStream());

            // Nombre del archivo a escribir cuando se reciban datos del servidor
            String nomArchivo = DIR_RECIBIDOS + File.separator + (id+1) + "-Prueba-" + cantConexiones + EXT_ARCHIVO;
            
            // Se ejecuta el protocolo TCP en el lado cliente
            logger.log("Cliente " + (id+1) + " conectado a " + Cliente.SERVIDOR + ":" + Cliente.PUERTO);
            ProtocoloCliente.procesar(lector, escritor, id, idArchivo, nomArchivo, logger);

            // Se cierran los flujos y el socket
            escritor.close();
            lector.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
