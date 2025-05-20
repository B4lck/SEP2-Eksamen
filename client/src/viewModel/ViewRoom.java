package viewModel;

public class ViewRoom {
    private String name;
    private long roomId;
    private long latestActivity;
    private String color;
    private String font;

    public ViewRoom(String name, long roomId, long latestActivity, String color, String font) {
        this.name = name;
        this.roomId = roomId;
        this.latestActivity = latestActivity;
        this.color = color;
        this.font = font;
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

    public String getFont() {
        return font;
    }

    public long getLatestActivity() {
        return latestActivity;
    }
}
