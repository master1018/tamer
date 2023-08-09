public class LineNumbers {
    static String enc = "UTF8";
    static String inEnc;
    static String outEnc;
    static int limit = 500;
    public static void main(String[] args) throws Exception {
        PrintWriter log
            = new PrintWriter(new OutputStreamWriter(System.err), true);
        if (inEnc == null)
            inEnc = enc;
        if (outEnc == null)
            outEnc = enc;
        PipedOutputStream co = new PipedOutputStream();
        PipedInputStream ci = new PipedInputStream(co);
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(co, outEnc));
        LineNumberReader r = new LineNumberReader(new InputStreamReader(ci, inEnc));
        Thread t1 = new Thread(new RandomLineSource(w, limit));
        Thread t2 = new Thread(new LineNumberSink(r, limit, log));
        t1.start();
        t2.start();
        t1.join();
        t2.join();
    }
}
class LineNumberSink implements Runnable {
    LineNumberReader r;
    int limit;
    PrintWriter log;
    LineNumberSink(LineNumberReader r, int limit, PrintWriter log) {
        this.r = r;
        this.limit = limit;
        this.log = log;
    }
    public void run() {
        String s;
        int n = 0;
        try {
            while ((s = r.readLine()) != null) {
                n++;
                int ln = r.getLineNumber();
                log.println("[" + ln + "] " + s.length());
                log.println(s);
                if (log.checkError())
                    log.println("Conversion errors"); 
                if (n != ln)
                    throw new RuntimeException("Line number mismatch: Expected " + n + ", got " + ln);
            }
            if (n != limit)
                throw new RuntimeException("Incorrect line count");
        }
        catch (IOException x) {
            throw new RuntimeException(x.toString());
        }
        log.println(n + " lines read");
    }
}
