class PongException extends Exception {
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        StackTraceElement[] trace = getStackTrace();
        if (trace.length > 0) {
            throw new RuntimeException(
                "TEST FAILED: exception contained non-empty stack trace: " +
                Arrays.asList(trace));
        }
    }
}
class Impl2 implements Pong {
    public void pong() throws PongException { __BAR__(); }
    public void __BAR__() throws PongException { throw new PongException(); }
}
class Impl1 implements Pong {
    public void pong() throws PongException { __BAR__(); }
    public void __BAR__() throws PongException { throw new PongException(); }
}
public class SuppressStackTraces {
    private static void __FOO__(Pong stub)
        throws PongException, RemoteException
    {
        stub.pong();
    }
    public static void main(String[] args) throws Exception {
        System.err.println("\nRegression test for RFE 4487532\n");
        System.setProperty("sun.rmi.server.suppressStackTraces", "true");
        Remote impl2 = new Impl2();
        Remote impl1 = new Impl1();
        try {
            verifySuppression((Pong) UnicastRemoteObject.exportObject(impl2));
            verifySuppression((Pong) UnicastRemoteObject.exportObject(impl1));
            System.err.println(
                "TEST PASSED (server-side stack trace data suppressed)");
        } finally {
            try {
                UnicastRemoteObject.unexportObject(impl1, true);
            } catch (Exception e) {
            }
            try {
                UnicastRemoteObject.unexportObject(impl2, true);
            } catch (Exception e) {
            }
        }
    }
    private static void verifySuppression(Pong stub) throws Exception {
        System.err.println("testing stub for exported object: " + stub);
        StackTraceElement[] remoteTrace;
        try {
            __FOO__(stub);
            throw new RuntimeException("TEST FAILED: no exception caught");
        } catch (PongException e) {
            System.err.println(
                "trace of exception thrown by remote call:");
            e.printStackTrace();
            System.err.println();
            remoteTrace = e.getStackTrace();
        }
        int fooIndex = -1;
        for (int i = 0; i < remoteTrace.length; i++) {
            StackTraceElement e = remoteTrace[i];
            if (e.getMethodName().equals("__FOO__")) {
                if (fooIndex != -1) {
                    throw new RuntimeException(
                        "TEST FAILED: trace contains more than one __FOO__");
                }
                fooIndex = i;
            } else if (e.getMethodName().equals("__BAR__")) {
                throw new RuntimeException(
                    "TEST FAILED: trace contains __BAR__");
            }
        }
        if (fooIndex == -1) {
            throw new RuntimeException(
                "TEST FAILED: trace lacks client-side method __FOO__");
        }
    }
}
