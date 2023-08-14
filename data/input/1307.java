public class Disposer implements Runnable {
    private static final ReferenceQueue queue = new ReferenceQueue();
    private static final Hashtable records = new Hashtable();
    private static Disposer disposerInstance;
    public static final int WEAK = 0;
    public static final int PHANTOM = 1;
    public static int refType = PHANTOM;
    static {
        java.security.AccessController.doPrivileged(
            new sun.security.action.LoadLibraryAction("awt"));
        initIDs();
        String type = (String) java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction("sun.java2d.reftype"));
        if (type != null) {
            if (type.equals("weak")) {
                refType = WEAK;
                System.err.println("Using WEAK refs");
            } else {
                refType = PHANTOM;
                System.err.println("Using PHANTOM refs");
            }
        }
        disposerInstance = new Disposer();
        java.security.AccessController.doPrivileged(
            new java.security.PrivilegedAction() {
                public Object run() {
                    ThreadGroup tg = Thread.currentThread().getThreadGroup();
                    for (ThreadGroup tgn = tg;
                         tgn != null;
                         tg = tgn, tgn = tg.getParent());
                    Thread t =
                        new Thread(tg, disposerInstance, "Java2D Disposer");
                    t.setContextClassLoader(null);
                    t.setDaemon(true);
                    t.setPriority(Thread.MAX_PRIORITY);
                    t.start();
                    return null;
                }
            }
        );
    }
    public static void addRecord(Object target,
                                 long disposeMethod, long pData)
    {
        disposerInstance.add(target,
                             new DefaultDisposerRecord(disposeMethod, pData));
    }
    public static void addRecord(Object target, DisposerRecord rec) {
        disposerInstance.add(target, rec);
    }
    synchronized void add(Object target, DisposerRecord rec) {
        if (target instanceof DisposerTarget) {
            target = ((DisposerTarget)target).getDisposerReferent();
        }
        java.lang.ref.Reference ref;
        if (refType == PHANTOM) {
            ref = new PhantomReference(target, queue);
        } else {
            ref = new WeakReference(target, queue);
        }
        records.put(ref, rec);
    }
    public void run() {
        while (true) {
            try {
                Object obj = queue.remove();
                ((Reference)obj).clear();
                DisposerRecord rec = (DisposerRecord)records.remove(obj);
                rec.dispose();
                obj = null;
                rec = null;
                clearDeferredRecords();
            } catch (Exception e) {
                System.out.println("Exception while removing reference: " + e);
                e.printStackTrace();
            }
        }
    }
    public static interface PollDisposable {
    };
    private static ArrayList<DisposerRecord> deferredRecords = null;
    private static void clearDeferredRecords() {
        if (deferredRecords == null || deferredRecords.isEmpty()) {
            return;
        }
        for (int i=0;i<deferredRecords.size(); i++) {
            try {
                DisposerRecord rec = deferredRecords.get(i);
                rec.dispose();
            } catch (Exception e) {
                System.out.println("Exception while disposing deferred rec.");
                e.printStackTrace();
            }
        }
        deferredRecords.clear();
    }
    public static volatile boolean pollingQueue = false;
    public static void pollRemove() {
        if (pollingQueue) {
            return;
        }
        Object obj;
        pollingQueue = true;
        int freed = 0;
        int deferred = 0;
        try {
            while ((obj = queue.poll()) != null
                   && freed < 10000 && deferred < 100) {
                freed++;
                ((Reference)obj).clear();
                DisposerRecord rec = (DisposerRecord)records.remove(obj);
                if (rec instanceof PollDisposable) {
                    rec.dispose();
                    obj = null;
                    rec = null;
                } else {
                    if (rec == null) { 
                        continue;
                    }
                    deferred++;
                    if (deferredRecords == null) {
                      deferredRecords = new ArrayList<DisposerRecord>(5);
                    }
                    deferredRecords.add(rec);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception while removing reference: " + e);
            e.printStackTrace();
        } finally {
            pollingQueue = false;
        }
    }
    private static native void initIDs();
    public static void addReference(Reference ref, DisposerRecord rec) {
        records.put(ref, rec);
    }
    public static void addObjectRecord(Object obj, DisposerRecord rec) {
        records.put(new WeakReference(obj, queue) , rec);
    }
    public static ReferenceQueue getQueue() {
        return queue;
    }
}
