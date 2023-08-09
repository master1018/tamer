public class URLTest extends TestCase {
    private static void get(String u) throws IOException {
        URL url = new URL(u);
        URLConnection cn = url.openConnection();
        cn.connect();
        InputStream stream = cn.getInputStream();
        if (stream == null) {
            throw new RuntimeException("stream is null");
        }
        byte[] data = new byte[1024];
        stream.read(data);
        assertTrue(new String(data).indexOf("<html>") >= 0);
    }
    @Suppress
    public void testGetHTTP() throws Exception {
        get("http:
    }
    @Suppress
    public void testGetHTTPS() throws Exception {
        get("https:
    }
    private static class DummyServer implements Runnable {
        private int keepAliveCount;
        private Map<String, String> headers = new HashMap<String, String>();
        public DummyServer(int keepAliveCount) {
            this.keepAliveCount = keepAliveCount;
        }
        public void run() {
            try {
                ServerSocket server = new ServerSocket(8182);
                Socket socket = server.accept();
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                try {
                    for (int i = 0; i < keepAliveCount; i++) {
                        reader.readLine();
                        headers.clear();
                        while (true) {
                            String header = reader.readLine();
                            if (header.length() == 0) {
                                break;
                            }
                            int colon = header.indexOf(":");
                            String key = header.substring(0, colon);
                            String value = header.substring(colon + 1).trim();
                            headers.put(key, value);
                        }
                        OutputStream output = socket.getOutputStream();
                        PrintWriter writer = new PrintWriter(output);
                        try {
                            writer.println("HTTP/1.1 200 OK");
                            String body = "Hello, Android world #" + i + "!";
                            writer.println("Content-Length: " + body.length());
                            writer.println("");
                            writer.print(body);
                            writer.flush();
                        } finally {
                            writer.close();
                        }
                    }
                } finally {
                    reader.close();
                }
                socket.close();
                server.close();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    private String request(URL url) throws Exception {
        URLConnection connection = url.openConnection();
        connection.connect();
        InputStream input = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }
    @Suppress
    public void testGetKeepAlive() throws Exception {
        new Thread(new DummyServer(3)).start();
        Thread.sleep(100);
        URL url = new URL("http:
        assertEquals("Hello, Android world #0!", request(url));
        assertEquals("Hello, Android world #1!", request(url));
        assertEquals("Hello, Android world #2!", request(url));
        try {
            request(url);
            fail("ConnectException expected.");
        } catch (Exception ex) {
        }
    }
    @Suppress
    public void testUserAgentHeader() throws Exception {
        DummyServer server = new DummyServer(1);
        new Thread(server).start();
        Thread.sleep(100);
        request(new URL("http:
        String userAgent = server.headers.get("User-Agent");
        assertTrue("Unexpected User-Agent: " + userAgent, userAgent.matches(
                "Dalvik/[\\d.]+ \\(Linux; U; Android \\w+(;.*)?( Build/\\w+)?\\)"));
    }
    @Suppress
    public void testHttpConnectionTimeout() throws Exception {
        int timeout = 5000;
        HttpURLConnection cn = null;
        long start = 0;
        try {
            start = System.currentTimeMillis();
            URL url = new URL("http:
            cn = (HttpURLConnection) url.openConnection();
            cn.setConnectTimeout(5000);
            cn.connect();
            fail("should have thrown an exception");
        } catch (IOException ioe) {
            long delay = System.currentTimeMillis() - start;
            if (Math.abs(timeout - delay) > 1000) {
                fail("Timeout was not accurate. it needed " + delay +
                        " instead of " + timeout + "miliseconds");
            }
        } finally {
            if (cn != null) {
                cn.disconnect();
            }
        }
    }
    @Suppress
    public void testMalformedUrl() throws Exception {
        URL url = new URL("http:
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        int status = conn.getResponseCode();
        android.util.Log.d("URLTest", "status: " + status);
    }
}
