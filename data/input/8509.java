public class LiveRef implements Cloneable {
    private final Endpoint ep;
    private final ObjID id;
    private transient Channel ch;
    private final boolean isLocal;
    public LiveRef(ObjID objID, Endpoint endpoint, boolean isLocal) {
        ep = endpoint;
        id = objID;
        this.isLocal = isLocal;
    }
    public LiveRef(int port) {
        this((new ObjID()), port);
    }
    public LiveRef(int port,
                   RMIClientSocketFactory csf,
                   RMIServerSocketFactory ssf)
    {
        this((new ObjID()), port, csf, ssf);
    }
    public LiveRef(ObjID objID, int port) {
        this(objID, TCPEndpoint.getLocalEndpoint(port), true);
    }
    public LiveRef(ObjID objID, int port, RMIClientSocketFactory csf,
                   RMIServerSocketFactory ssf)
    {
        this(objID, TCPEndpoint.getLocalEndpoint(port, csf, ssf), true);
    }
    public Object clone() {
        try {
            LiveRef newRef = (LiveRef) super.clone();
            return newRef;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
    public int getPort() {
        return ((TCPEndpoint) ep).getPort();
    }
    public RMIClientSocketFactory getClientSocketFactory() {
        return ((TCPEndpoint) ep).getClientSocketFactory();
    }
    public RMIServerSocketFactory getServerSocketFactory() {
        return ((TCPEndpoint) ep).getServerSocketFactory();
    }
    public void exportObject(Target target) throws RemoteException {
        ep.exportObject(target);
    }
    public Channel getChannel() throws RemoteException {
        if (ch == null) {
            ch = ep.getChannel();
        }
        return ch;
    }
    public ObjID getObjID() {
        return id;
    }
    Endpoint getEndpoint() {
        return ep;
    }
    public String toString() {
        String type;
        if (isLocal)
            type = "local";
        else
            type = "remote";
        return "[endpoint:" + ep + "(" + type + ")," +
            "objID:" + id + "]";
    }
    public int hashCode() {
        return id.hashCode();
    }
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof LiveRef) {
            LiveRef ref = (LiveRef) obj;
            return (ep.equals(ref.ep) && id.equals(ref.id) &&
                    isLocal == ref.isLocal);
        } else {
            return false;
        }
    }
    public boolean remoteEquals(Object obj) {
        if (obj != null && obj instanceof LiveRef) {
            LiveRef ref = (LiveRef) obj;
            TCPEndpoint thisEp = ((TCPEndpoint) ep);
            TCPEndpoint refEp = ((TCPEndpoint) ref.ep);
            RMIClientSocketFactory thisClientFactory =
                thisEp.getClientSocketFactory();
            RMIClientSocketFactory refClientFactory =
                refEp.getClientSocketFactory();
            if (thisEp.getPort() != refEp.getPort() ||
                !thisEp.getHost().equals(refEp.getHost()))
            {
                return false;
            }
            if ((thisClientFactory == null) ^ (refClientFactory == null)) {
                return false;
            }
            if ((thisClientFactory != null) &&
                !((thisClientFactory.getClass() ==
                   refClientFactory.getClass()) &&
                  (thisClientFactory.equals(refClientFactory))))
            {
                return false;
            }
            return (id.equals(ref.id));
        } else {
            return false;
        }
    }
    public void write(ObjectOutput out, boolean useNewFormat)
        throws IOException
    {
        boolean isResultStream = false;
        if (out instanceof ConnectionOutputStream) {
            ConnectionOutputStream stream = (ConnectionOutputStream) out;
            isResultStream = stream.isResultStream();
            if (isLocal) {
                ObjectEndpoint oe =
                    new ObjectEndpoint(id, ep.getInboundTransport());
                Target target = ObjectTable.getTarget(oe);
                if (target != null) {
                    Remote impl = target.getImpl();
                    if (impl != null) {
                        stream.saveObject(impl);
                    }
                }
            } else {
                stream.saveObject(this);
            }
        }
        if (useNewFormat) {
            ((TCPEndpoint) ep).write(out);
        } else {
            ((TCPEndpoint) ep).writeHostPortFormat(out);
        }
        id.write(out);
        out.writeBoolean(isResultStream);
    }
    public static LiveRef read(ObjectInput in, boolean useNewFormat)
        throws IOException, ClassNotFoundException
    {
        Endpoint ep;
        ObjID id;
        if (useNewFormat) {
            ep = TCPEndpoint.read(in);
        } else {
            ep = TCPEndpoint.readHostPortFormat(in);
        }
        id = ObjID.read(in);
        boolean isResultStream = in.readBoolean();
        LiveRef ref = new LiveRef(id, ep, false);
        if (in instanceof ConnectionInputStream) {
            ConnectionInputStream stream = (ConnectionInputStream)in;
            stream.saveRef(ref);
            if (isResultStream) {
                stream.setAckNeeded();
            }
        } else {
            DGCClient.registerRefs(ep, Arrays.asList(new LiveRef[] { ref }));
        }
        return ref;
    }
}
