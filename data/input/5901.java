public class Bind {
     void test(String[] args) {
        if (!Util.isSCTPSupported()) {
            out.println("SCTP protocol is not supported");
            out.println("Test cannot be run");
            return;
        }
        testBind();
        testBindUnbind(false);
    }
    void testBind() {
        SctpChannel channel = null;
        try {
            channel = SctpChannel.open();
            check(channel.getAllLocalAddresses().isEmpty(),
                    "getAllLocalAddresses returned non empty set for unbound channel");
            channel.bind(null);
            check(!channel.getAllLocalAddresses().isEmpty(),
                    "getAllLocalAddresses returned empty set for bound channel");
            debug("getAllLocalAddresses on channel bound to the wildcard:\n"
                    + channel.getAllLocalAddresses());
            try { channel.bind(null); }
            catch (AlreadyBoundException unused) { pass(); }
            catch (IOException ioe) { unexpected(ioe); }
            try {
                channel.close();  
                channel = SctpChannel.open();
                channel.bind(new UnsupportedSocketAddress());
                fail("UnsupportedSocketAddress expected");
            } catch (UnsupportedAddressTypeException unused) { pass();
            } catch (IOException ioe) { unexpected(ioe); }
            try {
                channel.close();  
                channel = SctpChannel.open();
                connectChannel(channel);
                channel.bind(null);
                fail("AlreadyConnectedException expected");
            } catch (AlreadyConnectedException unused) { pass();
            } catch (IOException ioe) { unexpected(ioe); }
            try {
                channel.close();  
                channel = SctpChannel.open();
                channel.close();
                channel.bind(null);
                fail("ClosedChannelException expected");
            } catch (ClosedChannelException unused) { pass();
            } catch (IOException ioe) { unexpected(ioe); }
            try {
                channel.getAllLocalAddresses();
                fail("should have thrown ClosedChannelException");
            } catch (ClosedChannelException cce) {
               pass();
            } catch (Exception ioe) {
                unexpected(ioe);
            }
        } catch (IOException ioe) {
            unexpected(ioe);
        } finally {
            try { channel.close(); }
            catch (IOException ioe) { unexpected(ioe); }
        }
    }
    void testBindUnbind(boolean connected) {
        SctpChannel channel = null;
        SctpChannel peerChannel = null;
        debug("testBindUnbind, connected: " + connected);
        try {
            channel = SctpChannel.open();
            List<InetAddress> addresses = Util.getAddresses(true, false);
            Iterator iterator = addresses.iterator();
            InetSocketAddress a = new InetSocketAddress((InetAddress)iterator.next(), 0);
            debug("channel.bind( " + a + ")");
            channel.bind(a);
            while (iterator.hasNext()) {
                InetAddress ia = (InetAddress)iterator.next();
                debug("channel.bindAddress(" + ia + ")");
                channel.bindAddress(ia);
            }
            if (debug) {Util.dumpAddresses(channel, out);}
            if (connected) {
                peerChannel = connectChannel(channel);
            }
            debug("bind/unbindAddresses on the system addresses");
            List<InetAddress> addrs = Util.getAddresses(true, false);
            for (InetAddress addr : addrs) {
                try {
                    debug("unbindAddress: " + addr);
                    check(boundAddress(channel, addr), "trying to remove address that is not bound");
                    channel.unbindAddress(addr);
                    if (debug) {Util.dumpAddresses(channel, out);}
                    check(!boundAddress(channel, addr), "address was not removed");
                    debug("bindAddress: " + addr);
                    channel.bindAddress(addr);
                    if (debug) {Util.dumpAddresses(channel, out);}
                    check(boundAddress(channel, addr), "address is not bound");
                } catch (IOException ioe) {
                    unexpected(ioe);
                }
            }
            InetAddress againAddress = addrs.get(0);
            try {
                debug("bind already bound address " + againAddress);
                channel.bindAddress(againAddress);
            } catch (AlreadyBoundException unused) {
                debug("Caught AlreadyBoundException - OK");
                pass();
            } catch (IOException ioe) {
                unexpected(ioe);
            }
            try {
                InetAddress nla = InetAddress.getByName("123.123.123.123");
                debug("bind non local address " + nla);
                channel.bindAddress(nla);
            } catch (IOException ioe) {
                debug("Informative only " + ioe);
            }
            try {
                debug("unbind address that is not bound " + againAddress);
                channel.unbindAddress(againAddress);
                channel.unbindAddress(againAddress);
            } catch (IllegalUnbindException unused) {
                debug("Caught IllegalUnbindException - OK");
                pass();
            } catch (IOException ioe) {
                unexpected(ioe);
            }
            try {
                InetAddress nla = InetAddress.getByName("123.123.123.123");
                debug("unbind address that is not bound " + nla);
                channel.unbindAddress(nla);
            } catch (IllegalUnbindException unused) {
                debug("Caught IllegalUnbindException - OK");
                pass();
            } catch (IOException ioe) {
                unexpected(ioe);
            }
            if (connected) {
                channel.shutdown();
                BindNotificationHandler handler = new BindNotificationHandler();
                ByteBuffer buffer = ByteBuffer.allocate(10);
                MessageInfo info;
                while((info = peerChannel.receive(buffer, null, handler)) != null) {
                    if (info != null) {
                        if (info.bytes() == -1) {
                            debug("peerChannel Reached EOF");
                            break;
                        }
                    }
                }
                while((info = channel.receive(buffer, null, handler)) != null) {
                    if (info != null) {
                        if (info.bytes() == -1) {
                            debug("channel Reached EOF");
                            break;
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try { if (channel != null) channel.close(); }
            catch (IOException ioe) { unexpected(ioe); }
        }
    }
    boolean boundAddress(SctpChannel channel, InetAddress addr)
        throws IOException {
        for (SocketAddress boundAddr : channel.getAllLocalAddresses()) {
            if (((InetSocketAddress) boundAddr).getAddress().equals(addr))
                return true;
        }
        return false;
    }
    SctpChannel connectChannel(SctpChannel channel)
        throws IOException {
        debug("connecting channel...");
        try {
            SctpServerChannel ssc = SctpServerChannel.open();
            ssc.bind(null);
            Set<SocketAddress> addrs = ssc.getAllLocalAddresses();
            Iterator<SocketAddress> iterator = addrs.iterator();
            SocketAddress addr = iterator.next();
            debug("using " + addr + "...");
            channel.connect(addr);
            SctpChannel peerChannel = ssc.accept();
            ssc.close();
            debug("connected");
            return peerChannel;
        } catch (IOException ioe) {
            debug("Cannot connect channel");
            unexpected(ioe);
            throw ioe;
        }
    }
    class BindNotificationHandler extends AbstractNotificationHandler<Object>
    {
        @Override
        public HandlerResult handleNotification(
                AssociationChangeNotification acn, Object unused)
        {
            debug("AssociationChangeNotification: " +  acn);
            return HandlerResult.CONTINUE;
        }
        @Override
        public HandlerResult handleNotification(
                PeerAddressChangeNotification pacn, Object unused)
        {
            debug("PeerAddressChangeNotification: " +  pacn);
            return HandlerResult.CONTINUE;
        }
        @Override
        public HandlerResult handleNotification(
                ShutdownNotification sn, Object unused)
        {
            debug("ShutdownNotification: " +  sn);
            return HandlerResult.CONTINUE;
        }
    }
    class UnsupportedSocketAddress extends SocketAddress { }
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
