package client;

import client.chat.Chat;
import client.login.Login;
import client.registration.Registration;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;

public class Client{
    private static final int PORT = 8189;
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int LIMIT = 2;
    private final Login login = new Login(this);
    private final Registration registration = new Registration(this);
    private final Chat chat = new Chat(this);
    private boolean authorized;
    private String answer;
    private SocketThread socketThread;
    private Socket socket;
    private Supervisiable currentController;

    public void setCurrentController(Supervisiable currentController) {
        this.currentController = currentController;
    }

    public Login getLogin() {
        return login;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void connect(){
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            socketThread = new SocketThread(socket, this);
        } catch (IOException e) {
            outputToLabel(SMC.NO_COMMUNICATION);
        }
    }

    public boolean isConnected(){
        return socket != null && !socket.isClosed();
    }

    public void disconnect(){
        if (socketThread.isAlive() || !socketThread.isInterrupted()) socketThread.interrupt();
        sendMessage(SMC.DISCONNECTION);
        chat.setExit(true);
    }

    void closeSocket(){
        socketThread.interrupt();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeWindow(Label label, String path){
        try {
            Supervisiable controller = (path.equals(SMC.CHAT_FXML)) ? chat :
                    (path.equals(SMC.LOGIN_FXML)) ? login : registration;
            Stage stage = (Stage) label.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(path));
            fxmlLoader.setController(controller);
            Parent root = fxmlLoader.load();
            currentController = controller;
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void authorize(boolean authorized){
        this.authorized = authorized;
        Platform.runLater(() ->{
            String path = (authorized) ? SMC.CHAT_FXML :
                    (answer.equals(SMC.REFUSAL))? SMC.REGISTRATION_FXML : SMC.LOGIN_FXML;
            changeWindow(currentController.getLabel(), path);
        });
    }

    void getMessage(String message){
        String[] strings = message.split("\\s+", LIMIT);
        if (strings[0].startsWith(SMC.SERVICE)){
            switch (answer = strings[0].toLowerCase()){
                case SMC.OK:
                    authorize(true);
                    String[] str = strings[1].trim().split("\\s+", LIMIT);
                    outputToLabel(str[0]);
                    if (str.length > 1) chat.updateListNicks(str[1]);
                    break;
                case SMC.DISCONNECTION:
                    authorize(false);
                    if (!chat.isExit()){
                        closeSocket();
                        outputToLabel("Отключение сервера");
                    }
                    break;
                case SMC.INVALID:
                    closeSocket();
                    outputToLabel("Неверный логин и/или пароль");
                    break;
                case SMC.REPETITION:
                    closeSocket();
                    outputToLabel("Учетная запись уже используется");
                    break;
                case SMC.REFUSAL:
                    closeSocket();
                    outputToLabel("В регистрации отказано");
                    registration.conclusionResultsOfRegistration(strings);
                    break;
                case SMC.YES_CHANGE:
                    sendMessage(String.format("%s изменил ник на %s", chat.getLabel().getText(), strings[1]));
                    outputToLabel(strings[1]);
                    break;
                case SMC.NO_CHANGE:
                    chat.getDenied(strings);
                    break;
                case SMC.NO:
                    Platform.runLater(()->{
                        new Caution(Alert.AlertType.ERROR,
                                String.format("Сообщение не было доставлено. Отсутствие адресата %s",
                                        strings[1])).showAndWait();
                    });
                    break;
                case SMC.ADD:
                    if (strings[1].split("\\s+", LIMIT).length == 1) chat.addNick(strings[1]);
                    else chat.updateListNicks(strings[1]);
                    break;
                case SMC.DEL:
                    chat.delNick(strings[1]);
                    break;
                default:
                    final String fString = message;
                    Platform.runLater(()->{
                        new Caution(Alert.AlertType.ERROR,
                                String.format("Неизвестное сообщение: %s", fString)).showAndWait();
                    });
            }
        }else if (authorized) chat.outInChat(message);
    }

    public void sendMessage(String message){
        if (isConnected()) socketThread.sendMessage(message);
        else outputToLabel(SMC.NO_COMMUNICATION);
    }

    public void outputToLabel(String string){
        Platform.runLater(() ->{
            Label label = currentController.getLabel();
            label.setText(string);
            if (!authorized) label.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
        });
    }
}
