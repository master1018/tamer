class RedirLimitServer extends Thread {
    static final int TIMEOUT = 10 * 1000;
    static final int NUM_REDIRECTS = 9;
    static final String reply1 = "HTTP/1.1 307 Temporary Redirect\r\n" +
        "Date: Mon, 15 Jan 2001 12:18:21 GMT\r\n" +
        "Server: Apache/1.3.14 (Unix)\r\n" +
        "Location: http:
    static final String reply2 = ".html\r\n" +
        "Connection: close\r\n" +
        "Content-Type: text/html; charset=iso-8859-1\r\n\r\n" +
        "<html>Hello</html>";
    static final String reply3 = "HTTP/1.1 200 Ok\r\n" +
        "Date: Mon, 15 Jan 2001 12:18:21 GMT\r\n" +
        "Server: Apache/1.3.14 (Unix)\r\n" +
        "Connection: close\r\n" +
        "Content-Type: text/html; charset=iso-8859-1\r\n\r\n" +
        "World";
    final ServerSocket ss;
    final int port;
    RedirLimitServer(ServerSocket ss) {
        this.ss = ss;
        port = ss.getLocalPort();
    }
    public void run() {
        try {
            ss.setSoTimeout(TIMEOUT);
            for (int i=0; i<NUM_REDIRECTS; i++) {
                try (Socket s = ss.accept()) {
                    s.setSoTimeout(TIMEOUT);
                    InputStream is = s.getInputStream();
                    OutputStream os = s.getOutputStream();
                    is.read();
                    String reply = reply1 + port + "/redirect" + i + reply2;
                    os.write(reply.getBytes());
                }
            }
            try (Socket s = ss.accept()) {
                InputStream is = s.getInputStream();
                OutputStream os = s.getOutputStream();
                is.read();
                os.write(reply3.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { ss.close(); } catch (IOException unused) {}
        }
    }
};
public class RedirectLimit {
    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket (0);
        int port = ss.getLocalPort();
        RedirLimitServer server = new RedirLimitServer(ss);
        server.start();
        InputStream in = null;
        try {
            URL url = new URL("http:
            URLConnection conURL =  url.openConnection();
            conURL.setDoInput(true);
            conURL.setAllowUserInteraction(false);
            conURL.setUseCaches(false);
            in = conURL.getInputStream();
            if ((in.read() != (int)'W') || (in.read()!=(int)'o')) {
                throw new RuntimeException("Unexpected string read");
            }
        } finally {
            if ( in != null ) { in.close(); }
        }
    }
}
