package gui;

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

public class Launch extends Application{

    private static final int POS_X = 1300;
    private static final int POS_Y = 200;
    private static final String GET_OUT = "Выйти из программы?";


    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("../gui/gui.fxml"));
        primaryStage.setTitle("сервер");
        primaryStage.setResizable(false);
        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setScene(new Scene(root));
        primaryStage.setOnCloseRequest((WindowEvent event) -> {
            if (event.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST){
                Optional<ButtonType> result = new Caution(Alert.AlertType.CONFIRMATION, GET_OUT).showAndWait();
                if (result.isPresent() && result.get() == OK) {
                    Optional<ButtonType> res = new Caution(Alert.AlertType.CONFIRMATION,
                            "Все подключения ДОЛЖНЫ быть остановлены").showAndWait();
                    if (res.isPresent() && res.get() == OK) {
                        Platform.exit();
                        System.exit(0);
                    }
                }
                event.consume();
            }
        });
        primaryStage.setAlwaysOnTop(true);
        primaryStage.setX(POS_X);
        primaryStage.setY(POS_Y);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
