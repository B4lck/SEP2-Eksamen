package viewmodel;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.Message;
import model.Model;

import java.util.ArrayList;

public class ChatRoomViewModel {

    private ObjectProperty<ArrayList<Message>> messagesProperty;
    private StringProperty composeMessageProperty;
    private StringProperty errorMessageProperty;
    private Model model;

    public ChatRoomViewModel(Model model) {
        this.model = model;

        this.composeMessageProperty = new SimpleStringProperty();
        this.messagesProperty = new SimpleObjectProperty<>();
        this.errorMessageProperty = new SimpleStringProperty();
    }

    public ObjectProperty<ArrayList<Message>> getMessagesProperty() {
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
        messagesProperty.set(model.getChatRoomManager().getMessages(0, 10));
    }

    public void logout() {
        model.getProfileManager().logout();
    }
}
