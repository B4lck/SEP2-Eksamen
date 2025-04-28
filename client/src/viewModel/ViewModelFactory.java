package viewModel;

import model.Model;

public class ViewModelFactory {

    private ChatRoomViewModel chatRoomViewModel = null;
    private SignUpViewModel signUpViewModel = null;
    private LogInViewModel logInViewModel = null;

    public ViewModelFactory(Model model) {
        chatRoomViewModel = new ChatRoomViewModel(model);
        signUpViewModel = new SignUpViewModel(model);
        logInViewModel = new LogInViewModel(model);
    }

    public ChatRoomViewModel getChatRoomViewModel() {
        return chatRoomViewModel;
    }

    public SignUpViewModel getSignUpViewModel() {
        return signUpViewModel;
    }

    public LogInViewModel getLogInViewModel() {
        return logInViewModel;
    }

}
