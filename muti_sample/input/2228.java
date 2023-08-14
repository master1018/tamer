public class GroupOfOne {
    public static void main(String[] args) throws Exception {
        final AsynchronousServerSocketChannel listener =
            AsynchronousServerSocketChannel.open()
                .bind(new InetSocketAddress(0));
        final List<AsynchronousSocketChannel> accepted = new ArrayList<AsynchronousSocketChannel>();
        listener.accept((Void)null, new CompletionHandler<AsynchronousSocketChannel,Void>() {
            public void completed(AsynchronousSocketChannel ch, Void att) {
                synchronized (accepted) {
                    accepted.add(ch);
                }
                listener.accept((Void)null, this);
            }
            public void failed(Throwable exc, Void att) {
            }
        });
        int port = ((InetSocketAddress)(listener.getLocalAddress())).getPort();
        SocketAddress sa = new InetSocketAddress(InetAddress.getLocalHost(), port);
        test(sa, true, false);
        test(sa, false, true);
        test(sa, true, true);
        listener.close();
        synchronized (accepted) {
            for (AsynchronousSocketChannel ch: accepted) {
                ch.close();
            }
        }
    }
    static void test(SocketAddress sa,
                     final boolean closeChannel,
                     final boolean shutdownGroup)
        throws Exception
    {
        final AsynchronousChannelGroup group = AsynchronousChannelGroup
            .withFixedThreadPool(1, new ThreadFactory() {
                @Override
                public Thread newThread(final Runnable r) {
                    return new Thread(r);
                }});
        final AsynchronousSocketChannel ch = AsynchronousSocketChannel.open(group);
        final CountDownLatch latch = new CountDownLatch(2);
        ch.connect(sa, (Void)null, new CompletionHandler<Void,Void>() {
            public void completed(Void result, Void att)  {
                System.out.println("Connected");
                ByteBuffer buf = ByteBuffer.allocate(100);
                ch.read(buf, (Void)null, new CompletionHandler<Integer,Void>() {
                    public void completed(Integer bytesRead, Void att)  {
                        throw new RuntimeException();
                    }
                    public void failed(Throwable exc, Void att) {
                        if (!(exc instanceof AsynchronousCloseException))
                            throw new RuntimeException(exc);
                        System.out.println("Read failed (expected)");
                        latch.countDown();
                    }
                });
                try {
                    if (closeChannel) {
                        System.out.print("Close channel ...");
                        ch.close();
                        System.out.println(" done.");
                    }
                    if (shutdownGroup) {
                        System.out.print("Shutdown group ...");
                        group.shutdownNow();
                        System.out.println(" done.");
                    }
                    latch.countDown();
                } catch (IOException e) {
                    throw new RuntimeException();
                }
            }
            public void failed(Throwable exc, Void att) {
                throw new RuntimeException(exc);
            }
        });
        latch.await();
        group.shutdown();
        boolean terminated = group.awaitTermination(5, TimeUnit.SECONDS);
        if (!terminated)
            throw new RuntimeException("Group did not terminate");
        System.out.println("TEST OKAY");
    }
}
