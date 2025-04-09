package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import viewModel.LoggedInViewModel;

public class LoggedinViewController extends ViewController <LoggedInViewModel>{

    @Override
    protected void init() {
    Profile.textProperty().bind(getViewModel().getUserNameProperty());
    }
    @FXML
    public MenuItem Profile;
    @FXML
    public MenuItem Logud;

    @FXML
    public void Profile(ActionEvent event) {
        // går til profil siden
    }
    @FXML
    public void logud(ActionEvent actionEvent) {
        // log ud -- sendes til logind siden
        getViewModel().logout();
    }
}
