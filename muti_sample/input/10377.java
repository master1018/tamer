public class RMIJRMPServerImpl extends RMIServerImpl {
    public RMIJRMPServerImpl(int port,
                             RMIClientSocketFactory csf,
                             RMIServerSocketFactory ssf,
                             Map<String,?> env)
            throws IOException {
        super(env);
        if (port < 0)
            throw new IllegalArgumentException("Negative port: " + port);
        this.port = port;
        this.csf = csf;
        this.ssf = ssf;
        this.env = (env == null) ? Collections.<String, Object>emptyMap() : env;
    }
    protected void export() throws IOException {
        export(this);
    }
    private void export(Remote obj) throws RemoteException {
        final RMIExporter exporter =
            (RMIExporter) env.get(RMIExporter.EXPORTER_ATTRIBUTE);
        final boolean daemon = EnvHelp.isServerDaemon(env);
        if (daemon && exporter != null) {
            throw new IllegalArgumentException("If "+EnvHelp.JMX_SERVER_DAEMON+
                    " is specified as true, "+RMIExporter.EXPORTER_ATTRIBUTE+
                    " cannot be used to specify an exporter!");
        }
        if (daemon) {
            if (csf == null && ssf == null) {
                new UnicastServerRef(port).exportObject(obj, null, true);
            } else {
                new UnicastServerRef2(port, csf, ssf).exportObject(obj, null, true);
            }
        } else if (exporter != null) {
            exporter.exportObject(obj, port, csf, ssf);
        } else {
            UnicastRemoteObject.exportObject(obj, port, csf, ssf);
        }
    }
    private void unexport(Remote obj, boolean force)
            throws NoSuchObjectException {
        RMIExporter exporter =
            (RMIExporter) env.get(RMIExporter.EXPORTER_ATTRIBUTE);
        if (exporter == null)
            UnicastRemoteObject.unexportObject(obj, force);
        else
            exporter.unexportObject(obj, force);
    }
    protected String getProtocol() {
        return "rmi";
    }
    public Remote toStub() throws IOException {
        return RemoteObject.toStub(this);
    }
    protected RMIConnection makeClient(String connectionId, Subject subject)
            throws IOException {
        if (connectionId == null)
            throw new NullPointerException("Null connectionId");
        RMIConnection client =
            new RMIConnectionImpl(this, connectionId, getDefaultClassLoader(),
                                  subject, env);
        export(client);
        return client;
    }
    protected void closeClient(RMIConnection client) throws IOException {
        unexport(client, true);
    }
    protected void closeServer() throws IOException {
        unexport(this, true);
    }
    private final int port;
    private final RMIClientSocketFactory csf;
    private final RMIServerSocketFactory ssf;
    private final Map<String, ?> env;
}
