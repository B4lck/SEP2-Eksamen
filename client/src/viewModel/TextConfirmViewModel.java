package viewModel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.Model;

public class TextConfirmViewModel extends ViewModel {
    private final StringProperty titleProperty;

    public TextConfirmViewModel(Model model) {
        super(model);

        this.titleProperty = new SimpleStringProperty();
    }

    @Override
    public void reset() {
        titleProperty.setValue("Rediger kaldenavn");
    }

    public StringProperty getTitleProperty() {
        return titleProperty;
    }
}
