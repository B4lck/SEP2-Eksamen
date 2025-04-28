package viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.Model;

public class LoggedInViewModel {

    private StringProperty userNameProperty = new SimpleStringProperty();
    private Model model;

    public LoggedInViewModel(Model model) {
        this.model = model;
    }

    public StringProperty getUserNameProperty() {
        return userNameProperty;
    }

    public void reset() {
        userNameProperty.set(model.getProfileManager().getCurrentUser().getUsername());
    }

    public void logout() {
        model.getProfileManager().logout();
    }
}
