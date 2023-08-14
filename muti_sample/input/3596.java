public class ProxyTunnelServer extends Thread {
    private static ServerSocket ss = null;
    private String userPlusPass;
    private Socket clientSocket = null;
    private InetAddress serverInetAddr;
    private int serverPort;
    static boolean needAuth = false;
    public ProxyTunnelServer() throws IOException {
        if (ss == null) {
          ss = (ServerSocket) ServerSocketFactory.getDefault().
          createServerSocket(0);
        }
    }
    public void needUserAuth(boolean auth) {
        needAuth = auth;
    }
    public void setUserAuth(String uname, String passwd) {
        userPlusPass = uname + ":" + passwd;
    }
    public void run() {
        try {
            clientSocket = ss.accept();
            processRequests();
        } catch (Exception e) {
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
    private void processRequests() throws Exception {
        InputStream in = clientSocket.getInputStream();
        MessageHeader mheader = new MessageHeader(in);
        String statusLine = mheader.getValue(0);
        if (statusLine.startsWith("CONNECT")) {
            retrieveConnectInfo(statusLine);
            if (needAuth) {
                String authInfo;
                if ((authInfo = mheader.findValue("Proxy-Authorization"))
                                         != null) {
                   if (authenticate(authInfo)) {
                        needAuth = false;
                        System.out.println(
                                "Proxy: client authentication successful");
                   }
                }
            }
            respondForConnect(needAuth);
            if (!needAuth) {
                doTunnel();
                ss.close();
            } else {
                in.close();
                clientSocket.close();
                restart();
            }
        } else {
            System.out.println("proxy server: processes only "
                                   + "CONNECT method requests, recieved: "
                                   + statusLine);
        }
    }
    private void respondForConnect(boolean needAuth) throws Exception {
        OutputStream out = clientSocket.getOutputStream();
        PrintWriter pout = new PrintWriter(out);
        if (needAuth) {
            pout.println("HTTP/1.1 407 Proxy Auth Required");
            pout.println("Proxy-Authenticate: Basic realm=\"WallyWorld\"");
            pout.println();
            pout.flush();
            out.close();
        } else {
            pout.println("HTTP/1.1 200 OK");
            pout.println();
            pout.flush();
        }
    }
    private void restart() throws IOException {
         (new Thread(this)).start();
    }
    private void doTunnel() throws Exception {
        Socket serverSocket = new Socket(serverInetAddr, serverPort);
        ProxyTunnel clientToServer = new ProxyTunnel(
                                clientSocket, serverSocket);
        ProxyTunnel serverToClient = new ProxyTunnel(
                                serverSocket, clientSocket);
        clientToServer.start();
        serverToClient.start();
        System.out.println("Proxy: Started tunneling.......");
        clientToServer.join();
        serverToClient.join();
        System.out.println("Proxy: Finished tunneling........");
        clientToServer.close();
        serverToClient.close();
    }
    class ProxyTunnel extends Thread {
        Socket sockIn;
        Socket sockOut;
        InputStream input;
        OutputStream output;
        public ProxyTunnel(Socket sockIn, Socket sockOut)
        throws Exception {
            this.sockIn = sockIn;
            this.sockOut = sockOut;
            input = sockIn.getInputStream();
            output = sockOut.getOutputStream();
        }
        public void run() {
            int BUFFER_SIZE = 400;
            byte[] buf = new byte[BUFFER_SIZE];
            int bytesRead = 0;
            int count = 0;  
            try {
                while ((bytesRead = input.read(buf)) >= 0) {
                    output.write(buf, 0, bytesRead);
                    output.flush();
                    count += bytesRead;
                }
            } catch (IOException e) {
                close();
              }
        }
        public void close() {
            try {
                if (!sockIn.isClosed())
                    sockIn.close();
                if (!sockOut.isClosed())
                    sockOut.close();
            } catch (IOException ignored) { }
        }
    }
    private void retrieveConnectInfo(String connectStr) throws Exception {
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
    public int getPort() {
        return ss.getLocalPort();
    }
    private boolean authenticate(String authInfo) throws IOException {
        boolean matched = false;
        try {
            authInfo.trim();
            int ind = authInfo.indexOf(' ');
            String recvdUserPlusPass = authInfo.substring(ind + 1).trim();
            if (userPlusPass.equals(
                                new String(
                                (new sun.misc.BASE64Decoder()).
                                decodeBuffer(recvdUserPlusPass)
                                ))) {
                matched = true;
            }
        } catch (Exception e) {
              throw new IOException(
                "Proxy received invalid Proxy-Authorization value: "
                 + authInfo);
          }
        return matched;
    }
}
