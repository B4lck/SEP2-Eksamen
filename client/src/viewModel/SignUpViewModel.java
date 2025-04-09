package viewModel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SignUpViewModel {

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

    public void reset() {
        userNameInputProperty.set("");
        passwordInputProperty.set("");
        errorProperty.set("");
    }

    public void signUp() {
        try {
            if (passwordInputProperty.getValue() != verifyPasswordInputProperty.getValue()) {
                throw new IllegalArgumentException("Passwords do not match");
            }
            model.getProfiles().signUp(userNameInputProperty.getValue(), passwordInputProperty.getValue());
            System.out.println("Du burde være oprettet"); // TODO, gør så man kommer ind på loggedInView
        } catch (Exception e) {
            errorProperty.set(e.getMessage());
        }
    }

    public void cancel() {
        // TODO, gør så man kommer tilbage til sidste view
    }

}
