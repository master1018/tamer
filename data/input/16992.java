public class ZeroContentLength {
    static boolean debug = false;
    static void debug(String msg) {
        if (debug)
            System.out.println(msg);
    }
    static String response;
    static int contentLength;
    static synchronized void setResponse(String rsp, int cl) {
        response = rsp;
        contentLength = cl;
    }
    static synchronized String getResponse() {
        return response;
    }
    static synchronized int getContentLength() {
        return contentLength;
    }
    class Worker extends Thread {
        Socket s;
        int id;
        Worker(Socket s, int id) {
            this.s = s;
            this.id = id;
        }
        final int CR = '\r';
        final int LF = '\n';
        public void run() {
            try {
                s.setSoTimeout(2000);
                int max = 20; 
                InputStream in = new BufferedInputStream(s.getInputStream());
                for (;;) {
                    int c, total=0;
                    try {
                        while ((c = in.read()) > 0) {
                            total++;
                            if (c == CR) {
                                if ((c = in.read()) > 0) {
                                    total++;
                                    if (c == LF) {
                                        if ((c = in.read()) > 0) {
                                            total++;
                                            if (c == CR) {
                                                if ((c = in.read()) > 0) {
                                                    total++;
                                                    if (c == LF) {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (SocketTimeoutException e) {}
                    debug("worker " + id +
                        ": Read request from client " +
                        "(" + total + " bytes).");
                    if (total == 0) {
                        debug("worker: " + id + ": Shutdown");
                        return;
                    }
                    PrintStream out = new PrintStream(
                                        new BufferedOutputStream(
                                                s.getOutputStream() ));
                    out.print("HTTP/1.1 " + getResponse() + "\r\n");
                    int clen = getContentLength();
                    if (clen >= 0) {
                        out.print("Content-Length: " + clen +
                                    "\r\n");
                    }
                    out.print("\r\n");
                    for (int i=0; i<clen; i++) {
                        out.write( (byte)'.' );
                    }
                    out.flush();
                    debug("worked " + id +
                        ": Sent response to client, length: " + clen);
                    if (--max == 0) {
                        s.close();
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    s.close();
                } catch (Exception e) { }
            }
        }
    }
    class Server extends Thread {
        ServerSocket ss;
        int connectionCount;
        boolean shutdown = false;
        Server(ServerSocket ss) {
            this.ss = ss;
        }
        public synchronized int connectionCount() {
            return connectionCount;
        }
        public synchronized void shutdown() {
            shutdown = true;
        }
        public void run() {
            try {
                ss.setSoTimeout(2000);
                for (;;) {
                    Socket s;
                    try {
                        debug("server: Waiting for connections");
                        s = ss.accept();
                    } catch (SocketTimeoutException te) {
                        synchronized (this) {
                            if (shutdown) {
                                debug("server: Shuting down.");
                                return;
                            }
                        }
                        continue;
                    }
                    int id;
                    synchronized (this) {
                        id = connectionCount++;
                    }
                    Worker w = new Worker(s, id);
                    w.start();
                    debug("server: Started worker " + id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    ss.close();
                } catch (Exception e) { }
            }
        }
    }
    int doRequest(String uri) throws Exception {
        URL url = new URL(uri);
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        int cl = http.getContentLength();
        InputStream in = http.getInputStream();
        byte b[] = new byte[100];
        int total = 0;
        int n;
        do {
            n = in.read(b);
            if (n > 0) total += n;
        } while (n > 0);
        in.close();
        if (cl >= 0 && total != cl) {
            System.err.println("content-length header indicated: " + cl);
            System.err.println("Actual received: " + total);
            throw new Exception("Content-length didn't match actual received");
        }
        return total;
    }
    ZeroContentLength() throws Exception {
        ServerSocket ss = new ServerSocket(0);
        Server svr = new Server(ss);
        svr.start();
        String uri = "http:
                     Integer.toString(ss.getLocalPort()) +
                     "/foo.html";
        int expectedTotal = 0;
        int actualTotal = 0;
        System.out.println("**********************************");
        System.out.println("200 OK, content-length:1024 ...");
        setResponse("200 OK", 1024);
        for (int i=0; i<5; i++) {
            actualTotal += doRequest(uri);
            expectedTotal += 1024;
        }
        System.out.println("**********************************");
        System.out.println("200 OK, content-length:0 ...");
        setResponse("200 OK", 0);
        for (int i=0; i<5; i++) {
            actualTotal += doRequest(uri);
        }
        System.out.println("**********************************");
        System.out.println("304 Not-Modified, (no content-length) ...");
        setResponse("304 Not-Modifed", -1);
        for (int i=0; i<5; i++) {
            actualTotal += doRequest(uri);
        }
        System.out.println("**********************************");
        System.out.println("204 No-Content, (no content-length) ...");
        setResponse("204 No-Content", -1);
        for (int i=0; i<5; i++) {
            actualTotal += doRequest(uri);
        }
        svr.shutdown();
        System.out.println("**********************************");
        if (actualTotal == expectedTotal) {
            System.out.println("Passed: Actual total equal to expected total");
        } else {
            throw new Exception("Actual total != Expected total!!!");
        }
        int cnt = svr.connectionCount();
        if (cnt == 1) {
            System.out.println("Passed: Only 1 connection established");
        } else {
            throw new Exception("Test failed: Number of connections " +
                "established: " + cnt + " - see log for details.");
        }
    }
    public static void main(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("-d")) {
            debug = true;
        }
        new ZeroContentLength();
    }
}
