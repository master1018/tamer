public class JavaVM {
    protected Process vm = null;
    private String classname = "";
    private String args = "";
    private String options = "";
    private OutputStream outputStream = System.out;
    private OutputStream errorStream = System.err;
    private String policyFileName = null;
    private static void mesg(Object mesg) {
        System.err.println("JAVAVM: " + mesg.toString());
    }
    private static String javaProgram = "java";
    static {
        try {
            javaProgram = TestLibrary.getProperty("java.home", "") +
                File.separator + "bin" + File.separator + javaProgram;
        } catch (SecurityException se) {
        }
    }
    public JavaVM(String classname) {
        this.classname = classname;
    }
    public JavaVM(String classname,
                  String options, String args) {
        this.classname = classname;
        this.options = options;
        this.args = args;
    }
    public JavaVM(String classname,
                  String options, String args,
                  OutputStream out, OutputStream err) {
        this(classname, options, args);
        this.outputStream = out;
        this.errorStream = err;
    }
    public void addOptions(String[] opts) {
        String newOpts = "";
        for (int i = 0 ; i < opts.length ; i ++) {
            newOpts += " " + opts[i];
        }
        newOpts += " ";
        options = newOpts + options;
    }
    public void addArguments(String[] arguments) {
        String newArgs = "";
        for (int i = 0 ; i < arguments.length ; i ++) {
            newArgs += " " + arguments[i];
        }
        newArgs += " ";
        args = newArgs + args;
    }
    public void setPolicyFile(String policyFileName) {
        this.policyFileName = policyFileName;
    }
    protected static String getCodeCoverageOptions() {
        return TestLibrary.getExtraProperty("jcov.options","");
    }
    public void start() throws IOException {
        if (vm != null) return;
        if (policyFileName != null) {
            String option = "-Djava.security.policy=" + policyFileName;
            addOptions(new String[] { option });
        }
        addOptions(new String[] { getCodeCoverageOptions() });
        StringTokenizer optionsTokenizer = new StringTokenizer(options);
        StringTokenizer argsTokenizer = new StringTokenizer(args);
        int optionsCount = optionsTokenizer.countTokens();
        int argsCount = argsTokenizer.countTokens();
        String javaCommand[] = new String[optionsCount + argsCount + 2];
        int count = 0;
        javaCommand[count++] = JavaVM.javaProgram;
        while (optionsTokenizer.hasMoreTokens()) {
            javaCommand[count++] = optionsTokenizer.nextToken();
        }
        javaCommand[count++] = classname;
        while (argsTokenizer.hasMoreTokens()) {
            javaCommand[count++] = argsTokenizer.nextToken();
        }
        mesg("command = " + Arrays.asList(javaCommand).toString());
        System.err.println("");
        vm = Runtime.getRuntime().exec(javaCommand);
        StreamPipe.plugTogether(vm.getInputStream(), this.outputStream);
        StreamPipe.plugTogether(vm.getErrorStream(), this.errorStream);
        try {
            Thread.sleep(2000);
        } catch (Exception ignore) {
        }
        mesg("finished starting vm.");
    }
    public void destroy() {
        if (vm != null) {
            vm.destroy();
        }
        vm = null;
    }
    protected Process getVM() {
        return vm;
    }
}
