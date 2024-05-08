package es.udc.redes.tutorial.tcp.server;

import java.net.*;
import java.io.*;

/**
 * MonoThread TCP echo server.
 */
public class MonoThreadTcpServer {

    public static void main(String argv[]) throws IOException {
        if (argv.length != 1) {
            System.err.println("Format: es.udc.redes.tutorial.tcp.server.MonoThreadTcpServer <port>");
            System.exit(-1);
        }
        ServerSocket socket = new ServerSocket(Integer.parseInt(argv[0]));
        try {
            // Create a server socket
            // Set a timeout of 300 secs
            socket.setSoTimeout(300000);
            
            while (true) {
                // Wait for connections
                Socket socketRec = socket.accept();
                System.out.println("SERVER: Connection established with "
                        + socketRec.getInetAddress().toString());
                // Set the input channel
                BufferedReader sInput = new BufferedReader(new InputStreamReader(
                        socketRec.getInputStream()));
                // Set the output channel
                PrintWriter sOutput = new PrintWriter(socketRec.getOutputStream(), true);
                // Receive the client message
                String received = sInput.readLine();
                System.out.println("SERVER: Received " + received
                        + " from " + socketRec.getInetAddress().toString()
                        + ":" + socketRec.getPort());
                // Send response to the client
                sOutput.println(received);
                System.out.println("SERVER: Sending " + received +
                        " to " + socketRec.getInetAddress().toString() +
                        ":" + socketRec.getPort());
                // Close the streams
                sOutput.close();
                sInput.close();
            }
        // Uncomment next catch clause after implementing the logic            
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
