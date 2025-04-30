package viewModel;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.ChatMessage;
import model.Model;
import util.ServerError;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;

public class ChatRoomViewModel implements ViewModel, PropertyChangeListener {

    private ObservableList<ViewMessage> messagesProperty;
    private StringProperty composeMessageProperty;
    private Model model;

    public ChatRoomViewModel(Model model) {
        this.model = model;

        this.composeMessageProperty = new SimpleStringProperty();
        this.messagesProperty = FXCollections.observableArrayList();

        model.getChatRoomManager().addListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            try {
                if (evt.getPropertyName().equals("MESSAGES")) {
                    messagesProperty.clear();
                    System.out.println(evt.getNewValue());
                    for (ChatMessage m : (ArrayList<ChatMessage>) evt.getNewValue()) {
                        messagesProperty.add(new ViewMessage() {{
                            sender = model.getProfileManager().getProfile(m.getSentBy()).getUsername();
                            body = m.getBody();
                            dateTime = LocalDateTime.ofEpochSecond(m.getDateTime() / 1000, (int) (m.getDateTime() % 1000 * 1000), ZoneOffset.UTC);
                        }});
                    }
                }
            } catch (ServerError e) {
                e.showAlert();
            }
        });
    }

    public ObservableList<ViewMessage> getMessagesProperty() {
        return messagesProperty;
    }

    public StringProperty getComposeMessageProperty() {
        return composeMessageProperty;
    }

    @Override
    public void reset() {
        try {
            model.getChatRoomManager().getMessages(0, 10);
        }
        catch (ServerError e) {
            e.showAlert();
        }
    }

    public void sendMessage() {
        try {
            model.getChatRoomManager().sendMessage(0, composeMessageProperty.getValue());
        }
        catch (ServerError e) {
            e.showAlert();
        }
    }
}
