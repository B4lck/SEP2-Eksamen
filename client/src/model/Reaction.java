package model;

import utils.DataMap;

public class Reaction {
    private long reactedBy;
    private String reaction;

    public Reaction(long reactedBy, String reaction) {
        this.reactedBy = reactedBy;
        this.reaction = reaction;
    }

    public long getReactedBy() {
        return reactedBy;
    }

    public String getReaction() {
        return reaction;
    }

    public static Reaction fromData(DataMap data) {
        return new Reaction(data.getLong("reactedBy"), data.getString("reaction"));
    }
}
