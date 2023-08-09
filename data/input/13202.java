public class NullHost {
    class Server extends Thread {
        private ServerSocket svr;
        public Server() throws IOException {
            svr = new ServerSocket();
            svr.bind(new InetSocketAddress(0));
        }
        public int getPort() {
            return svr.getLocalPort();
        }
        public void shutdown() {
            try {
                svr.close();
            } catch (IOException e) {
            }
        }
        public void run() {
            Socket s;
            try {
                while (true) {
                    s = svr.accept();
                    s.close();
                }
            } catch (IOException e) {
            }
        }
    }
    public static void main(String[] args) throws IOException {
        NullHost n = new NullHost();
    }
    public NullHost () throws IOException {
        Server s = new Server();
        int port = s.getPort();
        s.start();
        try {
            Socket sock = new Socket((String)null, port);
            sock.close();
            sock = new Socket((String)null, port, true);
            sock.close();
            sock = new Socket((String)null, port, null, 0);
            sock.close();
        } catch (NullPointerException e) {
            throw new RuntimeException("Got a NPE");
        } finally {
            s.shutdown();
        }
    }
}
