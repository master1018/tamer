public class QueryListener implements Runnable {
    private Snapshot snapshot;
    private OQLEngine engine;
    private int port;
    public QueryListener(int port) {
        this.port = port;
        this.snapshot = null;   
        this.engine = null; 
    }
    public void setModel(Snapshot ss) {
        this.snapshot = ss;
        if (OQLEngine.isOQLSupported()) {
            this.engine = new OQLEngine(ss);
        }
    }
    public void run() {
        try {
            waitForRequests();
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
    private void waitForRequests() throws IOException {
        ServerSocket ss = new ServerSocket(port);
        Thread last = null;
        for (;;) {
            Socket s = ss.accept();
            Thread t = new Thread(new HttpReader(s, snapshot, engine));
            if (snapshot == null) {
                t.setPriority(Thread.NORM_PRIORITY+1);
            } else {
                t.setPriority(Thread.NORM_PRIORITY-1);
                if (last != null) {
                    try {
                        last.setPriority(Thread.NORM_PRIORITY-2);
                    } catch (Throwable ignored) {
                    }
                }
            }
            t.start();
            last = t;
        }
    }
}
