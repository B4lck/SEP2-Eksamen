package model;

import model.statemachine.UserState;
import model.statemachine.UserStateId;
import utils.DataMap;

public class RoomUser {
    /**
     * Id'et på profilen som RoomUser skal tilknyttes
     */
    private long id;
    /**
     * Staten på objektet
     */
    private UserState state;
    private long latestReadMessage;
    private String nickname;

    /**
     * @param id Id'et på profilen som RoomUser skal tilknyttes
     * @param state Staten på profilen
     */
    public RoomUser(long id, UserStateId state, long latestReadMessage, String nickname) {
        this.id = id;
        this.state = UserState.stateFromString(state, this);
        this.latestReadMessage = latestReadMessage;
        this.nickname = nickname;
    }

    /**
     * Henter id'et
     *
     * @return id'et på brugeren
     */
    public long getId() {
        return id;
    }

    /**
     * Sætter staten
     *
     * @param state staten der skal gives
     */
    public void setState(UserState state) {
        this.state = state;
    }

    /**
     * Henter staten
     *
     * @return Staten
     */
    public UserState getState() {
        return state;
    }

    public long getLatestReadMessage() {
        return latestReadMessage;
    }

    public DataMap getData() {
        return new DataMap()
                .with("id", id)
                .with("state", state.getStateAsString())
                .with("latestReadMessage", latestReadMessage)
                .with("nickname", nickname);
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
