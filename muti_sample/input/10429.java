public class Accept {
    static CountDownLatch acceptLatch = new CountDownLatch(1);
    static CountDownLatch closeByIntLatch = new CountDownLatch(1);
    static CountDownLatch asyncCloseLatch = new CountDownLatch(1);
    AcceptServer server = null;
    void test(String[] args) {
        SocketAddress address = null;
        if (!Util.isSCTPSupported()) {
            out.println("SCTP protocol is not supported");
            out.println("Test cannot be run");
            return;
        }
        if (args.length == 2) {
            try {
                int port = Integer.valueOf(args[1]);
                address = new InetSocketAddress(args[0], port);
            } catch (NumberFormatException nfe) {
                err.println(nfe);
            }
        } else {
            try {
                server = new AcceptServer();
                server.start();
                address = server.address();
                debug("Server started and listening on " + address);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return;
            }
        }
        doClient(address);
    }
    void doClient(SocketAddress peerAddress) {
        SctpChannel channel = null;
        try {
            channel = SctpChannel.open(peerAddress, 0, 0);
            acceptLatch.await();
            closeByIntLatch.await();
            sleep(500);
            server.thread().interrupt();
            asyncCloseLatch.await();
            sleep(500);
            server.channel().close();
            join(server.thread(), 10000);
        } catch (IOException ioe) {
            unexpected(ioe);
        } catch (InterruptedException ie) {
            unexpected(ie);
        } finally {
            try { if (channel != null) channel.close(); }
            catch (IOException e) { unexpected(e);}
        }
    }
    class AcceptServer implements Runnable
    {
        final InetSocketAddress serverAddr;
        private SctpServerChannel ssc;
        private Thread serverThread;
        public AcceptServer() throws IOException {
            ssc = SctpServerChannel.open();
            debug("TEST 1: NotYetBoundException");
            try {
                ssc.accept();
                fail();
            } catch (NotYetBoundException nybe) {
                debug("  caught NotYetBoundException");
                pass();
            } catch (IOException ioe) {
                unexpected(ioe);
            }
            ssc.bind(null);
            java.util.Set<SocketAddress> addrs = ssc.getAllLocalAddresses();
            if (addrs.isEmpty())
                debug("addrs should not be empty");
            serverAddr = (InetSocketAddress) addrs.iterator().next();
            ssc.configureBlocking(false);
            debug("TEST 2: non-blocking mode null");
            try {
                SctpChannel sc = ssc.accept();
                check(sc == null, "non-blocking mode should return null");
            } catch (IOException ioe) {
                unexpected(ioe);
            } finally {
                ssc.configureBlocking(true);
            }
        }
        void start() {
            serverThread = new Thread(this, "AcceptServer-"  +
                                              serverAddr.getPort());
            serverThread.start();
        }
        InetSocketAddress address() {
            return serverAddr;
        }
        SctpServerChannel channel() {
            return ssc;
        }
        Thread thread() {
            return serverThread;
        }
        @Override
        public void run() {
            SctpChannel sc = null;
            try {
                debug("TEST 3: accepted channel");
                sc = ssc.accept();
                checkAcceptedChannel(sc);
                acceptLatch.countDown();
                debug("TEST 4: ClosedByInterruptException");
                try {
                    closeByIntLatch.countDown();
                    ssc.accept();
                    fail();
                } catch (ClosedByInterruptException unused) {
                    debug("  caught ClosedByInterruptException");
                    pass();
                }
                debug("TEST 5: AsynchronousCloseException");
                Thread.currentThread().interrupted();
                ssc = SctpServerChannel.open().bind(null);
                try {
                    asyncCloseLatch.countDown();
                    ssc.accept();
                    fail();
                } catch (AsynchronousCloseException unused) {
                    debug("  caught AsynchronousCloseException");
                    pass();
                }
                debug("TEST 6: ClosedChannelException");
                try {
                    ssc.accept();
                    fail();
                } catch (ClosedChannelException unused) {
                    debug("  caught ClosedChannelException");
                    pass();
                }
                ssc = null;
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try { if (ssc != null) ssc.close(); }
                catch (IOException  ioe) { unexpected(ioe); }
                try { if (sc != null) sc.close(); }
                catch (IOException  ioe) { unexpected(ioe); }
            }
        }
    }
    void checkAcceptedChannel(SctpChannel sc) {
        try {
            debug("Checking accepted SctpChannel");
            check(sc.association() != null,
                  "accepted channel should have an association");
            check(!(sc.getRemoteAddresses().isEmpty()),
                  "accepted channel should be connected");
            check(!(sc.isConnectionPending()),
                  "accepted channel should not have a connection pending");
            check(sc.isBlocking(),
                  "accepted channel should be blocking");
            try { sc.connect(new TestSocketAddress()); fail(); }
            catch (AlreadyConnectedException unused) { pass(); }
            try { sc.bind(new TestSocketAddress()); fail(); }
            catch (AlreadyConnectedException unused) { pass(); }
        } catch (IOException unused) { fail(); }
    }
    static class TestSocketAddress extends SocketAddress {}
    boolean debug = true;
    volatile int passed = 0, failed = 0;
    void pass() {passed++;}
    void fail() {failed++; Thread.dumpStack();}
    void fail(String msg) {err.println(msg); fail();}
    void unexpected(Throwable t) {failed++; t.printStackTrace();}
    void check(boolean cond) {if (cond) pass(); else fail();}
    void check(boolean cond, String failMessage) {if (cond) pass(); else fail(failMessage);}
    void debug(String message) {if(debug) { out.println(message); }  }
    void sleep(long millis) { try { Thread.currentThread().sleep(millis); }
                          catch(InterruptedException ie) { unexpected(ie); }}
    void join(Thread thread, long millis) { try { thread.join(millis); }
                          catch(InterruptedException ie) { unexpected(ie); }}
    public static void main(String[] args) throws Throwable {
        Class<?> k = new Object(){}.getClass().getEnclosingClass();
        try {k.getMethod("instanceMain",String[].class)
                .invoke( k.newInstance(), (Object) args);}
        catch (Throwable e) {throw e.getCause();}}
    public void instanceMain(String[] args) throws Throwable {
        try {test(args);} catch (Throwable t) {unexpected(t);}
        out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
