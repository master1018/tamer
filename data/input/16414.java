public class KeepAliveStreamCloseWithWrongContentLength {
    static class XServer extends Thread {
        ServerSocket srv;
        Socket s;
        InputStream is;
        OutputStream os;
        XServer (ServerSocket s) {
            srv = s;
        }
        public void run() {
            try {
                s = srv.accept ();
                InputStream is = s.getInputStream();
                for (int i=0; i<10; i++) {
                    is.read();
                }
                OutputStreamWriter ow =
                    new OutputStreamWriter((os = s.getOutputStream()));
                ow.write("HTTP/1.0 200 OK\n");
                ow.write("Content-Length: 10\n");
                ow.write("Content-Type: text/html\n");
                ow.write("Connection: Keep-Alive\n");
                ow.write("\n");
                ow.write("123456789");
                ow.flush();
            } catch (Exception e) {
            } finally {
                try {if (os != null) { os.close(); }} catch (IOException e) {}
            }
        }
    }
    public static void main (String[] args) throws Exception {
        ServerSocket serversocket = new ServerSocket (0);
        try {
            int port = serversocket.getLocalPort ();
            XServer server = new XServer (serversocket);
            server.start ();
            URL url = new URL ("http:
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection ();
            InputStream is = urlc.getInputStream ();
            int c = 0;
            while (c != -1) {
                try {
                    c=is.read();
                } catch (IOException ioe) {
                    is.read ();
                    break;
                }
            }
            is.close();
        } catch (IOException e) {
            return;
        } catch (NullPointerException e) {
            throw new RuntimeException (e);
        } finally {
            if (serversocket != null) serversocket.close();
        }
    }
}
