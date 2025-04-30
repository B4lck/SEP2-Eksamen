package viewModel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.Model;
import util.ServerError;

public class SignUpViewModel implements ViewModel {

    private StringProperty userNameInputProperty = new SimpleStringProperty();
    private StringProperty passwordInputProperty = new SimpleStringProperty();
    private StringProperty verifyPasswordInputProperty = new SimpleStringProperty();
    private StringProperty errorProperty = new SimpleStringProperty();
    private Model model;

    public SignUpViewModel(Model model) {
        this.model = model;
    }

    public StringProperty getUserNameInputProperty() {
        return userNameInputProperty;
    }

    public StringProperty getPasswordInputProperty() {
        return passwordInputProperty;
    }

    public StringProperty getVerifyPasswordInputProperty() {
        return verifyPasswordInputProperty;
    }

    public StringProperty getErrorProperty() {
        return errorProperty;
    }

    @Override
    public void reset() {
        userNameInputProperty.set("");
        passwordInputProperty.set("");
        errorProperty.set("");
    }

    public boolean signUp() {
        try {
            if (!passwordInputProperty.getValue().equals(verifyPasswordInputProperty.getValue())) {
                errorProperty.set("Passordene er ikke ens");
            }
            model.getProfileManager().signUp(userNameInputProperty.getValue(), passwordInputProperty.getValue());
            return true;
        } catch (ServerError e) {
            errorProperty.set(e.getMessage());
            return false;
        }
    }

}
