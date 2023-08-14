public class PerConnectionProxy implements HttpCallback {
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
            server = new HttpServer (new PerConnectionProxy(), 1, 10, 0);
            ProxyServer pserver = new ProxyServer(InetAddress.getByName("localhost"), server.getLocalPort());
            new Thread(pserver).start();
            URL url = new URL("http:
            try {
                InetSocketAddress isa = InetSocketAddress.createUnresolved("inexistent", 8080);
                Proxy proxy = new Proxy(Proxy.Type.HTTP, isa);
                HttpURLConnection urlc = (HttpURLConnection)url.openConnection (proxy);
                InputStream is = urlc.getInputStream ();
                is.close();
                throw new RuntimeException("non existing per connection proxy should lead to IOException");
            } catch (IOException ioex) {
            }
            try {
                HttpURLConnection urlc = (HttpURLConnection)url.openConnection (Proxy.NO_PROXY);
                int respCode = urlc.getResponseCode();
                urlc.disconnect();
            } catch (IOException ioex) {
                throw new RuntimeException("direct connection should succeed :"+ioex.getMessage());
            }
            try {
                InetSocketAddress isa = InetSocketAddress.createUnresolved("localhost", pserver.getPort());
                Proxy p = new Proxy(Proxy.Type.HTTP, isa);
                HttpURLConnection urlc = (HttpURLConnection)url.openConnection (p);
                int respCode = urlc.getResponseCode();
                urlc.disconnect();
            } catch (IOException ioex) {
                throw new RuntimeException("connection through a local proxy should succeed :"+ioex.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (server != null) {
                server.terminate();
            }
        }
    }
    static class ProxyServer extends Thread {
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
        public int getPort() {
            return ss.getLocalPort();
        }
        static class ProxyTunnel extends Thread {
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
    }
}
