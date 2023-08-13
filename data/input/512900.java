public class Finalizer extends Thread {
  private static final Logger logger
      = Logger.getLogger(Finalizer.class.getName());
  private static final String FINALIZABLE_REFERENCE
      = "com.google.common.base.FinalizableReference";
  public static ReferenceQueue<Object> startFinalizer(
      Class<?> finalizableReferenceClass, Object frq) {
    if (!finalizableReferenceClass.getName().equals(FINALIZABLE_REFERENCE)) {
      throw new IllegalArgumentException(
          "Expected " + FINALIZABLE_REFERENCE + ".");
    }
    Finalizer finalizer = new Finalizer(finalizableReferenceClass, frq);
    finalizer.start();
    return finalizer.queue;
  }
  private final WeakReference<Class<?>> finalizableReferenceClassReference;
  private final PhantomReference<Object> frqReference;
  private final ReferenceQueue<Object> queue = new ReferenceQueue<Object>();
  private static final Field inheritableThreadLocals
      = getInheritableThreadLocalsField();
  private Finalizer(Class<?> finalizableReferenceClass, Object frq) {
    super(Finalizer.class.getName());
    this.finalizableReferenceClassReference
        = new WeakReference<Class<?>>(finalizableReferenceClass);
    this.frqReference = new PhantomReference<Object>(frq, queue);
    setDaemon(true);
    try {
      if (inheritableThreadLocals != null) {
        inheritableThreadLocals.set(this, null);
      }
    } catch (Throwable t) {
      logger.log(Level.INFO, "Failed to clear thread local values inherited"
          + " by reference finalizer thread.", t);
    }
  }
  @SuppressWarnings("InfiniteLoopStatement")
  @Override
  public void run() {
    try {
      while (true) {
        try {
          cleanUp(queue.remove());
        } catch (InterruptedException e) {  }
      }
    } catch (ShutDown shutDown) {  }
  }
  private void cleanUp(Reference<?> reference) throws ShutDown {
    Method finalizeReferentMethod = getFinalizeReferentMethod();
    do {
      reference.clear();
      if (reference == frqReference) {
        throw new ShutDown();
      }
      try {
        finalizeReferentMethod.invoke(reference);
      } catch (Throwable t) {
        logger.log(Level.SEVERE, "Error cleaning up after reference.", t);
      }
    } while ((reference = queue.poll()) != null);
  }
  private Method getFinalizeReferentMethod() throws ShutDown {
    Class<?> finalizableReferenceClass
        = finalizableReferenceClassReference.get();
    if (finalizableReferenceClass == null) {
      throw new ShutDown();
    }
    try {
      return finalizableReferenceClass.getMethod("finalizeReferent");
    } catch (NoSuchMethodException e) {
      throw new AssertionError(e);
    }
  }
  public static Field getInheritableThreadLocalsField() {
    try {
      Field inheritableThreadLocals
          = Thread.class.getDeclaredField("inheritableThreadLocals");
      inheritableThreadLocals.setAccessible(true);
      return inheritableThreadLocals;
    } catch (Throwable t) {
      logger.log(Level.INFO, "Couldn't access Thread.inheritableThreadLocals."
          + " Reference finalizer threads will inherit thread local"
          + " values.");
      return null;
    }
  }
  @SuppressWarnings("serial") 
  private static class ShutDown extends Exception { }
}
