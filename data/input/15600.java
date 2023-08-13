class RandomLineSource implements Runnable {
    DataOutputStream uo;
    BufferedWriter to;
    LineGenerator lg;
    PrintWriter log;
    public RandomLineSource(OutputStream us, BufferedWriter ts, int limit,
                            PrintWriter log)
    {
        if (us != null)
            uo = new DataOutputStream(us);
        to = ts;
        IntGenerator ig = new IntGenerator();
        lg = new LineGenerator(ig, new StringGenerator(ig), limit);
        this.log = log;
    }
    public RandomLineSource(OutputStream us, BufferedWriter ts, int limit) {
        this(us, ts, limit, null);
    }
    public RandomLineSource(BufferedWriter ts, int limit) {
        this(null, ts, limit);
    }
    private void flush() throws IOException {
        if (uo != null) {
            uo.flush();
            Thread.currentThread().yield();
        }
        to.flush();
        for (int i = 0; i < 10; i++)
            Thread.currentThread().yield();
    }
    private int count = 0;
    public void run() {
        try {
            String s;
            while ((s = lg.next()) != null) {
                if (uo != null)
                    uo.writeUTF(s);
                to.write(s + lg.lineTerminator);
                flush();
                count++;
            }
            if (uo != null) {
                uo.close();
                Thread.currentThread().yield();
            }
            to.close();
            Thread.currentThread().yield();
        } catch (IOException x) {
            return;             
        }
    }
}
