public class URLConnectionTest extends junit.framework.TestCase {
    private int mPort;
    private Support_TestWebServer mServer;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mPort = Support_PortManager.getNextPort();
        mServer = new Support_TestWebServer();
        mServer.initServer(mPort, true);
    }
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        mServer.close();
    }
    private String readFirstLine() throws Exception {
        URLConnection connection = new URL("http:
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String result = in.readLine();
        in.close();
        return result;
    }
    public void test_2939() throws Exception {
        mServer.setChunked(true);
        mServer.setMaxChunkSize(8);
        assertTrue(readFirstLine().equals("<html>"));
        assertTrue(readFirstLine().equals("<html>"));
        assertEquals(1, mServer.getNumAcceptedConnections());
    }
    public void testConnectionsArePooled() throws Exception {
        readFirstLine();
        readFirstLine();
        readFirstLine();
        assertEquals(1, mServer.getNumAcceptedConnections());
    }
    enum UploadKind { CHUNKED, FIXED_LENGTH };
    enum WriteKind { BYTE_BY_BYTE, SMALL_BUFFERS, LARGE_BUFFERS };
    public void test_chunkedUpload_byteByByte() throws Exception {
        doUpload(UploadKind.CHUNKED, WriteKind.BYTE_BY_BYTE);
    }
    public void test_chunkedUpload_smallBuffers() throws Exception {
        doUpload(UploadKind.CHUNKED, WriteKind.SMALL_BUFFERS);
    }
    public void test_chunkedUpload_largeBuffers() throws Exception {
        doUpload(UploadKind.CHUNKED, WriteKind.LARGE_BUFFERS);
    }
    public void test_fixedLengthUpload_byteByByte() throws Exception {
        doUpload(UploadKind.FIXED_LENGTH, WriteKind.BYTE_BY_BYTE);
    }
    public void test_fixedLengthUpload_smallBuffers() throws Exception {
        doUpload(UploadKind.FIXED_LENGTH, WriteKind.SMALL_BUFFERS);
    }
    public void test_fixedLengthUpload_largeBuffers() throws Exception {
        doUpload(UploadKind.FIXED_LENGTH, WriteKind.LARGE_BUFFERS);
    }
    private void doUpload(UploadKind uploadKind, WriteKind writeKind) throws Exception {
        int n = 512*1024;
        AtomicInteger total = new AtomicInteger(0);
        ServerSocket ss = startSinkServer(total);
        URL url = new URL("http:
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        if (uploadKind == UploadKind.CHUNKED) {
            conn.setChunkedStreamingMode(-1);
        } else {
            conn.setFixedLengthStreamingMode(n);
        }
        OutputStream out = conn.getOutputStream();
        if (writeKind == WriteKind.BYTE_BY_BYTE) {
            for (int i = 0; i < n; ++i) {
                out.write('x');
            }
        } else {
            byte[] buf = new byte[writeKind == WriteKind.SMALL_BUFFERS ? 256 : 64*1024];
            Arrays.fill(buf, (byte) 'x');
            for (int i = 0; i < n; i += buf.length) {
                out.write(buf, 0, Math.min(buf.length, n - i));
            }
        }
        out.close();
        assertTrue(conn.getResponseCode() > 0);
        assertEquals(uploadKind == UploadKind.CHUNKED ? -1 : n, total.get());
    }
    private ServerSocket startSinkServer(final AtomicInteger totalByteCount) throws Exception {
        final ServerSocket ss = new ServerSocket(0);
        ss.setReuseAddress(true);
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    Socket s = ss.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    int contentLength = -1;
                    String line;
                    int emptyLineCount = 0;
                    while ((line = in.readLine()) != null) {
                        if (contentLength == -1 && line.toLowerCase().startsWith("content-length: ")) {
                            contentLength = Integer.parseInt(line.substring(16));
                        }
                        if (line.length() == 0) {
                            ++emptyLineCount;
                            if (contentLength != -1 || emptyLineCount == 2) {
                                break;
                            }
                        }
                    }
                    long left = contentLength;
                    while (left > 0) {
                        left -= in.skip(left);
                    }
                    totalByteCount.set(contentLength);
                    OutputStream out = s.getOutputStream();
                    out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                    out.flush();
                    out.close();
                    try {
                        assertEquals(-1, in.read());
                    } catch (SocketException expected) {
                    }
                } catch (Exception ex) {
                    throw new RuntimeException("server died unexpectedly", ex);
                }
            }
        });
        t.start();
        return ss;
    }
}
