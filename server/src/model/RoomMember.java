package model;

import model.statemachine.UserState;
import model.statemachine.UserStateId;
import utils.DataMap;

public class RoomMember {
    /**
     * Id'et på profilen som RoomMember skal tilknyttes
     */
    private long userId;
    /**
     * Staten på objektet
     */
    private UserState state;
    private long latestReadMessage;
    private String nickname;

    /**
     * @param userId Id'et på profilen som RoomMember skal tilknyttes
     * @param state Staten på profilen
     */
    public RoomMember(long userId, UserStateId state, long latestReadMessage, String nickname) {
        this.userId = userId;
        this.state = UserState.stateFromString(state, this);
        this.latestReadMessage = latestReadMessage;
        this.nickname = nickname;
    }

    /**
     * Henter id'et
     *
     * @return id'et på brugeren
     */
    public long getUserId() {
        return userId;
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
                .with("userId", userId)
                .with("state", state.getStateAsString())
                .with("latestReadMessage", latestReadMessage)
                .with("nickname", nickname);
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setLatestReadMessage(long messageId) {
        this.latestReadMessage = messageId;
    }
}
