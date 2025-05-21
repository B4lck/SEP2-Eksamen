package model;

import mediator.ServerRequest;
import utils.DataMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ProfilesDBManager implements Profiles {

    private final Map<Long, Profile> profiles = new HashMap<>();

    public ProfilesDBManager(Model model) {
        model.addHandler(this);
    }

    @Override
    public Optional<Profile> getProfile(long userId) {
        // Tjek om brugeren findes i cache
        if (profiles.containsKey(userId)) return Optional.of(profiles.get(userId));

        // Forsøg at hente brugeren fra databasen
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM profile WHERE id = ?");
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                profiles.put(userId, new DBProfile(userId, resultSet.getString("username"), resultSet.getLong("latest_activity_time")));
                return Optional.of(profiles.get(userId));
            } else {
                return Optional.empty();
            }
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public Optional<Profile> getProfileByUsername(String username) {
        if (username == null) throw new IllegalArgumentException("Username må ikke være null");

        // Forsøg at hente brugeren fra cache
        Optional<Profile> fromCache = profiles.values().stream().filter(profile -> username.equals(profile.getUsername())).findAny();
        if (fromCache.isPresent()) return fromCache;

        // Forsøg at finde brugeren i databasen
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM profile WHERE username = ?");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(new DBProfile(resultSet.getLong("id"), resultSet.getString("username"), resultSet.getLong("latest_activity_time")));
            } else {
                return Optional.empty();
            }
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public Profile createProfile(String username, String password) {
        if (username == null) throw new IllegalArgumentException("Username må ikke være null");
        if (password == null) throw new IllegalArgumentException("Password må ikke være null");
        if (getProfileByUsername(username).isPresent()) throw new IllegalStateException("Brugernavnet er taget");
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO profile (username, password) VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, username);
            statement.setString(2, password);
            statement.executeUpdate();
            ResultSet resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                long id = resultSet.getLong("id");
                profiles.put(id, new DBProfile(id, username, System.currentTimeMillis()));
                return profiles.get(id);
            } else {
                throw new RuntimeException("Profilen kunne ikke oprettes");
            }
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public List<Profile> searchProfiles(String query) {
        if (query == null) throw new IllegalArgumentException("Query må ikke være null");
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM profile WHERE username ILIKE ?");
            statement.setString(1, "%" + query + "%");
            ResultSet resultSet = statement.executeQuery();
            ArrayList<Profile> filtered = new ArrayList<>();

            while (resultSet.next()) {

                long id = resultSet.getLong("id");

                if (!profiles.containsKey(id)) {
                    profiles.put(id, new DBProfile(id, resultSet.getString("username"), resultSet.getLong("latest_activity_time")));
                }

                filtered.add(profiles.get(id));

            }

            return filtered;
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public void updateProfileActivity(long userId) {
        if (profiles.containsKey(userId)) profiles.get(userId).setLastActive(userId);
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE profile SET latest_activity_time = ? WHERE id = ?");
            statement.setLong(1, System.currentTimeMillis());
            statement.setLong(2, userId);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public void blockProfile(long blockUserId, long blockedByUserId) {
        if (getProfile(blockUserId).isEmpty()) throw new IllegalStateException("Brugeren der skal blokeres findes ikke");
        if (getProfile(blockedByUserId).isEmpty()) throw new IllegalStateException("Brugeren der vil blokere findes ikke");
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO blocklist (blocked_by, blocked) VALUES (?, ?) ON CONFLICT DO NOTHING");
            statement.setLong(1, blockedByUserId);
            statement.setLong(2, blockUserId);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public void unblockProfile(long blockUserId, long blockedByUserId) {
        if (getProfile(blockUserId).isEmpty()) throw new IllegalStateException("Brugeren der skal blive unblocked findes ikke");
        if (getProfile(blockedByUserId).isEmpty()) throw new IllegalStateException("Brugeren der vil unblock findes ikke");

        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM blocklist WHERE blocked_by = ? AND blocked = ?;");
            statement.setLong(1, blockedByUserId);
            statement.setLong(2, blockUserId);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public List<Long> getBlockedProfiles(long userId) {
        if (getProfile(userId).isEmpty()) throw new IllegalStateException("Brugeren findes ikke");

        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM blocklist WHERE blocked_by = ?");
            statement.setLong(1, userId);
            ResultSet results = statement.executeQuery();

            ArrayList<Long> blockedProfiles = new ArrayList<>();

            while (results.next()) {
                blockedProfiles.add(results.getLong("blocked"));
            }

            return blockedProfiles;
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public void handleRequest(ServerRequest request) {
        Profile user;
        ArrayList<DataMap> profiles;

        var data = request.getData();

        if (request.getUser() != -1) {
            updateProfileActivity(request.getUser());
        }

        try {
            switch (request.getType()) {
                // Sign up
                case "SIGN_UP":
                    // Create user
                    user = createProfile(data.getString("username"), data.getString("password"));
                    // Log user in
                    request.setUser(user.getUserId());
                    // Respond with id
                    request.respond(new DataMap().with("userId", user.getUserId()));
                    break;
                // Log in
                case "LOG_IN":
                    // Check if user exists
                    user = getProfileByUsername(data.getString("username"))
                            .orElseThrow(() -> new IllegalStateException("Forkert brugernavn eller adgangskode"));
                    // Check password
                    if (user.checkPassword(data.getString("password"))) {
                        request.setUser(user.getUserId());
                        request.respond(new DataMap().with("userId", user.getUserId()));
                    } else {
                        throw new IllegalStateException("Forkert brugernavn eller adgangskode");
                    }
                    break;
                // Log out
                case "LOG_OUT":
                    // Log out
                    request.setUser(-1);
                    request.respond("Du er blevet logget ud.");
                    break;
                // Get profile
                case "GET_CURRENT_PROFILE":
                    user = getProfile(request.getUser())
                            .orElseThrow(() -> new IllegalStateException("Brugeren findes ikke"));

                    request.respond(new DataMap().with("profile", user.getData()));
                    break;
                // Get profile
                case "GET_PROFILE":
                    user = getProfile(data.getLong("userId"))
                            .orElseThrow(() -> new IllegalStateException("Brugeren findes ikke"));

                    request.respond(new DataMap().with("profile", user.getData()));
                    break;
                case "GET_PROFILES":
                    profiles = new ArrayList<>();
                    for (long id : data.getLongsArray("profiles")) {
                        profiles.add(getProfile(id).orElseThrow(() -> new IllegalStateException("Brugeren findes ikke")).getData());
                    }
                    request.respond(new DataMap().with("profiles", profiles));
                    break;
                case "SEARCH_PROFILES":
                    profiles = new ArrayList<>();
                    for (Profile profile : searchProfiles(data.getString("query"))) {
                        profiles.add(profile.getData());
                    }
                    request.respond(new DataMap().with("profiles", profiles));
                    break;
                case "BLOCK":
                    blockProfile(data.getLong("userId"), request.getUser());
                    request.respond("En bruger blev blokeret");
                    break;
                case "UNBLOCK":
                    unblockProfile(data.getLong("userId"), request.getUser());
                    request.respond("En blokering blev fjernet");
                    break;
                case "GET_BLOCKED_PROFILES":
                    request.respond(new DataMap().with("blockedProfiles", getBlockedProfiles(request.getUser())));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.respondWithError(e.getMessage());
        }
    }
}
