public class bug4714674 {
    public static void main(String[] args) throws Exception {
        new bug4714674().test();
    }
    private void test() throws Exception {
        final DeafServer server = new DeafServer();
        final String baseURL = "http:
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    JEditorPane pane = new JEditorPane();
                    ((javax.swing.text.AbstractDocument)pane.getDocument()).
                            setAsynchronousLoadPriority(1);
                    pane.setPage(baseURL);
                } catch (IOException e) {
                    throw new Error(e);
                }
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (server) {
                    server.wakeup = true;
                    server.notifyAll();
                }
                pass();
            }
        });
        try {
            Thread.sleep(5000);
            if (!passed()) {
                throw new RuntimeException("Failed: EDT was blocked");
            }
        } finally {
            server.destroy();
        }
    }
    private boolean passed = false;
    private synchronized boolean passed() {
        return passed;
    }
    private synchronized void pass() {
        passed = true;
    }
}
class DeafServer {
    HttpServer server;
    boolean wakeup = false;
    public DeafServer() throws IOException {
        InetSocketAddress addr = new InetSocketAddress(0);
        server = HttpServer.create(addr, 0);
        HttpHandler handler = new DeafHandler();
        server.createContext("/", handler);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }
    public void destroy() {
        server.stop(0);
    }
    public int getPort() {
        return server.getAddress().getPort();
    }
    class DeafHandler implements HttpHandler {
        public void handle(HttpExchange r) throws IOException {
            synchronized (DeafServer.this) {
                while (! wakeup) {
                    try {
                        DeafServer.this.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }
}
