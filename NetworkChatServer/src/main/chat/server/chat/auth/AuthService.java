package main.chat.server.chat.auth;

import java.sql.*;

public class AuthService {

    public String getUsernameByLoginAndPassword(Connection connection, String login, String password) {

        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM users WHERE login = ? AND password = ?");
            ps.setString(1, login);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

}
