package model;

import mediator.ChatClient;
import mediator.ClientMessage;
import util.ServerError;
import utils.DataMap;

import java.util.*;

public class ProfileManager {
    // Lokal cache af profiler
    private Map<Long, Profile> profiles = new HashMap<>();
    // Lokal cache af blokerede brugere
    private ArrayList<Long> blockedProfiles = new ArrayList<>();
    // ID på nuværende logget ind bruger, eller -1 hvis brugeren ikke er logget ind.
    private long currentUserId = -1;

    private ChatClient client = ChatClient.getInstance();

    /**
     * Opretter en ny bruger.
     *
     * @param username Navnet på den nye profil.
     * @param password Adgangskoden til den nye profil.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void signUp(String username, String password) throws ServerError {
        client.sendMessage(new ClientMessage("SIGN_UP", new DataMap()
                .with("username", username)
                .with("password", password)));

        ClientMessage res = client.waitingForReply("ProfileManager signUp");

        currentUserId = res.getData().getLong("userId");

        client.sendMessage(new ClientMessage("GET_BLOCKED_PROFILES", new DataMap()));

        ClientMessage blockedRes = client.waitingForReply("ProfileManager signUp - blocked profiles");

        blockedProfiles = new ArrayList<>(blockedRes.getData().getLongsArray("blockedProfiles"));
    }

    /**
     * Log ind med brugernavn og adgangskode.
     *
     * @param username Brugernavnet på en profil.
     * @param password Adgangskoden til profilen.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void login(String username, String password) throws ServerError {
        client.sendMessage(new ClientMessage("LOG_IN", new DataMap()
                .with("username", username)
                .with("password", password)));

        ClientMessage res = client.waitingForReply("ProfileManager login");

        currentUserId = res.getData().getLong("userId");

        client.sendMessage(new ClientMessage("GET_BLOCKED_PROFILES", new DataMap()));

        ClientMessage blockedRes = client.waitingForReply("ProfileManager blocked users");

        blockedProfiles = new ArrayList<>(blockedRes.getData().getLongsArray("blockedProfiles"));
    }

    /**
     * Logger brugeren ud.
     *
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void logout() throws ServerError {
        this.currentUserId = -1;

        client.sendMessage(new ClientMessage("LOG_OUT", new DataMap()));

        client.waitingForReply("ProfileManager logger ud");
    }

    /**
     * Henter en profil fra serveren. Denne metode anmoder altid serveren om at hente profilen.
     *
     * @param userId ID'et på profilen.
     * @return Et Profile objekt med profilen.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public Profile fetchProfile(long userId) throws ServerError {
        client.sendMessage(new ClientMessage("GET_PROFILE", new DataMap()
                .with("userId", Long.toString(userId))));

        ClientMessage res = client.waitingForReply("ProfileManager getProfile");

        // Gem profil i cache
        Profile profile = Profile.fromData(res.getData().getMap("profile"));
        profiles.put(profile.getUserId(), profile);

        return profiles.get(profile.getUserId());
    }

    /**
     * Henter en profil fra serveren eller cache hvis muligt.
     *
     * @param userId ID'et på profilen.
     * @return Et Profile objekt med profilen.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public Profile getProfile(long userId) throws ServerError {
        // Hent fra cache
        if (profiles.containsKey(userId)) {
            return profiles.get(userId);
        }

        // Anmod server om profilen
        return fetchProfile(userId);
    }

    /**
     * Henter den nuværende brugers profil fra serveren.
     *
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public Profile getCurrentUserProfile() throws ServerError {
        client.sendMessage(new ClientMessage("GET_CURRENT_PROFILE", new DataMap()));

        ClientMessage res = client.waitingForReply("ProfileManager getCurrentUserProfile");

        return Profile.fromData(res.getData().getMap("profile"));
    }

    /**
     * Henter en række profiler på en gang fra serveren.
     *
     * @param profiles Liste med ID'er på profiler som skal hentes.
     * @return Et Profile objekt med profilen.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public List<Profile> getProfiles(List<Long> profiles) throws ServerError {
        client.sendMessage(new ClientMessage("GET_PROFILES", new DataMap()
                .with("profiles", profiles)));

        ClientMessage reply = client.waitingForReply("ProfileManager getProfiles");

        ArrayList<Profile> receivedProfiles = new ArrayList<>();

        for (var profile : reply.getData().getMapArray("profiles")) {
            receivedProfiles.add(Profile.fromData(profile));
        }

        return receivedProfiles;
    }

    /**
     * Henter en liste over profiler ud fra et søgeord.
     *
     * @param query Søgeord til at hente profiler.
     * @return En liste med profil-objekter, der matcher søgeordet.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public List<Profile> searchProfiles(String query) throws ServerError {
        client.sendMessage(new ClientMessage("SEARCH_PROFILES", new DataMap()
                .with("query", query)));

        ClientMessage reply = client.waitingForReply("ProfileManager searchProfiles");

        ArrayList<Profile> receivedProfiles = new ArrayList<>();

        for (var profile : reply.getData().getMapArray("profiles")) {
            receivedProfiles.add(Profile.fromData(profile));
        }

        return receivedProfiles;
    }

    /**
     * Er profilen blokeret?
     * @param userId ID'et på profilen.
     */
    public boolean isBlocked(long userId) {
        return this.blockedProfiles.contains(userId);
    }

    /**
     * Bloker en profil
     * @param userId ID'et på profilen.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void blockProfile(long userId) throws ServerError {
        client.sendMessage(new ClientMessage("BLOCK", new DataMap()
                .with("userId", userId)));
        client.waitingForReply("ProfileManager blockUser");
        this.blockedProfiles.add(userId);
    }

    /**
     * Fjern en blokering på en profil
     * @param userId ID'et på profilen.
     * @throws ServerError Hvis serveren støder på en fejl.
     */
    public void unblockProfile(long userId) throws ServerError {
        client.sendMessage(new ClientMessage("UNBLOCK", new DataMap()
                .with("userId", userId)));
        client.waitingForReply("ProfileManager unblockUser");
        this.blockedProfiles.remove(userId);
    }

    /**
     * Henter den nuværende brugers ID.
     * @return Den nuværende brugers ID, eller -1 hvis brugeren ikke er logget ind.
     */
    public long getCurrentUserId() {
        return this.currentUserId;
    }
}