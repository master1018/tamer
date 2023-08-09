public class Futures {
  private Futures() {}
  public static <V> UninterruptibleFuture<V> makeUninterruptible(
      final Future<V> future) {
    checkNotNull(future);
    if (future instanceof UninterruptibleFuture) {
      return (UninterruptibleFuture<V>) future;
    }
    return new UninterruptibleFuture<V>() {
      public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
      }
      public boolean isCancelled() {
        return future.isCancelled();
      }
      public boolean isDone() {
        return future.isDone();
      }
      public V get(long timeoutDuration, TimeUnit timeoutUnit)
          throws TimeoutException, ExecutionException {
        boolean interrupted = false;
        try {
          long timeoutNanos = timeoutUnit.toNanos(timeoutDuration);
          long end = System.nanoTime() + timeoutNanos;
          for (long remaining = timeoutNanos; remaining > 0;
              remaining = end - System.nanoTime()) {
            try {
              return future.get(remaining, TimeUnit.NANOSECONDS);
            } catch (InterruptedException ignored) {
              interrupted = true;
            }
          }
          throw new TimeoutException();
        } finally {
          if (interrupted) {
            Thread.currentThread().interrupt();
          }
        }
      }
      public V get() throws ExecutionException {
        boolean interrupted = false;
        try {
          while (true) {
            try {
              return future.get();
            } catch (InterruptedException ignored) {
              interrupted = true;
            }
          }
        } finally {
          if (interrupted) {
            Thread.currentThread().interrupt();
          }
        }
      }
    };
  }
  public static <T> ListenableFuture<T> makeListenable(Future<T> future) {
    if (future instanceof ListenableFuture) {
      return (ListenableFuture<T>) future;
    }
    return new ListenableFutureAdapter<T>(future);
  }
  public static <T, E extends Exception> CheckedFuture<T, E> makeChecked(
      Future<T> future, Function<Exception, E> mapper) {
    return new MappingCheckedFuture<T, E>(makeListenable(future), mapper);
  }
  public static <T> ListenableFuture<T> immediateFuture(@Nullable T value) {
    ValueFuture<T> future = ValueFuture.create();
    future.set(value);
    return future;
  }
  public static <T, E extends Exception> CheckedFuture<T, E>
      immediateCheckedFuture(@Nullable T value) {
    ValueFuture<T> future = ValueFuture.create();
    future.set(value);
    return Futures.makeChecked(future, new Function<Exception, E>() {
      public E apply(Exception e) {
        throw new AssertionError("impossible");
      }
    });
  }
  public static <T> ListenableFuture<T> immediateFailedFuture(
      Throwable throwable) {
    checkNotNull(throwable);
    ValueFuture<T> future = ValueFuture.create();
    future.setException(throwable);
    return future;
  }
  public static <T, E extends Exception> CheckedFuture<T, E>
      immediateFailedCheckedFuture(final E exception) {
    checkNotNull(exception);
    return makeChecked(Futures.<T>immediateFailedFuture(exception),
        new Function<Exception, E>() {
          public E apply(Exception e) {
            return exception;
          }
        });
  }
  public static <I, O> ListenableFuture<O> chain(ListenableFuture<I> input,
      Function<? super I, ? extends ListenableFuture<? extends O>> function) {
    return chain(input, function, Executors.sameThreadExecutor());
  }
  public static <I, O> ListenableFuture<O> chain(ListenableFuture<I> input,
      Function<? super I, ? extends ListenableFuture<? extends O>> function,
      Executor exec) {
    ChainingListenableFuture<I, O> chain =
        new ChainingListenableFuture<I, O>(function, input);
    input.addListener(chain, exec);
    return chain;
  }
  public static <I, O> ListenableFuture<O> compose(ListenableFuture<I> future,
      final Function<? super I, ? extends O> function) {
    return compose(future, function, Executors.sameThreadExecutor());
  }
  public static <I, O> ListenableFuture<O> compose(ListenableFuture<I> future,
      final Function<? super I, ? extends O> function, Executor exec) {
    Function<I, ListenableFuture<O>> wrapperFunction
        = new Function<I, ListenableFuture<O>>() {
             public ListenableFuture<O> apply(I input) {
              O output = function.apply(input);
              return immediateFuture(output);
            }
        };
    return chain(future, wrapperFunction, exec);
  }
  public static <I, O> Future<O> compose(final Future<I> future,
      final Function<? super I, ? extends O> function) {
    return new Future<O>() {
      private final Object lock = new Object();
      private boolean set = false;
      private O value = null;
      public O get() throws InterruptedException, ExecutionException {
        return apply(future.get());
      }
      public O get(long timeout, TimeUnit unit) throws InterruptedException,
          ExecutionException, TimeoutException {
        return apply(future.get(timeout, unit));
      }
      private O apply(I raw) {
        synchronized(lock) {
          if (!set) {
            value = function.apply(raw);
            set = true;
          }
          return value;
        }
      }
      public boolean cancel(boolean mayInterruptIfRunning) {
        return future.cancel(mayInterruptIfRunning);
      }
      public boolean isCancelled() {
        return future.isCancelled();
      }
      public boolean isDone() {
        return future.isDone();
      }
    };
  }
  private static class ChainingListenableFuture<I, O>
      extends AbstractListenableFuture<O> implements Runnable {
    private final Function<? super I, ? extends ListenableFuture<? extends O>>
        function;
    private final UninterruptibleFuture<? extends I> inputFuture;
    private ChainingListenableFuture(
        Function<? super I, ? extends ListenableFuture<? extends O>> function,
        ListenableFuture<? extends I> inputFuture) {
      this.function = function;
      this.inputFuture = makeUninterruptible(inputFuture);
    }
    public void run() {
      try {
        I sourceResult;
        try {
          sourceResult = inputFuture.get();
        } catch (CancellationException e) {
          cancel();
          return;
        } catch (ExecutionException e) {
          setException(e.getCause());
          return;
        }
        final ListenableFuture<? extends O> outputFuture =
            function.apply(sourceResult);
        outputFuture.addListener(new Runnable() {
            public void run() {
              try {
                set(makeUninterruptible(outputFuture).get());
              } catch (ExecutionException e) {
                setException(e.getCause());
              }
            }
          }, Executors.sameThreadExecutor());
      } catch (UndeclaredThrowableException e) {
        setException(e.getCause());
      } catch (RuntimeException e) {
        setException(e);
      } catch (Error e) {
        setException(e);
        throw e;
      }
    }
  }
  private static class MappingCheckedFuture<T, E extends Exception> extends
      AbstractCheckedFuture<T, E> {
    final Function<Exception, E> mapper;
    MappingCheckedFuture(ListenableFuture<T> delegate,
        Function<Exception, E> mapper) {
      super(delegate);
      this.mapper = mapper;
    }
    @Override
    protected E mapException(Exception e) {
      return mapper.apply(e);
    }
  }
  private static class ListenableFutureAdapter<T> extends ForwardingFuture<T>
      implements ListenableFuture<T> {
    private static final Executor adapterExecutor =
        java.util.concurrent.Executors.newCachedThreadPool();
    private final ExecutionList executionList = new ExecutionList();
    private final AtomicBoolean hasListeners = new AtomicBoolean(false);
    private final Future<T> delegate;
    ListenableFutureAdapter(final Future<T> delegate) {
      this.delegate = delegate;
    }
    @Override
    protected Future<T> delegate() {
      return delegate;
    }
    public void addListener(Runnable listener, Executor exec) {
      if (!hasListeners.get() && hasListeners.compareAndSet(false, true)) {
        adapterExecutor.execute(new Runnable() {
          public void run() {
            try {
              delegate.get();
            } catch (CancellationException e) {
            } catch (InterruptedException e) {
              throw new IllegalStateException("Adapter thread interrupted!", e);
            } catch (ExecutionException e) {
            }
            executionList.run();
          }
        });
      }
      executionList.add(listener, exec);
    }
  }
}
