package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import viewModel.SignUpViewModel;

public class SignupViewController extends ViewController<SignUpViewModel> {
    @Override
    protected void init() {
        Password.textProperty().bindBidirectional(getViewModel().getPasswordInputProperty());
        PasswordCheck.textProperty().bindBidirectional(getViewModel().getVerifyPasswordInputProperty());
        ViaId.textProperty().bindBidirectional(getViewModel().getUserNameInputProperty());
    }

    @FXML
    public PasswordField Password;
    @FXML
    public PasswordField PasswordCheck;
    @FXML
    public TextField ViaId;
    @FXML
    public Button CreatUser;
    @FXML
    public Button logInd;

    @FXML
    public void CreatUser(ActionEvent evt) {
        getViewModel().signUp();
    }

    @FXML
    public void LogIn(ActionEvent event) {
        // skal sendes til logind siden
    }
    //mangler ERROR
}
