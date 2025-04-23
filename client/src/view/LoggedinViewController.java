package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import viewmodel.LoggedInViewModel;

public class LoggedinViewController extends ViewController <LoggedInViewModel>{

    @Override
    protected void init() {
    profile.textProperty().bind(getViewModel().getUserNameProperty());
    }
    @FXML
    private MenuItem profile;
    @FXML
    private MenuItem Logud;

    @FXML
    public void profile(ActionEvent event) {
        // går til profil siden
    }
    @FXML
    public void logud(ActionEvent actionEvent) {
        // log ud -- sendes til logind siden
        getViewModel().logout();
    }
}
