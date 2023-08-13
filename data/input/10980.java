public class ExpirationTest {
    static final Locale AUSTRIA = new Locale("de", "AT");
    static String format;
    static String fileType;
    public static void main(String[] args) {
        if ("-latency".equals(args[0])) {
            System.out.print("Checking latency... ");
            for (int i = 0; i < 3; i++) {
                sleep(3);
            }
            System.out.println("done");
            System.exit(0);
        }
        format = args[0];
        fileType = args[1];
        Locale loc = Locale.getDefault();
        try {
            Locale.setDefault(Locale.JAPAN);
            ResourceBundle.Control control = new TestControl();
            ResourceBundle rb = ResourceBundle.getBundle("ExpirationData", Locale.GERMAN,
                                                         control);
            check(rb.getString("data"), "German");
            rb = ResourceBundle.getBundle("ExpirationData", AUSTRIA, control);
            check(rb.getString("january"), "Januar");
            sleep(7);
            rb = ResourceBundle.getBundle("ExpirationData", Locale.GERMAN, control);
            check(rb.getString("data"), "German");
            rb = ResourceBundle.getBundle("ExpirationData", AUSTRIA, control);
            check(rb.getString("january"), "Januar");
            sleep(33);
            rb = ResourceBundle.getBundle("ExpirationData", Locale.GERMAN, control);
            try {
                check(rb.getString("data"), "Deutsch");
            } catch (RuntimeException e) {
                if (format.equals("class")) {
                    System.out.println("Known class limitation: " + e.getMessage());
                }
            }
            rb = ResourceBundle.getBundle("ExpirationData", AUSTRIA, control);
            try {
                check(rb.getString("january"), "J\u00e4nner");
            } catch (RuntimeException e) {
                if (fileType.equals("jar")) {
                    System.out.println("Known jar limitation: " + e.getMessage());
                } else {
                    throw e;
                }
            }
        } finally {
            Locale.setDefault(loc);
        }
    }
    private static void check(String s, String expected) {
        String time = getTime();
        if (!s.equals(expected)) {
            throw new RuntimeException("got '" + s + "', expected '" + expected + "' at "
                                       + time);
        }
        System.out.println("ExpirationTest: got '" + s + "' at " + time);
    }
    private static void sleep(int seconds) {
        long millis = seconds * 1000;
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
        long end = System.currentTimeMillis();
        long latency = end - start - millis;
        if (latency > millis/100) {
            System.err.printf("Latency is too large: slept for %d [ms], "
                              + "expected %d [ms] latency rate: %+.2f%% (expected not more than 1%%)%n"
                              + "exiting...%n",
                              end - start, millis, (double)latency*100.0/millis);
            System.exit(2);
        }
    }
    private static final String getTime() {
        return new Date().toString().substring(11, 19);
    }
    private static class TestControl extends ResourceBundle.Control {
        @Override
        public long getTimeToLive(String name, Locale loc) {
            return 5000; 
        }
        @Override
        public ResourceBundle newBundle(String name, Locale loc,
                                        String fmt, ClassLoader cl, boolean reload)
            throws IllegalAccessException, InstantiationException, java.io.IOException {
            ResourceBundle bundle = super.newBundle(name, loc, fmt, cl, reload);
            if (bundle != null) {
                System.out.println("newBundle: " + (reload ? "**re" : "")
                                   + "loaded '" + toName(name, loc , fmt) + "' at " + getTime());
            }
            return bundle;
        }
        @Override
        public boolean needsReload(String name, Locale loc,
                                   String fmt, ClassLoader cl,
                                   ResourceBundle rb, long time) {
            boolean b = super.needsReload(name, loc, fmt, cl, rb, time);
            System.out.println("needsReload: '" + b + "' for " + toName(name, loc, fmt)
                               + " at " + getTime());
            return b;
        }
        private String toName(String name, Locale loc, String fmt) {
            return toResourceName(toBundleName(name, loc), fmt.substring(5));
        }
    }
}
