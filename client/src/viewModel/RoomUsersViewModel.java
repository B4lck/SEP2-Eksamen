package viewModel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import model.Profile;
import model.RoomUser;
import util.ServerError;

public class RoomUsersViewModel implements ViewModel {
    private final Model model;
    private final ViewState viewState;

    private final StringProperty errorText;
    private final StringProperty titleText;
    private ObservableList<ViewRoomUser> usersProperty;

    public RoomUsersViewModel(Model model, ViewState viewState) {
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
            var room = model.getRoomManager().getRoom(viewState.getCurrentChatRoom());
            for (RoomUser user : room.getUsers()) {
                addUser(user);
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

    public ObservableList<ViewRoomUser> getUsersProperty() {
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

    private void addUser(RoomUser user) {
        try {
            Profile userProfile = model.getProfileManager().fetchProfile(user.getUserId());
            ViewRoomUser viewRoomUser = new ViewRoomUser(
                    user.getUserId(),
                    userProfile.getUsername(),
                    user.getNickname(),
                    user.getState(),
                    user.getLatestReadMessage(),
                    userProfile.getLastActive(),
                    model.getProfileManager().isBlocked(user.getUserId())
            );
            usersProperty.add(viewRoomUser);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public boolean isBlocked(long userId) {
        return model.getProfileManager().isBlocked(userId);
    }

    public void block(long userId) {
        try {
            model.getProfileManager().blockUser(userId);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public void unblock(long userId) {
        try {
            model.getProfileManager().unblockUser(userId);
        } catch (ServerError e) {
            e.showAlert();
        }
    }
}
