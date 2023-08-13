public class AtomicAttachTest {
    public static void main(String[] args) throws Exception {
        Selector selector = Selector.open();
        Pipe pipe = Pipe.open();
        SelectableChannel channel = pipe.sink().configureBlocking(false);
        final SelectionKey key = channel.register(selector, 0);
        key.attach(new AtomicBoolean());
        final AtomicInteger errorCount = new AtomicInteger();
        Thread t = new Thread() {
            public void run() {
                AtomicBoolean att = new AtomicBoolean();
                for (int i=0; i<(10*1000*1000); i++) {
                    att = (AtomicBoolean)key.attach(att);
                    if (!att.compareAndSet(false, true) ||
                        !att.compareAndSet(true, false))
                    {
                        errorCount.incrementAndGet();
                    }
                }
            }
            {
                start();
                run();
            }
        };
        t.join();
        pipe.sink().close();
        pipe.source().close();
        selector.close();
        int count = errorCount.get();
        if (count > 0) {
            throw new RuntimeException("Error count:" + count);
        }
    }
}
