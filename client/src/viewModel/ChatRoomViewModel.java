package viewModel;

import javafx.beans.property.*;
import model.Message;
import model.Model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public class ChatRoomViewModel implements PropertyChangeListener {

    private ListProperty<Message> messagesProperty;
    private StringProperty composeMessageProperty;
    private StringProperty errorMessageProperty;
    private Model model;

    public ChatRoomViewModel(Model model) {
        this.model = model;

        this.composeMessageProperty = new SimpleStringProperty();
        this.messagesProperty = new SimpleListProperty<>();
        this.errorMessageProperty = new SimpleStringProperty();

        model.getChatRoomManager().addListener(this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("MESSAGES")) {
            messagesProperty.setAll((ArrayList<Message>) evt.getNewValue());
        }
    }

    public ListProperty<Message> getMessagesProperty() {
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
        messagesProperty.setAll(model.getChatRoomManager().getMessages(0, 10));
    }

    public void logout() {
        model.getProfileManager().logout();
    }
}
