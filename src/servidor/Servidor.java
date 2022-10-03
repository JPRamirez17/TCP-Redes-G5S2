package servidor;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import logger.Logger;

/**
 * Clase que representa el servidor principal TCP
 */
public class Servidor {

    /**
     * Archivos disponibles de 100MB y 150MB para transferencia
     */
    public final static String ARCHIVO_0 = "ArchivosEnviar" + File.separator + "100MB.txt";
    public final static String ARCHIVO_1 = "ArchivosEnviar" + File.separator + "250MB.txt";

    /**
     * Numero de puerto donde se van a escuchar peticiones
     */
    public final static int PUERTO = 3400;

    public static void main(String[] args) throws IOException{
        ServerSocket ss = null;
        boolean continuar = true;
        int numeroThreads = 0; // Multithread

        System.out.println("Servidor TCP ...");

        try {
            ss = new ServerSocket(PUERTO);
        } catch (IOException e) {
            System.err.println("No se pudo crear el socket en el puerto: " + PUERTO);
            System.exit(-1);
        }
        
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");
        String strAntes = dtf.format(LocalDateTime.now());
        Logger logger = new Logger(true);
        while (continuar) {
            // Crear el socket en el lado del servidor
            // Queda bloqueado esperando que llegue un cliente
            Socket socket = ss.accept();

            String strAhora = dtf.format(LocalDateTime.now());
            if(!strAhora.equals(strAntes)) {
                logger = new Logger(true);
                strAntes = strAhora;
            }

            // Crea el servidor delegado con el socket cliente y el id del thread
            ThreadServidor thread = new ThreadServidor(socket, numeroThreads, logger);
            numeroThreads ++;

            // Start
            thread.start();
        }
        ss.close();
    }

}
