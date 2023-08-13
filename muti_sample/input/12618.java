class RedirServer extends Thread {
    ServerSocket s;
    Socket   s1;
    InputStream  is;
    OutputStream os;
    int port;
    String reply1Part1 = "HTTP/1.1 307 Temporary Redirect\r\n" +
        "Date: Mon, 15 Jan 2001 12:18:21 GMT\r\n" +
        "Server: Apache/1.3.14 (Unix)\r\n" +
        "Location: http:
    String reply1Part2 = "/redirected.html\r\n" +
        "Connection: close\r\n" +
        "Content-Type: text/html; charset=iso-8859-1\r\n\r\n" +
        "<html>Hello</html>";
    RedirServer (ServerSocket y) {
        s = y;
        port = s.getLocalPort();
        System.out.println("Server created listening on " + port);
    }
    String reply2 = "HTTP/1.1 200 Ok\r\n" +
        "Date: Mon, 15 Jan 2001 12:18:21 GMT\r\n" +
        "Server: Apache/1.3.14 (Unix)\r\n" +
        "Connection: close\r\n" +
        "Content-Type: text/html; charset=iso-8859-1\r\n\r\n" +
        "World";
    public void run () {
        try {
            s1 = s.accept ();
            is = s1.getInputStream ();
            os = s1.getOutputStream ();
            is.read ();
            String reply = reply1Part1 + port + reply1Part2;
            os.write (reply.getBytes());
            os.close();
            s.setSoTimeout (5000);
            s1 = s.accept ();
            is = s1.getInputStream ();
            os = s1.getOutputStream ();
            is.read();
            os.write (reply2.getBytes());
            os.close();
        }
        catch (Exception e) {
            System.out.println("Server: caught " + e);
            e.printStackTrace();
        } finally {
            try { s.close(); } catch (IOException unused) {}
        }
    }
};
public class Redirect307Test {
    public static final int DELAY = 10;
    public static void main(String[] args) throws Exception {
        int port;
        RedirServer server;
        ServerSocket sock;
        try {
            sock = new ServerSocket (0);
            port = sock.getLocalPort ();
        }
        catch (Exception e) {
            System.out.println ("Exception: " + e);
            return;
        }
        server = new RedirServer(sock);
        server.start ();
        try  {
            String s = "http:
            URL url = new URL(s);
            URLConnection conURL =  url.openConnection();
            conURL.setDoInput(true);
            conURL.setAllowUserInteraction(false);
            conURL.setUseCaches(false);
            InputStream in = conURL.getInputStream();
            if ((in.read() != (int)'W') || (in.read()!=(int)'o')) {
                throw new RuntimeException ("Unexpected string read");
            }
        }
        catch(IOException e) {
            e.printStackTrace();
            throw new RuntimeException ("Exception caught + " + e);
        }
    }
}
