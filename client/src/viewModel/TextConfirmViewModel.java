package viewModel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.Model;

public class TextConfirmViewModel implements ViewModel {
    private StringProperty titleProperty;
    private StringProperty inputProperty;
    private StringProperty errorProperty;

    private Model model;
    private ViewState viewState;

    public TextConfirmViewModel(Model model, ViewState viewState) {
        this.model = model;
        this.viewState = viewState;

        this.titleProperty = new SimpleStringProperty();
        this.inputProperty = new SimpleStringProperty();
        this.errorProperty = new SimpleStringProperty();
    }

    @Override
    public void reset() {
        errorProperty.setValue("");
        inputProperty.setValue("");
        titleProperty.setValue("Rediger kaldenavn");
    }

    public StringProperty getErrorProperty() {
        return errorProperty;
    }

    public StringProperty getInputProperty() {
        return inputProperty;
    }

    public StringProperty getTitleProperty() {
        return titleProperty;
    }
}
