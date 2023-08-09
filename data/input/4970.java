public class ChunkedEncodingTest{
    private static MessageDigest serverDigest, clientDigest;
    private static volatile byte[] serverMac, clientMac;
    static void client(String u) throws Exception {
        URL url = new URL(u);
        out.println("client opening connection to: " + u);
        URLConnection urlc = url.openConnection();
        DigestInputStream dis =
                new DigestInputStream(urlc.getInputStream(), clientDigest);
        while (dis.read() != -1);
        clientMac = dis.getMessageDigest().digest();
        dis.close();
    }
    public static void test() {
        HttpServer server = null;
        try {
            serverDigest = MessageDigest.getInstance("MD5");
            clientDigest = MessageDigest.getInstance("MD5");
            server = startHttpServer();
            int port = server.getAddress().getPort();
            out.println ("Server listening on port: " + port);
            client("http:
            if (!MessageDigest.isEqual(clientMac, serverMac)) {
                throw new RuntimeException(
                 "Data received is NOT equal to the data sent");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (server != null)
                server.stop(0);
        }
    }
    public static void main(String[] args) {
        test();
    }
    static HttpServer startHttpServer() throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(0), 0);
        HttpHandler httpHandler = new SimpleHandler();
        httpServer.createContext("/chunked/", httpHandler);
        httpServer.start();
        return httpServer;
    }
    static class SimpleHandler implements HttpHandler {
        static byte[] baMessage;
        final static int CHUNK_SIZE = 8 * 1024;
        final static int MESSAGE_LENGTH = 52 * CHUNK_SIZE;
        static {
            baMessage = new byte[MESSAGE_LENGTH];
            for (int i=0; i<MESSAGE_LENGTH; i++)
                baMessage[i] = (byte)i;
        }
        @Override
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();
            while (is.read() != -1);
            is.close();
            t.sendResponseHeaders (200, 0);
            OutputStream os = t.getResponseBody();
            DigestOutputStream dos = new DigestOutputStream(os, serverDigest);
            int offset = 0;
            for (int i=0; i<52; i++) {
                dos.write(baMessage, offset, CHUNK_SIZE);
                offset += CHUNK_SIZE;
            }
            serverMac = serverDigest.digest();
            os.close();
            t.close();
        }
    }
}
