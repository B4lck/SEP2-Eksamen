package model;

import utils.DataMap;

class DBReaction implements Reaction {
    private long reactedBy;
    private String reaction;

    public DBReaction(long reactedBy, String reaction) {
        this.reactedBy = reactedBy;
        this.reaction = reaction;
    }

    @Override
    public long getReactedBy() {
        return reactedBy;
    }

    @Override
    public String getReaction() {
        return reaction;
    }

    @Override
    public DataMap getData() {
        return new DataMap()
                .with("reactedBy", getReactedBy())
                .with("reaction", getReaction());
    }
}
