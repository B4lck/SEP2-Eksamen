package viewModel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.Model;
import util.ServerError;

public class LogInViewModel extends ViewModel {

    private final StringProperty userNameInputProperty = new SimpleStringProperty();
    private final StringProperty passwordInputProperty = new SimpleStringProperty();
    private final StringProperty errorProperty = new SimpleStringProperty();

    public LogInViewModel(Model model) {
        super(model);
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
