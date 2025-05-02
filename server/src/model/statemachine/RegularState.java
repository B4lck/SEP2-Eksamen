package model.statemachine;

import model.ChatRoomUser;

public class RegularState extends UserState{
    private ChatRoomUser user;

    public RegularState(ChatRoomUser user) {
        this.user = user;
    }

    @Override
    public void promote() {
        user.setState(new AdministratorState(user));
    }

    @Override
    public void demote() {
        throw new IllegalStateException("User is not admin, therefor cannot demote");
    }

    @Override
    public void mute() {
        user.setState(new MutedUser(user));
    }

    @Override
    public void unmute() {
        throw new IllegalStateException("User is not muted, therefor cannot be unmuted");
    }
}
