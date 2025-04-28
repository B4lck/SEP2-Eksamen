package mediator;

import com.google.gson.Gson;
import model.Model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;

/**
 * Håndtere kommunikationen imellem serveren og en klient.
 */
public class ClientHandler implements Runnable, PropertyChangeListener {
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson;
    private Model model;

    /**
     * Hvis forbindelsen er til en bruger der er logget ind, er dette ID'et på den bruger.
     * Hvis brugeren ikke er logget ind, er det -1.
     */
    private long currentUser = -1;

    /**
     * Opret en ny client handler, for en socket.
     * @param socket Den forbindelse, der skal have en client handler
     * @param model Reference til modellen
     * @throws IOException
     */
    public ClientHandler(Socket socket, Model model) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.gson = new Gson();
        this.model = model;
    }

    /**
     * Set den nuværende bruger ID
     * @param userId ID'et på den bruger, som er logget ind
     */
    public void setAuthenticatedUser(long userId) {
        currentUser = userId;
    }

    /**
     * Hvis brugeren er logget ind, henter den brugerens ID
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
                String req = in.readLine();

                System.out.println(req);

                ClientMessage clmsg = gson.fromJson(req, ClientMessage.class);
                ServerMessage message = new ServerMessage(clmsg.type, (Map<String, Object>) clmsg.data);

                message.setHandler(this);

                System.out.println("Serveren modtog en besked af type " + message.getType());

                // Giv videre til model
                model.passClientMessage(message);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Send en besked til klienten
     * @param message
     */
    public void sendMessage(ClientMessage message) {
        System.out.println("sender besked til client");
        message.setAuthenticatedUser(currentUser);
        out.println(gson.toJson(message));
    }

    /**
     * Lytter til modellen, når modellen emitter en property change, broadcastes den til alle forbindelserne
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Broadcast
        out.println(gson.toJson(new ServerMessage(evt.getPropertyName(), (Map<String, Object>) evt.getNewValue())));
    }
}
