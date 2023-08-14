public class Hangup {
    static PrintStream log = System.err;
    static int failures = 0;
    private static class Failure
        extends RuntimeException
    {
        Failure(String s) {
            super(s);
        }
    }
    static void doSelect(Selector sel, SelectionKey sk, int count)
        throws IOException
    {
        int n = sel.select();
        if (n != 1)
            throw new Failure("Select returned zero");
        Set sks = sel.selectedKeys();
        if (sks.size() != 1)
            throw new Failure("Wrong size for selected-key set: "
                              + sks.size());
        if (!sks.remove(sk))
            throw new Failure("Key not in selected-key set");
        log.println("S: Socket selected #" + count);
    }
    static void dally() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException x) { }
    }
    static void test(boolean writeFromClient, boolean readAfterClose)
        throws IOException
    {
        ServerSocketChannel ssc = null;
        SocketChannel cl = null;        
        SocketChannel sv = null;        
        Selector sel = null;
        log.println();
        log.println("Test: writeFromClient = " + writeFromClient
                    + ", readAfterClose = " + readAfterClose);
        try {
            int ns = 0;                 
            ssc = ServerSocketChannel.open();
            SocketAddress sa = TestUtil.bindToRandomPort(ssc);
            log.println("S: Listening on port "
                        + ssc.socket().getLocalPort());
            cl = SocketChannel.open(sa);
            log.println("C: Connected via port "
                        + cl.socket().getLocalPort());
            sv = ssc.accept();
            log.println("S: Client connection accepted");
            sel = Selector.open();
            sv.configureBlocking(false);
            SelectionKey sk = sv.register(sel, SelectionKey.OP_READ);
            ByteBuffer stuff = ByteBuffer.allocate(10);
            int n;
            if (writeFromClient) {
                stuff.clear();
                if (cl.write(stuff) != stuff.capacity())
                    throw new Failure("Incorrect number of bytes written");
                log.println("C: Wrote stuff");
                dally();
                doSelect(sel, sk, ++ns);
                stuff.clear();
                if (sv.read(stuff) != stuff.capacity())
                    throw new Failure("Wrong number of bytes read");
                log.println("S: Read stuff");
            }
            cl.close();
            log.println("C: Socket closed");
            dally();
            doSelect(sel, sk, ++ns);
            if (readAfterClose) {
                stuff.clear();
                if (sv.read(stuff) != -1)
                    throw new Failure("Wrong number of bytes read");
                log.println("S: Read EOF");
            }
            doSelect(sel, sk, ++ns);
            doSelect(sel, sk, ++ns);
        } finally {
            if (ssc != null)
                ssc.close();
            if (cl != null)
                cl.close();
            if (sv != null)
                sv.close();
            if (sel != null)
                sel.close();
        }
    }
    public static void main(String[] args) throws IOException {
        for (boolean writeFromClient = false;; writeFromClient = true) {
            for (boolean readAfterClose = false;; readAfterClose = true) {
                try {
                    test(writeFromClient, readAfterClose);
                } catch (Failure x) {
                    x.printStackTrace(log);
                    failures++;
                }
                if (readAfterClose)
                    break;
            }
            if (writeFromClient)
                break;
        }
        if (failures > 0) {
            log.println();
            throw new RuntimeException("Some tests failed");
        }
    }
}
