package model;

import utils.DataMap;

public interface Reaction {
    long getReactedBy();

    String getReaction();

    DataMap getData();
}
