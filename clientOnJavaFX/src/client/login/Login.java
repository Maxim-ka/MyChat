package client.login;

import client.*;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class Login implements Supervisianble{

    @FXML
    private Label label;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passField;

    private Client client;

    public Login(Client client) {
        this.client = client;
    }

    public Label getLabel() {
        return label;
    }

    @FXML
    private void register(){
        if (client.isConnected()) return;
        client.changeWindow(label, SMC.REGISTRATION_FXML);
    }

    @FXML
    private void sendAuthMsg(){
        if (loginField.getText().isEmpty() || passField.getText().isEmpty()) {
            new Caution(Alert.AlertType.WARNING, SMC.FIELDS_ARE_NOT_FILLED).showAndWait();
            return;
        }
        client.connect();
        if (client.isConnected()){
            client.sendMessage(String.format("%s %s %s", SMC.AUTH, loginField.getText(), passField.getText()));
            loginField.clear();
            passField.clear();
        }else {
            new Caution(Alert.AlertType.ERROR, SMC.NO_COMMUNICATION).showAndWait();
        }
    }
}
