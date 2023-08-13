public class Bug4168625Test extends RBTestFmwk {
    public static void main(String[] args) throws Exception {
        new Bug4168625Test().run(args);
    }
    public void testMissingParent() throws Exception {
        final Locale oldDefault = Locale.getDefault();
        Locale.setDefault(new Locale("en", "US"));
        try {
            final Locale loc = new Locale("jf", "jf");
            ResourceBundle bundle = ResourceBundle.getBundle("Bug4168625Resource2", loc);
            final String s1 = bundle.getString("name");
            if (!s1.equals("Bug4168625Resource2_en_US")) {
                errln("getBundle did not find leaf bundle: "+bundle.getClass().getName());
            }
            final String s2 = bundle.getString("baseName");
            if (!s2.equals("Bug4168625Resource2")) {
                errln("getBundle did not set up proper inheritance chain");
            }
        } finally {
            Locale.setDefault(oldDefault);
        }
    }
    public void testCacheFailures() throws Exception {
        checkResourceLoading("Bug4168625Resource", new Locale("fr", "FR"));
    }
    public void testRedundantLoads() throws Exception {
        checkResourceLoading("Bug4168625Resource", Locale.getDefault());
    }
    private void checkResourceLoading(String resName, Locale l) throws Exception {
        final Loader loader = new Loader( new String[] { "Bug4168625Class" }, new String[] { "Bug4168625Resource3_en_US", "Bug4168625Resource3_en_CA" });
        final Class c = loader.loadClass("Bug4168625Class");
        Bug4168625Getter test = (Bug4168625Getter)c.newInstance();
        final String resClassName;
        if (l.toString().length() > 0) {
            resClassName = resName+"_"+l;
        } else {
            resClassName = resName;
        }
        Object bundle = test.getResourceBundle(resName, l);
        loader.logClasses("Initial lookup of "+resClassName+" generated the following loads:");
        final Vector lastLoad = new Vector(loader.loadedClasses.size());
        boolean dups = false;
        for (int i = loader.loadedClasses.size() - 1; i >= 0 ; i--) {
            final Object item = loader.loadedClasses.elementAt(i);
            loader.loadedClasses.removeElementAt(i);
            if (loader.loadedClasses.contains(item)) {
                logln("Resource loaded more than once: "+item);
                dups = true;
            } else {
                lastLoad.addElement(item);
            }
        }
        if (dups) {
            errln("ResourceBundle loaded some classes multiple times");
        }
        loader.loadedClasses.removeAllElements();
        bundle = test.getResourceBundle(resName, l);
        loader.logClasses("Second lookup of "+resClassName+" generated the following loads:");
        dups = false;
        for (int i = 0; i < loader.loadedClasses.size(); i++) {
            Object item = loader.loadedClasses.elementAt(i);
            if (lastLoad.contains(item)) {
                logln("ResourceBundle did not cache "+item+" correctly");
                dups = true;
            }
        }
        if (dups) {
            errln("Resource bundle not caching some classes properly");
        }
    }
    private class ConcurrentLoadingThread extends Thread {
        private Loader loader;
        public Object bundle;
        private Bug4168625Getter test;
        private Locale locale;
        private String resourceName = "Bug4168625Resource3";
        public ConcurrentLoadingThread(Loader loader, Bug4168625Getter test, Locale l, String resourceName) {
            this.loader = loader;
            this.test = test;
            this.locale = l;
            this.resourceName = resourceName;
        }
        public ConcurrentLoadingThread(Loader loader, Bug4168625Getter test, Locale l) {
            this.loader = loader;
            this.test = test;
            this.locale = l;
        }
        public void run() {
            try {
                logln(">>"+threadName()+">run");
                bundle = test.getResourceBundle(resourceName, locale);
            } catch (Exception e) {
                errln("TEST CAUGHT UNEXPECTED EXCEPTION: "+e);
            } finally {
                logln("<<"+threadName()+"<run");
            }
        }
        public synchronized void waitUntilPinged() {
            logln(">>"+threadName()+">waitUntilPinged");
            loader.notifyEveryone();
            try {
                wait(30000);    
            } catch (InterruptedException e) {
                logln("Test deadlocked.");
            }
            logln("<<"+threadName()+"<waitUntilPinged");
        }
        public synchronized void ping() {
            logln(">>"+threadName()+">ping "+threadName(this));
            notifyAll();
            logln("<<"+threadName()+"<ping "+threadName(this));
        }
    };
    public void testConcurrentLoading() throws Exception {
        final Loader loader = new Loader( new String[] { "Bug4168625Class" }, new String[] { "Bug4168625Resource3_en_US", "Bug4168625Resource3_en_CA" });
        final Class c = loader.loadClass("Bug4168625Class");
        final Bug4168625Getter test = (Bug4168625Getter)c.newInstance();
        ConcurrentLoadingThread thread1 = new ConcurrentLoadingThread(loader, test, new Locale("en", "CA"));
        ConcurrentLoadingThread thread2 = new ConcurrentLoadingThread(loader, test, new Locale("en", "IE"));
        thread1.start();            
        loader.waitForNotify(1);    
        thread2.start();            
        thread2.join();             
        if (!thread1.isAlive() || thread2.isAlive()) {
            errln("ResourceBundle.getBundle not allowing legal concurrent loads");
        }
        thread1.ping();             
        thread1.join();
    }
    public void testLowMemoryLoad() throws Exception {
        final String[] classToLoad = { "Bug4168625Class" };
        final String[] classToWait = { "Bug4168625Resource3_en_US","Bug4168625Resource3_en","Bug4168625Resource3" };
        final Loader loader = new Loader(classToLoad, classToWait);
        final Class c = loader.loadClass("Bug4168625Class");
        final Bug4168625Getter test = (Bug4168625Getter)c.newInstance();
        causeResourceBundleCacheFlush();
        ConcurrentLoadingThread thread1 = new ConcurrentLoadingThread(loader, test, new Locale("en", "US"));
        thread1.start();            
        loader.waitForNotify(1);    
        causeResourceBundleCacheFlush();    
        thread1.ping();             
        loader.waitForNotify(2);    
        causeResourceBundleCacheFlush();    
        thread1.ping();             
        loader.waitForNotify(3);    
        causeResourceBundleCacheFlush();    
        thread1.ping();             
        thread1.join();             
        ResourceBundle bundle = (ResourceBundle)thread1.bundle;
        String s1 = bundle.getString("Bug4168625Resource3_en_US");
        String s2 = bundle.getString("Bug4168625Resource3_en");
        String s3 = bundle.getString("Bug4168625Resource3");
        if ((s1 == null) || (s2 == null) || (s3 == null)) {
            errln("Bundle not constructed correctly.  The parent chain is incorrect.");
        }
    }
    private static final String CLASS_PREFIX = "";
    private static final String CLASS_SUFFIX = ".class";
    private static final class SimpleLoader extends ClassLoader {
        private boolean network = false;
        public SimpleLoader() {
            super(SimpleLoader.class.getClassLoader());
            this.network = false;
        }
        public SimpleLoader(boolean simulateNetworkLoad) {
            super(SimpleLoader.class.getClassLoader());
            this.network = simulateNetworkLoad;
        }
        public Class loadClass(final String className, final boolean resolveIt)
                throws ClassNotFoundException {
            Class result;
            synchronized (this) {
                result = findLoadedClass(className);
                if (result == null) {
                    if (network) {
                        try {
                             Thread.sleep(100);
                        } catch (java.lang.InterruptedException e) {
                        }
                    }
                    result = getParent().loadClass(className);
                    if ((result != null) && resolveIt) {
                        resolveClass(result);
                    }
                }
            }
            return result;
        }
    }
    private final class Loader extends ClassLoader {
        public final Vector loadedClasses = new Vector();
        private String[] classesToLoad;
        private String[] classesToWaitFor;
        public Loader() {
            super(Loader.class.getClassLoader());
            classesToLoad = new String[0];
            classesToWaitFor = new String[0];
        }
        public Loader(final String[] classesToLoadIn, final String[] classesToWaitForIn) {
            super(Loader.class.getClassLoader());
            classesToLoad = classesToLoadIn;
            classesToWaitFor = classesToWaitForIn;
        }
        private byte[] getClassData(final String className) {
            boolean shouldLoad = false;
            for (int i = classesToLoad.length-1; i >= 0; --i) {
                if (className.equals(classesToLoad[i])) {
                    shouldLoad = true;
                    break;
                }
            }
            if (shouldLoad) {
                final String name = CLASS_PREFIX+className+CLASS_SUFFIX;
                try {
                    final InputStream fi = this.getClass().getClassLoader().getResourceAsStream(name);
                    final byte[] result = new byte[fi.available()];
                    fi.read(result);
                    return result;
                } catch (Exception e) {
                    logln("Error loading test class: "+name);
                    logln(e.toString());
                    return null;
                }
            } else {
                return null;
            }
        }
        public Class loadClass(final String className, final boolean resolveIt)
                throws ClassNotFoundException {
            Class result;
            synchronized (this) {
                logln(">>"+threadName()+">load "+className);
                loadedClasses.addElement(className);
                result = findLoadedClass(className);
                if (result == null) {
                    final byte[] classData = getClassData(className);
                    if (classData == null) {
                        logln("Loading system class: "+className);
                        result = loadFromSystem(className);
                    } else {
                        result = defineClass(classData, 0, classData.length);
                        if (result == null) {
                            result = loadFromSystem(className);
                        }
                    }
                    if ((result != null) && resolveIt) {
                        resolveClass(result);
                    }
                }
            }
            for (int i = classesToWaitFor.length-1; i >= 0; --i) {
                if (className.equals(classesToWaitFor[i])) {
                    rendezvous();
                    break;
                }
            }
            logln("<<"+threadName()+"<load "+className);
            return result;
        }
        private Class loadFromSystem(String className) throws ClassNotFoundException {
            return getParent().loadClass(className);
        }
        public void logClasses(String title) {
            logln(title);
            for (int i = 0; i < loadedClasses.size(); i++) {
                logln("    "+loadedClasses.elementAt(i));
            }
            logln("");
        }
        public int notifyCount = 0;
        public int waitForNotify(int count) {
            return waitForNotify(count, 0);
        }
        public synchronized int waitForNotify(int count, long time) {
            logln(">>"+threadName()+">waitForNotify");
            if (count > notifyCount) {
                try {
                    wait(time);
                } catch (InterruptedException e) {
                }
            } else {
                logln("  count("+count+") > notifyCount("+notifyCount+")");
            }
            logln("<<"+threadName()+"<waitForNotify");
            return notifyCount;
        }
        private synchronized void notifyEveryone() {
            logln(">>"+threadName()+">notifyEveryone");
            notifyCount++;
            notifyAll();
            logln("<<"+threadName()+"<notifyEveryone");
        }
        private void rendezvous() {
            final Thread current = Thread.currentThread();
            if (current instanceof ConcurrentLoadingThread) {
                ((ConcurrentLoadingThread)current).waitUntilPinged();
            }
        }
    }
    private static String threadName() {
        return threadName(Thread.currentThread());
    }
    private static String threadName(Thread t) {
        String temp = t.toString();
        int ndx = temp.indexOf("Thread[");
        temp = temp.substring(ndx + "Thread[".length());
        ndx = temp.indexOf(',');
        temp = temp.substring(0, ndx);
        return temp;
    }
    private void causeResourceBundleCacheFlush() {
        logln("Filling memory...");
        int allocationSize = 1024;
        Vector memoryHog = new Vector();
        try {
            while (true) {
                memoryHog.addElement(new byte[allocationSize]);
                allocationSize *= 2;
            }
        } catch (Throwable e) {
            logln("Caught "+e+" filling memory");
        } finally{
            memoryHog = null;
            System.gc();
        }
        logln("last allocation size: " + allocationSize);
    }
}
