package chat;

import java.sql.*;

public class Oracle_jdbc {
    private Connection con;
    private PreparedStatement state;
    private ResultSet result;
    private String sql;

    // SQL문 수정 필요

    public Oracle_jdbc(){
        try {
            con = DriverManager.getConnection(
                    "jdbc:oracle:thin:@localhost:49161:xe",  /* 연결할 DMBS와 데이터베이스 정보 */
                    "jdbc_con", /* 해당 DB를 사용할 수 있는 아이디*/
                    "1469" /* 사용자 암호 */);

            Class.forName("oracle.jdbc.driver.OracleDriver");

            System.out.println(con.isClosed()?"DBMS와 접속 실패":"DBMS와 접속 성공");

//            result.close();
//            state.close();
//            con.close();

        } catch (SQLException e) {
            System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
            System.out.print("사유 : " + e.getMessage());
        } catch (Exception e) {
        }
    }

    public static void main(String[] args) {
        new Oracle_jdbc();
    }

    public String temp() throws SQLException {
        try {
            sql = String.format("insert into user_INDEX_T values (COUNT_UP_INDEX.nextval)");
            state = con.prepareStatement(sql);
            state.executeUpdate(); //Update, Insert, Delete

            return "";

//            sql = "SELECT * from %s";

        } catch (SQLException ex){
            return "중복된 ID가 이미 있습니다.";
        }
    }


    public String Insert_user(String ID_, String PW_, String Salt_) throws SQLException {
        try {
            sql = String.format("INSERT into user_ID_T values ('%s');", ID_, PW_, Salt_);


            state = con.prepareStatement(sql);
            state.executeUpdate(); //Update, Insert, Delete

            sql = String.format("INSERT into user_ID_T values ('%s');", ID_, PW_, Salt_);


            return "";

        } catch (SQLException ex){
            return "중복된 ID가 이미 있습니다.";
        }
    }

    public String Delete_user(String ID_, String PW_, String Salt_) throws SQLException {
        try {
            sql = String.format("DELETE from user where username='%s' and password='%s' and salt='%s';", ID_, PW_, Salt_);

            state = con.prepareStatement(sql);
            state.executeUpdate(); //Update, Insert, Delete

            return "등록에 성공했습니다.";

        } catch (SQLException ex){
            return "실패 : ID를 찾을 수 없습니다.";
        }
    }

    public void Delete_ALL_user() throws SQLException {
        sql = String.format("DELETE from user;");
        state = con.prepareStatement(sql);
        state.executeUpdate(); //Update, Insert, Delete
    }

//    public void Insert_chat(String ID_, String Chat_, String Date_) throws SQLException {
//        sql = String.format("INSERT into chat values('%s', '%s', '%s');", ID_, Chat_, Date_);
//        state = con.prepareStatement(sql);
//        state.executeUpdate(); //Update, Insert, Delete
//    }

    public void Insert_chat(String ID_, String Chat_, String Date_, String SILENT_) throws SQLException {
        sql = String.format("INSERT into chat values('%s', '%s', '%s', '%s');", ID_, Chat_, Date_, SILENT_);
        state = con.prepareStatement(sql);
        state.executeUpdate(); //Update, Insert, Delete
    }

    public void Delete_chat(String ID_, String Chat_) throws SQLException {
        sql = String.format("DELETE from chat where username='%s' and text_log='%s';", ID_, Chat_);
        state = con.prepareStatement(sql);
        state.executeUpdate(); //Update, Insert, Delete
    }

    public void Delete_ALL_chat() throws SQLException {
        sql = String.format("DELETE from chat;");
        state = con.prepareStatement(sql);
        state.executeUpdate(); //Update, Insert, Delete
    }

    public String Select_All(String table_) throws SQLException {
        state = con.prepareStatement(String.format("SELECT * from %s", table_));
        result = state.executeQuery(); //Select
        String temp = "";
        System.out.println("검색 결과");
        if (table_.compareTo("user") == 0){
            while (result.next()){
                String id = result.getString("username");
                String pw = result.getString("password");
                String create_time = result.getString("create_time");
                temp += "ID : " + id + " | PW : " + pw + " | 가입 날짜 : " + create_time + "\n";
//                System.out.println("ID : " + id + " | PW : " + pw + " | 가입 날짜 : " + create_time);
            }
        } else if (table_.compareTo("chat") == 0){
            while (result.next()){
                String id = result.getString("username");
                String chat_log = result.getString("text_log");
                String create_time = result.getString("create_time");
                temp += id + " | " + create_time + " | " + chat_log + "\n";
//                System.out.println(id + " | " + create_time + " \n " + chat_log);
            }
        }
        return temp;
    }

    // 들어온 ID 와 비밀번호가 일치하는지 체크
    public boolean check(String ID_, String password_) throws SQLException {
        state = con.prepareStatement(String.format(String.format("SELECT username from user where username='%s' and password='%s';", ID_, password_)));
        result = state.executeQuery(); //Select
        String temp = "error";
        if (result.next() == true) {
            temp = result.getString("username");
            if (temp.equals(ID_))
                return true;
        }
        return false;
    }

    // 해당 ID 의 SALT 값 찾기
    public String get_SALT(String ID_) throws SQLException {
        state = con.prepareStatement(String.format(String.format("SELECT salt from user where username='%s';", ID_)));
        result = state.executeQuery(); //Select
        if (result.next() == true)
            return result.getString("salt");
        else
            return "";
    }
}