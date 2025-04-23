package mediator;

import com.google.gson.Gson;
import util.PropertyChangeSubject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient implements PropertyChangeSubject {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ArrayList<ClientMessage> receivedMessages;
    private ChatClientReceiver receiver;
    private Gson gson;
    private PropertyChangeSupport property;

    public ChatClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.receivedMessages = new ArrayList<>();
        this.receiver = new ChatClientReceiver(this, in);
        this.gson = new Gson();
        this.property = new PropertyChangeSupport(this);
    }

    public void receive(String s) {
        receivedMessages.add(gson.fromJson(s, ClientMessage.class));
        notify();
    }

    public synchronized ClientMessage waitingForReply(String type) throws InterruptedException {
        ClientMessage received = null;
        boolean found = false;

        while (!found) {
            while(!receivedMessages.isEmpty()) {
                received = receivedMessages.removeLast();
                found = received.getType().equals(type);
                if (found) break;
            }
            if (!found) wait();
        }

        return received;
    }

    public void sendMessage(ClientMessage message) {
        out.println(gson.toJson(message));
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        property.addPropertyChangeListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        property.removePropertyChangeListener(listener);
    }
}
