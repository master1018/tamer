public class StreamPipe extends Thread {
    private InputStream in;
    private OutputStream out;
    private String preamble;
    private static Object lock = new Object();
    private static int count = 0;
    public StreamPipe(InputStream in, OutputStream out, String name) {
        super(name);
        this.in  = in;
        this.out = out;
        this.preamble = "# ";
    }
    public void run() {
        BufferedReader r = new BufferedReader(new InputStreamReader(in), 1);
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(out));
        byte[] buf = new byte[256];
        boolean bol = true;     
        int count;
        try {
            String line;
            while ((line = r.readLine()) != null) {
                w.write(preamble);
                w.write(line);
                w.newLine();
                w.flush();
            }
        } catch (IOException e) {
            System.err.println("*** IOException in StreamPipe.run:");
            e.printStackTrace();
        }
    }
    public static void plugTogether(InputStream in, OutputStream out) {
        String name = null;
        synchronized (lock) {
            name = "TestLibrary: StreamPipe-" + (count ++ );
        }
        Thread pipe = new StreamPipe(in, out, name);
        pipe.setDaemon(true);
        pipe.start();
    }
}
