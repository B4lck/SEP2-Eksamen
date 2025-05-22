package viewModel;

public class ViewRoom {
    private final String name;
    private final long roomId;
    private final long latestActivity;
    private final String color;
    private final String font;
    private final boolean newActivity;

    public ViewRoom(String name, long roomId, long latestActivity, String color, String font, boolean newActivity) {
        this.name = name;
        this.roomId = roomId;
        this.latestActivity = latestActivity;
        this.color = color;
        this.font = font;
        this.newActivity = newActivity;
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

    public boolean hasNewActivity() {
        return newActivity;
    }
}
