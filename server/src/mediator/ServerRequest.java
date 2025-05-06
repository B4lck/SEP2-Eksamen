package mediator;

import utils.DataMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * ServerRequest objektet bliver oprettet og delt til modellen når en klient anmoder serveren om en ressource.
 * Klassen har server message metoder til at svare klienten med en ClientMessage.
 */
public class ServerRequest {
    private String type;
    private Map<String, Object> data;
    private ClientHandler handler;
    private List<String> attachments;

    public ServerRequest(String type, Map<String, Object> data, List<String> attachments) {
        this.type = type;
        this.data = data;

        this.attachments = attachments;
    }

    /**
     * Hent beskedens type
     */
    public String getType() {
        return type;
    }

    /**
     * Hent beskedens data
     */
    public DataMap getData() {
        return new DataMap(data);
    }

    /**
     * Bruges af client handleren, til at binde sig selv til beskeden.
     */
    public void setHandler(ClientHandler handler) {
        if (this.handler != null) throw new IllegalStateException("Handleren er allerede sat");
        this.handler = handler;
    }

    /**
     * Henter ID'et den bruger som har sendt beskeden, eller -1, hvis brugeren er logget ud.
     */
    public long getUser() {
        return handler.getAuthenticatedUser();
    }

    /**
     * Set brugeren, som har logget ind på den her forbindelse
     * Burde kun kaldes efter authentication
     * @param userId ID'et på den bruger der skal logges ind
     */
    public void setUser(long userId) {
        handler.setAuthenticatedUser(userId);
    }

    /**
     * Hent antallet af attachments i alt
     */
    public List<String> getAttachments() {
        return attachments;
    }

    /**
     * Downlaod næste attachment
     */
    public String downloadNextAttachment() {
        if (attachments.isEmpty()) throw new IllegalStateException("Du har ikke noget attachment tilbage");

        var attachmentName = attachments.removeFirst();

        long randomId = Math.abs(new Random().nextLong());
        String randomIdString = Long.toString(randomId, 36);

        String attachmentId = randomIdString + "-" + sanitizeFileName(attachmentName);

        handler.downloadAttachment(attachmentId, attachmentName);

        if (attachments.isEmpty()) respond(new ClientMessage("DONE", new DataMap()) );

        return attachmentId;
    }

    /**
     * Svar klienten med en ClientMessage
     * @param message
     */
    public void respond(ClientMessage message) {
        if (handler == null) throw new IllegalStateException("Du kan kun køre respond på server messages modtaget fra klienten.");

        if (!attachments.isEmpty()) respond(new ClientMessage("DONE", new DataMap()) );

        handler.sendMessage(message);
    }

    /**
     * Svar klienten med rå data
     */
    public void respond(FileInputStream data, String name) {
        handler.sendFile(data, name);
    }

    private String sanitizeFileName(String fileName) {
        // Fjern eventuelle directory-dele (path traversal prevention)
        fileName = new File(fileName).getName();

        // Fjern ugyldige filsystem karakterer
        // Behold kun bogstaver, tal, bindestreg, underscore og punktum
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_")
                // Undgå skjulte filer (filer der starter med punkt)
                .replaceAll("^\\.", "_")
                // Hvis navnet er tomt efter sanitizing
                .replaceAll("^$", "unnamed_file");
    }
}
