final class PendingFuture<V,A> implements Future<V> {
    private static final CancellationException CANCELLED =
        new CancellationException();
    private final AsynchronousChannel channel;
    private final CompletionHandler<V,? super A> handler;
    private final A attachment;
    private volatile boolean haveResult;
    private volatile V result;
    private volatile Throwable exc;
    private CountDownLatch latch;
    private Future<?> timeoutTask;
    private volatile Object context;
    PendingFuture(AsynchronousChannel channel,
                  CompletionHandler<V,? super A> handler,
                  A attachment,
                  Object context)
    {
        this.channel = channel;
        this.handler = handler;
        this.attachment = attachment;
        this.context = context;
    }
    PendingFuture(AsynchronousChannel channel,
                  CompletionHandler<V,? super A> handler,
                  A attachment)
    {
        this.channel = channel;
        this.handler = handler;
        this.attachment = attachment;
    }
    PendingFuture(AsynchronousChannel channel) {
        this(channel, null, null);
    }
    PendingFuture(AsynchronousChannel channel, Object context) {
        this(channel, null, null, context);
    }
    AsynchronousChannel channel() {
        return channel;
    }
    CompletionHandler<V,? super A> handler() {
        return handler;
    }
    A attachment() {
        return attachment;
    }
    void setContext(Object context) {
        this.context = context;
    }
    Object getContext() {
        return context;
    }
    void setTimeoutTask(Future<?> task) {
        synchronized (this) {
            if (haveResult) {
                task.cancel(false);
            } else {
                this.timeoutTask = task;
            }
        }
    }
    private boolean prepareForWait() {
        synchronized (this) {
            if (haveResult) {
                return false;
            } else {
                if (latch == null)
                    latch = new CountDownLatch(1);
                return true;
            }
        }
    }
    void setResult(V res) {
        synchronized (this) {
            if (haveResult)
                return;
            result = res;
            haveResult = true;
            if (timeoutTask != null)
                timeoutTask.cancel(false);
            if (latch != null)
                latch.countDown();
        }
    }
    void setFailure(Throwable x) {
        if (!(x instanceof IOException) && !(x instanceof SecurityException))
            x = new IOException(x);
        synchronized (this) {
            if (haveResult)
                return;
            exc = x;
            haveResult = true;
            if (timeoutTask != null)
                timeoutTask.cancel(false);
            if (latch != null)
                latch.countDown();
        }
    }
    void setResult(V res, Throwable x) {
        if (x == null) {
            setResult(res);
        } else {
            setFailure(x);
        }
    }
    @Override
    public V get() throws ExecutionException, InterruptedException {
        if (!haveResult) {
            boolean needToWait = prepareForWait();
            if (needToWait)
                latch.await();
        }
        if (exc != null) {
            if (exc == CANCELLED)
                throw new CancellationException();
            throw new ExecutionException(exc);
        }
        return result;
    }
    @Override
    public V get(long timeout, TimeUnit unit)
        throws ExecutionException, InterruptedException, TimeoutException
    {
        if (!haveResult) {
            boolean needToWait = prepareForWait();
            if (needToWait)
                if (!latch.await(timeout, unit)) throw new TimeoutException();
        }
        if (exc != null) {
            if (exc == CANCELLED)
                throw new CancellationException();
            throw new ExecutionException(exc);
        }
        return result;
    }
    Throwable exception() {
        return (exc != CANCELLED) ? exc : null;
    }
    V value() {
        return result;
    }
    @Override
    public boolean isCancelled() {
        return (exc == CANCELLED);
    }
    @Override
    public boolean isDone() {
        return haveResult;
    }
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        synchronized (this) {
            if (haveResult)
                return false;    
            if (channel() instanceof Cancellable)
                ((Cancellable)channel()).onCancel(this);
            exc = CANCELLED;
            haveResult = true;
            if (timeoutTask != null)
                timeoutTask.cancel(false);
        }
        if (mayInterruptIfRunning) {
            try {
                channel().close();
            } catch (IOException ignore) { }
        }
        if (latch != null)
            latch.countDown();
        return true;
    }
}
