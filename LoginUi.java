package chat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoginUi extends Application {

    public Button REG_BT;
    public Button LOGIN_BT;
    @FXML
    private TextField TXT_ID;
    @FXML
    private TextField TXT_PW;
    @FXML
    private TextField TXT_PW_CF;

    Stage MAIN_WIN;
    private boolean register_mode = false;
    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        AnchorPane root;
        MAIN_WIN = primaryStage;

        root = (AnchorPane) FXMLLoader.load(getClass().getResource("login_page.fxml"));
        Scene scene = new Scene(root, 550, 950);
        scene.getStylesheets().add(getClass().getResource("global_style.css").toExternalForm());
        MAIN_WIN.initStyle(StageStyle.UNDECORATED);
        MAIN_WIN.setScene(scene);
        MAIN_WIN.setResizable(false);

        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                primaryStage.setX(event.getScreenX() - xOffset);
                primaryStage.setY(event.getScreenY() - yOffset);
            }
        });

        MAIN_WIN.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

    public void REGISTER_IN(ActionEvent actionEvent) {
        if (!register_mode) {
            TXT_PW_CF.setVisible(true);
            register_mode = true;
            REG_BT.setText("처음이 아닌가요?");
            LOGIN_BT.setText("가입하기");
        } else {
            TXT_PW_CF.setVisible(false);
            TXT_PW_CF.setText("");
            register_mode = false;
            REG_BT.setText("처음이신가요?");
            LOGIN_BT.setText("로그인하기");
        }
    }

    public void LOGIN_IN(ActionEvent actionEvent) {
        if (!register_mode) {
            System.out.println(TXT_ID.getText());
            System.out.println(TXT_PW.getText());
        } else {
            System.out.println(TXT_ID.getText());
            System.out.println(TXT_PW.getText());
            System.out.println(TXT_PW_CF.getText());
        }
    }

    public void MINIMIZE_WIN(ActionEvent actionEvent) {
        System.out.println(222222222);

    }

    public void CLOSE_WIN(ActionEvent actionEvent) {
        Platform.exit();
    }
}
