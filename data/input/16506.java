public class HttpTest {
    static class HttpServer implements Runnable {
        private static HttpServer svr = null;
        private static Counters cnts = null;
        private static ServerSocket ss;
        private static Object counterLock = new Object();
        private static int getCount = 0;
        private static int headCount = 0;
        class Worker extends Thread {
            Socket s;
            Worker(Socket s) {
                this.s = s;
            }
            public void run() {
                InputStream in = null;
                try {
                    in = s.getInputStream();
                    for (;;) {
                        byte b[] = new byte[1024];
                        int n, total=0;
                        s.setSoTimeout(5000);
                        try {
                            do {
                                n = in.read(b, total, b.length-total);
                                s.setSoTimeout(500);
                                if (n > 0) total += n;
                            } while (n > 0);
                        } catch (SocketTimeoutException e) { }
                        if (total == 0) {
                            s.close();
                            return;
                        }
                        boolean getRequest = false;
                        if (b[0] == 'G' && b[1] == 'E' && b[2] == 'T')
                            getRequest = true;
                        synchronized (counterLock) {
                            if (getRequest)
                                getCount++;
                            else
                                headCount++;
                        }
                        PrintStream out = new PrintStream(
                                new BufferedOutputStream(
                                        s.getOutputStream() ));
                        out.print("HTTP/1.1 200 OK\r\n");
                        out.print("Content-Length: 75000\r\n");
                        out.print("\r\n");
                        if (getRequest) {
                            for (int i=0; i<75*1000; i++) {
                                out.write( (byte)'.' );
                            }
                        }
                        out.flush();
                    } 
                } catch (Exception e) {
                    unexpected(e);
                } finally {
                    if (in != null) { try {in.close(); } catch(IOException e) {unexpected(e);} }
                }
            }
        }
        HttpServer() throws Exception {
            ss = new ServerSocket(0);
        }
        public void run() {
            try {
                ss.setSoTimeout(10000);
                for (;;) {
                    Socket s = ss.accept();
                    (new Worker(s)).start();
                }
            } catch (Exception e) {
            }
        }
        void unexpected(Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
        public static HttpServer create() throws Exception {
            if (svr != null)
                return svr;
            cnts = new Counters();
            svr = new HttpServer();
            (new Thread(svr)).start();
            return svr;
        }
        public static void shutdown() throws Exception {
            if (svr != null) {
                ss.close();
                svr = null;
            }
        }
        public int port() {
            return ss.getLocalPort();
        }
        public static class Counters {
            public void reset() {
                synchronized (counterLock) {
                    getCount = 0;
                    headCount = 0;
                }
            }
            public int getCount() {
                synchronized (counterLock) {
                    return getCount;
                }
            }
            public int headCount() {
                synchronized (counterLock) {
                    return headCount;
                }
            }
            public String toString() {
                synchronized (counterLock) {
                    return "GET count: " + getCount + "; " +
                       "HEAD count: " + headCount;
                }
            }
        }
        public Counters counters() {
            return cnts;
        }
    }
    public static void main(String args[]) throws Exception {
        boolean failed = false;
        HttpServer svr = HttpServer.create();
        URL urls[] =
            { new URL("http:
              new URL("http:
        URLClassLoader cl = new URLClassLoader(urls);
        svr.counters().reset();
        URL url = cl.getResource("foo.gif");
        System.out.println(svr.counters());
        if (svr.counters().getCount() > 0 ||
            svr.counters().headCount() > 1) {
            failed = true;
        }
        svr.counters().reset();
        InputStream in = cl.getResourceAsStream("foo2.gif");
        in.close();
        System.out.println(svr.counters());
        if (svr.counters().getCount() > 1) {
            failed = true;
        }
        svr.counters().reset();
        Enumeration e = cl.getResources("foos.gif");
        try {
            for (;;) {
                e.nextElement();
            }
        } catch (NoSuchElementException exc) { }
        System.out.println(svr.counters());
        if (svr.counters().getCount() > 1) {
            failed = true;
        }
        svr.shutdown();
        if (failed) {
            throw new Exception("Excessive http connections established - Test failed");
        }
    }
}
