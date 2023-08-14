public class SetIfModifiedSince {
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
        public void run() {
            try {
                String x;
                s = srv.accept ();
                is = s.getInputStream ();
                BufferedReader r = new BufferedReader(new InputStreamReader(is));
                os = s.getOutputStream ();
                while ((x=r.readLine()) != null) {
                    String header = "If-Modified-Since: ";
                    if (x.startsWith(header)) {
                        if (x.charAt(header.length()) == '?') {
                            s.close ();
                            srv.close (); 
                            throw new RuntimeException
                                    ("Invalid HTTP date specification");
                        }
                        break;
                    }
                }
                s.close ();
                srv.close (); 
            } catch (IOException e) {}
        }
    }
    public static void main (String[] args) {
        try {
            Locale.setDefault(Locale.JAPAN);
            ServerSocket serversocket = new ServerSocket (0);
            int port = serversocket.getLocalPort ();
            XServer server = new XServer (serversocket);
            server.start ();
            Thread.sleep (2000);
            URL url = new URL ("http:
            URLConnection urlc = url.openConnection ();
            urlc.setIfModifiedSince (10000000);
            InputStream is = urlc.getInputStream ();
            int i=0, c;
            Thread.sleep (5000);
        } catch (Exception e) {
        }
    }
}
