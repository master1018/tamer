public class StreamCloser {
    private static WeakHashMap<CloseAction, Object> toCloseQueue;
    private static Thread streamCloser;
    public static void addToQueue(CloseAction ca) {
        synchronized (StreamCloser.class) {
            if (toCloseQueue == null) {
                toCloseQueue =
                    new WeakHashMap<CloseAction, Object>();
            }
            toCloseQueue.put(ca, null);
            if (streamCloser == null) {
                final Runnable streamCloserRunnable = new Runnable() {
                    public void run() {
                        if (toCloseQueue != null) {
                            synchronized (StreamCloser.class) {
                                Set<CloseAction> set =
                                    toCloseQueue.keySet();
                                CloseAction[] actions =
                                    new CloseAction[set.size()];
                                actions = set.toArray(actions);
                                for (CloseAction ca : actions) {
                                    if (ca != null) {
                                        try {
                                            ca.performAction();
                                        } catch (IOException e) {
                                        }
                                    }
                                }
                            }
                        }
                    }
                };
                java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction() {
                        public Object run() {
                            ThreadGroup tg =
                                Thread.currentThread().getThreadGroup();
                            for (ThreadGroup tgn = tg;
                                 tgn != null;
                                 tg = tgn, tgn = tg.getParent());
                            streamCloser = new Thread(tg, streamCloserRunnable);
                            streamCloser.setContextClassLoader(null);
                            Runtime.getRuntime().addShutdownHook(streamCloser);
                            return null;
                        }
                    });
            }
        }
    }
    public static void removeFromQueue(CloseAction ca) {
        synchronized (StreamCloser.class) {
            if (toCloseQueue != null) {
                toCloseQueue.remove(ca);
            }
        }
    }
    public static CloseAction createCloseAction(ImageInputStream iis) {
        return new CloseAction(iis);
    }
    public static final class CloseAction {
        private ImageInputStream iis;
        private CloseAction(ImageInputStream iis) {
            this.iis = iis;
        }
        public void performAction() throws IOException {
            if (iis != null) {
                iis.close();
            }
        }
    }
}
