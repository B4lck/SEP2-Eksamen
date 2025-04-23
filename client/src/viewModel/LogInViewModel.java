package viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.Model;

public class LogInViewModel {

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

    public void reset() {
        userNameInputProperty.set("");
        passwordInputProperty.set("");
        errorProperty.set("");
    }

    public void login() {
        try {
            model.getProfiles().login(userNameInputProperty.getValue(), passwordInputProperty.getValue());
            System.out.println("logget ind?");
        } catch (Exception e) {
            errorProperty.set(e.getMessage());
        }
    }

    public void cancel() {

    }

}
