package viewModel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import model.Profile;
import util.ServerError;

public class CreateEditChatRoomViewModel implements ViewModel{
    private StringProperty nameField;
    private ObservableList<ViewUser> profiles;
    private StringProperty titleText;
    private StringProperty errorText;
    private Model model;
    private ViewState viewState;

    private boolean isEdit = false;

    public CreateEditChatRoomViewModel(Model model, ViewState viewState) {
        nameField = new SimpleStringProperty();
        errorText = new SimpleStringProperty();
        titleText = new SimpleStringProperty();
        profiles = FXCollections.observableArrayList();
        this.model = model;
        this.viewState = viewState;
    }

    @Override
    public void reset() {
        if (viewState.getCurrentChatRoom() == -1) {
            isEdit = false;
            titleText.set("Opret chat rum");
            nameField.set("");
        }
        else {
            isEdit = true;
            titleText.set("Rediger chat rum");
            try {
                nameField.set(model.getChatRoomManager().getChatRoom(viewState.getCurrentChatRoom()).getName());
            } catch (ServerError e) {
                e.showAlert();
            }
        }
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
            if (isEdit) {
                // Opdater eksisterende chatrum (id: viewState.getCurrentChatRoom())
            }
            else {
                // Opret et nyt chat rum
                long room = model.getChatRoomManager().createRoom(nameField.getValue());
                for (ViewUser profile : profiles) {
                    model.getChatRoomManager().addUser(room, profile.userId);
                }
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

    public StringProperty getTitleTextProperty() {
        return titleText;
    }
}
