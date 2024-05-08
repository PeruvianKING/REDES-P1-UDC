package es.udc.redes.webserver;

import jdk.jfr.ContentType;

import java.net.*;
import java.io.*;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ServerThread extends Thread {

    private Socket socket;

    private String path;

    public ServerThread(Socket s) {
        // Store the socket s
        this.socket = s;
    }

    private void getPath() {
        boolean seguir = true;
        Path root = Paths.get("").toAbsolutePath();
        File p1 = new File("p1-files");

        while (seguir) {
            if (p1.exists()) {
                seguir = false;
                path = p1.getAbsolutePath();
            } else {
                root = root.getParent().toAbsolutePath();
                p1 = new File(root + "/p1-files");
            }
        }
    }

    private boolean compare_dates(long last_modified, String if_modified_since) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss z", Locale.ENGLISH); // Sat, 1 Jan 2000 12:00:15 GMT
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        format.setTimeZone(timeZone);
        long time;

        Date fecha_if_modified = format.parse(if_modified_since);
        time = fecha_if_modified.getTime();
        return (time + 1000) < last_modified;
    }

    private void request_info(BufferedOutputStream output, File infoFile) throws IOException {
        FileNameMap type = URLConnection.getFileNameMap();
        String mimeType = type.getContentTypeFor(infoFile.getPath());
        ZonedDateTime zoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(infoFile.lastModified()), ZoneId.systemDefault());
        ZonedDateTime zonedNow = ZonedDateTime.now();

        output.flush();
        output.write("HTTP/1.0   200 OK\n".getBytes());
        output.write(("Date: " + DateTimeFormatter.RFC_1123_DATE_TIME.format(zonedNow) + "\n").getBytes());
        output.write("Server: ServidorEjemplo\n".getBytes());
        output.write(("Content-Length: " + infoFile.length() + "\n").getBytes());
        output.write(("Content-Type: " + mimeType + "\n").getBytes());
        output.write(("Last-Modified: " + DateTimeFormatter.RFC_1123_DATE_TIME.format(zoned) + "\n\n").getBytes());
        output.flush();
    }

    private boolean request_info(BufferedOutputStream output, File infoFile, String if_modified_since_string) throws IOException, ParseException {
        FileNameMap type = URLConnection.getFileNameMap();
        String mimeType = type.getContentTypeFor(infoFile.getPath());
        ZonedDateTime zoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(infoFile.lastModified()), ZoneId.systemDefault());
        ZonedDateTime zonedNow = ZonedDateTime.now();
        boolean modif;

        modif = compare_dates(infoFile.lastModified(), if_modified_since_string);

        output.flush();
        if (modif)
            output.write("HTTP/1.0   200 OK\n".getBytes());

        else
            output.write("HTTP/1.0 304 Not Modified\n".getBytes());

        output.write(("Date: " + DateTimeFormatter.RFC_1123_DATE_TIME.format(zonedNow) + "\n").getBytes());
        output.write("Server: ServidorEjemplo\n".getBytes());
        if (modif) {
            output.write(("Content-Length: " + infoFile.length() + "\n").getBytes());
            output.write(("Content-Type: " + mimeType + "\n").getBytes());
            output.write(("Last-Modified: " + DateTimeFormatter.RFC_1123_DATE_TIME.format(zoned) + "\n\n").getBytes());
            output.flush();
            return true;
        }
        output.flush();
        return false;
    }

    public void run() {
        boolean head, if_modified_since = false, ok = true;
        try {
            getPath();
            // Set the input channel
            BufferedReader sInput = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            // Set the output channel
            PrintWriter sOutput = new PrintWriter(socket.getOutputStream(), true);
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());

            // Receive the message from the client
            List<String> received = new ArrayList<>();
            do {
                received.addLast(sInput.readLine());
            }
            while (!received.getLast().isEmpty());

            int i;
            String if_modified_string = null;
            for (i = 0; i < received.size() - 1; i++) {
                if_modified_string = received.get(i);
                if (if_modified_string.startsWith("If-Modified-Since:")) {
                    if_modified_since = true;
                    if_modified_string = if_modified_string.substring(19); //47 o 49?
                    break;
                }
            }

            System.out.println("SERVER: Received " + received
                    + " from " + socket.getInetAddress().toString()
                    + ":" + socket.getPort());

            //partir la petición en trozos para procesarla
            String[] requestParam = received.getFirst().split(" ");
            head = requestParam[0].equals("HEAD") || requestParam[0].equals("head");

            //abrimos el recurso requerido y lo mandamos
            File file = new File(path + "/" + requestParam[1]);
            FileInputStream fis = null;

            if (!file.exists()) {
                file = new File(path + "/error404.html");
                fis = new FileInputStream(file);
                out.flush();
                out.write("HTTP/1.0    404 Not Found\n".getBytes());

                ZonedDateTime zonedNow = ZonedDateTime.now();
                out.write(("Date: " + DateTimeFormatter.RFC_1123_DATE_TIME.format(zonedNow) + "\n").getBytes());

                out.write(("Content-Length: " + file.length() + "\n").getBytes());
                out.write(("Content-Type: " + Files.probeContentType(file.toPath()) + "\n\n").getBytes());
                out.flush();
                if (!head) {
                    byte[] bytes = fis.readAllBytes();
                    out.flush();
                    out.write(bytes);
                    out.flush();
                }
            } else {
                fis = new FileInputStream(file);
                if (requestParam[0].equals("GET") || requestParam[0].equals("get") || head) {
                    if (if_modified_since) {
                        ok = request_info(out, file, if_modified_string);
                    } else
                        request_info(out, file);
                    if (!head && ok) {
                        byte[] bytes = fis.readAllBytes();
                        out.flush();
                        out.write(bytes);
                        out.flush();
                    }
                } else {
                    file = new File(path + "/error400.html");
                    fis = new FileInputStream(file);

                    out.flush();
                    out.write("HTTP/1.0   400 Bad Request\n".getBytes());


                    ZonedDateTime zonedNow = ZonedDateTime.now();
                    out.write(("Date: " + DateTimeFormatter.RFC_1123_DATE_TIME.format(zonedNow) + "\n").getBytes());

                    out.write(("Content-Length: " + file.length() + "\n").getBytes());
                    out.write(("Content-Type: " + Files.probeContentType(file.toPath()) + "\n\n").getBytes());

                    byte[] bytes = fis.readAllBytes();
                    out.flush();
                    out.write(bytes);
                    out.flush();
                }
            }

            System.out.println("SERVER: Sending " + received +
                    " to " + socket.getInetAddress().toString() +
                    ":" + socket.getPort());
            // Close the streams
            sOutput.close();
            out.close();
            sInput.close();
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            // Close the client socket
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
