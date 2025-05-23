package view;

public enum PopupViewID {
    USER_PICKER("UserPicker.fxml"),
    TEXT_CONFIRM("TextConfirmView.fxml");

    private final String filename;

    private PopupViewID(String filename) {
        this.filename = filename;
    }
    public String getFilename() {
        return filename;
    }
}
