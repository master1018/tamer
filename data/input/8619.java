public class Branch {
    final CountDownLatch clientFinishedLatch = new CountDownLatch(1);
    final CountDownLatch serverFinishedLatch = new CountDownLatch(1);
    void test(String[] args) {
        SocketAddress address = null;
        Server server = null;
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
                server = new Server();
                server.start();
                address = server.address();
                debug("Server started and listening on " + address);
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return;
            }
        }
        doTest(address);
    }
    void doTest(SocketAddress peerAddress) {
        SctpMultiChannel channel = null;
        ByteBuffer buffer = ByteBuffer.allocate(Util.LARGE_BUFFER);
        MessageInfo info = MessageInfo.createOutgoing(null, 0);
        try {
            channel = SctpMultiChannel.open();
            int streamNumber = 0;
            debug("sending to " + peerAddress + " on stream number: " + streamNumber);
            info = MessageInfo.createOutgoing(peerAddress, streamNumber);
            buffer.put(Util.SMALL_MESSAGE.getBytes("ISO-8859-1"));
            buffer.flip();
            int position = buffer.position();
            int remaining = buffer.remaining();
            debug("sending small message: " + buffer);
            int sent = channel.send(buffer, info);
            check(sent == remaining, "sent should be equal to remaining");
            check(buffer.position() == (position + sent),
                    "buffers position should have been incremented by sent");
            buffer.clear();
            BranchNotificationHandler handler = new BranchNotificationHandler();
            info = channel.receive(buffer, null, handler);
            check(handler.receivedCommUp(), "COMM_UP no received");
            Set<Association> associations = channel.associations();
            check(!associations.isEmpty(),"There should be some associations");
            Association bassoc = associations.iterator().next();
            SctpChannel bchannel = channel.branch(bassoc);
            check(!bchannel.getAllLocalAddresses().isEmpty(),
                                   "branched channel should be bound");
            check(!bchannel.getRemoteAddresses().isEmpty(),
                                   "branched channel should be connected");
            check(channel.associations().isEmpty(),
                  "there should be no associations since the only one was branched off");
            buffer.clear();
            info = bchannel.receive(buffer, null, null);
            buffer.flip();
            check(info != null, "info is null");
            check(info.streamNumber() == streamNumber,
                    "message not sent on the correct stream");
            check(info.bytes() == Util.SMALL_MESSAGE.getBytes("ISO-8859-1").
                  length, "bytes received not equal to message length");
            check(info.bytes() == buffer.remaining(), "bytes != remaining");
            check(Util.compare(buffer, Util.SMALL_MESSAGE),
              "received message not the same as sent message");
        } catch (IOException ioe) {
            unexpected(ioe);
        } finally {
            clientFinishedLatch.countDown();
            try { serverFinishedLatch.await(10L, TimeUnit.SECONDS); }
            catch (InterruptedException ie) { unexpected(ie); }
            if (channel != null) {
                try { channel.close(); }
                catch (IOException e) { unexpected (e);}
            }
        }
    }
    class Server implements Runnable
    {
        final InetSocketAddress serverAddr;
        private SctpMultiChannel serverChannel;
        public Server() throws IOException {
            serverChannel = SctpMultiChannel.open().bind(null);
            java.util.Set<SocketAddress> addrs = serverChannel.getAllLocalAddresses();
            if (addrs.isEmpty())
                debug("addrs should not be empty");
            serverAddr = (InetSocketAddress) addrs.iterator().next();
        }
        public void start() {
            (new Thread(this, "Server-"  + serverAddr.getPort())).start();
        }
        public InetSocketAddress address() {
            return serverAddr;
        }
        @Override
        public void run() {
            ByteBuffer buffer = ByteBuffer.allocateDirect(Util.LARGE_BUFFER);
            try {
                MessageInfo info;
                do {
                    info = serverChannel.receive(buffer, null, null);
                    if (info == null) {
                        fail("Server: unexpected null from receive");
                            return;
                    }
                } while (!info.isComplete());
                buffer.flip();
                check(info != null, "info is null");
                check(info.streamNumber() == 0,
                        "message not sent on the correct stream");
                check(info.bytes() == Util.SMALL_MESSAGE.getBytes("ISO-8859-1").
                      length, "bytes received not equal to message length");
                check(info.bytes() == buffer.remaining(), "bytes != remaining");
                check(Util.compare(buffer, Util.SMALL_MESSAGE),
                  "received message not the same as sent message");
                check(info != null, "info is null");
                Set<Association> assocs = serverChannel.associations();
                check(assocs.size() == 1, "there should be only one association");
                debug("Server: echoing first message");
                buffer.flip();
                int bytes = serverChannel.send(buffer, info);
                debug("Server: sent " + bytes + "bytes");
                clientFinishedLatch.await(10L, TimeUnit.SECONDS);
                serverFinishedLatch.countDown();
            } catch (IOException ioe) {
                unexpected(ioe);
            } catch (InterruptedException ie) {
                unexpected(ie);
            } finally {
                try { if (serverChannel != null) serverChannel.close(); }
                catch (IOException  unused) {}
            }
        }
    }
    class BranchNotificationHandler extends AbstractNotificationHandler<Object>
    {
        boolean receivedCommUp;  
        boolean receivedCommUp() {
            return receivedCommUp;
        }
        @Override
        public HandlerResult handleNotification(
                AssociationChangeNotification notification, Object attachment) {
            AssocChangeEvent event = notification.event();
            debug("AssociationChangeNotification");
            debug("  Association: " + notification.association());
            debug("  Event: " + event);
            if (event.equals(AssocChangeEvent.COMM_UP))
                receivedCommUp = true;
            return HandlerResult.RETURN;
        }
        @Override
        public HandlerResult handleNotification(
                ShutdownNotification notification, Object attachment) {
            debug("ShutdownNotification");
            debug("  Association: " + notification.association());
            fail("Shutdown should not be received");
            return HandlerResult.RETURN;
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
