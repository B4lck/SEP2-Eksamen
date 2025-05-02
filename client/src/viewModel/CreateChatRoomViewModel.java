package viewModel;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import model.Profile;
import util.ServerError;

import java.util.ArrayList;

public class CreateChatRoomViewModel implements ViewModel{
    private StringProperty nameField;
    private ObservableList<ViewUser> profiles;
    private StringProperty errorText;
    private Model model;

    public CreateChatRoomViewModel(Model model) {
        nameField = new SimpleStringProperty();
        errorText = new SimpleStringProperty();
        profiles = FXCollections.observableArrayList();
        this.model = model;
    }

    @Override
    public void reset() {

    }

    public StringProperty getNameField() {
        return nameField;
    }

    public ObservableList<ViewUser> getProfiles() {
        return profiles;
    }

    public void addUser(long _userId) {
        try {
            Profile profile = model.getProfileManager().getProfile(_userId);
            ViewUser viewUser = new ViewUser() {{
                userId = _userId;
                username = profile.getUsername();
            }};
            profiles.add(viewUser);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public boolean confirm() {
        if (nameField.isEmpty().get()) {
            errorText.setValue("Brormand der mangler et navn!!!");
            return false;
        }
        try {
            long room = model.getChatRoomManager().createRoom(nameField.getValue());
            for (ViewUser profile : profiles) {
                model.getChatRoomManager().addUser(room, profile.userId);
            }
        } catch (ServerError e) {
            e.showAlert();
            return false;
        }
        return true;
    }

    public StringProperty getErrorTextProperty() {
        return errorText;
    }
}
