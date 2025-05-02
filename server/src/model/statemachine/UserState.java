package model.statemachine;

import model.ChatRoomUser;

public abstract class UserState {
    public abstract void promote();
    public abstract void demote();
    public abstract void mute();
    public abstract void unmute();

    public static UserState stateFromString(UserStateId state, ChatRoomUser user) {
        return switch (state.getStateId()) {
            case "regular" -> new RegularState(user);
            case "muted" -> new MutedUser(user);
            case "admin" -> new AdministratorState(user);
            default -> throw new IllegalStateException("State does not exist");
        };
    }
}
