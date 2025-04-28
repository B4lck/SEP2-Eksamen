package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import viewModel.LogInViewModel;

public class LoginViewController extends ViewController<LogInViewModel> {

    @Override
    protected void init() {
    username.textProperty().bindBidirectional(getViewModel().getUserNameInputProperty());
    password.textProperty().bindBidirectional(getViewModel().getPasswordInputProperty());
    error.textProperty().bind(getViewModel().getErrorProperty());
    }
    @FXML
    private TextField username;
    @FXML
    private Label error;
    @FXML
    private PasswordField password;
    @FXML
    public void Check(ActionEvent evt) {
        // tjekker om brugernavn og koden er rigtig, og  if true sender til profil siden, else ERROR!
        getViewModel().login();
    }
    @FXML
    public void SignUp(ActionEvent actionEvent) {
        //sendes til siden for opret profil
        getViewHandler().openView(ViewID.SIGNUP);
    }
}
