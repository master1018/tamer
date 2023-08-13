public class CLHSDB {
    public static void main(String[] args) {
        new CLHSDB(args).run();
    }
    private void run() {
        agent = new HotSpotAgent();
        Runtime.getRuntime().addShutdownHook(new java.lang.Thread() {
                public void run() {
                    detachDebugger();
                }
            });
        if (pidText != null) {
            attachDebugger(pidText);
        } else if (execPath != null) {
            attachDebugger(execPath, coreFilename);
        }
        CommandProcessor.DebuggerInterface di = new CommandProcessor.DebuggerInterface() {
                public HotSpotAgent getAgent() {
                    return agent;
                }
                public boolean isAttached() {
                    return attached;
                }
                public void attach(String pid) {
                    attachDebugger(pid);
                }
                public void attach(String java, String core) {
                    attachDebugger(java, core);
                }
                public void detach() {
                    detachDebugger();
                }
                public void reattach() {
                    if (attached) {
                        detachDebugger();
                    }
                    if (pidText != null) {
                        attach(pidText);
                    } else {
                        attach(execPath, coreFilename);
                    }
                }
            };
        BufferedReader in =
            new BufferedReader(new InputStreamReader(System.in));
        CommandProcessor cp = new CommandProcessor(di, in, System.out, System.err);
        cp.run(true);
    }
    private HotSpotAgent agent;
    private boolean      attached;
    private String pidText;
    private int pid;
    private String execPath;
    private String coreFilename;
    private void doUsage() {
        System.out.println("Usage:  java CLHSDB [[pid] | [path-to-java-executable [path-to-corefile]] | help ]");
        System.out.println("           pid:                     attach to the process whose id is 'pid'");
        System.out.println("           path-to-java-executable: Debug a core file produced by this program");
        System.out.println("           path-to-corefile:        Debug this corefile.  The default is 'core'");
        System.out.println("        If no arguments are specified, you can select what to do from the GUI.\n");
        HotSpotAgent.showUsage();
    }
    private CLHSDB(String[] args) {
        switch (args.length) {
        case (0):
            break;
        case (1):
            if (args[0].equals("help") || args[0].equals("-help")) {
                doUsage();
                System.exit(0);
            }
            try {
                int unused = Integer.parseInt(args[0]);
                pidText = args[0];
            } catch (NumberFormatException e) {
                execPath = args[0];
                coreFilename = "core";
            }
            break;
        case (2):
            execPath = args[0];
            coreFilename = args[1];
            break;
        default:
            System.out.println("HSDB Error: Too many options specified");
            doUsage();
            System.exit(1);
        }
    }
    private void attachDebugger(String pidText) {
        try {
            this.pidText = pidText;
            pid = Integer.parseInt(pidText);
        }
        catch (NumberFormatException e) {
            System.err.print("Unable to parse process ID \"" + pidText + "\".\nPlease enter a number.");
        }
        try {
            System.err.println("Attaching to process " + pid + ", please wait...");
            agent.attach(pid);
            attached = true;
        }
        catch (DebuggerException e) {
            final String errMsg = formatMessage(e.getMessage(), 80);
            System.err.println("Unable to connect to process ID " + pid + ":\n\n" + errMsg);
            agent.detach();
            return;
        }
    }
    private void attachDebugger(final String executablePath, final String corePath) {
        try {
            System.err.println("Opening core file, please wait...");
            agent.attach(executablePath, corePath);
            attached = true;
        }
        catch (DebuggerException e) {
            final String errMsg = formatMessage(e.getMessage(), 80);
            System.err.println("Unable to open core file\n" + corePath + ":\n\n" + errMsg);
            agent.detach();
            return;
        }
    }
    private void connect(final String remoteMachineName) {
        try {
            System.err.println("Connecting to debug server, please wait...");
            agent.attach(remoteMachineName);
            attached = true;
        }
        catch (DebuggerException e) {
            final String errMsg = formatMessage(e.getMessage(), 80);
            System.err.println("Unable to connect to machine \"" + remoteMachineName + "\":\n\n" + errMsg);
            agent.detach();
            return;
        }
    }
    private void detachDebugger() {
        if (!attached) {
            return;
        }
        agent.detach();
        attached = false;
    }
    private void detach() {
        detachDebugger();
    }
    private String formatMessage(String message, int charsPerLine) {
        StringBuffer buf = new StringBuffer(message.length());
        StringTokenizer tokenizer = new StringTokenizer(message);
        int curLineLength = 0;
        while (tokenizer.hasMoreTokens()) {
            String tok = tokenizer.nextToken();
            if (curLineLength + tok.length() > charsPerLine) {
                buf.append('\n');
                curLineLength = 0;
            } else {
                if (curLineLength != 0) {
                    buf.append(' ');
                    ++curLineLength;
                }
            }
            buf.append(tok);
            curLineLength += tok.length();
        }
        return buf.toString();
    }
}
