package model;

import model.statemachine.RegularState;
import model.statemachine.UserState;

public class RoomUser {
    /**
     * Id'et på profilen som RoomUser skal tilknyttes
     */
    private long id;
    /**
     * Staten på objektet
     */
    private UserState state;

    /**
     * @param id Id'et på profilen som RoomUser skal tilknyttes
     */
    public RoomUser(long id) {
        this.id = id;
        this.state = new RegularState(this);
    }

    /**
     * Henter id'et
     * @return id'et på brugeren
     */
    public long getId() {
        return id;
    }

    /**
     * Sætter staten
     * @param state staten der skal gives
     */
    public void setState(UserState state) {
        this.state = state;
    }

    /**
     * Henter staten
     * @return Staten
     */
    public UserState getState() {
        return state;
    }
}
