public abstract class HotSpotVirtualMachine extends VirtualMachine {
    HotSpotVirtualMachine(AttachProvider provider, String id) {
        super(provider, id);
    }
    private void loadAgentLibrary(String agentLibrary, boolean isAbsolute, String options)
        throws AgentLoadException, AgentInitializationException, IOException
    {
        InputStream in = execute("load",
                                 agentLibrary,
                                 isAbsolute ? "true" : "false",
                                 options);
        try {
            int result = readInt(in);
            if (result != 0) {
                throw new AgentInitializationException("Agent_OnAttach failed", result);
            }
        } finally {
            in.close();
        }
    }
    public void loadAgentLibrary(String agentLibrary, String options)
        throws AgentLoadException, AgentInitializationException, IOException
    {
        loadAgentLibrary(agentLibrary, false, options);
    }
    public void loadAgentPath(String agentLibrary, String options)
        throws AgentLoadException, AgentInitializationException, IOException
    {
        loadAgentLibrary(agentLibrary, true, options);
    }
    public void loadAgent(String agent, String options)
        throws AgentLoadException, AgentInitializationException, IOException
    {
        String args = agent;
        if (options != null) {
            args = args + "=" + options;
        }
        try {
            loadAgentLibrary("instrument", args);
        } catch (AgentLoadException x) {
            throw new InternalError("instrument library is missing in target VM");
        } catch (AgentInitializationException x) {
            int rc = x.returnValue();
            switch (rc) {
                case JNI_ENOMEM:
                    throw new AgentLoadException("Insuffient memory");
                case ATTACH_ERROR_BADJAR:
                    throw new AgentLoadException("Agent JAR not found or no Agent-Class attribute");
                case ATTACH_ERROR_NOTONCP:
                    throw new AgentLoadException("Unable to add JAR file to system class path");
                case ATTACH_ERROR_STARTFAIL:
                    throw new AgentInitializationException("Agent JAR loaded but agent failed to initialize");
                default :
                    throw new AgentLoadException("Failed to load agent - unknown reason: " + rc);
            }
        }
    }
    private static final int JNI_ENOMEM                 = -4;
    private static final int ATTACH_ERROR_BADJAR        = 100;
    private static final int ATTACH_ERROR_NOTONCP       = 101;
    private static final int ATTACH_ERROR_STARTFAIL     = 102;
    public Properties getSystemProperties() throws IOException {
        InputStream in = null;
        Properties props = new Properties();
        try {
            in = executeCommand("properties");
            props.load(in);
        } finally {
            if (in != null) in.close();
        }
        return props;
    }
    public Properties getAgentProperties() throws IOException {
        InputStream in = null;
        Properties props = new Properties();
        try {
            in = executeCommand("agentProperties");
            props.load(in);
        } finally {
            if (in != null) in.close();
        }
        return props;
    }
    public void localDataDump() throws IOException {
        executeCommand("datadump").close();
    }
    public InputStream remoteDataDump(Object ... args) throws IOException {
        return executeCommand("threaddump", args);
    }
    public InputStream dumpHeap(Object ... args) throws IOException {
        return executeCommand("dumpheap", args);
    }
    public InputStream heapHisto(Object ... args) throws IOException {
        return executeCommand("inspectheap", args);
    }
    public InputStream setFlag(String name, String value) throws IOException {
        return executeCommand("setflag", name, value);
    }
    public InputStream printFlag(String name) throws IOException {
        return executeCommand("printflag", name);
    }
    abstract InputStream execute(String cmd, Object ... args)
        throws AgentLoadException, IOException;
    private InputStream executeCommand(String cmd, Object ... args) throws IOException {
        try {
            return execute(cmd, args);
        } catch (AgentLoadException x) {
            throw new InternalError("Should not get here");
        }
    }
    int readInt(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        int n;
        byte buf[] = new byte[1];
        do {
            n = in.read(buf, 0, 1);
            if (n > 0) {
                char c = (char)buf[0];
                if (c == '\n') {
                    break;                  
                } else {
                    sb.append(c);
                }
            }
        } while (n > 0);
        if (sb.length() == 0) {
            throw new IOException("Premature EOF");
        }
        int value;
        try {
            value = Integer.parseInt(sb.toString());
        } catch (NumberFormatException x) {
            throw new IOException("Non-numeric value found - int expected");
        }
        return value;
    }
    private static long defaultAttachTimeout = 5000;
    private volatile long attachTimeout;
    long attachTimeout() {
        if (attachTimeout == 0) {
            synchronized(this) {
                if (attachTimeout == 0) {
                    try {
                        String s =
                            System.getProperty("sun.tools.attach.attachTimeout");
                        attachTimeout = Long.parseLong(s);
                    } catch (SecurityException se) {
                    } catch (NumberFormatException ne) {
                    }
                    if (attachTimeout <= 0) {
                       attachTimeout = defaultAttachTimeout;
                    }
                }
            }
        }
        return attachTimeout;
    }
}
