package viewModel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.Model;
import util.ServerError;

public class LogInViewModel implements ViewModel {

    private StringProperty userNameInputProperty = new SimpleStringProperty();
    private StringProperty passwordInputProperty = new SimpleStringProperty();
    private StringProperty errorProperty = new SimpleStringProperty();
    private Model model;

    public LogInViewModel(Model model) {
        this.model = model;
    }

    public StringProperty getUserNameInputProperty() {
        return userNameInputProperty;
    }

    public StringProperty getPasswordInputProperty() {
        return passwordInputProperty;
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

    /**
     * Forsøg at logge ind
     *
     * @return Om brugeren blev logget ind.
     */
    public boolean login() {
        try {
            model.getProfileManager().login(userNameInputProperty.getValue(), passwordInputProperty.getValue());
            return true;
        } catch (ServerError e) {
            errorProperty.set(e.getMessage());
            return false;
        }
    }

}
