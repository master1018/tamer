public class KeepAliveTimerThread {
    static class Fetcher implements Runnable {
        String url;
        Fetcher(String url) {
            this.url = url;
        }
        public void run() {
            try {
                InputStream in =
                    (new URL(url)).openConnection().getInputStream();
                byte b[] = new byte[128];
                int n;
                do {
                    n = in.read(b);
                } while (n > 0);
                in.close();
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
    }
    static class Server extends Thread {
        ServerSocket server;
        Server (ServerSocket server) {
            super ();
            this.server = server;
        }
        void readAll (Socket s) throws IOException {
            byte[] buf = new byte [128];
            InputStream is = s.getInputStream ();
            s.setSoTimeout(1000);
            try {
                while (is.read(buf) > 0) ;
            } catch (SocketTimeoutException x) { }
        }
        public void run() {
            try {
                Socket s = server.accept();
                readAll(s);
                PrintStream out = new PrintStream(
                                                  new BufferedOutputStream(
                                                                           s.getOutputStream() ));
                out.print("HTTP/1.1 200 OK\r\n");
                out.print("Content-Type: text/html; charset=iso-8859-1\r\n");
                out.print("Content-Length: 78\r\n");
                out.print("\r\n");
                out.print("<HTML>");
                out.print("<HEAD><TITLE>File Content</TITLE></HEAD>");
                out.print("<BODY>A dummy body.</BODY>");
                out.print("</HTML>");
                out.flush();
                s.close();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try { server.close(); } catch (IOException unused) {}
            }
        }
    }
    public static void main(String args[]) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        Server s = new Server (ss);
        s.start();
        String url = "http:
        ThreadGroup grp = new ThreadGroup("MyGroup");
        Thread thr = new Thread(grp, new Fetcher(url));
        thr.start();
        thr.join();
        if (grp.activeCount() > 0) {
            throw new RuntimeException("Keep-alive thread started in wrong thread group");
        }
        grp.destroy();
    }
}
