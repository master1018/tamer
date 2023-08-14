public class FooReceiverImpl implements DownloadParameterClass.FooReceiver {
    private ActivationID id;
    public FooReceiverImpl(ActivationID id, MarshalledObject mobj)
        throws ActivationException, RemoteException
    {
        this.id = id;
        Activatable.exportObject(this, id, 0);
    }
    public void receiveFoo(Object obj) {
        Foo foo = (Foo) obj;
    }
}
