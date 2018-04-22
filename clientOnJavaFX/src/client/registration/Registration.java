package client.registration;

import client.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;


public class Registration implements Supervisianble{

    @FXML
    private Label label;
    @FXML
    private TextField login;
    @FXML
    private PasswordField password;
    @FXML
    private TextField nickname;

    private Client client;

    public Label getLabel(){
        return label;
    }

    public Registration(Client client) {
        this.client = client;
    }

    @FXML
    private void register(){
        if (login.getText().isEmpty() || password.getText().isEmpty() || nickname.getText().isEmpty()) {
            new Caution(Alert.AlertType.WARNING, SMC.FIELDS_ARE_NOT_FILLED).showAndWait();
            return;
        }
        client.connect();
        if (client.isConnected()){
            client.sendMessage(String.format("%s %s %s %s", SMC.REG,
                    login.getText(), password.getText(), nickname.getText()));
        }else {
            new Caution(Alert.AlertType.ERROR, SMC.NO_COMMUNICATION).showAndWait();
        }
    }

    @FXML
    private void cancel(){
        if (client.isConnected()) return;
        client.changeWindow(label, SMC.LOGIN_FXML);
    }

    public void conclusionResultsOfRegistration(String[] strings){
        if (strings.length == 2){
            if (strings[1].equals(login.getText())){
                pointField(login);
                Platform.runLater(()->{
                    new Caution(Alert.AlertType.WARNING, "Такой логин уже используется").showAndWait();
                });
                return;
            }
            if (strings[1].equals(password.getText())) {
                Platform.runLater(() -> {
                    new Caution(Alert.AlertType.ERROR, "Не удалось зарегистрировать").showAndWait();
                });
                return;
            }
            if (strings[1].equals(nickname.getText())){
                pointField(nickname);
                Platform.runLater(()->{
                    new Caution(Alert.AlertType.WARNING, "Такой никнейм уже используется").showAndWait();
                });
            }
        }else Platform.runLater(()->{
            new Caution(Alert.AlertType.ERROR, "Пробелы при задании логина, никнейма," +
                    " пароля не используются").showAndWait();
            });
    }

    private void pointField(TextField textField){
        textField.setBackground(new Background(new BackgroundFill(Color.YELLOW, null, null)));
    }
}
