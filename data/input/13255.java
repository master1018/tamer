public class Connect {
    void test(String[] args) {
        if (!Util.isSCTPSupported()) {
            out.println("SCTP protocol is not supported");
            out.println("Test cannot be run");
            return;
        }
        doTest();
    }
    void doTest() {
        SctpChannel channel = null;
        SctpServerChannel ssc = null;
        try {
            ssc = SctpServerChannel.open().bind(null);
            Set<SocketAddress> addrs = ssc.getAllLocalAddresses();
            if (addrs.isEmpty())
                debug("addrs should not be empty");
            final SocketAddress peerAddress = (InetSocketAddress) addrs.iterator().next();
            channel = SctpChannel.open();
            check(channel.getRemoteAddresses().isEmpty(),
                    "non empty set for unconnected channel");
            check(channel.association() == null,
                    "non-null association for unconnected channel");
            check(!channel.isConnectionPending(),
                    "should not have a connection pending");
            channel.configureBlocking(false);
            if (channel.connect(peerAddress) != true) {
                debug("non-blocking connect did not immediately succeed");
                check(channel.isConnectionPending(),
                        "should return true for isConnectionPending");
                try {
                    channel.connect(peerAddress);
                    fail("should have thrown ConnectionPendingException");
                } catch (ConnectionPendingException cpe) {
                    pass();
                } catch (IOException ioe) {
                    unexpected(ioe);
                }
                channel.configureBlocking(true);
                check(channel.finishConnect(),
                        "finishConnect should have returned true");
            }
            ssc.accept();
            ssc.close();
            check(!channel.getRemoteAddresses().isEmpty(),
                    "empty set for connected channel");
            check(channel.association() != null,
                    "null association for connected channel");
            check(!channel.isConnectionPending(),
                    "pending connection for connected channel");
            try {
                channel.connect(peerAddress);
                fail("should have thrown AlreadyConnectedException");
            } catch (AlreadyConnectedException unused) {
                pass();
            }  catch (IOException ioe) {
                unexpected(ioe);
            }
            try {
                channel.connect(peerAddress, 5, 5);
                fail("should have thrown AlreadyConnectedException");
            } catch (AlreadyConnectedException unused) {
                pass();
            }  catch (IOException ioe) {
                unexpected(ioe);
            }
            channel.close();
            channel = SctpChannel.open();
            InetSocketAddress unresolved =
                    InetSocketAddress.createUnresolved("xxyyzzabc", 4567);
            try {
                channel.connect(unresolved);
                fail("should have thrown UnresolvedAddressException");
            } catch (UnresolvedAddressException unused) {
                pass();
            }  catch (IOException ioe) {
                unexpected(ioe);
            }
            SocketAddress unsupported = new UnsupportedSocketAddress();
            try {
                channel.connect(unsupported);
                fail("should have thrown UnsupportedAddressTypeException");
            } catch (UnsupportedAddressTypeException unused) {
                pass();
            }  catch (IOException ioe) {
                unexpected(ioe);
            }
            channel.close();
            final SctpChannel closedChannel = channel;
            testCCE(new Callable<Void>() {
                public Void call() throws IOException {
                    closedChannel.connect(peerAddress); return null; } });
            testCCE(new Callable<Void>() {
                public Void call() throws IOException {
                    closedChannel.getRemoteAddresses(); return null; } });
            testCCE(new Callable<Void>() {
                public Void call() throws IOException {
                    closedChannel.association(); return null; } });
            check(!channel.isConnectionPending(),
                    "pending connection for closed channel");
            channel = SctpChannel.open();
            try {
                channel.finishConnect();
                fail("should have thrown NoConnectionPendingException");
            } catch (NoConnectionPendingException unused) {
                pass();
            }  catch (IOException ioe) {
                unexpected(ioe);
            }
            channel.close();
            final SctpChannel cceChannel = channel;
            testCCE(new Callable<Void>() {
                public Void call() throws IOException {
                    cceChannel.finishConnect(); return null; } });
            SocketAddress addr = new InetSocketAddress("localhost", 3456);
            channel = SctpChannel.open();
            try {
                channel.connect(addr);
                fail("should have thrown ConnectException: Connection refused");
            } catch (IOException ioe) {
                pass();
            }
        } catch (IOException ioe) {
            unexpected(ioe);
        } finally {
            try { if (channel != null) channel.close(); }
            catch (IOException unused) {}
            try { if (ssc != null) ssc.close(); }
            catch (IOException unused) {}
        }
    }
    class UnsupportedSocketAddress extends SocketAddress { }
    void testCCE(Callable callable) {
        try {
            callable.call();
            fail("should have thrown ClosedChannelException");
        } catch (ClosedChannelException cce) {
           pass();
        } catch (Exception ioe) {
            unexpected(ioe);
        }
    }
    boolean debug = true;
    volatile int passed = 0, failed = 0;
    void pass() {passed++;}
    void fail() {failed++; Thread.dumpStack();}
    void fail(String msg) {System.err.println(msg); fail();}
    void unexpected(Throwable t) {failed++; t.printStackTrace();}
    void check(boolean cond) {if (cond) pass(); else fail();}
    void check(boolean cond, String failMessage) {if (cond) pass(); else fail(failMessage);}
    void debug(String message) {if(debug) { System.out.println(message); }  }
    public static void main(String[] args) throws Throwable {
        Class<?> k = new Object(){}.getClass().getEnclosingClass();
        try {k.getMethod("instanceMain",String[].class)
                .invoke( k.newInstance(), (Object) args);}
        catch (Throwable e) {throw e.getCause();}}
    public void instanceMain(String[] args) throws Throwable {
        try {test(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
