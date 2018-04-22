package dataBase;

import java.sql.*;

public class AuthService {

    private Connection connection;
    private Statement statement;

    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.
                getConnection("jdbc:sqlite:chatDatabase.db");
        statement = connection.createStatement();
    }

    public void  disconnect(){
        try {
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized String getNick(String login, String password){
        String request = "SELECT nickname FROM users WHERE login= '" + login.toLowerCase()
                + "' AND password= '" + password.toLowerCase() + "'";
        try (ResultSet resultSet = statement.executeQuery(request)){
            if (resultSet.next()) return resultSet.getString("nickname");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized String registrationOfUsers(String login, String password, String nickname){
        String answer;
        answer = check("login", login);
        if (answer != null) return login;
        answer = check("nickname", nickname);
        if (answer != null) return nickname;
        String request = String.format("INSERT INTO users (login, password, nickname) VALUES ('%s', '%s', '%s');",
                login.toLowerCase(), password.toLowerCase(), nickname);
        int result;
        try {
            result = statement.executeUpdate(request);
            if (result == 1) return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
         return password;
    }

    public String change(String oldNick, String newNick){
        String answer = check("nickname", newNick);
        if (answer != null) return newNick;
        String request = String.format("UPDATE users SET nickname = '%s' WHERE nickname = '%s';",
                newNick, oldNick);
        int result;
        try {
            result = statement.executeUpdate(request);
            if (result == 1) return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  oldNick;
    }

    private String check(String nameCol, String string){
        String request = String.format("SELECT %s FROM users WHERE %s = '%s';",  nameCol, nameCol, string.toLowerCase());
        try (ResultSet resultSet = statement.executeQuery(request)){
            if (resultSet.next()) return resultSet.getString(nameCol);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
