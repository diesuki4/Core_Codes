import java.sql.*;
import java.util.*;

// DB에 접근해 타 서버에서 필요한 정보들을 가공해 제공
public class MemberDAO {
   String jdbcDriver = "com.mysql.jdbc.Driver";
   String jdbcUrl = "jdbc:mysql://" + ServerConstants.DATABASE_IP + ":" + ServerConstants.DATABASE_PORT + "/othello";
   Connection conn;
   //자바와 JDBC를 연동하기 위함
   
   PreparedStatement pstmt;
   ResultSet rs;
   //명령에 대한 반환값과 명령을 저장하기 위함

   String sql;
   //명령어를 저장하기 위한 String 변수
   
   public MemberDAO()
   {   
       try{
       Class.forName(jdbcDriver);
       ServerManager.getInstance().setMemberDAO(this);
       
      }catch(Exception e){
          e.printStackTrace();
       }
   }
   
   //연결 메소드
   public void connectDB(){
      try{
         conn = DriverManager.getConnection(jdbcUrl,ServerConstants.DATABASE_ID,ServerConstants.DATABASE_PW);
         System.out.println("DB 연결 성공!!");
      }catch(Exception e){
         e.printStackTrace();
      }
   }
   public void closeDB(){
      try{
         if (pstmt != null) pstmt.close();
         if (rs != null) rs.close();
         if (conn != null) conn.close();
      }catch (Exception e) {
         // TODO: handle exception
         e.printStackTrace();
      }
   }
   //DB에 접속을 끊기 위함
      
   //1. info테이블에 추가 메소드
   public synchronized boolean insert(MemberDTO mdto){
      connectDB();
      sql = "insert into info values(?,?,?,?,?,?,?)";
      
      int result = 0;
      
      try{
         pstmt = conn.prepareStatement(sql);
         pstmt.setString(1, mdto.getId());
         pstmt.setString(2, mdto.getPassword());
         pstmt.setInt(3, mdto.getTotal());
         pstmt.setDouble(4, mdto.getWrate());
         pstmt.setInt(5, mdto.getWin());
         pstmt.setInt(6, mdto.getLose());
         pstmt.setInt(7, mdto.getEscape());
         result = pstmt.executeUpdate();
      }catch(SQLException e){
         e.printStackTrace();
      }
      closeDB();
      if(result>0)
         return true;
      else
         return false;
   }
   //새로운 데이터를 삽입하기 위한 함수
      
   //2. 로그인 판정 대행 메소드
   public synchronized boolean login(String id, String password){
      connectDB();
      sql = "select * from info where id=? and password=?";
      
      ResultSet rs = null;
      
      try{
         pstmt = conn.prepareStatement(sql);
         pstmt.setString(1, id);
         pstmt.setString(2, password);
         
         rs = pstmt.executeQuery();
      }catch(SQLException e){
         e.printStackTrace();
      }
      
      try {
          if(rs.next())
          {
              closeDB();
             return true;
          }
          else
          {
              closeDB();
             return false;
          }
      }catch(SQLException e){
          e.printStackTrace();
          return false;
      }
   }   
   //3. 사용자 정보를 업데이트 하기 위한 함수
   public synchronized boolean update(MemberDTO mdto){
      connectDB();
      sql="update info set id=?, password=?, total=?, wrate=?, win=?, lose=?, escape=? where id=?";
      int result=0;
      
      try{
         pstmt=conn.prepareStatement(sql);
         pstmt.setString(1, mdto.getId());
         pstmt.setString(2, mdto.getPassword());
         pstmt.setInt(3, mdto.getTotal());
         pstmt.setDouble(4, mdto.getWrate());
         pstmt.setInt(5, mdto.getWin());
         pstmt.setInt(6, mdto.getLose());
         pstmt.setInt(7, mdto.getEscape());
         pstmt.setString(8, mdto.getId());
         result=pstmt.executeUpdate();
      }catch(SQLException e){
         e.printStackTrace();
      }
      closeDB();
      if(result>0)
         return true;
      else
         return false;
   }
   //이미 저장된 데이터를 수정하기 위한 함수
   
   //4. 회원탈퇴를 위한 함수
   public synchronized boolean drop(String id, String password){
      connectDB();
      sql = "delete from info where id=? and password=?";
      int result=0;
      
      try{
         pstmt=conn.prepareStatement(sql);
         pstmt.setString(1, id);
         pstmt.setString(2, password);
         result = pstmt.executeUpdate();
      }catch(SQLException e){
         e.printStackTrace();
      }
      
      closeDB();
      if(result>0)
         return true;
      else
         return false;
   }   
   
   //4.아이디을 이용하여 회원 1명의 정보를 구하는 메소드
   public synchronized MemberDTO info(String id){
      connectDB();
      sql = "select * from info where id=?";
      
      try{
         pstmt=conn.prepareStatement(sql);
         pstmt.setString(1,id);
      }catch(SQLException e){
         e.printStackTrace();
      }

      MemberDTO mdto=null;   
      try{
          ResultSet rs=pstmt.executeQuery();
          //rs의 데이터를 MemberDTO객체에 복사(11개)
             
          if(rs.next()){
             mdto = new MemberDTO();
             mdto.setId(rs.getString("id"));
             mdto.setPassword(rs.getString("password"));
             mdto.setTotal(rs.getInt("total"));
             mdto.setWrate(rs.getDouble("wrate"));
             mdto.setWin(rs.getInt("win"));
             mdto.setLose(rs.getInt("lose"));
             mdto.setEscape(rs.getInt("escape"));
          }
      }catch(SQLException e){
          e.printStackTrace();
      }
      //데이터가 있을때에만 복사
         
         
      closeDB();
         
      return mdto;//MemberDTO의 객체
         
   }
      
   //5.전체 회원 목록 조회 메소드
   public synchronized ArrayList<MemberDTO> search(String id){
      connectDB();
      sql = "select * from info";
      ArrayList<MemberDTO> list=new ArrayList<MemberDTO>();

      try{
         pstmt=conn.prepareStatement(sql);
      }catch(SQLException e){
         e.printStackTrace();
      }
      
      try{
          ResultSet rs=pstmt.executeQuery();
          //rs=>ArrayList<MemberDTO>변환
             
          while(rs.next()){
             MemberDTO mdto=new MemberDTO();
                
             mdto.setId(rs.getString("id"));
             mdto.setPassword(rs.getString("password"));
             mdto.setTotal(rs.getInt("total"));
             mdto.setWrate(rs.getDouble("wrate"));
             mdto.setWin(rs.getInt("win"));
             mdto.setLose(rs.getInt("lose"));
             mdto.setEscape(rs.getInt("escape"));
             
             list.add(mdto);
          }
      }catch(SQLException e){
          e.printStackTrace();
      }
         
      closeDB();
         
      return list;
   }   

    // 6. 채팅 로그 저장
    public synchronized boolean chatLogging(String origin, String dest, String message)
    {
        int result = 0;
        sql = "INSERT INTO chatlog VALUES(?, ?, ?, ?)";
        
        connectDB();

        try
        {
            long timeNow = Calendar.getInstance().getTimeInMillis();
            Timestamp ts = new Timestamp(timeNow);

            // 채팅 기록을 타임스탬프와 함께 저장
            pstmt = conn.prepareStatement(sql);
            pstmt.setTimestamp(1, ts);
            pstmt.setString(2, origin);
            pstmt.setString(3, dest);
            pstmt.setString(4, message);
            result = pstmt.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        
        closeDB();
        
        return (result > 0);
    }
}
