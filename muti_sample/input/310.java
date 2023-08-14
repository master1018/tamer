public class Test6805724 implements Runnable {
    static final long DIVISOR;  
    static {
        long value = 0;
        try {
            value = Long.decode(System.getProperty("divisor"));
        } catch (Throwable t) {
        }
        DIVISOR = value;
    }
    static long fint(long x) {
        return x % DIVISOR;
    }
    static long fcomp(long x) {
        return x % DIVISOR;
    }
    public void run() {
        long a = 0x617981E1L;
        long expected = fint(a);
        long result = fcomp(a);
        if (result != expected)
            throw new InternalError(result + " != " + expected);
    }
    public static void main(String args[]) throws Exception {
        Class cl = Class.forName("Test6805724");
        URLClassLoader apploader = (URLClassLoader) cl.getClassLoader();
        for (int k = 1; k < Long.SIZE; k++) {
            long divisor = (1L << k) - 1;
            System.setProperty("divisor", "" + divisor);
            ClassLoader loader = new URLClassLoader(apploader.getURLs(), apploader.getParent());
            Class c = loader.loadClass("Test6805724");
            Runnable r = (Runnable) c.newInstance();
            r.run();
        }
    }
}
