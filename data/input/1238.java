public class Shell {
    private String shellCommand;
    private String[] shellArgs;
    public void setShellCommand(String shellCommand) {
        this.shellCommand = shellCommand;
    }
    public String getShellCommand() {
        return shellCommand;
    }
    public void setShellArgs(String[] shellArgs) {
        this.shellArgs = shellArgs;
    }
    public String[] getShellArgs() {
        return shellArgs;
    }
    public List getCommandLine(String executable, String[] arguments) {
        List commandLine = new ArrayList();
        try {
            StringBuffer sb = new StringBuffer();
            if (executable != null) {
                sb.append(Commandline.quoteArgument(executable));
            }
            for (int i = 0; i < arguments.length; i++) {
                sb.append(" ");
                sb.append(Commandline.quoteArgument(arguments[i]));
            }
            commandLine.add(sb.toString());
        } catch (CommandLineException e) {
            throw new RuntimeException(e);
        }
        return commandLine;
    }
    public List getShellCommandLine(String executable, String[] arguments) {
        List commandLine = new ArrayList();
        if (getShellCommand() != null) {
            commandLine.add(getShellCommand());
        }
        if (getShellArgs() != null) {
            commandLine.addAll(Arrays.asList(getShellArgs()));
        }
        commandLine.addAll(getCommandLine(executable, arguments));
        return commandLine;
    }
}
