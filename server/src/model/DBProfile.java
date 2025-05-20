package model;

import utils.DataMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBProfile implements Profile {
    private long userId;
    private String username;
    private long lastActive;

    public DBProfile(long userId, String username, long lastActive) {
        this.userId = userId;
        this.username = username;
        this.lastActive = lastActive;
    }

    @Override
    public long getUserId() {
        return userId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE profile SET username = ? WHERE id = ?");
            statement.setString(1, username);
            statement.setLong(2, userId);
            statement.executeUpdate();
            this.username = username;
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public boolean checkPassword(String password) {
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT password FROM profile WHERE id = ?");
            statement.setLong(1, userId);
            ResultSet res = statement.executeQuery();
            if (res.next()) {
                return password.equals(res.getString("password"));
            } else {
                throw new IllegalStateException("Brugeren findes ikke");
            }
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public void setPassword(String password) {
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE profile SET password = ? WHERE id = ?");
            statement.setString(1, password);
            statement.setLong(2, userId);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public long getLastActive() {
        return lastActive;
    }

    @Override
    public void setLastActive(long userId) {
        lastActive = System.currentTimeMillis();

        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("UPDATE profile SET latest_activity_time = ? WHERE id = ?");
            statement.setLong(1, lastActive);
            statement.setLong(2, this.userId);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public DataMap getData() {
        return new DataMap()
                .with("username", username)
                .with("userId", userId)
                .with("lastActive", getLastActive());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DBProfile dbProfile = (DBProfile) o;
        return userId == dbProfile.userId;
    }
}
