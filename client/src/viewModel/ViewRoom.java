package viewModel;

public class ViewRoom {
    private String name;
    private long roomId;
    private long latestActivity;

    public ViewRoom(String name, long roomId, long latestActivity) {
        this.name = name;
        this.roomId = roomId;
        this.latestActivity = latestActivity;
    }

    public String getName() {
        return name;
    }

    public long getRoomId() {
        return roomId;
    }

    public long getLatestActivity() {
        return latestActivity;
    }
}
