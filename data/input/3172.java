public class KeepAliveStreamClose {
    static class XServer extends Thread {
        ServerSocket srv;
        Socket s;
        InputStream is;
        OutputStream os;
        XServer (ServerSocket s) {
            srv = s;
        }
        Socket getSocket () {
            return (s);
        }
        static String response = "HTTP/1.1 200 OK\nDate: Thu, 07 Dec 2000 11:32:28 GMT\n"+
            "Server: Apache/1.3.6 (Unix)\nKeep-Alive: timeout=15, max=100\nConnection: Keep-Alive\n"+
            "Content-length: 255\nContent-Type: text/html\n\n";
        public void run() {
            try {
                s = srv.accept ();
                is = s.getInputStream ();
                os = s.getOutputStream ();
                for (int i=0; i<10; i++) {
                    is.read();
                }
                os.write (response.getBytes());
                for (int i=0; i<255; i++) {
                    os.write ("X".getBytes());
                    Thread.sleep (1000);
                }
            } catch (Exception e) {
            }
        }
    }
    public static void main (String[] args) {
        try {
            ServerSocket serversocket = new ServerSocket (0);
            int port = serversocket.getLocalPort ();
            XServer server = new XServer (serversocket);
            server.start ();
            URL url = new URL ("http:
            URLConnection urlc = url.openConnection ();
            InputStream is = urlc.getInputStream ();
            int i=0, c;
            while ((c=is.read())!= -1) {
                i++;
                if (i == 5) {
                    server.getSocket().close ();
                    is.close();
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException ("Unexpected exception");
        }
    }
}
