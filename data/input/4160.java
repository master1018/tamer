public class ProxyFromCache
{
    public static void main(String[] args) {
        ServerSocket proxySSocket, httpSSocket;
        int proxyPort, httpPort;
        try {
            proxySSocket = new ServerSocket(0);
            proxyPort = proxySSocket.getLocalPort();
            httpSSocket = new ServerSocket(0);
            httpPort = httpSSocket.getLocalPort();
        } catch (Exception e) {
            System.out.println ("Exception: " + e);
            return;
        }
        SimpleServer proxyServer = new SimpleServer(proxySSocket);
        proxyServer.start();
        SimpleServer httpServer = new SimpleServer(httpSSocket);
        httpServer.start();
        InetSocketAddress addr = new InetSocketAddress("localhost", proxyPort);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
        try {
            String urlStr = "http:
            URL url = new URL(urlStr);
            HttpURLConnection uc = (HttpURLConnection) url.openConnection(proxy);
            InputStream is = uc.getInputStream();
            byte[] ba = new byte[1024];
            while(is.read(ba) != -1);
            is.close();
            uc = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
            is = uc.getInputStream();
            while(is.read(ba) != -1);
            is.close();
            try {
                proxySSocket.close();
                httpSSocket.close();
            } catch (IOException e) {}
            proxyServer.terminate();
            httpServer.terminate();
            int httpCount = httpServer.getConnectionCount();
            int proxyCount = proxyServer.getConnectionCount();
            if (proxyCount != 1 && httpCount != 1) {
                System.out.println("Proxy = " + proxyCount + ", http = " + httpCount);
                throw new RuntimeException("Failed: Proxy being sent " + proxyCount  + " requests");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
class SimpleServer extends Thread
{
    private ServerSocket ss;
    private Socket sock;
    private int connectionCount;
    String replyOK =  "HTTP/1.1 200 OK\r\n" +
                      "Content-Length: 0\r\n\r\n";
    public SimpleServer(ServerSocket ss) {
        this.ss = ss;
    }
    public void run() {
        try {
            sock = ss.accept();
            connectionCount++;
            InputStream is = sock.getInputStream();
            OutputStream os = sock.getOutputStream();
            MessageHeader headers =  new MessageHeader (is);
            os.write(replyOK.getBytes("UTF-8"));
            headers =  new MessageHeader (is);
            connectionCount++;
            os.write(replyOK.getBytes("UTF-8"));
            sock.close();
        } catch (Exception e) {
            if (sock != null && !sock.isClosed()) {
                try { sock.close();
                } catch (IOException ioe) {}
            }
        }
    }
    public int getConnectionCount() {
        return connectionCount;
    }
    public void terminate() {
        if (sock != null && !sock.isClosed()) {
            try { sock.close();
            } catch (IOException ioe) {}
        }
    }
}
