public class Receive {
    final CountDownLatch clientFinishedLatch = new CountDownLatch(1);
    final CountDownLatch serverFinishedLatch = new CountDownLatch(1);
    static final int PPID = 5;
    void test(String[] args) {
        SocketAddress address = null;
        Server server;
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
        SctpChannel channel = null;
        ByteBuffer buffer = ByteBuffer.allocate(Util.LARGE_BUFFER);
        MessageInfo info;
        try {
            channel = SctpChannel.open();
            ReceiveNotificationHandler handler =
                new ReceiveNotificationHandler(channel);
            try {
                channel.receive(buffer, null, handler);
                fail("should have thrown NotYetConnectedException");
            } catch (NotYetConnectedException unused) {
                pass();
            }  catch (IOException ioe) {
                unexpected(ioe);
            }
            channel.connect(peerAddress);
            do {
                debug("Test 2: invoking receive");
                info = channel.receive(buffer, null, handler);
                if (info == null) {
                    fail("unexpected null from receive");
                    return;
                }
            } while (!info.isComplete());
            buffer.flip();
            check(handler.receivedCommUp(), "SCTP_COMM_UP not received");
            check(info != null, "info is null");
            check(info.address() != null, "address is null");
            check(info.association() != null, "association is null");
            check(info.isComplete(), "message is not complete");
            check(info.isUnordered() != true,
                  "message should not be unordered");
            check(info.streamNumber() >= 0, "invalid stream number");
            check(info.payloadProtocolID() == PPID, "PPID incorrect");
            check(info.bytes() == Util.SMALL_MESSAGE.getBytes("ISO-8859-1").
                  length, "bytes received not equal to message length");
            check(info.bytes() == buffer.remaining(), "bytes != remaining");
            check(Util.compare(buffer, Util.SMALL_MESSAGE),
                  "received message not the same as sent message");
            buffer.clear();
            do {
                debug("Test 3: invoking receive");
                info = channel.receive(buffer, null, handler);
                if (info == null) {
                    fail("unexpected null from receive");
                    return;
                }
            } while (!info.isComplete());
            buffer.flip();
            check(info != null, "info is null");
            check(info.address() != null, "address is null");
            check(info.association() != null, "association is null");
            check(info.isComplete(), "message is not complete");
            check(info.isUnordered() != true,
                  "message should not be unordered");
            check(info.streamNumber() >= 0, "invalid stream number");
            check(info.bytes() == Util.LARGE_MESSAGE.getBytes("ISO-8859-1").
                  length, "bytes received not equal to message length");
            check(info.bytes() == buffer.remaining(), "bytes != remaining");
            check(Util.compare(buffer, Util.LARGE_MESSAGE),
                  "received message not the same as sent message");
            buffer.clear();
            buffer.clear();  
            info = channel.receive(buffer,null, handler);
            check(info != null, "info is null");
            check(info.bytes() == -1, "should have received EOF");
            check(buffer.position() == 0, "buffer position should be unchanged");
            channel.close();
            try {
                channel.receive(buffer, null, null);
                fail("should have thrown ClosedChannelException");
            } catch (ClosedChannelException cce) {
               pass();
            } catch (IOException ioe) {
                unexpected(ioe);
            }
            handler = null;
            ReceiveNotificationHandler handler2 =
                new ReceiveNotificationHandler(null); 
            channel = SctpChannel.open(peerAddress, 0, 0);
            info = channel.receive(buffer, null, handler2);
            check(info == null, "channel should return null");
            check(handler2.receivedCommUp(), "SCTP_COMM_UP not received");
            check(buffer.position() == 0, "buffer position should be unchanged");
            channel.configureBlocking(false);
            info = channel.receive(buffer, null, null);
            check(info == null, "non-blocking channel should return null");
            check(buffer.position() == 0, "buffer position should be unchanged");
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
        private SctpServerChannel ssc;
        public Server() throws IOException {
            ssc = SctpServerChannel.open().bind(null);
            java.util.Set<SocketAddress> addrs = ssc.getAllLocalAddresses();
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
            try {
                SctpChannel sc = ssc.accept();
                MessageInfo info = MessageInfo.createOutgoing(null, 0)
                        .payloadProtocolID(PPID);
                ByteBuffer buf = ByteBuffer.allocateDirect(Util.SMALL_BUFFER);
                buf.put(Util.SMALL_MESSAGE.getBytes("ISO-8859-1"));
                buf.flip();
                debug("sending small message: " + buf);
                sc.send(buf, info);
                buf = ByteBuffer.allocateDirect(Util.LARGE_BUFFER);
                buf.put(Util.LARGE_MESSAGE.getBytes("ISO-8859-1"));
                buf.flip();
                debug("sending large message: " + buf);
                sc.send(buf, info);
                sc.shutdown();
                debug("shutdown");
                ReceiveNotificationHandler handler =
                    new ReceiveNotificationHandler(sc);
                sc.receive(buf, null, handler);
                sc.close();
                sc = ssc.accept();
                ssc.close();
                clientFinishedLatch.await(10L, TimeUnit.SECONDS);
                serverFinishedLatch.countDown();
                sc.close();
            } catch (IOException ioe) {
                unexpected(ioe);
            } catch (InterruptedException ie) {
                unexpected(ie);
            }
        }
    }
    class ReceiveNotificationHandler extends AbstractNotificationHandler<Object>
    {
        SctpChannel channel;
        boolean receivedCommUp;  
        public ReceiveNotificationHandler(SctpChannel channel) {
            this.channel = channel;
        }
        public boolean receivedCommUp() {
            return receivedCommUp;
        }
        @Override
        public HandlerResult handleNotification(
                Notification notification, Object attachment) {
            fail("Unknown notification type");
            return HandlerResult.CONTINUE;
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
            if (channel == null)
                return HandlerResult.RETURN;
            ByteBuffer buffer = ByteBuffer.allocate(10);
            try {
                channel.receive(buffer, null, this);
                fail("IllegalReceiveException expected");
            } catch (IllegalReceiveException unused) {
                pass();
            } catch (IOException ioe) {
                unexpected(ioe);
            }
            return HandlerResult.CONTINUE;
        }
        @Override
        public HandlerResult handleNotification(
                ShutdownNotification notification, Object attachment) {
            debug("ShutdownNotification");
            debug("  Association: " + notification.association());
            return HandlerResult.CONTINUE;
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
    void debug(String message) {if(debug) {
          System.out.println(Thread.currentThread() + " " + message); } }
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
