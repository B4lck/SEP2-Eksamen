package view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import viewmodel.SignUpViewModel;

public class SignupViewController extends ViewController<SignUpViewModel> {
    @Override
    protected void init() {
        Password.textProperty().bindBidirectional(getViewModel().getPasswordInputProperty());
        PasswordCheck.textProperty().bindBidirectional(getViewModel().getVerifyPasswordInputProperty());
        ViaId.textProperty().bindBidirectional(getViewModel().getUserNameInputProperty());
        error.textProperty().bind(getViewModel().getErrorProperty());
    }

    @FXML
    private PasswordField Password;
    @FXML
    private PasswordField PasswordCheck;
    @FXML
    private TextField ViaId;
    @FXML
    private Button CreatUser;
    @FXML
    private Button logInd;
    @FXML
    private Label error;

    @FXML
    public void CreatUser(ActionEvent evt) {
        getViewModel().signUp();
    }

    @FXML
    public void LogIn(ActionEvent event) {
        getViewHandler().openView(ViewID.LOGIN);
    }
    //mangler ERROR
}
