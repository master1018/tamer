public class SimpleServer
{
  private final static int BYTESPEROP= PollingServer.BYTESPEROP;
  private final static int PORTNUM   = PollingServer.PORTNUM;
  private final static int MAXCONN   = PollingServer.MAXCONN;
  private final static Object eventSync = new Object();
  private static InputStream[] instr = new InputStream[MAXCONN];
  private static int bytesRead;
  private static int bytesToRead;
  public SimpleServer() {
    Socket[] sockArr = new Socket[MAXCONN];
    long timestart, timestop;
    int bytes;
    int totalConn=0;
    System.out.println ("Serv: Initializing port " + PORTNUM);
    try {
      ServerSocket skMain = new ServerSocket (PORTNUM);
      bytesRead = 0;
      Socket ctrlSock = skMain.accept();
      BufferedReader ctrlReader =
        new BufferedReader(new InputStreamReader(ctrlSock.getInputStream()));
      String ctrlString = ctrlReader.readLine();
      bytesToRead = Integer.valueOf(ctrlString).intValue();
      ctrlString = ctrlReader.readLine();
      totalConn = Integer.valueOf(ctrlString).intValue();
      System.out.println("Receiving " + bytesToRead + " bytes from " +
                         totalConn + " client connections");
      timestart = System.currentTimeMillis();
      ConnHandler[] connHA = new ConnHandler[MAXCONN];
      int conn = 0;
      while ( conn < totalConn ) {
          Socket sock = skMain.accept();
          connHA[conn] = new ConnHandler(sock.getInputStream());
          connHA[conn].start();
          conn++;
      }
      while ( bytesRead < bytesToRead ) {
          java.lang.Thread.sleep(500);
      }
      timestop = System.currentTimeMillis();
      System.out.println("Time for all reads (" + totalConn +
                         " sockets) : " + (timestop-timestart));
      byte[] buff = new byte[BYTESPEROP];
      ctrlSock.getOutputStream().write(buff,0,BYTESPEROP);
    } catch (Exception exc) { exc.printStackTrace(); }
  }
  public static void main (String args[])
  {
    SimpleServer server = new SimpleServer();
  }
  class ConnHandler extends Thread {
    private InputStream instr;
    public ConnHandler(InputStream inputStr) { instr = inputStr; }
    public void run() {
      try {
        int bytes;
        byte[] buff = new byte[BYTESPEROP];
        while ( bytesRead < bytesToRead ) {
          bytes = instr.read (buff, 0, BYTESPEROP);
          if (bytes > 0 ) {
            synchronized(eventSync) {
              bytesRead += bytes;
            }
          }
          else {
            if (bytesRead < bytesToRead)
              System.out.println("instr.read returned : " + bytes);
          }
        }
      }
      catch (Exception e) {e.printStackTrace();}
    }
  }
}
