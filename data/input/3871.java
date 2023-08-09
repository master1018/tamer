public class StreamingRetry implements Runnable {
    static final int ACCEPT_TIMEOUT = 20 * 1000; 
    ServerSocket ss;
    public static void main(String[] args) throws IOException {
        (new StreamingRetry()).instanceMain();
    }
    void instanceMain() throws IOException {
        test();
        if (failed > 0) throw new RuntimeException("Some tests failed");
    }
    void test() throws IOException {
        ss = new ServerSocket(0);
        ss.setSoTimeout(ACCEPT_TIMEOUT);
        int port = ss.getLocalPort();
        (new Thread(this)).start();
        try {
            URL url = new URL("http:
            HttpURLConnection uc = (HttpURLConnection) url.openConnection();
            uc.setDoOutput(true);
            uc.setChunkedStreamingMode(4096);
            OutputStream os = uc.getOutputStream();
            os.write("Hello there".getBytes());
            InputStream is = uc.getInputStream();
            is.close();
        } catch (IOException expected) {
        } finally {
            ss.close();
        }
    }
    public void run() {
        try {
            (ss.accept()).close();
            (ss.accept()).close();
            ss.close();
            fail("The server shouldn't accept a second connection");
         } catch (IOException e) {
        }
    }
    volatile int failed = 0;
    void fail() {failed++; Thread.dumpStack();}
    void fail(String msg) {System.err.println(msg); fail();}
}
