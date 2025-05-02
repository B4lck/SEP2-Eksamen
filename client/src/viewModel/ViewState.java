package viewModel;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

public class ViewState {
    private LongProperty currentChatRoom = new SimpleLongProperty(-1);

    public LongProperty getCurrentChatRoomProperty() {
        return currentChatRoom;
    }

    public long getCurrentChatRoom() {
        return currentChatRoom.get();
    }

    public void setCurrentChatRoom(long roomId) {
        currentChatRoom.set(roomId);
    }
}
