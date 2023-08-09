public class SubclassGC {
        private static final long TIMEOUT = 1000;
        public static final void main(String[] args) throws Exception {
                System.err.println("\n Regression test for bug 6232010\n");
                if (System.getSecurityManager() == null) {
                        System.setSecurityManager(new SecurityManager());
                }
                ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
                ClassLoader loader = new URLClassLoader(((URLClassLoader) systemLoader).getURLs(),
                                                                                        systemLoader.getParent());
                Class<? extends ObjectOutputStream> cl =
                        Class.forName(SubclassOfOOS.class.getName(), false,
                                                  loader).asSubclass(ObjectOutputStream.class);
                Constructor<? extends ObjectOutputStream> cons =
                        cl.getConstructor(OutputStream.class);
                OutputStream os = new ByteArrayOutputStream();
                ObjectOutputStream obj = cons.newInstance(os);
                final ReferenceQueue<Class<?>> queue = new ReferenceQueue<Class<?>>();
                WeakReference<Class<?>> ref = new WeakReference<Class<?>>(cl, queue);
                cl = null;
                obj = null;
                loader = null;
                cons = null;
                systemLoader = null;
                System.err.println("\nStart Garbage Collection right now");
                System.gc();
                Reference<? extends Class<?>> dequeued = queue.remove(TIMEOUT);
                if (dequeued == ref) {
                        System.err.println("\nTEST PASSED");
                } else {
                        throw new Error();
                }
        }
}
