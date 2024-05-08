package es.udc.redes.tutorial.tcp.server;
import java.io.IOException;
import java.net.*;

/** Multithread TCP echo server. */

public class TcpServer {

  public static void main(String argv[]) throws IOException {
    if (argv.length != 1) {
      System.err.println("Format: es.udc.redes.tutorial.tcp.server.TcpServer <port>");
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
        // Create a ServerThread object, with the new connection as parameter
        ServerThread thread = new ServerThread(socketRec);
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
