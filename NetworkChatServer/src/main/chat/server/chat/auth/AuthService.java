package main.chat.server.chat.auth;

import java.sql.*;

public class AuthService {

    private PreparedStatement ps;

    public AuthService(Connection connection) {
        try {
            this.ps = connection.prepareStatement("SELECT * FROM users WHERE login = ? AND password = ?");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getUsernameByLoginAndPassword(String login, String password) {
        try {
            this.ps.setString(1, login);
            this.ps.setString(2, password);

            ResultSet rs = this.ps.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

}
