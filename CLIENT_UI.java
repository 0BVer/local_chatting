package chat;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CLIENT_UI extends Application {

    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        AnchorPane root;

        root = (AnchorPane) FXMLLoader.load(getClass().getResource("login_page.fxml"));
        Scene scene = new Scene(root, 550, 950);
        scene.getStylesheets().add(getClass().getResource("global_style.css").toExternalForm());
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
                if (480 > xOffset && xOffset > 410 && 45 > yOffset){
                    event.consume();
                    primaryStage.setIconified(true);
                } else if (550 > xOffset && xOffset > 480 && 45 > yOffset){
                    event.consume();
                    primaryStage.close();
                }
            }
        });
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                primaryStage.setX(event.getScreenX() - xOffset);
                primaryStage.setY(event.getScreenY() - yOffset);
            }
        });
        primaryStage.show();
    }

    public static void main(String[] args) throws Exception {
        launch(args);
    }
}
