public class SmallReads {
    static class OneByteInputStream extends FilterInputStream {
        OneByteInputStream(InputStream in) {
            super(in);
        }
        public int read(byte[] b, int off, int len) throws IOException {
            return in.read(b, off, 1);
        }
    }
    static String enc = "UTF8";
    static String inEnc;
    static String outEnc;
    static int count = 100;
    public static void main(String[] args) throws Exception {
        PrintWriter log
            = new PrintWriter(new OutputStreamWriter(System.err), true);
        if (inEnc == null)
            inEnc = enc;
        if (outEnc == null)
            outEnc = enc;
        PipedOutputStream uo = new PipedOutputStream();
        PipedInputStream ui = new PipedInputStream(uo);
        PipedOutputStream co = new PipedOutputStream();
        PipedInputStream ci = new PipedInputStream(co);
        InputStream ci2 = new OneByteInputStream(ci);
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(co, outEnc));
        BufferedReader r = new BufferedReader(new InputStreamReader(ci2, inEnc));
        Thread t1 = new Thread(new RandomLineSource(uo, w, count, log));
        Thread t2 = new Thread(new LineSink(ui, r, count, log));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}
