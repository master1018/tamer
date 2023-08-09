public class SetChunkedStreamingMode implements HttpCallback {
    void okReply (HttpTransaction req) throws IOException {
        req.setResponseEntityBody ("Hello .");
        req.sendResponse (200, "Ok");
            System.out.println ("Server: sent response");
        req.orderlyClose();
    }
    public void request (HttpTransaction req) {
        try {
            okReply (req);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static void read (InputStream is) throws IOException {
        int c;
        System.out.println ("reading");
        while ((c=is.read()) != -1) {
            System.out.write (c);
        }
        System.out.println ("");
        System.out.println ("finished reading");
    }
    static HttpServer server;
    public static void main (String[] args) throws Exception {
        try {
            server = new HttpServer (new SetChunkedStreamingMode(), 1, 10, 0);
            System.out.println ("Server: listening on port: " + server.getLocalPort());
            URL url = new URL ("http:
            System.out.println ("Client: connecting to " + url);
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            urlc.setChunkedStreamingMode (0);
            urlc.setRequestMethod("POST");
            urlc.setDoOutput(true);
            InputStream is = urlc.getInputStream();
        } catch (Exception e) {
            if (server != null) {
                server.terminate();
            }
            throw e;
        }
        server.terminate();
    }
    public static void except (String s) {
        server.terminate();
        throw new RuntimeException (s);
    }
}
