public class ClassLoaderLeakTest {
    private static CountDownLatch doneSignal;
    private static CountDownLatch launchSignal;
    private static ThreadGroup appsThreadGroup;
    private static Throwable launchFailure = null;
    public static void main(String[] args) {
        appsThreadGroup = new ThreadGroup("MyAppsThreadGroup");
        doneSignal = new CountDownLatch(1);
        launchSignal = new CountDownLatch(1);
        Runnable launcher = new Runnable() {
            public void run() {
                try {
                    ClassLoader cl =
                        Thread.currentThread().getContextClassLoader();
                    Class appMain = cl.loadClass("AppTest");
                    Method launch =
                        appMain.getDeclaredMethod("launch", doneSignal.getClass());
                    Constructor c = appMain.getConstructor();
                    Object o = c.newInstance();
                    launch.invoke(o, doneSignal);
                } catch (Throwable e) {
                    launchFailure = e;
                } finally {
                    launchSignal.countDown();
                }
            }
        };
        URL pwd = null;
        try {
            pwd = new File(System.getProperty("test.classes",".")).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Test failed.", e);
        }
        URL[] urls = new URL[] { pwd };
         MyClassLoader appClassLoader = new MyClassLoader(urls, "test0");
         WeakReference<MyClassLoader> ref =
                 new WeakReference<>(appClassLoader);
         Thread appThread = new Thread(appsThreadGroup, launcher, "AppThread-0");
         appThread.setContextClassLoader(appClassLoader);
         appThread.start();
         appClassLoader = null;
         launcher = null;
         appThread = null;
         try {
             launchSignal.await();
         } catch (InterruptedException e) {
         }
         if (launchFailure != null) {
             throw new RuntimeException("Test failed.", launchFailure);
         }
         try {
             doneSignal.await();
         } catch (InterruptedException e) {
         }
         waitAndGC(5);
         if (ref.get() != null) {
             throw new RuntimeException("Test failed: classloader is still alive");
         }
         System.out.println("Test passed.");
    }
    private static class MyClassLoader extends URLClassLoader {
        private static boolean verbose =
            Boolean.getBoolean("verboseClassLoading");
        private String uniqClassName;
        public MyClassLoader(URL[] urls, String uniq) {
            super(urls);
            uniqClassName = uniq;
        }
        public Class loadClass(String name) throws ClassNotFoundException {
            if (verbose) {
                System.out.printf("%s: load class %s\n", uniqClassName, name);
            }
            if (uniqClassName.equals(name)) {
                return Object.class;
            }
            return super.loadClass(name);
        }
        public String toString() {
            return "MyClassLoader(" + uniqClassName + ")";
        }
    }
    private static void waitAndGC(int sec) {
        int cnt = sec;
        System.out.print("Wait ");
        while (cnt-- > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            if (cnt % 3 == 2) {
                System.gc();
                System.out.print("+");
            } else {
                System.out.print(".");
            }
        }
        System.out.println("");
    }
}
class AppTest {
    public AppTest() {
    }
    public void launch(CountDownLatch done) {
        Logger log = Logger.getLogger("app_test_logger");
        log.fine("Test app is launched");
        done.countDown();
    }
}
