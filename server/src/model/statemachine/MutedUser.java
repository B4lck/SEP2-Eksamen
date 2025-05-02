package model.statemachine;

import model.ChatRoomUser;

public class MutedUser extends UserState{
    private ChatRoomUser user;

    public MutedUser(ChatRoomUser user) {
        this.user = user;
    }

    @Override
    public void promote() {
        throw new IllegalStateException("Muted user cannot promote");
    }

    @Override
    public void demote() {
        throw new IllegalStateException("Muted user cannot be demoted");
    }

    @Override
    public void mute() {
        throw new IllegalStateException("User is already muted");
    }

    @Override
    public void unmute() {
        user.setState(new RegularState(user));
    }
}
