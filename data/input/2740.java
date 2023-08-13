public class MyActivationGroupImpl extends ActivationGroupImpl {
    public MyActivationGroupImpl(ActivationGroupID id, MarshalledObject mobj)
        throws RemoteException, ActivationException
    {
        super(id, mobj);
        System.err.println("custom group implementation created");
    }
}
