package viewModel;

import model.Model;

public abstract class ViewModel {
    protected final Model model;

    public ViewModel(Model model) {
        this.model = model;
    }

    public abstract void reset();
}
