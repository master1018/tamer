public class InputArgument {
    private static RuntimeMXBean rm = ManagementFactory.getRuntimeMXBean();
    private static String vmOption = null;
    public static void main(String args[]) throws Exception {
        if (args.length > 0) {
            vmOption = args[0];
        }
        List<String> options = rm.getInputArguments();
        if (vmOption == null) {
            return;
        }
        boolean testFailed = true;
        System.out.println("Number of arguments = " + options.size());
        int i = 0;
        for (String arg : options) {
            System.out.println("Input arg " + i + " = " + arg);
            i++;
            if (arg.equals(vmOption)) {
                testFailed = false;
                break;
            }
        }
        if (testFailed) {
            throw new RuntimeException("TEST FAILED: " +
                "VM option " + vmOption + " not found");
        }
        System.out.println("Test passed.");
    }
}
