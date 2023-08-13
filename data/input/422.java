public class GetMethodDataSize {
    private static HotspotClassLoadingMBean mbean =
        (HotspotClassLoadingMBean)ManagementFactoryHelper.getHotspotClassLoadingMBean();
    private static final long MIN_VALUE_FOR_PASS = 1;
    private static final long MAX_VALUE_FOR_PASS = Long.MAX_VALUE;
    private static boolean trace = false;
    public static void main(String args[]) throws Exception {
        if (args.length > 0 && args[0].equals("trace")) {
            trace = true;
        }
        long value = mbean.getMethodDataSize();
        if (trace) {
            System.out.println("Method data size (bytes): " + value);
        }
        if (value < MIN_VALUE_FOR_PASS || value > MAX_VALUE_FOR_PASS) {
            throw new RuntimeException("Method data size " +
                                       "illegal value: " + value + " bytes " +
                                       "(MIN = " + MIN_VALUE_FOR_PASS + "; " +
                                       "MAX = " + MAX_VALUE_FOR_PASS + ")");
        }
        Class.forName("ClassToLoad2");
        long value2 = mbean.getMethodDataSize();
        if (trace) {
            System.out.println("Method data size2 (bytes): " + value2);
        }
        if (value2 <= value) {
            throw new RuntimeException("Method data size " +
                                       "did not increase " +
                                       "(value = " + value + "; " +
                                       "value2 = " + value2 + ")");
        }
        System.out.println("Test passed.");
    }
}
class ClassToLoad2 {
    int i = 0;
    public void method1() {
        i = 1;
    }
}
