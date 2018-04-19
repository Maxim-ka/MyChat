package gui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.shape.Circle;
import server.ServerSocketThread;

public class Controller {

    @FXML
    private TextField portField;
    @FXML
    public  Label count;
    @FXML
    public Circle start;
    @FXML
    public Circle finish;

    private ServerSocketThread serverSocketThread;

    @FXML
    private void startServer(){
        if (serverSocketThread != null && serverSocketThread.isAlive()) return;
        if (portField.getText().isEmpty()){
            new Caution(Alert.AlertType.ERROR, "не указан номер порта сервера").showAndWait();
            return;
        }
        int port = enterPort();
        if (port == -1) return;
        serverSocketThread = new ServerSocketThread(port, this);
    }

    private int enterPort(){
        int portNum = -1;
        try {
            portNum = Integer.parseInt(portField.getText());
        }catch (NumberFormatException e){
            new Caution(Alert.AlertType.ERROR, "неправильно указан номер порта").showAndWait();
            return portNum;
        }
        portField.setEditable(false);
        portField.setOpacity(0.5);
        return  portNum;
    }

    @FXML
    private void finishServer(){
        if (serverSocketThread == null || !serverSocketThread.isAlive())return;
        serverSocketThread.interrupt();
        portField.setEditable(true);
        portField.setOpacity(1);
    }

}
