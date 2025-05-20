package viewModel;

public class ViewRoom {
    private String name;
    private long roomId;
    private long latestActivity;
    private String color;

    public ViewRoom(String name, long roomId, long latestActivity, String color) {
        this.name = name;
        this.roomId = roomId;
        this.latestActivity = latestActivity;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public long getRoomId() {
        return roomId;
    }

    public String getColor() {
        return color;
    }

    public long getLatestActivity() {
        return latestActivity;
    }
}
