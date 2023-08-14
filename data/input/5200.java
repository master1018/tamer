public class InvalidThreadID {
    public static void main(String argv[]) {
        ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
        int cnt = 0;
        long [] idArr = {0, -1, -2, (Long.MIN_VALUE + 1), Long.MIN_VALUE};
        if (mbean.isThreadCpuTimeSupported()) {
            for (int i = 0; i < idArr.length; i++) {
                try {
                    mbean.getThreadCpuTime(idArr[i]);
                    System.out.println("Test failed. IllegalArgumentException" +
                        " expected for ID = " + idArr[i]);
                } catch (IllegalArgumentException iae) {
                    cnt++;
                }
            }
            if (cnt != idArr.length) {
                throw new RuntimeException("Unexpected number of " +
                    "IllegalArgumentException = " + cnt +
                    " expected = " + idArr.length);
            }
            long time = mbean.getThreadCpuTime(999999);
            if (time < 0 && time != -1) {
                throw new RuntimeException("Cpu time for thread 999999" +
                    " is invalid = " + time + " expected to be -1.");
            }
        }
        System.out.println("Test passed.");
    }
}
