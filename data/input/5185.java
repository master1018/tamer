class PingException extends Exception {
}
public class ServerStackTrace implements Ping {
    public void ping() throws PingException {
        __BAR__();
    }
    private void __BAR__() throws PingException {
        throw new PingException();
    }
    private static void __FOO__(Ping stub)
        throws PingException, RemoteException
    {
        stub.ping();
    }
    public static void main(String[] args) throws Exception {
        System.err.println("\nRegression test for RFE 4010355\n");
        ServerStackTrace impl = new ServerStackTrace();
        try {
            Ping stub = (Ping) UnicastRemoteObject.exportObject(impl);
            StackTraceElement[] remoteTrace;
            try {
                __FOO__(stub);
                throw new RuntimeException("TEST FAILED: no exception caught");
            } catch (PingException e) {
                System.err.println(
                    "trace of exception thrown by remote call:");
                e.printStackTrace();
                System.err.println();
                remoteTrace = e.getStackTrace();
            }
            int fooIndex = -1;
            int barIndex = -1;
            for (int i = 0; i < remoteTrace.length; i++) {
                StackTraceElement e = remoteTrace[i];
                if (e.getMethodName().equals("__FOO__")) {
                    if (fooIndex != -1) {
                        throw new RuntimeException("TEST FAILED: " +
                            "trace contains more than one __FOO__");
                    }
                    fooIndex = i;
                } else if (e.getMethodName().equals("__BAR__")) {
                    if (barIndex != -1) {
                        throw new RuntimeException("TEST FAILED: " +
                            "trace contains more than one __BAR__");
                    }
                    barIndex = i;
                }
            }
            if (fooIndex == -1) {
                throw new RuntimeException(
                   "TEST FAILED: trace lacks client-side method __FOO__");
            }
            if (barIndex == -1) {
                throw new RuntimeException(
                   "TEST FAILED: trace lacks server-side method __BAR__");
            }
            if (fooIndex < barIndex) {
                throw new RuntimeException(
                   "TEST FAILED: trace contains client-side method __FOO__ " +
                   "before server-side method __BAR__");
            }
            System.err.println("TEST PASSED");
        } finally {
            try {
                UnicastRemoteObject.unexportObject(impl, true);
            } catch (Exception e) {
            }
        }
    }
}
