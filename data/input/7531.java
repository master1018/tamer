public class JavaProcess {
    protected Process process = null;
    private String classname;
    private StringBuilder classArgs;
    private StringBuilder javaOptions;
    private static String java = System.getProperty("java.home")
                                 + File.separator + "bin"
                                 + File.separator + "java";
    public JavaProcess(String classname) {
        this(classname, "", "");
    }
    public JavaProcess(String classname, String classArgs) {
        this(classname, "", classArgs);
    }
    public JavaProcess(String classname, String javaOptions, String classArgs) {
        this.classname = classname;
        this.javaOptions = new StringBuilder(javaOptions);
        this.classArgs = new StringBuilder(classArgs);
    }
    public void addOptions(String[] opts) {
        if (javaOptions != null && javaOptions.length() > 0) {
            javaOptions.append(" ");
        }
        for (int i = 0; i < opts.length; i++) {
            if (i != 0) {
                javaOptions.append(" ");
            }
            javaOptions.append(opts[i]);
        }
    }
    public void addArguments(String[] args) {
        if (classArgs != null && classArgs.length() > 0) {
            classArgs.append(" ");
        }
        for (int i = 0; i < args.length; i++) {
            if (i != 0) {
                classArgs.append(" ");
            }
            classArgs.append(args[i]);
        }
    }
    public void start() throws IOException {
        if (process != null) {
            return;
        }
        String javaCommand = java + " " + javaOptions + " "
                             + classname + " " + classArgs;
        System.out.println("exec'ing: " + javaCommand);
        process = Runtime.getRuntime().exec(javaCommand);
    }
    public void destroy() {
        if (process != null) {
            process.destroy();
        }
        process = null;
    }
    public int exitValue() {
        if (process != null) {
            return process.exitValue();
        }
        throw new RuntimeException("exitValue called with process == null");
    }
    public InputStream getErrorStream() {
        if (process != null) {
            return process.getErrorStream();
        }
        throw new RuntimeException(
                "getErrorStream() called with process == null");
    }
    public InputStream getInputStream() {
        if (process != null) {
            return process.getInputStream();
        }
        throw new RuntimeException(
                "getInputStream() called with process == null");
    }
    public OutputStream getOutputStream() {
        if (process != null) {
            return process.getOutputStream();
        }
        throw new RuntimeException(
                "getOutputStream() called with process == null");
    }
    public int waitFor() throws InterruptedException {
        if (process != null) {
            return process.waitFor();
        }
        throw new RuntimeException("waitFor() called with process == null");
    }
}
