package mediator;

import model.UserFilesManager;
import util.Attachment;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChatClientReceiver implements Runnable {

    private ChatClient client;
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private List<Attachment> attachmentsToUpload = new ArrayList<>();

    public ChatClientReceiver(ChatClient client, Socket socket) throws IOException {
        this.client = client;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.socket = socket;
    }

    public void addAttachmentsToBeUploaded(List<Attachment> attachments) {
        this.attachmentsToUpload.addAll(attachments);
    }

    @Override
    public void run() {
        while (true) {
            try {
                String s = in.readLine();
                System.out.println("FRA SERVER: " + s);

                if (s.equals("DOWNLOAD")) {
                    // Efterfulgt af navn og størrelse på filen
                    String fileName = in.readLine();
                    long fileSize = Long.parseLong(in.readLine());

                    // Opret mappe til downloads, hvis den ikke findes
                    File dir = new File(UserFilesManager.DOWNLOADS_DIRECTORY);
                    dir.mkdirs();

                    // Opret fil
                    File file = new File(UserFilesManager.DOWNLOADS_DIRECTORY + "/" + fileName);
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
                else if (s.equals("UPLOAD")) {
                    String fileName = in.readLine();

                    if (attachmentsToUpload.isEmpty()) {
                        out.println("NO_UP");
                        continue;
                    }

                    Optional<Attachment> attachment = attachmentsToUpload.stream()
                            .filter(a -> a.getName().equals(fileName))
                            .findAny();

                    if (attachment.isEmpty()) {
                        out.println("NO_UP");
                        continue;
                    }

                    try {
                        FileInputStream fileStream = new FileInputStream(attachment.get().getFile());

                        out.println(fileStream.getChannel().size()); // Send fil-størrelse

                        in.readLine(); // Vent på klar signal

                        fileStream.transferTo(socket.getOutputStream()); // Overfør fil
                        socket.getOutputStream().flush();

                        fileStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }
                    System.out.println("UPLOAD FÆRDIG");
                }
                // Beskeden er gson
                else {
                    attachmentsToUpload.clear();
                    client.receive(s);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }
}
