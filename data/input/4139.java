public class CheckLogging {
    private static final String LOCATION =
        "rmi:
    private static final ByteArrayOutputStream clientCallOut =
        new ByteArrayOutputStream();
    private static final boolean usingOld =
        Boolean.getBoolean("sun.rmi.log.useOld");
    static {
        System.setProperty("sun.rmi.client.logCalls", "true");
        if (usingOld) {
            System.err.println("set default stream");
            LogStream.setDefaultStream(new PrintStream(clientCallOut));
        } else {
            Logger.getLogger("sun.rmi.client.call").
                addHandler(new InternalStreamHandler(clientCallOut));
        }
    }
    private static Registry registry;
    static {
        try {
            registry = LocateRegistry.createRegistry(TestLibrary.REGISTRY_PORT);
        } catch (Exception e) {
            TestLibrary.bomb("could not create registry");
        }
    }
    private static class InternalStreamHandler extends StreamHandler {
        private InternalStreamHandler(OutputStream out) {
            super(out, new SimpleFormatter());
            setLevel(Level.ALL);
        }
        public void publish(LogRecord record) {
            super.publish(record);
            flush();
        }
        public void close() {
            flush();
        }
    }
    private static void verifyLog(ByteArrayOutputStream bout,
                                  String mustContain)
    {
        byte[] bytes = bout.toByteArray();
        if (bytes.length == 0) {
            TestLibrary.bomb("log data length is zero");
        } else if ((mustContain != null) &&
                   (bout.toString().indexOf(mustContain) < 0))
        {
            TestLibrary.bomb("log output did not contain: " + mustContain);
        }
    }
    private static void checkServerCallLog() throws Exception {
        ByteArrayOutputStream serverCallLog = new ByteArrayOutputStream();
        RemoteServer.setLog(serverCallLog);
        Naming.list(LOCATION);
        verifyLog(serverCallLog, "list");
        serverCallLog.reset();
        RemoteServer.setLog(null);
        PrintStream callStream = RemoteServer.getLog();
        if (callStream != null) {
            TestLibrary.bomb("call stream not null after calling " +
                             "setLog(null)");
        } else {
            System.err.println("call stream should be null and it is");
        }
        Naming.list(LOCATION);
        if (usingOld) {
            if (serverCallLog.toString().indexOf("UnicastServerRef") >= 0) {
                TestLibrary.bomb("server call logging not turned off");
            }
        } else if (serverCallLog.toByteArray().length != 0) {
            TestLibrary.bomb("call log contains output but it " +
                             "should be empty");
        }
        serverCallLog.reset();
        RemoteServer.setLog(serverCallLog);
        try {
            Naming.lookup(LOCATION + "notthere");
        } catch (Exception e) {
        }
        verifyLog(serverCallLog, "exception");
        serverCallLog.reset();
        RemoteServer.setLog(serverCallLog);
        callStream = RemoteServer.getLog();
        callStream.println("bingo, this is a getLog test");
        verifyLog(serverCallLog, "bingo");
    }
    private static void checkPermissions() {
        SecurityException ex = null;
        try {
            RemoteServer.setLog(System.err);
        } catch (SecurityException e) {
            System.err.println("security excepton caught correctly");
            ex = e;
        }
        if (ex == null) {
            TestLibrary.bomb("able to set log without permission");
        }
    }
    public static void main(String[] args) {
        try {
            checkServerCallLog();
            if (!usingOld) {
                verifyLog(clientCallOut, "outbound call");
                System.setSecurityManager(new java.lang.SecurityManager());
                checkPermissions();
            }
            System.err.println("TEST PASSED");
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            TestLibrary.bomb("unexpected exception", e);
        } finally {
            TestLibrary.unexport(registry);
        }
    }
}
