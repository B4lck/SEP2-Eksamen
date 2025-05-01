package viewModel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.ChatRoom;
import model.Model;
import util.ServerError;

public class ChatRoomsViewModel implements ViewModel {
    private ObservableList<ViewRoom> roomsProperty;
    private Model model;
    private ViewState viewState;

    public ChatRoomsViewModel(Model model, ViewState viewState) {
        this.model = model;
        this.roomsProperty = FXCollections.observableArrayList();
        this.viewState = viewState;
    }

    public ObservableList<ViewRoom> getChatRoomsProperty() {
        return roomsProperty;
    }

    public void setChatRoom(long chatRoom) {
        viewState.setCurrentChatRoom(chatRoom);
    }

    @Override
    public void reset() {
        try {
            this.roomsProperty.clear();
            for (ChatRoom chatRoom : model.getChatRoomManager().getChatRooms()) {
                roomsProperty.add(new ViewRoom() {{
                    name = chatRoom.getName();
                }});
            }
        } catch (ServerError error) {
            error.printStackTrace();
            error.showAlert();
        }
    }
}
