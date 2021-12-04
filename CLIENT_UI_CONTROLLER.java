package chat;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.ResourceBundle;

public class CLIENT_UI_CONTROLLER implements Initializable {
    public Button REG_BT;
    public Button LOGIN_BT;
    public Text WARNING_MSG;
    public Pane CHAT_PANE;
    public Pane LOGIN_PANE;
    public TextArea TXT_CHAT;
    public VBox CHAT_BOX;
    public ScrollPane SCROLL_PANE;

    @FXML
    TextField TXT_ID;
    @FXML
    private TextField TXT_PW;
    @FXML
    private TextField TXT_PW_CF;
    private boolean LOGIN_NOW = false;
    private boolean register_mode = false;
    Socket sock;
    ObjectOutputStream toServer_Obj;
    command temp_COMMAND;
    chat_ temp_CHAT;
    String USER_ID = "";

    LinkedList<Pane> CHAT_LIST = new LinkedList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            sock = new Socket("localhost", 8888);
            toServer_Obj = new ObjectOutputStream(sock.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread From_Server = new Thread(() -> {
            InputStream fromServer1 = null;
            ObjectInputStream fromServer_OBJ = null;
            try {
                fromServer1 = sock.getInputStream();
                fromServer_OBJ = new ObjectInputStream(fromServer1);

                while (true) { //수신을 기다리는 부분 (스트림이 종료되면 -1이 됨)
                    Object temp_Object = (Object) fromServer_OBJ.readObject(); //Socket로부터 받은 데이터를 Object로 수신합니다.
                    if (temp_Object instanceof command) { //전달받은 객체가 명령어 타입일때
                        temp_COMMAND = (command) temp_Object;
                        if (temp_COMMAND.command_type == 1) { //클라이언트의 로그인에 대한 응답
                            if (temp_COMMAND.state) {
                                USER_ID = temp_COMMAND.message;
                                LOGIN_NOW = true;
                                Platform.runLater(() -> {
                                    try {
                                        CHANGE_SCENE();
                                    } catch (IOException e) {
                                    }
                                });
                            } else {
                                Platform.runLater(() -> WARNING_MSG.setText(temp_COMMAND.message));
                            }
                        } else if (temp_COMMAND.command_type == 2) { //클라이언트의 등록에 대한 응답
                            if (temp_COMMAND.state) {
                                Platform.runLater(() -> WARNING_MSG.setText("가입에 성공하였습니다."));
                            } else {
                                Platform.runLater(() -> WARNING_MSG.setText(temp_COMMAND.message));
                            }
                        }
                    } else if (temp_Object instanceof chat_) { //전달받은 객체가 채팅타입일 때
                        temp_CHAT = (chat_) temp_Object;
                        if (temp_CHAT.ID_.compareTo(USER_ID) == 0)
                            Platform.runLater(() -> create_CHAT_BOX_(true, temp_CHAT.upload_TIME_, temp_CHAT.chat_TEXT_));
                        else
                            Platform.runLater(() -> create_CHAT_BOX_(false, USER_ID + " - " + temp_CHAT.upload_TIME_, temp_CHAT.chat_TEXT_));
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                Platform.runLater(() -> WARNING_MSG.setText("서버를 찾을 수 없습니다. 다시 접속해 주세요"));
                System.out.println("연결 종료 (" + ex + ")");
            } finally {
                try {
                    if (fromServer1 != null) {
                        fromServer1.close();
                        fromServer_OBJ.close();
                    }
                    if (sock != null)
                        sock.close();
                } catch (IOException ex) {
                }
            }
        });
        From_Server.setDaemon(true);
        From_Server.start();
    }

    public void CHANGE_SCENE() throws IOException {
        if (LOGIN_NOW) {
            CHAT_PANE.setVisible(true);
            LOGIN_PANE.setVisible(false);
        }
    }

    public void REGISTER_IN(ActionEvent actionEvent) {
        if (!register_mode) {
            TXT_PW_CF.setVisible(true);
            register_mode = true;
            REG_BT.setText("처음이 아닌가요?");
            LOGIN_BT.setText("가입하기");
            WARNING_MSG.setText("");
        } else {
            TXT_PW_CF.setVisible(false);
            TXT_PW_CF.setText("");
            register_mode = false;
            REG_BT.setText("처음이신가요?");
            LOGIN_BT.setText("로그인하기");
            WARNING_MSG.setText("");
        }
    }

    public void LOGIN_IN(ActionEvent actionEvent) throws NoSuchAlgorithmException, IOException {
        if (!register_mode) {
            if (TXT_ID.getText().length() == 0) {
                Platform.runLater(() -> WARNING_MSG.setText("ID를 입력해주십시오."));
            } else if (TXT_PW.getText().length() == 0) {
                Platform.runLater(() -> WARNING_MSG.setText("PW를 입력해주십시오."));
            } else {
                toServer_Obj.writeObject(new user_(TXT_ID.getText(), TXT_PW.getText(), 0));
                toServer_Obj.flush();
            }
        } else {
            if (TXT_ID.getText().length() == 0) {
                Platform.runLater(() -> WARNING_MSG.setText("ID를 입력해주십시오."));
            } else if (TXT_PW.getText().length() == 0) {
                Platform.runLater(() -> WARNING_MSG.setText("PW를 입력해주십시오."));
            } else if (TXT_PW_CF.getText().length() == 0) {
                Platform.runLater(() -> WARNING_MSG.setText("PW Confirm를 입력해주십시오."));
            } else if (TXT_PW.getText().compareTo(TXT_PW_CF.getText()) != 0) {
                Platform.runLater(() -> WARNING_MSG.setText("PW와 PW Confirm이 일치하지 않습니다."));
            } else {
                toServer_Obj.writeObject(new user_(TXT_ID.getText(), TXT_PW.getText(), 2));
                toServer_Obj.flush();
            }
        }
    }

    public void SEND_CHAT(ActionEvent actionEvent) throws IOException {
        if (TXT_CHAT.getText().length() > 0) {
            toServer_Obj.writeObject(new chat_(USER_ID, TXT_CHAT.getText(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"))));
            toServer_Obj.flush();
        }
        TXT_CHAT.setText("");
    }

    public void create_CHAT_BOX_(boolean myCHAT, String ID_DATE, String CHAT_) {
        Text ID_DATE_FIELD = new Text(ID_DATE);
        ID_DATE_FIELD.setLayoutX(13);
        ID_DATE_FIELD.setLayoutY(19);
        ID_DATE_FIELD.setWrappingWidth(400);
        ID_DATE_FIELD.setFont(new Font(14));

        Text CHAT_FIELD = new Text(CHAT_);
        CHAT_FIELD.setLayoutX(13);
        CHAT_FIELD.setLayoutY(37);
        CHAT_FIELD.setWrappingWidth(400);
        CHAT_FIELD.setFont(new Font(14));

        Pane IN_PANE = new Pane(ID_DATE_FIELD, CHAT_FIELD);
        IN_PANE.setPrefHeight(37 + CHAT_FIELD.getLayoutBounds().getHeight());
        IN_PANE.setPrefWidth(425);

        if (myCHAT) {
            ID_DATE_FIELD.setFill(Paint.valueOf("WHITE"));
            ID_DATE_FIELD.setTextAlignment(TextAlignment.valueOf("RIGHT"));
            CHAT_FIELD.setFill(Paint.valueOf("WHITE"));
            CHAT_FIELD.setTextAlignment(TextAlignment.valueOf("RIGHT"));
            IN_PANE.setLayoutX(75);
            IN_PANE.setStyle("-fx-background-color: #434343;");
        } else {
            IN_PANE.setStyle("-fx-background-color: #C4C4C4;");
        }

        Pane OUT_PANE = new Pane(IN_PANE);
        OUT_PANE.setPrefHeight(10 + IN_PANE.getPrefHeight());
        OUT_PANE.setPrefWidth(500);
        OUT_PANE.setStyle("-fx-background-color: #2F2F2F;");
        CHAT_LIST.addLast(OUT_PANE);

        CHAT_BOX.getChildren().add(CHAT_LIST.getLast());
        CHAT_BOX.setPrefHeight(CHAT_BOX.getPrefHeight() + OUT_PANE.getPrefHeight());
    }
}