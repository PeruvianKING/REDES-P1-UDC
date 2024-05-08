package es.udc.redes.webserver;

import es.udc.redes.tutorial.tcp.server.ServerThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class WebServer {
    
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Format: es.udc.redes.webserver.WebServer <port>");
            System.exit(-1);
        }
        ServerSocket socket = new ServerSocket(Integer.parseInt(args[0]));

        try {
            // Create a server socket
            // Set a timeout of 300 secs
            socket.setSoTimeout(300000);
            while (true) {
                // Wait for connections
                Socket socketRec = socket.accept();
                System.out.println("SERVER: Connection established with "
                        + socketRec.getInetAddress().toString());
                // Create a ServerThread object, with the new connection as parameter
                es.udc.redes.webserver.ServerThread thread = new es.udc.redes.webserver.ServerThread(socketRec);
                // Initiate thread using the start() method
                thread.start();
            }
            // Uncomment next catch clause after implementing the logic
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally{
            try{
                socket.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    
}
