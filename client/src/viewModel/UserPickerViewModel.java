package viewModel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import model.Profile;
import util.ServerError;

public class UserPickerViewModel extends ViewModel {
    private ObservableList<ViewUser> resultsProperty;
    private StringProperty searchProperty;

    private Model model;

    public UserPickerViewModel(Model model) {
        super(model);
        this.resultsProperty = FXCollections.observableArrayList();
        this.searchProperty = new SimpleStringProperty();
        this.model = model;
    }

    public ObservableList<ViewUser> getResultsProperty() {
        return resultsProperty;
    }

    public StringProperty getSearchProperty() {
        return searchProperty;
    }

    public void search() {
        try {
            this.resultsProperty.clear();

            var profiles = model.getProfileManager().searchProfiles(searchProperty.getValue());

            for (Profile profile : profiles) {
                resultsProperty.add(new ViewUser(profile.getUserId(), profile.getUsername()));
            }

        } catch (ServerError e) {
            e.printStackTrace();
            e.showAlert();
        }
    }

    @Override
    public void reset() {
        searchProperty.set("");
        resultsProperty.clear();
    }
}
