public class RMID extends JavaVM {
    public static String MANAGER_OPTION="-Djava.security.manager=";
    private final int port;
    protected static String log = "log";
    protected static String LOGDIR = ".";
    private static void mesg(Object mesg) {
        System.err.println("RMID: " + mesg.toString());
    }
    private static String makeOptions(boolean debugExec) {
        String options = " -Dsun.rmi.server.activation.debugExec=" +
            debugExec;
        if (!TestParams.testSrc.equals("")) {
            options += " -Dtest.src=" + TestParams.testSrc + " ";
        }
        options += " -Dtest.classes=" + TestParams.testClasses 
         +
         " -Djava.rmi.server.logLevel=v ";
        return options;
    }
    private static String makeArgs(boolean includePortArg, int port) {
        String propagateManager = null;
        if (System.getSecurityManager() == null) {
            propagateManager = MANAGER_OPTION +
                TestParams.defaultSecurityManager;
        } else {
            propagateManager = MANAGER_OPTION +
                System.getSecurityManager().getClass().getName();
        }
        String args =
            " -log " + (new File(LOGDIR, log)).getAbsolutePath();
        if (includePortArg) {
            args += " -port " + port;
        }
        if (!TestParams.testSrc.equals("")) {
            args += " -C-Dtest.src=" + TestParams.testSrc;
        }
        if (!TestParams.testClasses.equals("")) {
            args += " -C-Dtest.classes=" + TestParams.testClasses;
        }
        args += " " + getCodeCoverageArgs();
        return args;
    }
    public static RMID createRMID() {
        return createRMID(System.out, System.err, true);
    }
    public static RMID createRMID(boolean debugExec) {
        return createRMID(System.out, System.err, debugExec);
    }
    public static RMID createRMID(OutputStream out, OutputStream err) {
        return createRMID(out, err, true);
    }
    public static RMID createRMID(OutputStream out, OutputStream err,
                                  boolean debugExec)
    {
        return createRMID(out, err, debugExec, true,
                          TestLibrary.RMID_PORT);
    }
    public static RMID createRMID(OutputStream out, OutputStream err,
                                  boolean debugExec, boolean includePortArg,
                                  int port)
    {
        String options = makeOptions(debugExec);
        String args = makeArgs(includePortArg, port);
        RMID rmid = new RMID("sun.rmi.server.Activation", options, args,
                             out, err, port);
        rmid.setPolicyFile(TestParams.defaultRmidPolicy);
        return rmid;
    }
    protected RMID(String classname, String options, String args,
                   OutputStream out, OutputStream err, int port)
    {
        super(classname, options, args, out, err);
        this.port = port;
    }
    public static void removeLog() {
        File f = new File(LOGDIR, log);
        if (f.exists()) {
            mesg("removing rmid's old log file...");
            String[] files = f.list();
            if (files != null) {
                for (int i=0; i<files.length; i++) {
                    (new File(f, files[i])).delete();
                }
            }
            if (f.delete() != true) {
                mesg("\t" + " unable to delete old log file.");
            }
        }
    }
    protected static String getCodeCoverageArgs() {
        return TestLibrary.getExtraProperty("rmid.jcov.args","");
    }
    public void start() throws IOException {
        start(10000);
    }
    public void slowStart() throws IOException {
        start(60000);
    }
    public void start(long waitTime) throws IOException {
        if (getVM() != null) return;
        mesg("starting rmid...");
        super.start();
        int slopFactor = 1;
        try {
            slopFactor = Integer.valueOf(
                TestLibrary.getExtraProperty("jcov.sleep.multiplier","1"));
        } catch (NumberFormatException ignore) {}
        waitTime = waitTime * slopFactor;
        do {
            try {
                Thread.sleep(Math.min(waitTime, 10000));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            waitTime -= 10000;
            if (ActivationLibrary.rmidRunning(port)) {
                mesg("finished starting rmid.");
                return;
            }
        } while (waitTime > 0);
        TestLibrary.bomb("start rmid failed... giving up", null);
    }
    public void restart() throws IOException {
        destroy();
        start();
    }
    public static void shutdown() {
        shutdown(TestLibrary.RMID_PORT);
    }
    public static void shutdown(int port) {
        try {
            ActivationSystem system = null;
            try {
                mesg("getting a reference to the activation system");
                system = (ActivationSystem) Naming.lookup("
                    port +
                    "/java.rmi.activation.ActivationSystem");
                mesg("obtained a reference to the activation system");
            } catch (java.net.MalformedURLException mue) {
            }
            if (system == null) {
                TestLibrary.bomb("reference to the activation system was null");
            }
            system.shutdown();
        } catch (Exception e) {
            mesg("caught exception trying to shutdown rmid");
            mesg(e.getMessage());
            e.printStackTrace();
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        mesg("testlibrary finished shutting down rmid");
    }
    public void destroy() {
        shutdown(port);
        if (vm != null) {
            try {
                try {
                    vm.exitValue();
                    mesg("rmid exited on shutdown request");
                } catch (IllegalThreadStateException illegal) {
                    mesg("Had to destroy RMID's process " +
                         "using Process.destroy()");
                    super.destroy();
                }
            } catch (Exception e) {
                mesg("caught exception trying to destroy rmid: " +
                     e.getMessage());
                e.printStackTrace();
            }
            vm = null;
        }
    }
}
