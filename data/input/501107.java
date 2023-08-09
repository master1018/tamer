public class ProcessBuilderTest extends junit.framework.TestCase {
    private static String shell() {
        return "Dalvik".equals(System.getProperty("java.vm.name")) ? "/system/bin/sh" : "/bin/sh";
    }
    public void testRedirectErrorStream(boolean doRedirect,
            String expectedOut, String expectedErr) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(shell(), "-c", "echo out; echo err 1>&2");
        pb.redirectErrorStream(doRedirect);
        execAndCheckOutput(pb, expectedOut, expectedErr);
    }
    public void test_redirectErrorStream_true() throws Exception {
        testRedirectErrorStream(true, "out\nerr\n", "");
    }
    public void test_redirectErrorStream_false() throws Exception {
        testRedirectErrorStream(false, "out\n", "err\n");
    }
}
