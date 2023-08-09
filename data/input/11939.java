public class B6226610 {
    static HeaderCheckerProxyTunnelServer proxy;
    public static void main(String[] args) throws Exception
    {
        proxy = new HeaderCheckerProxyTunnelServer();
        proxy.start();
        String hostname = InetAddress.getLocalHost().getHostName();
        try {
           URL u = new URL("https:
           System.out.println("Connecting to " + u);
           InetSocketAddress proxyAddr = new InetSocketAddress(hostname, proxy.getLocalPort());
           java.net.URLConnection c = u.openConnection(new Proxy(Proxy.Type.HTTP, proxyAddr));
           c.setRequestProperty("X-TestHeader", "value");
           c.connect();
         } catch (IOException e) {
            if ( e.getMessage().equals("Unable to tunnel through proxy. Proxy returns \"HTTP/1.1 400 Bad Request\"") )
            {
            }
            else
               System.out.println(e);
         } finally {
             if (proxy != null) proxy.shutdown();
         }
         if (HeaderCheckerProxyTunnelServer.failed)
            throw new RuntimeException("Test failed; see output");
    }
}
class HeaderCheckerProxyTunnelServer extends Thread
{
    public static boolean failed = false;
    private static ServerSocket ss = null;
    private Socket clientSocket = null;
    private InetAddress serverInetAddr;
    private int serverPort;
    public HeaderCheckerProxyTunnelServer() throws IOException
    {
       if (ss == null) {
          ss = new ServerSocket(0);
       }
    }
    void shutdown() {
        try { ss.close(); } catch (IOException e) {}
    }
    public void run()
    {
        try {
            clientSocket = ss.accept();
            processRequests();
        } catch (IOException e) {
            System.out.println("Proxy Failed: " + e);
            e.printStackTrace();
            try {
                   ss.close();
            }
            catch (IOException excep) {
               System.out.println("ProxyServer close error: " + excep);
               excep.printStackTrace();
            }
        }
    }
    public int getLocalPort() {
        return ss.getLocalPort();
    }
    private void processRequests() throws IOException
    {
        InputStream in = clientSocket.getInputStream();
        MessageHeader mheader = new MessageHeader(in);
        String statusLine = mheader.getValue(0);
        if (statusLine.startsWith("CONNECT")) {
           retrieveConnectInfo(statusLine);
           if (mheader.findValue("X-TestHeader") != null) {
             System.out.println("Proxy should not receive user defined headers for tunneled requests");
             failed = true;
           }
           String value;
           if ((value = mheader.findValue("Proxy-Connection")) == null ||
                !value.equals("keep-alive")) {
             System.out.println("Proxy-Connection:keep-alive not being sent");
             failed = true;
           }
           send400();
           in.close();
           clientSocket.close();
           ss.close();
        }
        else {
            System.out.println("proxy server: processes only "
                                   + "CONNECT method requests, recieved: "
                                   + statusLine);
        }
    }
    private void send400() throws IOException
    {
        OutputStream out = clientSocket.getOutputStream();
        PrintWriter pout = new PrintWriter(out);
        pout.println("HTTP/1.1 400 Bad Request");
        pout.println();
        pout.flush();
    }
    private void restart() throws IOException {
         (new Thread(this)).start();
    }
    private void retrieveConnectInfo(String connectStr) throws IOException {
        int starti;
        int endi;
        String connectInfo;
        String serverName = null;
        try {
            starti = connectStr.indexOf(' ');
            endi = connectStr.lastIndexOf(' ');
            connectInfo = connectStr.substring(starti+1, endi).trim();
            endi = connectInfo.indexOf(':');
            serverName = connectInfo.substring(0, endi);
            serverPort = Integer.parseInt(connectInfo.substring(endi+1));
        } catch (Exception e) {
            throw new IOException("Proxy recieved a request: "
                                        + connectStr);
          }
        serverInetAddr = InetAddress.getByName(serverName);
    }
}
