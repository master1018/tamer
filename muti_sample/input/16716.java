public class LoopbackAddresses implements HttpCallback {
    static HttpServer server;
    public void request (HttpTransaction req) {
        req.setResponseEntityBody ("Hello .");
        try {
            req.sendResponse (200, "Ok");
            req.orderlyClose();
        } catch (IOException e) {
        }
    }
    public static void main(String[] args) {
        try {
            server = new HttpServer (new LoopbackAddresses(), 1, 10, 0);
            ProxyServer pserver = new ProxyServer(InetAddress.getByName("localhost"), server.getLocalPort());
            new Thread(pserver).start();
            System.setProperty("http.proxyHost", "localhost");
            System.setProperty("http.proxyPort", pserver.getPort()+"");
            URL url = new URL("http:
            try {
                HttpURLConnection urlc = (HttpURLConnection)url.openConnection ();
                int respCode = urlc.getResponseCode();
                urlc.disconnect();
            } catch (IOException ioex) {
                throw new RuntimeException("direct connection should succeed :"+ioex.getMessage());
            }
            try {
                url = new URL("http:
                HttpURLConnection urlc = (HttpURLConnection)url.openConnection ();
                int respCode = urlc.getResponseCode();
                urlc.disconnect();
            } catch (IOException ioex) {
                throw new RuntimeException("direct connection should succeed :"+ioex.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (server != null) {
                server.terminate();
            }
        }
    }
    private static class ProxyServer extends Thread {
        private static ServerSocket ss = null;
        private Socket clientSocket = null;
        private InetAddress serverInetAddr;
        private int     serverPort;
        public ProxyServer(InetAddress server, int port) throws IOException {
            serverInetAddr = server;
            serverPort = port;
            ss = new ServerSocket(0);
        }
        public void run() {
            try {
                clientSocket = ss.accept();
                throw new RuntimeException("loopback addresses shouldn't go through the proxy "+clientSocket);
            } catch (IOException e) {
                System.out.println("Proxy Failed: " + e);
                e.printStackTrace();
            } finally {
                try {
                    ss.close();
                }
                catch (IOException excep) {
                    System.out.println("ProxyServer close error: " + excep);
                    excep.printStackTrace();
                }
            }
        }
        public int getPort() {
            return ss.getLocalPort();
        }
    }
}
