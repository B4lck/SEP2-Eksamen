package model;

public interface ChatRooms extends ClientMessageHandler {
    /**
     * Opretter et nyt chatrum
     * @param name navn på chatrummet
     * @param user id'et på brugeren som opretter chatrummet
     * @return ChatRoom objektet som bliver oprettet
     */
    ChatRoom createRoom(String name, long user);

    /**
     * Henter et rum ud fra id'et på rummet
     * @param room id'et på rummet
     * @param user id'et på brugeren som prøver at hente rummet
     * @return ChatRoom hvis brugeren er en del af rummet, ellers thrower den
     */
    ChatRoom getRoom(long room, long user);

    /**
     * Henter alle rum som en bruger deltager i
     * @param user id'et på brugeren
     * @return ChatRoom objektet for alle rum som brugeren deltager i
     */
    ChatRoom[] getParticipatingRooms(long user);

    /**
     * Tilføjer en ny bruger til et chatrum
     * @param chatroom id'et på chatrummet
     * @param newUser id'et på den nye bruger
     * @param adminUser id'et på brugeren som forsøger at tilføje newUser
     */
    void addUser(long chatroom, long newUser, long adminUser);
}
