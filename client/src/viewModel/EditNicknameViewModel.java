package viewModel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import model.Profile;
import model.RoomUser;
import util.ServerError;

public class EditNicknameViewModel implements ViewModel{
    private final Model model;
    private final ViewState viewState;

    private final StringProperty errorText;
    private final StringProperty titleText;
    private ObservableList<ViewUser> usersProperty;

    public EditNicknameViewModel(Model model, ViewState viewState) {
        this.model = model;
        this.viewState = viewState;

        this.errorText = new SimpleStringProperty();
        this.titleText = new SimpleStringProperty();
        this.usersProperty = FXCollections.observableArrayList();
    }

    @Override
    public void reset() {
        usersProperty.clear();
        try {
            errorText.setValue("");
            titleText.setValue(viewState.getCurrentChatRoomProperty().getName());
            var room = model.getRoomManager().getChatRoom(viewState.getCurrentChatRoom());
            for (RoomUser user : room.getUsers()) {
                addUser(user.getUserId());
            }
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public StringProperty getErrorText() {
        return errorText;
    }

    public StringProperty getTitleText() {
        return titleText;
    }

    public ObservableList<ViewUser> getUsersProperty() {
        return usersProperty;
    }

    public void editNickname(long userId, String nickname) {
        if (nickname == null || nickname.isEmpty()) return;
        try {
            model.getRoomManager().editNickname(viewState.getCurrentChatRoom(), userId, nickname);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public void removeNickname(long userId) {
        try {
            model.getRoomManager().removeNickname(viewState.getCurrentChatRoom(), userId);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public void addUser(long _userId) {
        try {
            Profile profile = model.getProfileManager().getProfile(_userId);
            ViewUser viewUser = new ViewUser() {{
                userId = _userId;
                username = profile.getUsername();
                nickname = model.getRoomManager().getNicknameOf(viewState.getCurrentChatRoom(), _userId);
            }};
            usersProperty.add(viewUser);
        } catch (ServerError e) {
            e.showAlert();
        }
    }
}
