public abstract class AbstractInterruptibleChannel implements Channel,
        InterruptibleChannel {
    static Method setInterruptAction = null;
    static {
        try {
            setInterruptAction = AccessController
                    .doPrivileged(new PrivilegedExceptionAction<Method>() {
                        public Method run() throws Exception {
                            return Thread.class.getDeclaredMethod(
                                    "setInterruptAction", 
                                    new Class[] { Runnable.class });
                        }
                    });
            setInterruptAction.setAccessible(true);
        } catch (PrivilegedActionException e) {
        }
    }
    private volatile boolean closed = false;
    volatile boolean interrupted = false;
    protected AbstractInterruptibleChannel() {
        super();
    }
    public synchronized final boolean isOpen() {
        return !closed;
    }
    public final void close() throws IOException {
        if (!closed) {
            synchronized (this) {
                if (!closed) {
                    closed = true;
                    implCloseChannel();
                }
            }
        }
    }
    protected final void begin() {
        if (setInterruptAction != null) {
            try {
                setInterruptAction.invoke(Thread.currentThread(),
                        new Object[] { new Runnable() {
                            public void run() {
                                try {
                                    interrupted = true;
                                    AbstractInterruptibleChannel.this.close();
                                } catch (IOException e) {
                                }
                            }
                        } });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    protected final void end(boolean success) throws AsynchronousCloseException {
        if (setInterruptAction != null) {
            try {
                setInterruptAction.invoke(Thread.currentThread(),
                        new Object[] { null });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (interrupted) {
                interrupted = false;
                throw new ClosedByInterruptException();
            }
        }
        if (!success && closed) {
            throw new AsynchronousCloseException();
        }
    }
    protected abstract void implCloseChannel() throws IOException;
}
