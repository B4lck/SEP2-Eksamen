package model;

import mediator.ChatClient;
import mediator.ClientMessage;
import util.ServerError;
import utils.DataMap;
import utils.PropertyChangeSubject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomManager implements PropertyChangeSubject, PropertyChangeListener {
    private PropertyChangeSupport property = new PropertyChangeSupport(this);

    private List<Room> rooms = new ArrayList<>();
    private boolean hasMyRooms = false;

    private ChatClient client = ChatClient.getInstance();

    public RoomManager() {
        client.addListener(this);
    }

    @Override
    public void addListener(PropertyChangeListener listener) {
        property.addPropertyChangeListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        property.removePropertyChangeListener(listener);
    }

    /**
     * Håndter broadcasts fra serveren.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        var message = (ClientMessage) evt.getNewValue();
        var request = message.getData();

        switch (message.getType().toUpperCase()) {
            case "READ_MESSAGE":
                try {
                    Room room = getRoom(request.getLong("roomId"));
                    RoomMember user = room.getUser(request.getLong("userId")).orElseThrow();
                    user.setRead(request.getLong("messageId"));
                    property.firePropertyChange("READ_UPDATE", room.getRoomId(), user);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Gør intet
                }
                break;
            case "ROOM_CHANGED":
                try {
                    Room room = Room.fromData(request.getMap("room"));
                    addOrSetRoom(room);
                    property.firePropertyChange("ROOM_CHANGED", null, room.getRoomId());
                } catch (Exception e) {
                    e.printStackTrace();
                    // Gør intet
                }
                break;
            case "KICKED_OUT_OF_ROOM":
                try {
                    long id = request.getLong("roomId");
                    rooms.removeIf(room -> room.getRoomId() == id);
                    property.firePropertyChange("ROOM_CHANGED", id, null);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Gør intet
                }
                break;
            case "RECEIVE_MESSAGE":
                try {
                    DataMap msg = request.getMap("message");
                    Room room = getRoom(msg.getLong("roomId"));
                    room.setLatestActivityTime(msg.getLong("dateTime"));
                    room.setLatestMessageId(msg.getLong("messageId"));
                    property.firePropertyChange("ROOM_CHANGED", null, msg.getLong("roomId"));
                } catch (Exception e) {
                    e.printStackTrace();
                    // Gør intet
                }
                break;
        }
    }

    /**
     * Henter en liste over rum som den nuværende logget ind bruger deltager i.
     * Listen hentes kun en gang per session, bagefter opdateres den af broadcasts fra serveren.
     *
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public List<Room> getMyRooms() throws ServerError {
        if (hasMyRooms) return rooms;

        client.sendMessage(new ClientMessage("GET_MY_ROOMS", new DataMap()));
        var reply = client.waitingForReply("RoomManager getChatRooms");

        for (var room : reply.getData().getMapArray("rooms")) {
            addOrSetRoom(Room.fromData(room));
        }

        hasMyRooms = true;

        return rooms;
    }

    /**
     * Henter et rum fra serveren, eller cache hvis muligst.
     *
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public Room getRoom(long roomId) throws ServerError {
        Optional<Room> room = rooms.stream().filter(r -> r.getRoomId() == roomId).findAny();

        if (room.isPresent()) return room.get();

        client.sendMessage(new ClientMessage("GET_ROOM", new DataMap()
                .with("roomId", roomId)));

        var reply = client.waitingForReply("RoomManager getChatRoom");

        return addOrSetRoom(Room.fromData(reply.getData().getMap("room")));
    }

    /**
     * Tilføjer eller opdaterer et rum til/i cachen.
     *
     * @param room Rum-objekt med nye oplysninger.
     * @return Hvis rummet ikke findes, returneres rum-objektet. Ellers returneres det eksisterende rum-objektet fra cachen.
     */
    private Room addOrSetRoom(Room room) {
        Optional<Room> existingRoom = rooms.stream().filter(r2 -> r2.getRoomId() == room.getRoomId()).findAny();
        if (existingRoom.isPresent()) {
            existingRoom.get().update(room);
            return existingRoom.get();
        } else {
            rooms.add(room);
            return room;
        }
    }

    /**
     * Opretter et nyt rum.
     *
     * @param name Navnet på rummet.
     * @return ID'et på det nye rum.
     * @throws ServerError Hvis rummet ikke findes.
     */
    public long createRoom(String name) throws ServerError {
        client.sendMessage(new ClientMessage("CREATE_ROOM", new DataMap().with("name", name)));

        return Room.fromData(client.waitingForReply("RoomManager createRoom").getData().getMap("room")).getRoomId();
    }

    /**
     * Skifter navnet på et rum
     *
     * @param roomId ID'et på rummet.
     * @param name   Rummets nye navn.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void setName(long roomId, String name) throws ServerError {
        client.sendMessage(new ClientMessage("UPDATE_ROOM_NAME", new DataMap()
                .with("roomId", roomId)
                .with("name", name)));

        client.waitingForReply("RoomManager setName");
    }

    /**
     * Tilføjer en bruger til et rum.
     *
     * @param roomId    ID'et på rummet.
     * @param userId ID'et på brugeren som skal tilføjes til rummet.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void addMember(long roomId, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("ADD_MEMBER", new DataMap()
                .with("roomId", roomId)
                .with("userId", userId)));
        client.waitingForReply("RoomManager addUser");
    }

    /**
     * Fjerner en bruger fra et rum.
     *
     * @param roomId    ID'et på rummet.
     * @param userId ID'et på brugeren som skal fjernes fra rummet.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void removeMember(long roomId, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("REMOVE_MEMBER", new DataMap()
                .with("roomId", roomId)
                .with("userId", userId)));

        client.waitingForReply("RoomManager removeUser");
    }

    /**
     * Muter en bruger i et rum.
     *
     * @param roomId    ID'et på rummet, hvor brugeren skal mutes.
     * @param userId ID'et på brugeren, som skal mutes.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void muteMember(long roomId, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("MUTE_MEMBER", new DataMap()
                .with("roomId", roomId)
                .with("userId", userId)));

        client.waitingForReply("RoomManager muteUser");
    }

    /**
     * Unmuter en bruger i et rum.
     *
     * @param roomId    ID'et på rummet, hvor brugeren skal unmutes.
     * @param userId ID'et på brugeren, som skal unmutes.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void unmuteMember(long roomId, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("UNMUTE_MEMBER", new DataMap()
                .with("roomId", roomId)
                .with("userId", userId)));

        client.waitingForReply("SUCCESS");
    }

    /**
     * Forfremmer en bruger i et rum.
     *
     * @param roomId    ID'et på rummet, hvor brugeren skal forfremmes.
     * @param userId ID'et på brugeren, som skal forfremmes.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void promoteMember(long roomId, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("PROMOTE_MEMBER", new DataMap()
                .with("roomId", roomId)
                .with("userId", userId)));

        client.waitingForReply("SUCCESS");
    }

    /**
     * Degrader en bruger i et rum.
     *
     * @param roomId    ID'et på rummet, hvor brugeren skal degraderes.
     * @param userId ID'et på brugeren, som skal degraderes.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void demoteMember(long roomId, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("DEMOTE_MEMBER", new DataMap()
                .with("roomId", roomId)
                .with("userId", userId)));

        client.waitingForReply("SUCCESS");
    }

    /**
     * Ændre kaldenavnet på en bruger.
     *
     * @param roomId    ID'et på rummet.
     * @param userId ID'et på brugeren, hvis kaldenavn skal ændres.
     * @param nickname  Det nye kaldenavn.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void setNickname(long roomId, long userId, String nickname) throws ServerError {
        client.sendMessage(new ClientMessage("SET_NICKNAME", new DataMap()
                .with("roomId", roomId)
                .with("userId", userId)
                .with("nickname", nickname)));

        client.waitingForReply("SUCCESS");
    }

    /**
     * Fjern kaldenavnet fra en bruger.
     *
     * @param roomId    ID'et på rummet.
     * @param userId ID'et på brugeren, hvis kaldenavn skal fjernes.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void removeNickname(long roomId, long userId) throws ServerError {
        client.sendMessage(new ClientMessage("REMOVE_NICKNAME", new DataMap()
                .with("roomId", roomId)
                .with("userId", userId)));

        client.waitingForReply("SUCCESS");
    }

    /**
     * Skift farven på et rum
     *
     * @param roomId ID'et på rummet.
     * @param color  Streng med hex værdi for den nye farve, i formatet #RRGGBB.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void editColor(long roomId, String color) throws ServerError {
        client.sendMessage(new ClientMessage("EDIT_COLOR", new DataMap()
                .with("roomId", roomId)
                .with("color", color)));
        client.waitingForReply("SUCCESS");
    }

    /**
     * Skift skrifttypen på et rum.
     *
     * @param roomId ID'et på rummet.
     * @param font   Navnet på den nye skrifttype.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void setFont(long roomId, String font) throws ServerError {
        client.sendMessage(new ClientMessage("SET_FONT", new DataMap()
                .with("roomId", roomId)
                .with("font", font)));
        client.waitingForReply("SUCCESS");
    }
}
