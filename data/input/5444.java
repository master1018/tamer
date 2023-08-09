public class StreamTimeout {
    private static PrintStream log = System.err;
    private static String charset = "US-ASCII";
    private static Object lock = new Object();
    private static synchronized void waitABit(int millisec) {
        synchronized(lock) {
            try {
                lock.wait(millisec);
            } catch (InterruptedException e) {
            }
        }
    }
    private static class Client extends Thread {
        public void run() {
            try {
                Socket so = new Socket("127.0.0.1", 22222);
                Writer wr = new OutputStreamWriter(so.getOutputStream(),
                                                   charset);
                wr.write("ab");
                wr.flush();
            } catch (IOException x) {
                log.print("Unexpected exception in writer: ");
                x.printStackTrace();
                System.exit(1);
            }
        }
    }
    private static void gobble(InputStream is, Reader rd,
                               int ec, boolean force)
        throws Exception
    {
        int a = is.available();
        boolean r = rd.ready();
        log.print("" + a + " bytes available, "
                  + "reader " + (r ? "" : "not ") + "ready");
        if (!r && !force) {
            log.println();
            return;
        }
        int c;
        try {
            c = rd.read();
        } catch (InterruptedIOException x) {
            log.println();
            throw x;
        }
        log.println(", read() ==> "
                    + (c >= 0 ? ("'" + (char)c + "'" ): "EOF"));
        if (c != ec)
            throw new Exception("Incorrect value read: Expected "
                                + ec + ", read " + (char)c);
    }
    public static void main(String[] args) throws Exception {
        if (args.length > 0)
            charset = args[0];
        ServerSocket ss = new ServerSocket(22222);
        Thread cl = new Client();
        cl.start();
        Socket s = ss.accept();
        s.setSoTimeout(150);
        InputStream is = s.getInputStream();
        Reader rd = new InputStreamReader(is, charset);
        while (is.available() <= 0)
            Thread.yield();
        gobble(is, rd, 'a', false);
        gobble(is, rd, 'b', false);
        gobble(is, rd, -1, false);
        boolean caught = false;
        try {
            gobble(is, rd, -1, true);
        } catch (InterruptedIOException e) {
            log.println("Read timed out, as expected");
            caught = true;
        }
        if (!caught) {
            log.println("Read did not time out, test inapplicable");
            return;
        }
        caught = false;
        try {
            gobble(is, rd, -1, true);
        } catch (InterruptedIOException x) {
            log.println("Second read timed out, as expected");
            caught = true;
        }
        if (!caught)
            throw new Exception("Second read completed");
    }
}
