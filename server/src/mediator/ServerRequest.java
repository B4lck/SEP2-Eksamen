package mediator;

import utils.DataMap;

import java.util.Map;

/**
 * ServerRequest objektet bliver oprettet og delt til modellen når en klient anmoder serveren om en ressource.
 * Klassen har server message metoder til at svare klienten med en ClientMessage.
 */
public class ServerRequest {
    private String type;
    private Map<String, Object> data;
    private ClientHandler handler;

    public ServerRequest(String type, Map<String, Object> data) {
        this.type = type;
        this.data = data;
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
     * Svar klienten med en ClientMessage
     * @param message
     */
    public void respond(ClientMessage message) {
        if (handler == null) throw new IllegalStateException("Du kan kun køre respond på server messages modtaget fra klienten.");
        handler.sendMessage(message);
    }
}
