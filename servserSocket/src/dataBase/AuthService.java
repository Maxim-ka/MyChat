package dataBase;

import java.sql.*;

public class AuthService {
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS users (" +
                                                "login STRING NOT NULL," +
                                                "password STRING NOT NULL," +
                                                "nickname STRING NOT NULL);";
    private static final String REQUEST_NICKNAME = "SELECT nickname FROM users WHERE login = ? AND password = ? ;";
    private static final String REQUEST_VERIFICATION = "SELECT %s FROM users WHERE %s = ? ;";
    private static final String REQUEST_CHANGE_NICKNAME = "UPDATE users SET nickname = ? WHERE nickname = ? ;";
    private static final String REQUEST_REGISTRATION = "INSERT INTO users (login, password, nickname) VALUES (?, ?, ?);";
    private static final String NICKNAME = "nickname";
    private static final String LOGIN = "login";
    private Connection connection;

    public void connect() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:chatDatabase.db");
        createTable();
    }

    private void createTable(){
        try (Statement statement = connection.createStatement()){
            statement.executeUpdate(CREATE_TABLE);
        }catch (SQLException e){
            System.out.println("Не удалось создать таблицу");
            e.printStackTrace();
        }
    }

    public void  disconnect(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized String getNick(String login, String password){
        try (PreparedStatement preRequestNickname = connection.prepareStatement(REQUEST_NICKNAME)){
            preRequestNickname.setString(1, login.toLowerCase());
            preRequestNickname.setString(2, password.toLowerCase());
            ResultSet resultSet = preRequestNickname.executeQuery();
            if (resultSet.next()) return resultSet.getString(NICKNAME);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public synchronized String registrationOfUsers(String login, String password, String nickname){
        String answer;
        answer = check(LOGIN, login);
        if (answer != null) return login;
        answer = check(NICKNAME, nickname);
        if (answer != null) return nickname;
        try (PreparedStatement preRequestForRegistration = connection.prepareStatement(REQUEST_REGISTRATION)){
            preRequestForRegistration.setString(1, login.toLowerCase());
            preRequestForRegistration.setString(2, password.toLowerCase());
            preRequestForRegistration.setString(3, nickname);
            if (preRequestForRegistration.executeUpdate() == 1) return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return password;
    }

    public synchronized String change(String oldNick, String newNick){
        String answer = check(NICKNAME, newNick);
        if (answer != null) return newNick;
        try (PreparedStatement preRequestChangeNickname = connection.prepareStatement(REQUEST_CHANGE_NICKNAME)){
            preRequestChangeNickname.setString(1, newNick);
            preRequestChangeNickname.setString(2, oldNick);
            if (preRequestChangeNickname.executeUpdate() == 1) return null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return  oldNick;
    }

    private synchronized String check(String nameCol, String string){
        try (PreparedStatement preRequestVerification = connection.prepareStatement(String.format(REQUEST_VERIFICATION, nameCol, nameCol))){
            preRequestVerification.setString(1, string.toLowerCase());
            ResultSet resultSet = preRequestVerification.executeQuery();
            if (resultSet.next()) return resultSet.getString(nameCol);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
