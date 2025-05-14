package viewModel;

public class ViewRoom {
    private String name;
    private long roomId;

    public ViewRoom(String name, long roomId) {
        this.name = name;
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public long getRoomId() {
        return roomId;
    }
}
