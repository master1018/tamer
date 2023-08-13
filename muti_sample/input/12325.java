public class GetClassInitializationTime {
    private static HotspotClassLoadingMBean mbean =
        (HotspotClassLoadingMBean)ManagementFactoryHelper.getHotspotClassLoadingMBean();
    private static final long MIN_TIME_FOR_PASS = 1;
    private static final long MAX_TIME_FOR_PASS = Long.MAX_VALUE;
    private static boolean trace = false;
    public static void main(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("trace")) {
            trace = true;
        }
        long time = mbean.getClassInitializationTime();
        if (trace) {
            System.out.println("Class initialization time (ms): " + time);
        }
        if (time < MIN_TIME_FOR_PASS || time > MAX_TIME_FOR_PASS) {
            throw new RuntimeException("Class initialization time " +
                                       "illegal value: " + time + " ms " +
                                       "(MIN = " + MIN_TIME_FOR_PASS + "; " +
                                       "MAX = " + MAX_TIME_FOR_PASS + ")");
        }
        Class.forName("ClassToInitialize");
        long time2 = mbean.getClassInitializationTime();
        if (trace) {
            System.out.println("Class initialization time2 (ms): " + time2);
        }
        if (time2 <= time) {
            throw new RuntimeException("Class initialization time " +
                                       "did not increase when class loaded " +
                                       "(time = " + time + "; " +
                                       "time2 = " + time2 + ")");
        }
        System.out.println("Test passed.");
    }
}
class ClassToInitialize {
    static {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
        }
    }
}
