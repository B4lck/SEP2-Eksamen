package util;

import javafx.scene.control.Alert;

public class ServerError extends Exception {
    public ServerError(String message) {
        super(message);
    }

    public void showAlert() {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("Error");
        error.setHeaderText("Server Error");
        error.setContentText(this.getMessage());
        error.showAndWait();
    }
}
