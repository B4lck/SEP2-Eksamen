package model;

import mediator.ClientMessage;

public interface BroadcastReceiver {
    void onBroadcast(ClientMessage message);
}
