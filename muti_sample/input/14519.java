class LineLengthsSource implements Runnable {
    DataOutputStream uo;
    BufferedWriter to;
    PrintWriter log;
    public LineLengthsSource(OutputStream us, BufferedWriter ts,
                             PrintWriter log)
        throws IOException
    {
        uo = new DataOutputStream(us);
        to = ts;
        this.log = log;
    }
    private void flush() throws IOException {
        uo.flush();
        Thread.currentThread().yield();
        to.flush();
        Thread.currentThread().yield();
    }
    private String termString(int t) {
        switch (t) {
        case 0: return "\n";
        case 1: return "\r";
        case 2: return "\r\n";
        default: return "";
        }
    }
    private String termName(int t) {
        switch (t) {
        case 0: return "\\n";
        case 1: return "\\r";
        case 2: return "\\r\\n";
        default: return "";
        }
    }
    private void go(int t) throws IOException {
        for (int ln = 0; ln < 128; ln++) {
            String ts = termString(t);
            StringBuffer s = new StringBuffer(ln + ts.length());
            for (int i = 0; i < ln; i++)
                s.append('x');
            log.println("[" + ln + "]" + termName(t));
            uo.writeUTF(s.toString());
            s.append(ts);
            to.write(s.toString());
            flush();
        }
    }
    public void run() {
        try {
            go(0);
            go(1);
            go(2);
            uo.close();
            Thread.currentThread().yield();
            to.close();
            Thread.currentThread().yield();
        }
        catch (IOException x) {
            return;             
        }
    }
}
