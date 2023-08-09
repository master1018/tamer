class Server extends Thread {
    Server (ServerSocket server) {
        this.server = server;
    }
    public void run () {
        try {
            String version = System.getProperty ("java.version");
            String expected = "foo Java/"+version;
            Socket s = server.accept ();
            MessageHeader header = new MessageHeader (s.getInputStream());
            String v = header.findValue ("User-Agent");
            if (!expected.equals (v)) {
                error ("Got unexpected User-Agent: " + v);
            } else {
                success ();
            }
            OutputStream w = s.getOutputStream();
            w.write("HTTP/1.1 200 OK\r\n".getBytes());
            w.write("Content-Type: text/plain\r\n".getBytes());
            w.write("Content-Length: 5\r\n".getBytes());
            w.write("\r\n".getBytes());
            w.write("12345\r\n".getBytes());
        } catch (Exception e) {
            error (e.toString());
        }
    }
    String msg;
    ServerSocket server;
    boolean success;
    synchronized String getMessage () {
        return msg;
    }
    synchronized boolean succeeded () {
        return success;
    }
    synchronized void success () {
        success = true;
    }
    synchronized void error (String s) {
        success = false;
        msg = s;
    }
}
public class UserAgent {
    public static void main(String[] args) throws Exception {
        ServerSocket server = new ServerSocket (0);
        Server s = new Server (server);
        s.start ();
        int port = server.getLocalPort ();
        URL url = new URL ("http:
        URLConnection urlc = url.openConnection ();
        urlc.getInputStream ();
        s.join ();
        if (!s.succeeded()) {
            throw new RuntimeException (s.getMessage());
        }
    }
}
