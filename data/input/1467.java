public class RMIIIOPServerImpl extends RMIServerImpl {
    public RMIIIOPServerImpl(Map<String,?> env)
            throws IOException {
        super(env);
        this.env = (env == null) ? Collections.<String, Object>emptyMap() : env;
        callerACC = AccessController.getContext();
    }
    protected void export() throws IOException {
        IIOPHelper.exportObject(this);
    }
    protected String getProtocol() {
        return "iiop";
    }
    public Remote toStub() throws IOException {
        final Remote stub = IIOPHelper.toStub(this);
        return stub;
    }
    protected RMIConnection makeClient(String connectionId, Subject subject)
            throws IOException {
        if (connectionId == null)
            throw new NullPointerException("Null connectionId");
        RMIConnection client =
            new RMIConnectionImpl(this, connectionId, getDefaultClassLoader(),
                                  subject, env);
        IIOPHelper.exportObject(client);
        return client;
    }
    protected void closeClient(RMIConnection client) throws IOException {
        IIOPHelper.unexportObject(client);
    }
    protected void closeServer() throws IOException {
        IIOPHelper.unexportObject(this);
    }
    @Override
    RMIConnection doNewClient(final Object credentials) throws IOException {
        if (callerACC == null) {
            throw new SecurityException("AccessControlContext cannot be null");
        }
        try {
            return AccessController.doPrivileged(
                new PrivilegedExceptionAction<RMIConnection>() {
                    public RMIConnection run() throws IOException {
                        return superDoNewClient(credentials);
                    }
            }, callerACC);
        } catch (PrivilegedActionException pae) {
            throw (IOException) pae.getCause();
        }
    }
    RMIConnection superDoNewClient(Object credentials) throws IOException {
        return super.doNewClient(credentials);
    }
    private final Map<String, ?> env;
    private final AccessControlContext callerACC;
}
