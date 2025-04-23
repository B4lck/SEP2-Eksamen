package view;

public enum ViewID {
    LOGGED_IN("LoggedinView.fxml"),
    LOGIN("LoginView.fxml"),
    SIGNUP("SignupView.fxml");

    private final String filename;
    private ViewID(String filename) {
        this.filename = filename;
    }
    public String getFilename() {
        return filename;
    }
}
