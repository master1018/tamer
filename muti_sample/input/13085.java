public class CustomThreadPool {
    public static void main(String[] args) throws Exception {
        File blah = File.createTempFile("blah", null);
        blah.deleteOnExit();
        AsynchronousFileChannel ch =
            AsynchronousFileChannel.open(blah.toPath(), READ, WRITE);
        ByteBuffer src = ByteBuffer.wrap("Scooby Snacks".getBytes());
        final AtomicReference<Thread> invoker = new AtomicReference<Thread>();
        ch.write(src, 0, invoker,
            new CompletionHandler<Integer,AtomicReference<Thread>>() {
                public void completed(Integer result, AtomicReference<Thread> invoker) {
                    invoker.set(Thread.currentThread());
                }
                public void failed(Throwable exc, AtomicReference<Thread> invoker) {
                }
            });
        Thread t;
        while ((t = invoker.get()) == null) {
            Thread.sleep(100);
        }
        ch.close();
        if (!MyThreadFactory.created(t))
            throw new RuntimeException("Handler invoked by unknown thread");
    }
}
