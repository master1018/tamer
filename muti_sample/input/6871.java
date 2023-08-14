public class NativeMethodPrefixApp implements StringIdCallback {
    static String goldenNativeMethodName = "getStartupTime";
    static boolean gotIt[] = {false, false, false};
    public static void main(String args[]) throws Exception {
        (new NativeMethodPrefixApp()).run(args, System.err);
    }
    public void run(String args[], PrintStream out) throws Exception {
        StringIdCallbackReporter.registerCallback(this);
        System.err.println("start");
        java.lang.reflect.Array.getLength(new short[5]);
        RuntimeMXBean mxbean = ManagementFactory.getRuntimeMXBean();
        System.err.println(mxbean.getVmVendor());
        for (int i = 0; i < gotIt.length; ++i) {
            if (!gotIt[i]) {
                throw new Exception("ERROR: Missing callback for transform " + i);
            }
        }
    }
    public void tracker(String name, int id) {
        if (name.endsWith(goldenNativeMethodName)) {
            System.err.println("Tracked #" + id + ": MATCHED -- " + name);
            gotIt[id] = true;
        } else {
            System.err.println("Tracked #" + id + ": " + name);
        }
    }
}
