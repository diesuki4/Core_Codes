public class MemberDTO
{
   private String id;
   private String password;
   private int total;
   private double wrate;
   private int win;
   private int lose;
   private int escape;
   
   public MemberDTO()
   {
      setId("");
      setPassword("");
      setTotal(0);
      setWrate(0.0);
      setWin(0);
      setLose(0);
      setEscape(0);
   }     
   
   public String getId()        { return id; }
   public String getPassword()    { return password; }
   public int getTotal()        { return total; }
   public double getWrate()        { return wrate; }
   public int getWin()            { return win; }
   public int getLose()            { return lose; }
   public int getEscape()        { return escape; }
   public void setId(String id)                { this.id = id; }
   public void setPassword(String password)    { this.password = password; }
   public void setTotal(int total)            { this.total = total; }
   public void setWrate(double wrate)        { this.wrate = wrate; }
   public void setWin(int win)                { this.win = win; }
   public void setLose(int lose)            { this.lose = lose; }
   public void setEscape(int escape)        { this.escape = escape; }
}