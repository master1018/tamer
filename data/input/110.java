public class UnicastServerRef2 extends UnicastServerRef
{
    private static final long serialVersionUID = -2289703812660767614L;
    public UnicastServerRef2()
    {}
    public UnicastServerRef2(LiveRef ref)
    {
        super(ref);
    }
    public UnicastServerRef2(int port,
                             RMIClientSocketFactory csf,
                             RMIServerSocketFactory ssf)
    {
        super(new LiveRef(port, csf, ssf));
    }
    public String getRefClass(ObjectOutput out)
    {
        return "UnicastServerRef2";
    }
    protected RemoteRef getClientRef() {
        return new UnicastRef2(ref);
    }
}
