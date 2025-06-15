package model.statemachine;

import model.RoomMember;

public class MutedState extends UserState {
    private RoomMember user;

    public MutedState(RoomMember user) {
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

    @Override
    public String toStateIdString() {
        return UserStateId.MUTED.toString();
    }
}
