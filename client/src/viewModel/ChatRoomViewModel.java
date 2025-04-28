package viewModel;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Message;
import model.Model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class ChatRoomViewModel implements PropertyChangeListener {

    private ObservableList<Message> messagesProperty;
    private StringProperty composeMessageProperty;
    private StringProperty errorMessageProperty;
    private Model model;

    public ChatRoomViewModel(Model model) {
        this.model = model;

        this.composeMessageProperty = new SimpleStringProperty();
        this.messagesProperty = FXCollections.observableArrayList();
        this.errorMessageProperty = new SimpleStringProperty();

        model.getChatRoomManager().addListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            if (evt.getPropertyName().equals("MESSAGES")) {
                messagesProperty.clear();
                System.out.println(evt.getNewValue());
                for (Message m : (ArrayList<Message>) evt.getNewValue()) {
                    messagesProperty.add(m);
                }
            }
        });
    }

    public ObservableList<Message> getMessagesProperty() {
        return messagesProperty;
    }

    public StringProperty getComposeMessageProperty() {
        return composeMessageProperty;
    }

    public StringProperty getErrorMessageProperty() {
        return errorMessageProperty;
    }

    public void sendMessage() {
        model.getChatRoomManager().sendMessage(0, composeMessageProperty.getValue());
    }

    public void reset() {
        model.getChatRoomManager().getMessages(0, 10);
    }

    public void logout() {
        model.getProfileManager().logout();
    }
}
