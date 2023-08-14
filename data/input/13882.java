public class PollingServer
{
  public final static int MAXCONN    = 10000;
  public final static int PORTNUM    = 4444;
  public final static int BYTESPEROP = 10;
  private final static Object eventSync = new Object();
  private static InputStream[] instr = new InputStream[MAXCONN];
  private static int[] mapping = new int[65535];
  private static LinkedQueue linkedQ = new LinkedQueue();
  private static int bytesRead = 0;
  private static int bytesToRead;
  private static int eventsToProcess=0;
  public PollingServer(int concurrency) {
    Socket[] sockArr = new Socket[MAXCONN];
    long timestart, timestop;
    short[] revents = new short[MAXCONN];
    int[] fds = new int[MAXCONN];
    int bytes;
    Poller Mux;
    int serverFd;
    int totalConn=0;
    int connects=0;
    System.out.println ("Serv: Initializing port " + PORTNUM);
    try {
      ServerSocket skMain = new ServerSocket (PORTNUM);
      Mux = new Poller(MAXCONN);
      serverFd = Mux.add(skMain, Poller.POLLIN);
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
      for (int consumerThread = 0;
           consumerThread < concurrency; consumerThread++ ) {
        new Consumer(consumerThread).start();
      }
      int numEvents=0;
      while ( bytesRead < bytesToRead ) {
        int loopWaits=0;
        while (eventsToProcess > 0) {
          synchronized (eventSync) {
            loopWaits++;
            if (eventsToProcess <= 0) break;
            try { eventSync.wait(); } catch (Exception e) {e.printStackTrace();};
          }
        }
        if (loopWaits > 1)
          System.out.println("Done waiting...loops = " + loopWaits +
                             " events " + numEvents +
                             " bytes read : " + bytesRead );
        if (bytesRead >= bytesToRead) break; 
        numEvents = Mux.waitMultiple(100, fds, revents);
        synchronized (eventSync) {
          eventsToProcess = numEvents;
        }
        int cnt = 0;
        while ( (cnt < numEvents) && (bytesRead < bytesToRead) ) {
          int fd = fds[cnt];
          if (revents[cnt] == Poller.POLLIN) {
            if (fd == serverFd) {
              sockArr[connects] = skMain.accept();
              instr[connects] = sockArr[connects].getInputStream();
              int fdval = Mux.add(sockArr[connects], Poller.POLLIN);
              mapping[fdval] = connects;
              synchronized(eventSync) {
                eventsToProcess--; 
              }
              connects++;
            } else {
              linkedQ.put(new Integer(fd));
            }
          } else {
            System.out.println("Got revents[" + cnt + "] == " + revents[cnt]);
          }
          cnt++;
        }
      }
      timestop = System.currentTimeMillis();
      System.out.println("Time for all reads (" + totalConn +
                         " sockets) : " + (timestop-timestart));
      byte[] buff = new byte[BYTESPEROP];
      ctrlSock.getOutputStream().write(buff,0,BYTESPEROP);
      for (int cThread = 0; cThread < concurrency; cThread++ ) {
        linkedQ.put(new Integer(-1));
      }
    } catch (Exception exc) { exc.printStackTrace(); }
  }
  public static void main (String args[])
  {
    int concurrency;
    if (args.length == 1)
      concurrency = java.lang.Integer.valueOf(args[0]).intValue();
    else
      concurrency = Poller.getNumCPUs() + 1;
    PollingServer server = new PollingServer(concurrency);
  }
  class Consumer extends Thread {
    private int threadNumber;
    public Consumer(int i) { threadNumber = i; }
    public void run() {
      byte[] buff = new byte[BYTESPEROP];
      int bytes = 0;
      InputStream instream;
      while (bytesRead < bytesToRead) {
        try {
          Integer Fd = (Integer) linkedQ.take();
          int fd = Fd.intValue();
          if (fd == -1) break; 
          int map = mapping[fd];
          instream = instr[map];
          bytes = instream.read(buff,0,BYTESPEROP);
        } catch (Exception e) { System.out.println(e.toString()); }
        if (bytes > 0) {
          synchronized(eventSync) {
            bytesRead += bytes;
            eventsToProcess--;
            if (eventsToProcess <= 0) {
              eventSync.notify();
            }
          }
        }
      }
    }
  }
}
