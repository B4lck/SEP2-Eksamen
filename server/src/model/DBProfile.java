package model;

import utils.DataMap;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBProfile implements Profile {
    private Long id;
    private String username;

    public DBProfile(long id, String username) {
        this.id = id;
        this.username = username;
    }

    @Override
    public long getUUID() {
        return id;
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
            statement.setLong(2, id);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public boolean checkPassword(String password) {
        try (Connection connection = Database.getConnection()) {
            PreparedStatement statement = connection.prepareStatement("SELECT password FROM profile WHERE id = ?");
            statement.setLong(1, id);
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
            statement.setLong(2, id);
            statement.executeUpdate();
        } catch (SQLException error) {
            throw new RuntimeException(error);
        }
    }

    @Override
    public DataMap getData() {
        return new DataMap()
                .with("username", username)
                .with("uuid", id);
    }
}
