public class URLConnectionHeaders {
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
                BufferedWriter w = new BufferedWriter(new OutputStreamWriter(os));
                w.write("HTTP/1.1 200 OK\r\n");
                w.write("Content-Type: text/plain\r\n");
                while ((x=r.readLine()).length() != 0) {
                    System.out.println("request: "+x);
                    if (!x.startsWith("GET")) {
                        w.write(x);
                        w.newLine();
                    }
                }
                w.newLine();
                w.flush();
                s.close ();
            } catch (IOException e) { e.printStackTrace();
            } finally {
                try { srv.close(); } catch (IOException unused) {}
            }
        }
    }
    public static void main(String[] args) {
        try {
            ServerSocket serversocket = new ServerSocket (0);
            int port = serversocket.getLocalPort ();
            XServer server = new XServer (serversocket);
            server.start ();
            Thread.sleep (200);
            URL url = new URL ("http:
            URLConnection uc = url.openConnection ();
            uc.addRequestProperty("Cookie", "cookie1");
            uc.addRequestProperty("Cookie", "cookie2");
            uc.addRequestProperty("Cookie", "cookie3");
            Map e = uc.getRequestProperties();
            if (!((List)e.get("Cookie")).toString().equals("[cookie3, cookie2, cookie1]")) {
                throw new RuntimeException("getRequestProperties fails");
            }
            e = uc.getHeaderFields();
            if ((e.get("Content-Type") == null) || (e.get(null) == null)) {
                throw new RuntimeException("getHeaderFields fails");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
