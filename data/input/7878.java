public class WriteLengths {
    static PrintStream log = System.err;
    static int failures = 0;
    static ByteArrayOutputStream bos = new ByteArrayOutputStream(1 << 15);
    static void go(int len, String enc) throws Exception {
        bos.reset();
        OutputStreamWriter osw = new OutputStreamWriter(bos, enc);
        char[] cs = new char[len];
        osw.write(cs);
        osw.close();
        byte[] ba = bos.toByteArray();
        if (ba.length != len) {
            log.println("FAIL: Wrote " + len + ", got " + ba.length
                        + "; enc = " + enc);
            failures++;
        }
    }
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < (1 << 15); i += 1024)
            go(i, "us-ascii");
    }
}
