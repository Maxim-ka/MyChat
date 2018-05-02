package client.chat;

import client.Caution;
import client.Client;
import client.SMC;
import client.Supervisiable;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class Chat implements Initializable, Supervisiable {

    @FXML
    private Label label;
    @FXML
    private TextField textField;
    @FXML
    private TextArea textArea;
    @FXML
    private ListView<String> nicksListView;

    private boolean exit;
    private final ObservableList<String> listNicks = FXCollections.observableArrayList();
    private String recipient;
    private Client client;

    public void setExit(boolean exit) {
        this.exit = exit;
    }

    public boolean isExit() {
        return exit;
    }

    public Chat(Client client) {
        this.client = client;
    }

    @FXML
    private void toChangeNick(MouseEvent mouseEvent){
        if (mouseEvent.getClickCount() == 2){
            String oldNick = label.getText();
            TextInputDialog text = new TextInputDialog();
            text.setContentText(String.format("Поменять %s на: ", oldNick));
            text.setHeaderText(null);
            text.setTitle("Изменить никнейм");
            Optional<String> result = text.showAndWait();
            if (result.isPresent() && !result.get().isEmpty())
                client.sendMessage(String.format("%s %s %s", SMC.CHANGE, oldNick, result.get()));
        }
    }

    public void getDenied(String[] strings){
        if (strings.length == 1){
            Platform.runLater(()->{
                new Caution(Alert.AlertType.ERROR, "Не удалось изменить никнейм").showAndWait();
            });
        }else {
            Platform.runLater(()->{
                new Caution(Alert.AlertType.WARNING, String.format("Такой никнейм %s уже используется", strings[1]))
                        .showAndWait();
            });
        }

    }

    @FXML
    private void sendMessage() {
        if (textField.getText().isEmpty()) return;
        String string = textField.getText();
        if (string.equalsIgnoreCase(SMC.DISCONNECTION)) exit = true;
        else if (recipient != null) {
            string = String.format("%s %s %s", SMC.W, recipient, string);
            recipient = null;
        }
        client.sendMessage(string);
        textField.clear();
        textField.requestFocus();
    }

    @FXML
    public void selectNick(MouseEvent mouseEvent){
        if (mouseEvent.getEventType() ==  MouseEvent.MOUSE_CLICKED){
            recipient =  nicksListView.getSelectionModel().getSelectedItem();
        }
    }

    @FXML
    private void clearMessage() {
        if (textArea.getText().isEmpty()) return;
        Optional<ButtonType> result = new Caution(Alert.AlertType.CONFIRMATION,
                "Вы точно хотите удалить все записи в чате?").showAndWait();
        if (result.isPresent() && result.get().equals(ButtonType.OK)) {
            textArea.clear();
            textField.requestFocus();
        }
    }

    public void outInChat(String string){
        textArea.appendText(string + "\r\n");
    }

    public void addNick(String string){
        Platform.runLater(() ->{
            textArea.appendText(String.format("В чат вошел %s\n", string));
            listNicks.add(string);
        });
    }

    public void updateListNicks(String string){
        Platform.runLater(() ->{
            String[] strings = string.split("\\s+");
            if (listNicks.size() != 0) listNicks.clear();
            for (String s : strings) {
                if (s.equals(label.getText())) continue;
                listNicks.add(s);
            }
        });
    }

    public void delNick(String string){
        Platform.runLater(() ->{
            textArea.appendText(String.format("Из чата вышел %s\n", string));
            listNicks.remove(string);
        });
    }

    public Label getLabel() {
        return label;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        nicksListView.setItems(listNicks);
        listNicks.clear();
    }
}