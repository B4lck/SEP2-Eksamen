package viewmodel;

import model.Model;

public class ViewModelFactory {

    private LoggedInViewModel loggedInViewModel = null;
    private SignUpViewModel signUpViewModel = null;
    private LogInViewModel logInViewModel = null;

    public ViewModelFactory(Model model) {
        loggedInViewModel = new LoggedInViewModel(model);
        signUpViewModel = new SignUpViewModel(model);
        logInViewModel = new LogInViewModel(model);
    }

    public LoggedInViewModel getLoggedInViewModel() {
        return loggedInViewModel;
    }

    public SignUpViewModel getSignUpViewModel() {
        return signUpViewModel;
    }

    public LogInViewModel getLogInViewModel() {
        return logInViewModel;
    }

}
