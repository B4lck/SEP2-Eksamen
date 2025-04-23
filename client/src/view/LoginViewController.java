package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import viewmodel.LogInViewModel;

public class LoginViewController extends ViewController<LogInViewModel> {

    @Override
    protected void init() {
    login.textProperty().bindBidirectional(getViewModel().getUserNameInputProperty());
    Password.textProperty().bindBidirectional(getViewModel().getPasswordInputProperty());
    Error.textProperty().bind(getViewModel().getErrorProperty());
    }
    @FXML
    private TextField login;
    @FXML
    private Label Error;
    @FXML
    private PasswordField Password;
    @FXML
    private Button logind;
    @FXML
    private Button SignUp;
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
