abstract class Cancellable implements Runnable {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private final long pollingAddress;
    private final Object lock = new Object();
    private boolean completed;
    private Throwable exception;
    protected Cancellable() {
        pollingAddress = unsafe.allocateMemory(4);
        unsafe.putIntVolatile(null, pollingAddress, 0);
    }
    protected long addressToPollForCancel() {
        return pollingAddress;
    }
    protected int cancelValue() {
        return Integer.MAX_VALUE;
    }
    final void cancel() {
        synchronized (lock) {
            if (!completed) {
                unsafe.putIntVolatile(null, pollingAddress, cancelValue());
            }
        }
    }
    private Throwable exception() {
        synchronized (lock) {
            return exception;
        }
    }
    @Override
    public final void run() {
        try {
            implRun();
        } catch (Throwable t) {
            synchronized (lock) {
                exception = t;
            }
        } finally {
            synchronized (lock) {
                completed = true;
                unsafe.freeMemory(pollingAddress);
            }
        }
    }
    abstract void implRun() throws Throwable;
    static void runInterruptibly(Cancellable task) throws ExecutionException {
        Thread t = new Thread(task);
        t.start();
        boolean cancelledByInterrupt = false;
        while (t.isAlive()) {
            try {
                t.join();
            } catch (InterruptedException e) {
                cancelledByInterrupt = true;
                task.cancel();
            }
        }
        if (cancelledByInterrupt)
            Thread.currentThread().interrupt();
        Throwable exc = task.exception();
        if (exc != null)
            throw new ExecutionException(exc);
    }
}
