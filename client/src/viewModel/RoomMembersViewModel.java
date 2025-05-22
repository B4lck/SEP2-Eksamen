package viewModel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import model.Profile;
import model.RoomMember;
import util.ServerError;

public class RoomMembersViewModel extends ViewModel {
    private final StringProperty errorProperty;
    private final StringProperty titleProperty;
    private final ObservableList<ViewRoomMember> membersProperty;

    private final ViewState viewState;

    public RoomMembersViewModel(Model model, ViewState viewState) {
        super(model);
        this.viewState = viewState;

        this.errorProperty = new SimpleStringProperty();
        this.titleProperty = new SimpleStringProperty();
        this.membersProperty = FXCollections.observableArrayList();
    }

    @Override
    public void reset() {
        membersProperty.clear();
        try {
            errorProperty.setValue("");
            titleProperty.setValue(viewState.getCurrentChatRoomProperty().getName());
            var room = model.getRoomManager().getRoom(viewState.getCurrentChatRoom());
            for (RoomMember user : room.getMembers()) {
                addMember(user);
            }
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public StringProperty getErrorProperty() {
        return errorProperty;
    }

    public StringProperty getTitleProperty() {
        return titleProperty;
    }

    public ObservableList<ViewRoomMember> getMembersProperty() {
        return membersProperty;
    }

    public void editNickname(long userId, String nickname) {
        if (nickname == null || nickname.isEmpty()) return;
        try {
            model.getRoomManager().setNickname(viewState.getCurrentChatRoom(), userId, nickname);
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

    private void addMember(RoomMember user) {
        try {
            Profile userProfile = model.getProfileManager().fetchProfile(user.getUserId());
            ViewRoomMember viewRoomMember = new ViewRoomMember(
                    user.getUserId(),
                    userProfile.getUsername(),
                    user.getNickname(),
                    user.getState(),
                    user.getLatestReadMessage(),
                    userProfile.getLastActive(),
                    model.getProfileManager().isBlocked(user.getUserId())
            );
            membersProperty.add(viewRoomMember);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public boolean isBlocked(long userId) {
        return model.getProfileManager().isBlocked(userId);
    }

    public void block(long userId) {
        try {
            model.getProfileManager().blockProfile(userId);
        } catch (ServerError e) {
            e.showAlert();
        }
    }

    public void unblock(long userId) {
        try {
            model.getProfileManager().unblockProfile(userId);
        } catch (ServerError e) {
            e.showAlert();
        }
    }
}
