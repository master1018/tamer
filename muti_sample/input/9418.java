public class LineLengths {
    static String enc = "UTF8";
    static String inEnc = null;
    static String outEnc = null;
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
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(co, outEnc));
        BufferedReader r = new BufferedReader(new InputStreamReader(ci, inEnc));
        Thread t1 = new Thread(new LineLengthsSource(uo, w, log));
        Thread t2 = new Thread(new LineSink(ui, r, log));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}
