package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static Database instance;

    private static boolean testing = false;

    private Database() {
        try {
            DriverManager.registerDriver(new org.postgresql.Driver());
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (instance == null) {
            instance = new Database();
        }

        var context = DriverManager.getConnection("jdbc:postgresql://localhost:5432/sep2_chat", "sep2_chat", "sep2_chat_kode");
        if (testing) context.setSchema("test");
        return context;
    }

    public static void startTesting() throws SQLException {
        testing = true;
    }

    public static void endTesting() throws SQLException {
        if (testing) {
            try (Connection connection = Database.getConnection()) {
                connection.prepareStatement("DELETE FROM blocklist CASCADE").executeUpdate();
                connection.prepareStatement("DELETE FROM reaction CASCADE").executeUpdate();
                connection.prepareStatement("DELETE FROM room_user CASCADE").executeUpdate();
                connection.prepareStatement("DELETE FROM attachment CASCADE").executeUpdate();
                connection.prepareStatement("DELETE FROM message CASCADE").executeUpdate();
                connection.prepareStatement("DELETE FROM profile CASCADE").executeUpdate();
                connection.prepareStatement("DELETE FROM room CASCADE").executeUpdate();
            }
        }
    }
}
