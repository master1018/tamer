public class ActivatableServerRef extends UnicastServerRef2 {
    private static final long serialVersionUID = 2002967993223003793L;
    private ActivationID id;
    public ActivatableServerRef(ActivationID id, int port)
    {
        this(id, port, null, null);
    }
    public ActivatableServerRef(ActivationID id, int port,
                                RMIClientSocketFactory csf,
                                RMIServerSocketFactory ssf)
    {
        super(new LiveRef(port, csf, ssf));
        this.id = id;
    }
    public String getRefClass(ObjectOutput out)
    {
        return "ActivatableServerRef";
    }
    protected RemoteRef getClientRef() {
        return new ActivatableRef(id, new UnicastRef2(ref));
    }
    public void writeExternal(ObjectOutput out) throws IOException {
        throw new NotSerializableException(
            "ActivatableServerRef not serializable");
    }
}
