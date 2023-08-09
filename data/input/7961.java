public class GetSystemLoadAverage {
    private static OperatingSystemMXBean mbean =
        ManagementFactory.getOperatingSystemMXBean();
    private static double DELTA = 0.05;
    public static void main(String args[]) throws Exception {
        if (args.length > 1)  {
            throw new IllegalArgumentException("Unexpected number of args " + args.length);
        }
        if (args.length == 0) {
            checkLoadAvg();
        } else {
            if (!args[0].equals("-1.0")) {
                throw new IllegalArgumentException("Invalid argument: " + args[0]);
            } else {
                double loadavg = mbean.getSystemLoadAverage();
                if (loadavg != -1.0) {
                    throw new RuntimeException("Expected load average : -1.0" +
                        " but getSystemLoadAverage returned: " +
                        loadavg);
                }
            }
        }
        System.out.println("Test passed.");
    }
    private static String LOAD_AVERAGE_TEXT = "load average:";
    private static void checkLoadAvg() throws Exception {
        ProcessBuilder pb = new ProcessBuilder("/usr/bin/uptime");
        Process p = pb.start();
        String output = commandOutput(p);
        double loadavg = mbean.getSystemLoadAverage();
        output = output.substring(output.lastIndexOf(LOAD_AVERAGE_TEXT) +
                                  LOAD_AVERAGE_TEXT.length());
        System.out.println("Load average returned from uptime = " + output);
        System.out.println("getSystemLoadAverage() returned " + loadavg);
        String[] lavg = output.split(",");
        double expected = Double.parseDouble(lavg[0]);
        double lowRange = expected * (1 - DELTA);
        double highRange = expected * (1 + DELTA);
        if (loadavg < lowRange || loadavg >  highRange) {
            throw new RuntimeException("Expected load average : " +
                    expected +
                    " but getSystemLoadAverage returned: " +
                    loadavg);
        }
    }
    private static String commandOutput(Reader r) throws Exception {
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = r.read()) > 0) {
            if (c != '\r') {
                sb.append((char) c);
            }
        }
        return sb.toString();
    }
    private static String commandOutput(Process p) throws Exception {
        Reader r = new InputStreamReader(p.getInputStream(),"UTF-8");
        String output = commandOutput(r);
        p.waitFor();
        p.exitValue();
        return output;
    }
}
