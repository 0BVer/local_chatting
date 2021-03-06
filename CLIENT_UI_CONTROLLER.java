/*package chat;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ResourceBundle;

public class CLIENT_UI_CONTROLLER implements Initializable {

    public Text TITLE_MSG;
    public Text PARTY_MSG;
    public Text WARNING_MSG;
    public Text SILENT_info;

    public ScrollPane SCROLL_PANE;
    public VBox CHAT_BOX;
    public TextArea TXT_CHAT;

    public Pane LOGIN_PANE;
    public Pane CHAT_PANE;
    public Pane MENU_CLOSE_BT;
    public Pane MENU_BT;

    public Button LOGIN_BT;
    public Button REG_BT;
    public Button SILENT_BT;
    public Button SAVE_BT;

    @FXML
    private TextField TXT_ID;
    @FXML
    private TextField TXT_PW;
    @FXML
    private TextField TXT_PW_CF;
    @FXML
    private TextField SILENT_ID;

    private boolean LOGIN_NOW = false;
    private boolean register_mode = false;

    Socket sock;
    ObjectOutputStream toServer_Obj;
    ObjectInputStream fromServer_OBJ;

    String USER_ID = "";

    LinkedList<chat_> CHAT_LIST = new LinkedList();
    ArrayList<String> Participant = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SCROLL_PANE.vvalueProperty().bind(CHAT_BOX.heightProperty());
        try {
//            sock = new Socket("creater.iptime.org", 58088);
            sock = new Socket("localhost", 8888);
            toServer_Obj = new ObjectOutputStream(sock.getOutputStream());
            fromServer_OBJ = new ObjectInputStream(sock.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread From_Server = new Thread(() -> {
            try {
                while (true) { //????????? ???????????? ?????? (???????????? ???????????? -1??? ???)
                    Object temp_Object = (Object) fromServer_OBJ.readObject(); //Socket????????? ?????? ???????????? Object??? ???????????????.
                    if (temp_Object instanceof command) { //???????????? ????????? ????????? ????????????
                        get_command_((command) temp_Object);
                    } else if (temp_Object instanceof chat_) { //???????????? ????????? ??????????????? ???
                        get_chat_((chat_) temp_Object);
                    } else if (temp_Object instanceof login_users) {
                        get_login_users_((login_users) temp_Object);
                    }
                }
            } catch (IOException | ClassNotFoundException ex) {
                Platform.runLater(() -> {
                    WARNING_MSG.setText("????????? ?????? ??? ????????????. ?????? ????????? ?????????");
                    TITLE_MSG.setText("CONNECTION LOST");
                });
                System.out.println("?????? ?????? (" + ex + ")");
            } finally {
                try {
                    if (sock != null)
                        sock.close();
                } catch (IOException ex) {
                }
            }
        });
        From_Server.setDaemon(true);
        From_Server.start();
    }

    private void get_command_(command temp_COMMAND) throws IOException {
        if (temp_COMMAND.command_type == 1) { //?????????????????? ???????????? ?????? ??????
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
        } else if (temp_COMMAND.command_type == 2) { //?????????????????? ????????? ?????? ??????
            if (temp_COMMAND.state) {
                Platform.runLater(() -> WARNING_MSG.setText("????????? ?????????????????????."));
            } else {
                Platform.runLater(() -> WARNING_MSG.setText(temp_COMMAND.message));
            }
        } else if (temp_COMMAND.command_type == 5) {
            SAVE_ALL_("chat", temp_COMMAND.message);
        } else if (temp_COMMAND.command_type == 6) {
            SAVE_ALL_("user", temp_COMMAND.message);
        }
    }

    private void get_chat_(chat_ temp_CHAT) {
        synchronized (CHAT_LIST) {
            CHAT_LIST.addFirst(temp_CHAT);
        }
        if (temp_CHAT.ID_.compareTo("SERVER ALERT") == 0)
            create_CHAT_BOX_(0, temp_CHAT.ID_ + " | " + temp_CHAT.upload_TIME_, temp_CHAT.chat_TEXT_, false);
        else if (temp_CHAT.SILENT.compareTo("") == 0)
            create_CHAT_BOX_(2, temp_CHAT.ID_ + " | " + temp_CHAT.upload_TIME_, temp_CHAT.chat_TEXT_, false);
        else
            create_CHAT_BOX_(2, temp_CHAT.ID_ + " | " + temp_CHAT.upload_TIME_, temp_CHAT.chat_TEXT_, true);
    }

    private void get_login_users_(login_users temp_LOGIN_USERS){
        String alert_message = "";

        if (temp_LOGIN_USERS.ID_.compareTo(USER_ID) == 0){
            alert_message = temp_LOGIN_USERS.ID_ + "??? ????????? ???????????? ???????????????.";
            Participant = (ArrayList<String>) temp_LOGIN_USERS.users_ID_.clone();
        } else {
            if (temp_LOGIN_USERS.state) {
                Participant.add(temp_LOGIN_USERS.ID_);
                alert_message = temp_LOGIN_USERS.ID_ + "?????? ??????????????????.";
            } else {
                Participant.remove(temp_LOGIN_USERS.ID_);
                alert_message = temp_LOGIN_USERS.ID_ + "?????? ??????????????????.";
            }
        }
        String finalAlert_message = alert_message;
        create_CHAT_BOX_(0, "SEVER ALERT", finalAlert_message, false);
        PARTY_MSG.setText(temp_LOGIN_USERS.toString());
        synchronized (CHAT_LIST) {
            CHAT_LIST.addFirst(new chat_("SERVER ALERT", temp_LOGIN_USERS.toString(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"))));
        }
    }

//    private String participant_toString(ArrayList<String> participant) {
//        String temp = "";
//        int count = 0;
//        for (String user : participant) {
//            if (user.length() > 0) {
//                temp += user + ", ";
//                count++;
//            }
//        }
//        return String.format("Online [%d/10] ", count) + temp.substring(0, temp.length() - 2);
//    }

    private void CHANGE_SCENE() throws IOException {
        if (LOGIN_NOW) {
            CHAT_PANE.setVisible(true);
            LOGIN_PANE.setVisible(false);
        }
    }

    public void REGISTER_IN(ActionEvent actionEvent) {
        if (!register_mode) {
            TXT_PW_CF.setVisible(true);
            register_mode = true;
            REG_BT.setText("????????? ?????????????");
            LOGIN_BT.setText("????????????");
            WARNING_MSG.setText("");
        } else {
            TXT_PW_CF.setVisible(false);
            TXT_PW_CF.setText("");
            register_mode = false;
            REG_BT.setText("???????????????????");
            LOGIN_BT.setText("???????????????");
            WARNING_MSG.setText("");
        }
    }

    public void LOGIN_IN(ActionEvent actionEvent) throws NoSuchAlgorithmException, IOException {
        if (!register_mode) {
            if (TXT_ID.getText().length() == 0) {
                Platform.runLater(() -> WARNING_MSG.setText("ID??? ?????????????????????."));
            } else if (TXT_PW.getText().length() == 0) {
                Platform.runLater(() -> WARNING_MSG.setText("PW??? ?????????????????????."));
            } else {
                toServer_Obj.writeObject(new user_(TXT_ID.getText(), TXT_PW.getText(), 0));
                toServer_Obj.flush();
            }
        } else {
            if (TXT_ID.getText().length() == 0) {
                Platform.runLater(() -> WARNING_MSG.setText("ID??? ?????????????????????."));
            } else if (TXT_PW.getText().length() == 0) {
                Platform.runLater(() -> WARNING_MSG.setText("PW??? ?????????????????????."));
            } else if (TXT_PW_CF.getText().length() == 0) {
                Platform.runLater(() -> WARNING_MSG.setText("PW Confirm??? ?????????????????????."));
            } else if (TXT_PW.getText().compareTo(TXT_PW_CF.getText()) != 0) {
                Platform.runLater(() -> WARNING_MSG.setText("PW??? PW Confirm??? ???????????? ????????????."));
            } else {
                toServer_Obj.writeObject(new user_(TXT_ID.getText(), TXT_PW.getText(), 2));
                toServer_Obj.flush();
            }
        }
    }

    public void SEND_CHAT(ActionEvent actionEvent) throws IOException {
        String time_;
        if (TXT_CHAT.getText().length() > 0) {
            time_ = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"));
            chat_ temp = null;

            //??????????????? ???????????? ??????
            if (SILENT_ID.getText().compareTo("") == 0) {
                temp = new chat_(USER_ID, TXT_CHAT.getText(), time_);
                create_CHAT_BOX_(1, time_, TXT_CHAT.getText(), false);
                TXT_CHAT.setText("");
            } else if (Participant.contains(SILENT_ID.getText())){
                temp = new chat_(USER_ID, TXT_CHAT.getText(), time_, SILENT_ID.getText());
                create_CHAT_BOX_(1, time_, TXT_CHAT.getText(), true);
                TXT_CHAT.setText("");
            } else {
                create_CHAT_BOX_(0, time_, SILENT_ID.getText() + "?????? ???????????? ?????? ??? ????????????.", true);
            }

            toServer_Obj.writeObject(temp);
            toServer_Obj.flush();
            synchronized (CHAT_LIST) {
                CHAT_LIST.addFirst(temp);
            }

            //????????? ???????????? ????????? ??????????????? ?????? ??????
            if (MENU_CLOSE_BT.isVisible()) {
                MENU_CLOSE(new ActionEvent());
            }
        }
    }

    public void create_CHAT_BOX_(int SENDER, String ID_DATE, String CHAT_, boolean SILENT) {
        double view = SCROLL_PANE.getVvalue();

        Text ID_DATE_FIELD = new Text(ID_DATE);
        if (SILENT)
            ID_DATE_FIELD.setText("-SILENT- " + ID_DATE_FIELD.getText());
        ID_DATE_FIELD.setLayoutX(13);
        ID_DATE_FIELD.setLayoutY(19);
        ID_DATE_FIELD.setWrappingWidth(350);
        ID_DATE_FIELD.setFont(new Font(15));

        Text CHAT_FIELD = new Text(CHAT_);
        CHAT_FIELD.setLayoutX(13);
        CHAT_FIELD.setLayoutY(37);
        CHAT_FIELD.setWrappingWidth(350);
        CHAT_FIELD.setFont(new Font(15));

        Pane IN_PANE = new Pane(ID_DATE_FIELD, CHAT_FIELD);
        IN_PANE.setPrefHeight(37 + CHAT_FIELD.getLayoutBounds().getHeight());
        IN_PANE.setPrefWidth(375);

        if (SENDER == 0) { //??????, ?????????
            IN_PANE.setPrefWidth(500);
            IN_PANE.setStyle("-fx-background-color: #C4C4C4;");
        } else if (SENDER == 1) { //??????
            ID_DATE_FIELD.setFill(Paint.valueOf("WHITE"));
            ID_DATE_FIELD.setTextAlignment(TextAlignment.valueOf("RIGHT"));
            CHAT_FIELD.setFill(Paint.valueOf("WHITE"));
            CHAT_FIELD.setTextAlignment(TextAlignment.valueOf("RIGHT"));
            IN_PANE.setLayoutX(125);
            IN_PANE.setStyle("-fx-background-color: #434343;");
        } else if (SENDER == 2) { //??????
            IN_PANE.setStyle("-fx-background-color: #C4C4C4;");
        }

        Pane OUT_PANE = new Pane(IN_PANE);
        OUT_PANE.setPrefHeight(10 + IN_PANE.getPrefHeight());
        OUT_PANE.setPrefWidth(500);
        OUT_PANE.setStyle("-fx-background-color: #2F2F2F;");
        Platform.runLater(() -> {
            CHAT_BOX.getChildren().add(OUT_PANE);
            CHAT_BOX.setPrefHeight(CHAT_BOX.getPrefHeight() + OUT_PANE.getPrefHeight());

        });
//        ???????????? ??? ?????? ?????? ????????? ?????? ???????????? ???????????? ?????? (?????????)
//        if (CHAT_BOX.getPrefHeight() > 600 && view < 0.8)
//            SCROLL_PANE.vvalueProperty().unbind();
//        else
//            SCROLL_PANE.vvalueProperty().bind(CHAT_BOX.heightProperty());
    }

    public void SAVE_CHAT(ActionEvent actionEvent) throws IOException {
        String chat_log = "";
        for (chat_ temp : CHAT_LIST) {
            chat_log = temp.toString() + '\n' + chat_log;
        }
        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        chat_log = String.format("---%s?????? %s ?????? ??????---\n", USER_ID, today) + chat_log;
        FileOutputStream txt_saver = new FileOutputStream(String.format("chatlog_%s.txt", today));
        txt_saver.write(chat_log.getBytes(StandardCharsets.UTF_8));
        txt_saver.close();

    }

    public void SAVE_ALL_(String table, String all) throws IOException {
        FileOutputStream txt_saver = new FileOutputStream(String.format("log_%s.txt", table));
        txt_saver.write(all.getBytes(StandardCharsets.UTF_8));
        txt_saver.close();
    }

    public void SILENT_MODE(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            if (SILENT_BT.getText().compareTo("?????????") == 0) {
                SILENT_BT.setText("??????");
                SILENT_ID.setVisible(true);
                SILENT_info.setVisible(true);
            } else {
                SILENT_BT.setText("?????????");
                SILENT_ID.setVisible(false);
                SILENT_info.setVisible(false);
                SILENT_ID.setText("");
            }
        });
    }

    public void MENU_OPEN(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            MENU_CLOSE_BT.setVisible(true);
            MENU_BT.setVisible(false);
            SILENT_BT.setVisible(true);
            SAVE_BT.setVisible(true);
            if (SILENT_ID.getText().compareTo("") != 0) {
                SILENT_ID.setVisible(true);
                SILENT_info.setVisible(true);
            }
        });

    }

    public void MENU_CLOSE(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            MENU_BT.setVisible(true);
            MENU_CLOSE_BT.setVisible(false);
            SILENT_BT.setVisible(false);
            SAVE_BT.setVisible(false);
            SILENT_ID.setVisible(false);
            SILENT_info.setVisible(false);
            if (SILENT_ID.getText().compareTo("") == 0)
                SILENT_BT.setText("?????????");
        });

    }

}*/