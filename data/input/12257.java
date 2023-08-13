public final class Impl1_Stub
    extends java.rmi.server.RemoteStub
    implements Pong
{
    private static final java.rmi.server.Operation[] operations = {
        new java.rmi.server.Operation("void pong()")
    };
    private static final long interfaceHash = -6081108852319377105L;
    public Impl1_Stub() {
        super();
    }
    public Impl1_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public void pong()
        throws PongException, java.rmi.RemoteException
    {
        try {
            java.rmi.server.RemoteCall call = ref.newCall((java.rmi.server.RemoteObject) this, operations, 0, interfaceHash);
            ref.invoke(call);
            ref.done(call);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (PongException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
}
