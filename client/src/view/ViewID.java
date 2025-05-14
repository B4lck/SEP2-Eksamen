package view;

public enum ViewID {
    CHATROOM("ChatRoomView.fxml"),
    LOGIN("LoginView.fxml"),
    SIGNUP("SignupView.fxml"),
    CREATE_EDIT_ROOM("CreateEditChatRoom.fxml"),
    EDIT_NICKNAME("EditNicknameView.fxml");

    private final String filename;

    private ViewID(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
