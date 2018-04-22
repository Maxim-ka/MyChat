package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.util.Optional;

import static javafx.scene.control.ButtonType.OK;

public class Main extends Application {

    private final Client client = new Client();

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(SMC.LOGIN_FXML));
        fxmlLoader.setController(client.getLogin());
        Parent root = fxmlLoader.load();
        client.setCurrentController(client.getLogin());
        primaryStage.setTitle("ЧАТ клиент");
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setScene(new Scene(root));
        primaryStage.centerOnScreen();
        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            if (event.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST){
                Optional<ButtonType> result = new Caution(Alert.AlertType.CONFIRMATION,
                        "Вы точно хотите выйти из чата?").showAndWait();
                if (result.isPresent() && result.get() == OK) {
                    if (client.isAuthorized())client.disconnect();
                    else Platform.exit();
                }else event.consume();
            }
        });
        primaryStage.show();
    }

    public static void main(String[] args) {

        launch(args);
    }
}
