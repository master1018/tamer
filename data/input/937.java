public class B5052093 implements HttpCallback {
    private static HttpServer server;
    private static long testSize = ((long) (Integer.MAX_VALUE)) + 2;
    public static class LargeFile extends File {
        public LargeFile() {
            super("/dev/zero");
        }
        public long length() {
            return testSize;
        }
    }
    public static class LargeFileURLConnection extends FileURLConnection {
        public LargeFileURLConnection(LargeFile f) throws IOException {
                super(new URL("file:
        }
    }
    public void request(HttpTransaction req) {
        try {
            req.setResponseHeader("content-length", Long.toString(testSize));
            req.sendResponse(200, "OK");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) throws Exception {
        server = new HttpServer(new B5052093(), 1, 10, 0);
        try {
            URL url = new URL("http:
            URLConnection conn = url.openConnection();
            int i = conn.getContentLength();
            long l = conn.getContentLengthLong();
            if (i != -1 || l != testSize) {
                System.out.println("conn.getContentLength = " + i);
                System.out.println("conn.getContentLengthLong = " + l);
                System.out.println("testSize = " + testSize);
                throw new RuntimeException("Wrong content-length from http");
            }
            URLConnection fu = new LargeFileURLConnection(new LargeFile());
            i = fu.getContentLength();
            l = fu.getContentLengthLong();
            if (i != -1 || l != testSize) {
                System.out.println("fu.getContentLength = " + i);
                System.out.println("fu.getContentLengthLong = " + l);
                System.out.println("testSize = " + testSize);
                throw new RuntimeException("Wrong content-length from file");
            }
        } finally {
            server.terminate();
        }
    }
}
