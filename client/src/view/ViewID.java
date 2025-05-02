package view;

public enum ViewID {
    CHATROOM("ChatRoomView.fxml"),
    LOGIN("LoginView.fxml"),
    SIGNUP("SignupView.fxml"),
    CREATE_ROOM("CreateChatroom.fxml");

    private final String filename;
    private ViewID(String filename) {
        this.filename = filename;
    }
    public String getFilename() {
        return filename;
    }
}
