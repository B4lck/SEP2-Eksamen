package mediator;

import model.UserFileManager;

import java.io.*;
import java.net.Socket;

public class ChatClientReceiver implements Runnable {

    private ChatClient client;
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;

    public ChatClientReceiver(ChatClient client, Socket socket) throws IOException {
        this.client = client;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.socket = socket;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String s = in.readLine();

                // Håndter downloads, når serveren vil sende en fil, sender den første FILE
                if (s.equals("FILE")) {
                    // Efterfulgt af navn og størrelse på filen
                    String fileName = in.readLine();
                    long fileSize = Long.parseLong(in.readLine());

                    // Opret mappe til downloads, hvis den ikke findes
                    File dir = new File(UserFileManager.DOWNLOADS_DIRECTORY);
                    dir.mkdirs();

                    // Opret fil
                    File file = new File(UserFileManager.DOWNLOADS_DIRECTORY + "/" + fileName);
                    file.createNewFile();

                    // Hent filen, indtil alt er hentet
                    try (FileOutputStream writer = new FileOutputStream(file)) {
                        InputStream inputStream = socket.getInputStream();

                        // Midlertidig buffer til at flytte data fra input stream til writer
                        byte[] buffer = new byte[8192];
                        long totalBytesRead = 0;
                        int bytesRead;

                        // Send besked til serveren, at vi er klar
                        out.println("READY");

                        // Aflæs og skriv alle bytes
                        while (totalBytesRead < fileSize &&
                                (bytesRead = inputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                            writer.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                        }
                    }

                    // Giv filen videre til hvor den venter på den
                    client.receiveFile(file);
                }
                // Beskeden er gson
                else {
                    client.receive(s);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}
