package mediator;

import com.google.gson.Gson;
import model.Model;
import model.UserFilesManager;
import utils.DataMap;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.net.Socket;

/**
 * Håndtere kommunikationen imellem serveren og en klient.
 */
public class ClientHandler implements Runnable, PropertyChangeListener {
    private final BufferedReader in;
    private final PrintWriter out;
    private final Gson gson;
    private final Model model;
    private final Socket socket;

    /**
     * Hvis forbindelsen er til en bruger der er logget ind, er dette ID'et på den bruger.
     * Hvis brugeren ikke er logget ind, er det -1.
     */
    private long currentUser = -1;

    /**
     * Opret en ny client handler, for en socket.
     *
     * @param socket       - Den forbindelse, der skal have en client handler
     * @param model        - Reference til modellen
     * @throws IOException - Hvis socket'ens streams ikke kan oprettes eller bliver uventet lukket.
     */
    public ClientHandler(Socket socket, Model model) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.gson = new Gson();
        this.model = model;

        model.addListener(this);
    }

    /**
     * Set den nuværende bruger ID
     *
     * @param userId - ID'et på den bruger, som er logget ind
     */
    public void setAuthenticatedUser(long userId) {
        currentUser = userId;
    }

    /**
     * Hvis brugeren er logget ind, hentes brugerens ID
     * Ellers returnere den -1
     */
    public long getAuthenticatedUser() {
        return currentUser;
    }

    /**
     * Kør ClientHandler på ny tråd
     */
    @Override
    public void run() {
        while (true) {
            try {
                // Håndter indgående beskeder
                String req = in.readLine();

                // Logging
                System.out.println(req);

                ClientMessage message = gson.fromJson(req, ClientMessage.class);
                ServerRequest request = new ServerRequest(message.type, message.data, message.attachments);

                request.setHandler(this);

                // Giv videre til model
                model.passServerRequest(request);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Send en besked til klienten
     *
     * @param message
     */
    public void sendMessage(ClientMessage message) {
        message.setAuthenticatedUser(currentUser);
        out.println(gson.toJson(message));
    }

    /**
     * Lytter til modellen, når modellen emitter en property change, broadcastes den til alle forbindelserne
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Broadcast
        ClientMessage message = new ClientMessage(evt.getPropertyName(), (DataMap) evt.getNewValue());
        message.broadcast = true;
        out.println(gson.toJson(message));
    }

    public void downloadAttachment(String attachmentId, String attachmentName) {
        out.println(gson.toJson(new ClientMessage("SEND_NEXT", new DataMap()
                .with("attachmentName", attachmentName))));

        try {
            // Opret fil
            File dir = new File(UserFilesManager.UPLOADS_DIRECTORY);
            dir.mkdirs();
            File file = new File(UserFilesManager.UPLOADS_DIRECTORY + "/" + attachmentId);
            file.createNewFile();

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            InputStream inputStream = socket.getInputStream();

            // Hent filstørrelsen
            long fileSize = Long.parseLong(reader.readLine());

            // Hent filen, indtil alt er hentet
            try (FileOutputStream writer = new FileOutputStream(file)) {
                byte[] buffer = new byte[8192];
                long totalBytesRead = 0;
                int bytesRead;

                out.println(gson.toJson(new ClientMessage("READY", new DataMap())));

                while (totalBytesRead < fileSize &&
                        (bytesRead = inputStream.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                    writer.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void sendFile(FileInputStream data, String name) {
        try {
            out.println("FILE");
            out.println(name);
            out.println(data.getChannel().size());

            // Venter til at klient er klar
            in.readLine();

            // Skift til BufferedOutputStream for binære data
            data.transferTo(socket.getOutputStream());
            socket.getOutputStream().flush();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
