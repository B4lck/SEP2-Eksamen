package viewModel;

import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import model.Model;
import model.Profile;
import util.ServerError;

public class UserPickerViewModel implements ViewModel {
    private ObservableList<ViewUser> resultsProperty;
    private StringProperty searchProperty;

    private Model model;

    public UserPickerViewModel(Model model) {
        this.model = model;
    }

    public void search() {
        try {
            this.resultsProperty.clear();

            for (Profile profile : model.getProfileManager().searchProfiles(searchProperty.getValue())) {
                resultsProperty.add(new ViewUser(){{
                    username = profile.getUsername();
                    userId = profile.getUUID();
                }});
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
