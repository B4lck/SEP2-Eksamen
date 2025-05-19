package model;

import mediator.ServerRequest;
import utils.DataMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProfilesDBManager implements Profiles {

    public ProfilesDBManager(Model model) {
        model.addHandler(this);
    }

    @Override
    public Optional<Profile> getProfile(long uuid) {
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM profile WHERE id = ?");
            statement.setLong(1, uuid);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(new DBProfile(resultSet.getLong("id"), resultSet.getString("username")));
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
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM profile WHERE username = ?");
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(new DBProfile(resultSet.getLong("id"), resultSet.getString("username")));
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
                return new DBProfile(resultSet.getLong("id"), username);
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
            ArrayList<Profile> profiles = new ArrayList<>();

            while (resultSet.next()) {
                profiles.add(new DBProfile(resultSet.getLong("id"), resultSet.getString("username")));
            }

            return profiles;
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public void updateUserActivity(long userId) {
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
    public void blockUser(long blockUserId, long blockedByUserId) {
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
    public void unblockUser(long blockUserId, long blockedByUserId) {
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
    public List<Long> getBlockedUsers(long userId) {
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM blocklist WHERE blocked_by = ?");
            statement.setLong(1, userId);
            ResultSet results = statement.executeQuery();

            ArrayList<Long> blockedUsers = new ArrayList<>();

            while (results.next()) {
                blockedUsers.add(results.getLong("blocked"));
            }

            return blockedUsers;
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
            updateUserActivity(request.getUser());
        }

        try {
            switch (request.getType()) {
                // Sign up
                case "SIGN_UP":
                    // Create user
                    user = createProfile(data.getString("username"), data.getString("password"));
                    // Log user in
                    request.setUser(user.getUUID());
                    // Respond with uuid
                    request.respond(new DataMap().with("uuid", user.getUUID()));
                    break;
                // Log in
                case "LOG_IN":
                    // Check if user exists
                    user = getProfileByUsername(data.getString("username"))
                            .orElseThrow(() -> new IllegalStateException("Forkert brugernavn eller adgangskode"));
                    // Check password
                    if (user.checkPassword(data.getString("password"))) {
                        request.setUser(user.getUUID());
                        request.respond(new DataMap().with("uuid", user.getUUID()));
                    } else {
                        throw new IllegalStateException("Forkert brugernavn eller adgangskode");
                    }
                    break;
                // Get profile
                case "GET_CURRENT_PROFILE":
                    user = getProfile(request.getUser())
                            .orElseThrow(() -> new IllegalStateException("Brugeren findes ikke"));

                    request.respond(new DataMap().with("profile", user.getData()));
                    break;
                // Get profile
                case "GET_PROFILE":
                    user = getProfile(data.getLong("uuid"))
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
                    blockUser(data.getLong("userId"), request.getUser());
                    request.respond("En bruger blev blokeret");
                    break;
                case "UNBLOCK":
                    unblockUser(data.getLong("userId"), request.getUser());
                    request.respond("En blokering blev fjernet");
                    break;
                case "GET_BLOCKED_USERS":
                    request.respond(new DataMap().with("blockedUsers", getBlockedUsers(request.getUser())));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.respondWithError(e.getMessage());
        }
    }
}
