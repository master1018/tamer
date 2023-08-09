public class GetInitializedClassCount {
    private static HotspotClassLoadingMBean mbean =
        (HotspotClassLoadingMBean)ManagementFactoryHelper.getHotspotClassLoadingMBean();
    private static final long MIN_VALUE_FOR_PASS = 1;
    private static final long MAX_VALUE_FOR_PASS = Long.MAX_VALUE;
    private static boolean trace = false;
    public static void main(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("trace")) {
            trace = true;
        }
        long value = mbean.getInitializedClassCount();
        if (trace) {
            System.out.println("Initialized class count: " + value);
        }
        if (value < MIN_VALUE_FOR_PASS || value > MAX_VALUE_FOR_PASS) {
            throw new RuntimeException("Initialized class count " +
                                       "illegal value: " + value + " " +
                                       "(MIN = " + MIN_VALUE_FOR_PASS + "; " +
                                       "MAX = " + MAX_VALUE_FOR_PASS + ")");
        }
        Class.forName("ClassToInitialize0");
        long value2 = mbean.getInitializedClassCount();
        if (trace) {
            System.out.println("Initialized class count2: " + value2);
        }
        if (value2 < value + 1) {
            throw new RuntimeException("Initialized class count " +
                                       "did not increase " +
                                       "(value = " + value + "; " +
                                       "value2 = " + value2 + ")");
        }
        System.out.println("Test passed.");
    }
}
class ClassToInitialize0 {
    static int i = 0;
    static {
        i = 1;
    }
}
