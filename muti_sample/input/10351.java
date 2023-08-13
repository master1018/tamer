public abstract class SwingWorker<T, V> implements RunnableFuture<T> {
    private static final int MAX_WORKER_THREADS = 10;
    private volatile int progress;
    private volatile StateValue state;
    private final FutureTask<T> future;
    private final PropertyChangeSupport propertyChangeSupport;
    private AccumulativeRunnable<V> doProcess;
    private AccumulativeRunnable<Integer> doNotifyProgressChange;
    private final AccumulativeRunnable<Runnable> doSubmit = getDoSubmit();
    public enum StateValue {
        PENDING,
        STARTED,
        DONE
    }
    public SwingWorker() {
        Callable<T> callable =
                new Callable<T>() {
                    public T call() throws Exception {
                        setState(StateValue.STARTED);
                        return doInBackground();
                    }
                };
        future = new FutureTask<T>(callable) {
                       @Override
                       protected void done() {
                           doneEDT();
                           setState(StateValue.DONE);
                       }
                   };
       state = StateValue.PENDING;
       propertyChangeSupport = new SwingWorkerPropertyChangeSupport(this);
       doProcess = null;
       doNotifyProgressChange = null;
    }
    protected abstract T doInBackground() throws Exception ;
    public final void run() {
        future.run();
    }
    @SafeVarargs
    protected final void publish(V... chunks) {
        synchronized (this) {
            if (doProcess == null) {
                doProcess = new AccumulativeRunnable<V>() {
                    @Override
                    public void run(List<V> args) {
                        process(args);
                    }
                    @Override
                    protected void submit() {
                        doSubmit.add(this);
                    }
                };
            }
        }
        doProcess.add(chunks);
    }
    protected void process(List<V> chunks) {
    }
    protected void done() {
    }
    protected final void setProgress(int progress) {
        if (progress < 0 || progress > 100) {
            throw new IllegalArgumentException("the value should be from 0 to 100");
        }
        if (this.progress == progress) {
            return;
        }
        int oldProgress = this.progress;
        this.progress = progress;
        if (! getPropertyChangeSupport().hasListeners("progress")) {
            return;
        }
        synchronized (this) {
            if (doNotifyProgressChange == null) {
                doNotifyProgressChange =
                    new AccumulativeRunnable<Integer>() {
                        @Override
                        public void run(List<Integer> args) {
                            firePropertyChange("progress",
                               args.get(0),
                               args.get(args.size() - 1));
                        }
                        @Override
                        protected void submit() {
                            doSubmit.add(this);
                        }
                    };
            }
        }
        doNotifyProgressChange.add(oldProgress, progress);
    }
    public final int getProgress() {
        return progress;
    }
    public final void execute() {
        getWorkersExecutorService().execute(this);
    }
    public final boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
    }
    public final boolean isCancelled() {
        return future.isCancelled();
    }
    public final boolean isDone() {
        return future.isDone();
    }
    public final T get() throws InterruptedException, ExecutionException {
        return future.get();
    }
    public final T get(long timeout, TimeUnit unit) throws InterruptedException,
            ExecutionException, TimeoutException {
        return future.get(timeout, unit);
    }
    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        getPropertyChangeSupport().addPropertyChangeListener(listener);
    }
    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        getPropertyChangeSupport().removePropertyChangeListener(listener);
    }
    public final void firePropertyChange(String propertyName, Object oldValue,
            Object newValue) {
        getPropertyChangeSupport().firePropertyChange(propertyName,
            oldValue, newValue);
    }
    public final PropertyChangeSupport getPropertyChangeSupport() {
        return propertyChangeSupport;
    }
    public final StateValue getState() {
        if (isDone()) {
            return StateValue.DONE;
        } else {
            return state;
        }
    }
    private void setState(StateValue state) {
        StateValue old = this.state;
        this.state = state;
        firePropertyChange("state", old, state);
    }
    private void doneEDT() {
        Runnable doDone =
            new Runnable() {
                public void run() {
                    done();
                }
            };
        if (SwingUtilities.isEventDispatchThread()) {
            doDone.run();
        } else {
            doSubmit.add(doDone);
        }
    }
    private static synchronized ExecutorService getWorkersExecutorService() {
        final AppContext appContext = AppContext.getAppContext();
        ExecutorService executorService =
            (ExecutorService) appContext.get(SwingWorker.class);
        if (executorService == null) {
            ThreadFactory threadFactory =
                new ThreadFactory() {
                    final ThreadFactory defaultFactory =
                        Executors.defaultThreadFactory();
                    public Thread newThread(final Runnable r) {
                        Thread thread =
                            defaultFactory.newThread(r);
                        thread.setName("SwingWorker-"
                            + thread.getName());
                        thread.setDaemon(true);
                        return thread;
                    }
                };
            executorService =
                new ThreadPoolExecutor(MAX_WORKER_THREADS, MAX_WORKER_THREADS,
                                       10L, TimeUnit.MINUTES,
                                       new LinkedBlockingQueue<Runnable>(),
                                       threadFactory);
            appContext.put(SwingWorker.class, executorService);
            final ExecutorService es = executorService;
            appContext.addPropertyChangeListener(AppContext.DISPOSED_PROPERTY_NAME,
                new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent pce) {
                        boolean disposed = (Boolean)pce.getNewValue();
                        if (disposed) {
                            final WeakReference<ExecutorService> executorServiceRef =
                                new WeakReference<ExecutorService>(es);
                            final ExecutorService executorService =
                                executorServiceRef.get();
                            if (executorService != null) {
                                AccessController.doPrivileged(
                                    new PrivilegedAction<Void>() {
                                        public Void run() {
                                            executorService.shutdown();
                                            return null;
                                        }
                                    }
                                );
                            }
                        }
                    }
                }
            );
        }
        return executorService;
    }
    private static final Object DO_SUBMIT_KEY = new StringBuilder("doSubmit");
    private static AccumulativeRunnable<Runnable> getDoSubmit() {
        synchronized (DO_SUBMIT_KEY) {
            final AppContext appContext = AppContext.getAppContext();
            Object doSubmit = appContext.get(DO_SUBMIT_KEY);
            if (doSubmit == null) {
                doSubmit = new DoSubmitAccumulativeRunnable();
                appContext.put(DO_SUBMIT_KEY, doSubmit);
            }
            return (AccumulativeRunnable<Runnable>) doSubmit;
        }
    }
    private static class DoSubmitAccumulativeRunnable
          extends AccumulativeRunnable<Runnable> implements ActionListener {
        private final static int DELAY = 1000 / 30;
        @Override
        protected void run(List<Runnable> args) {
            for (Runnable runnable : args) {
                runnable.run();
            }
        }
        @Override
        protected void submit() {
            Timer timer = new Timer(DELAY, this);
            timer.setRepeats(false);
            timer.start();
        }
        public void actionPerformed(ActionEvent event) {
            run();
        }
    }
    private class SwingWorkerPropertyChangeSupport
            extends PropertyChangeSupport {
        SwingWorkerPropertyChangeSupport(Object source) {
            super(source);
        }
        @Override
        public void firePropertyChange(final PropertyChangeEvent evt) {
            if (SwingUtilities.isEventDispatchThread()) {
                super.firePropertyChange(evt);
            } else {
                doSubmit.add(
                    new Runnable() {
                        public void run() {
                            SwingWorkerPropertyChangeSupport.this
                                .firePropertyChange(evt);
                        }
                    });
            }
        }
    }
}
