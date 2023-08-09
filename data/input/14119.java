public class AsyncClose {
    public static void main(String args[]) throws Exception {
        AsyncCloseTest tests[] = {
            new Socket_getInputStream_read(),
            new Socket_getInputStream_read(5000),
            new Socket_getOutputStream_write(),
            new DatagramSocket_receive(),
            new DatagramSocket_receive(5000),
            new ServerSocket_accept(),
            new ServerSocket_accept(5000),
        };
        int failures = 0;
        for (int i=0; i<tests.length; i++) {
            AsyncCloseTest tst = tests[i];
            System.out.println("******************************");
            System.out.println("Test: " + tst.description());
            if (tst.go()) {
                System.out.println("Passed.");
            } else {
                System.out.println("Failed: " + tst.failureReason());
                failures++;
            }
            System.out.println("");
        }
        if (failures > 0) {
            throw new Exception(failures + " sub-tests failed - see log.");
        }
    }
}
