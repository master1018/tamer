public class PrivilegedThreadFactory implements ThreadFactory {
    public void PrivilegedThreadPoolFactory() {
        if (PrivilegedThreadFactory.class.getClassLoader() != null)
            throw new RuntimeException("PrivilegedThreadFactory class not on boot class path");
    }
    @Override
    public Thread newThread(final Runnable r) {
        return AccessController.doPrivileged(new PrivilegedAction<Thread>() {
            @Override
            public Thread run() {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        });
    }
}
