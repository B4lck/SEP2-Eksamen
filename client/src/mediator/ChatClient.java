package mediator;

import com.google.gson.Gson;
import util.Attachment;
import util.ServerError;
import utils.DataMap;
import utils.PropertyChangeSubject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatClient implements PropertyChangeSubject {
    private Socket socket;
    private PrintWriter out;
    private ArrayList<ClientMessage> receivedMessages;
    private ChatClientReceiver receiver;
    private Gson gson;
    private PropertyChangeSupport property;

    private static ChatClient instance;
    private static Object lock = new Object();

    private ChatClient(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.receivedMessages = new ArrayList<>();
        this.receiver = new ChatClientReceiver(this, socket);
        var recieverThread = new Thread(receiver);
        recieverThread.start();
        this.gson = new Gson();
        this.property = new PropertyChangeSupport(this);
    }

    public static ChatClient getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    try {
                        // Hent data fra MyApplication på en eller anden måde
                        instance = new ChatClient("localhost", 42069);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return instance;
    }

    public synchronized void receive(String s) {
        System.out.println("fra server: " + s);
        ClientMessage message = gson.fromJson(s, ClientMessage.class);
        if (message.isBroadcast()) {
            property.firePropertyChange("broadcast", null, message);
        } else {
            receivedMessages.add(message);
            notify();
        }
    }

    public synchronized ClientMessage waitingForReply(String who) throws ServerError {
        System.out.println(who + " venter på en besked fra server");
        ClientMessage received = null;

        while (true) {
            if (receivedMessages.isEmpty()) {
                try {
                    wait();
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }

            received = receivedMessages.removeFirst();
            if (received.hasError()) {
                System.out.println("SERVER FEJL:");
                System.out.println(received.getError());
                throw new ServerError(received.getError());
            }

            break;
        }

        return received;
    }

    public void sendMessage(ClientMessage message) {
        out.println(gson.toJson(message));
    }

    /**
     * Send en besked med filer
     *
     * @param message     - Beskeden
     * @param attachments - Liste af attachments (filer) der skal sendes til serveren
     * @throws ServerError Hvis serveren støder på en fejl
     */
    public void sendMessageWithAttachments(ClientMessage message, List<Attachment> attachments) throws ServerError {
        // Tilføj attachments til besked
        for (Attachment attachment : attachments) {
            message.addAttachment(attachment.getName());
        }

        // Send besked
        sendMessage(message);

        // Lyt efter de forskellige kommandoer, som serveren kan anmode om, i forbindelse med at uploade filer
        listen:
        while (true) {
            ClientMessage command = waitingForReply("ChatClient attachment sender");

            System.out.println(command.getType());

            switch (command.getType()) {
                case "SEND_NEXT":
                    var attachmentIndex = command.getData().getString("attachmentName");
                    var attachment = attachments.stream()
                            .filter(a -> a.getName().equals(attachmentIndex))
                            .findAny()
                            .orElseThrow();

                    try {
                        FileInputStream fileStream = new FileInputStream(attachment.getFile());

                        out.println(fileStream.getChannel().size());
                        waitingForReply("ChatClient attachment sender venter på ready signal");
                        fileStream.transferTo(socket.getOutputStream());
                        socket.getOutputStream().flush();

                        fileStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                    break;
                case "DONE":
                    break listen;
                default:
                    throw new RuntimeException("Serveren har sendt en ugyldig kommando.. Jeg kan ikke recover, rip.");
            }
        }
    }

    public synchronized void receiveFile(File file) {
        this.receivedFile = file;
        notifyAll();
    }

    private File receivedFile = null;

    /**
     *
     */
    public synchronized File downloadFile(String fileId) throws ServerError {
        try {
            sendMessage(new ClientMessage("DOWNLOAD_FILE", new DataMap()
                    .with("fileId", fileId)));

            ClientMessage fileInfo = waitingForReply("Downloader");

            // Vent på fil
            while (receivedFile == null) {
                wait();
            }

            File file = receivedFile;
            receivedFile = null;
            return file;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
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
