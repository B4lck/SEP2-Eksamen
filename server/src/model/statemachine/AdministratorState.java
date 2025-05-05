package model.statemachine;

import model.RoomUser;

public class AdministratorState extends UserState {
    private RoomUser user;

    public AdministratorState(RoomUser user) {
        this.user = user;
    }

    @Override
    public void promote() {
        throw new IllegalStateException("User is already admin, therefor cannot promoted further");
    }

    @Override
    public void demote() {
        user.setState(new RegularState(user));
    }

    @Override
    public void mute() {
        throw new IllegalStateException("Admin cannot be muted");
    }

    @Override
    public void unmute() {
        throw new IllegalStateException("Admin is not muted");
    }
}
