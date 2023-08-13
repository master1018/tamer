public class Test6800154 implements Runnable {
    static final long[] DIVIDENDS = {
        0,
        1,
        2,
        1423487,
        4444441,
        4918923241323L,
        -1,
        -24351,
        0x3333,
        0x0000000080000000L,
        0x7fffffffffffffffL,
        0x8000000000000000L
    };
    static final long[] DIVISORS = {
        1,
        2,
        17,
        12342,
        24123,
        143444,
        123444442344L,
        -1,
        -2,
        -4423423234231423L,
        0x0000000080000000L,
        0x7fffffffffffffffL,
        0x8000000000000000L
    };
    static final long DIVISOR;
    static {
        long value = 0;
        try {
            value = Long.decode(System.getProperty("divisor"));
        } catch (Throwable e) {
        }
        DIVISOR = value;
    }
    public static void main(String[] args) throws Exception
    {
        Class cl = Class.forName("Test6800154");
        URLClassLoader apploader = (URLClassLoader) cl.getClassLoader();
        for (int i = 0; i < DIVISORS.length; i++) {
            System.setProperty("divisor", "" + DIVISORS[i]);
            ClassLoader loader = new URLClassLoader(apploader.getURLs(), apploader.getParent());
            Class c = loader.loadClass("Test6800154");
            Runnable r = (Runnable) c.newInstance();
            r.run();
        }
    }
    public void run()
    {
        for (int i = 0; i < DIVIDENDS.length; i++) {
            long dividend = DIVIDENDS[i];
            long expected = divint(dividend);
            long result = divcomp(dividend);
            if (result != expected)
                throw new InternalError(dividend + " / " + DIVISOR + " failed: " + result + " != " + expected);
        }
    }
    static long divint(long a)  { return a / DIVISOR; }
    static long divcomp(long a) { return a / DIVISOR; }
}
