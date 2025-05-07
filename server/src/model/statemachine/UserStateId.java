package model.statemachine;

public enum UserStateId {
    ADMIN("admin"),
    MUTED("muted"),
    REGULAR("regular");


    private final String stateId;

    private UserStateId(String stateId) {
        this.stateId = stateId;
    }

    public String getStateId() {
        return stateId;
    }
}
