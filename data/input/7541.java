public class ProcessCommunicator {
    private static final String javaHome = System.getProperty("java.home", "");
    private static final String javaPath = javaHome + File.separator + "bin" +
            File.separator + "java ";
    private static String command = "";
    private ProcessCommunicator() {}
    public static ProcessResults executeChildProcess(final Class classToExecute,
                           final String classPathArguments, final String [] args)
    {
        try {
            String command = buildCommand(classToExecute, classPathArguments, args);
            Process process = Runtime.getRuntime().exec(command);
            return doWaitFor(process);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static ProcessResults executeChildProcess(final Class classToExecute,
                           final String [] args)
    {
        return executeChildProcess(classToExecute, " ", args);
    }
    private static ProcessResults doWaitFor(final Process p) {
        ProcessResults pres = new ProcessResults();
        final InputStream in;
        final InputStream err;
        try {
            in = p.getInputStream();
            err = p.getErrorStream();
            boolean finished = false;
            while (!finished) {
                try {
                    while (in.available() > 0) {
                        pres.appendToStdOut((char)in.read());
                    }
                    while (err.available() > 0) {
                        pres.appendToStdErr((char)err.read());
                    }
                    pres.setExitValue(p.exitValue());
                    finished  = true;
                }
                catch (IllegalThreadStateException e) {
                    Thread.currentThread().sleep(500);
                }
            }
            if (in != null) in.close();
            if (err != null) err.close();
        }
        catch (Throwable e) {
            System.err.println("doWaitFor(): unexpected exception");
            e.printStackTrace();
        }
        return pres;
    }
    private static String buildCommand(final Class classToExecute,
                         final String classPathArguments, final String [] args)
    {
        StringBuilder commandBuilder = new StringBuilder();
        commandBuilder.append(javaPath).append(" ");
        commandBuilder.append("-cp ").append(System.getProperty("test.classes", ".")).append(File.pathSeparatorChar);
        if (classPathArguments.trim().length() > 0) {
            commandBuilder.append(classPathArguments).append(" ");
        }
        commandBuilder.append(" ");
        commandBuilder.append(classToExecute.getName());
        for (String argument:args) {
            commandBuilder.append(" ").append(argument);
        }
        command = commandBuilder.toString();
        return command;
    }
    public static String getExecutionCommand () {
        return command;
    }
}
