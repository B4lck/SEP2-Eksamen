package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static Database instance;

    private Database() {
        try {
            DriverManager.registerDriver(new org.postgresql.Driver());
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/sep2_chat", "sep2_chat", "");
    }

}
