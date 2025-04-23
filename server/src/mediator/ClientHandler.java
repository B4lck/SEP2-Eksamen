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

public class ClientHandler implements Runnable, PropertyChangeListener {
    private BufferedReader in;
    private PrintWriter out;
    private Gson gson;
    private Model model;

    private long currentUser = -1;

    public ClientHandler(Socket socket, Model model) throws IOException {
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.gson = new Gson();
        this.model = model;
    }

    public void setAuthenticatedUser(long userId) {
        currentUser = userId;
    }

    public long getAuthenticatedUser() {
        return currentUser;
    }

    @Override
    public void run() {
        while (true) {
            try {
                String req = in.readLine();

                ServerMessage message = gson.fromJson(req, ServerMessage.class);

                message.setHandler(this);

                System.out.println("Serveren modtog en besked af type " + message.getType());

                // Giv videre til model
                model.passClientMessage(message);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void sendMessage(ClientMessage message) {
        message.setAuthenticatedUser(currentUser);
        out.println(gson.toJson(message));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // Broadcast
        out.println(gson.toJson(new ServerMessage<>(evt.getPropertyName(), evt.getNewValue())));
    }
}
