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

import java.io.IOException;
import java.util.Optional;

import static javafx.scene.control.ButtonType.OK;

public class Main extends Application {

    private final Controller controller = new Controller();

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new  FXMLLoader(getClass().getResource("login/login.fxml"));
        loader.setController(controller);
        Parent root = loader.load();
        primaryStage.setTitle("ЧАТ клиент");
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            if (event.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST){
                Optional<ButtonType> result = new Caution(Alert.AlertType.CONFIRMATION,
                        "Вы точно хотите выйти из чата?").showAndWait();
                if (result.isPresent() && result.get() == OK) {
                    if (controller.isAuthorized()){
                        try {
                            controller.getOut().writeUTF(SMC.DISCONNECTION);
                            controller.getOut().flush();
                            controller.setExit(true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else Platform.exit();
                }else event.consume();
            }
        });
        primaryStage.setScene(new Scene(root));
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
