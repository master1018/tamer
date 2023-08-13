public class B6181108 implements Runnable {
    ServerSocket ss;
    static String urlWithSpace;
    public void run() {
        try {
            Socket s = ss.accept();
            InputStream is = s.getInputStream ();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            String x;
            while ((x=r.readLine()) != null) {
                if (x.length() ==0) {
                    break;
                }
            }
            PrintStream out = new PrintStream(
                                 new BufferedOutputStream(
                                    s.getOutputStream() ));
            out.print("HTTP/1.1 200 OK\r\n");
            out.print("Content-Type: text/html; charset=iso-8859-1\r\n");
            out.print("Content-Length: 0\r\n");
            out.print("Connection: close\r\n");
            out.print("\r\n");
            out.print("\r\n");
            out.flush();
            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { ss.close(); } catch (IOException unused) {}
        }
    }
    static class ResponseCache extends java.net.ResponseCache {
        public CacheResponse get (URI uri, String method, Map<String,List<String>> hdrs) {
            System.out.println ("get uri = " + uri);
            if (!urlWithSpace.equals(uri.toString())) {
                throw new RuntimeException("test failed");
            }
            return null;
        }
        public CacheRequest put (URI uri,  URLConnection urlc) {
            System.out.println ("put uri = " + uri);
            return null;
        }
    }
    B6181108() throws Exception {
        ss = new ServerSocket(0);
        (new Thread(this)).start();
        ResponseCache.setDefault (new ResponseCache());
        urlWithSpace = "http:
                        Integer.toString(ss.getLocalPort()) +
                        "/space%20test/page1.html";
        URL url = new URL (urlWithSpace);
        URLConnection urlc = url.openConnection();
        int i = ((HttpURLConnection)(urlc)).getResponseCode();
        System.out.println ("response code = " + i);
        ResponseCache.setDefault(null);
    }
    public static void main(String args[]) throws Exception {
        new B6181108();
    }
}
