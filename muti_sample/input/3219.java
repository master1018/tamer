public final class Impl1_Skel
    implements java.rmi.server.Skeleton
{
    private static final java.rmi.server.Operation[] operations = {
        new java.rmi.server.Operation("void pong()")
    };
    private static final long interfaceHash = -6081108852319377105L;
    public java.rmi.server.Operation[] getOperations() {
        return (java.rmi.server.Operation[]) operations.clone();
    }
    public void dispatch(java.rmi.Remote obj, java.rmi.server.RemoteCall call, int opnum, long hash)
        throws java.lang.Exception
    {
        if (hash != interfaceHash)
            throw new java.rmi.server.SkeletonMismatchException("interface hash mismatch");
        Impl1 server = (Impl1) obj;
        switch (opnum) {
        case 0: 
        {
            call.releaseInputStream();
            server.pong();
            try {
                call.getResultStream(true);
            } catch (java.io.IOException e) {
                throw new java.rmi.MarshalException("error marshalling return", e);
            }
            break;
        }
        default:
            throw new java.rmi.UnmarshalException("invalid method number");
        }
    }
}
