package model;

import model.statemachine.RegularState;
import model.statemachine.UserState;

public class RoomUser {
    private long id;
    private UserState state;

    public RoomUser(long id) {
        this.id = id;
        this.state = new RegularState(this);
    }

    public long getId() {
        return id;
    }

    public void setState(UserState state) {
        this.state = state;
    }

    public UserState getState() {
        return state;
    }
}
