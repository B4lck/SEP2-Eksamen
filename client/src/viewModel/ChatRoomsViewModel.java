package viewModel;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.ChatRoom;
import model.Model;
import util.ServerError;

public class ChatRoomsViewModel implements ViewModel {
    private ObservableList<ViewRoom> roomsProperty;
    private Model model;


    public ChatRoomsViewModel(Model model) {
        this.model = model;
        this.roomsProperty = FXCollections.observableArrayList();
    }

    public ObservableList<ViewRoom> getChatRoomsProperty() {
        return roomsProperty;
    }

    public void setRoomsProperty(ObservableList<ViewRoom> RoomsProperty) {
        this.roomsProperty = RoomsProperty;
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
