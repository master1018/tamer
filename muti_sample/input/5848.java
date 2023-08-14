public final class NotExtending_Skel
    implements java.rmi.server.Skeleton
{
    private static java.rmi.server.Operation[] operations = {
    };
    private static final long interfaceHash = 3103311997983563335L;
    public java.rmi.server.Operation[] getOperations() {
        return operations;
    }
    public void dispatch(java.rmi.Remote obj, java.rmi.server.RemoteCall call, int opnum, long hash)
        throws java.lang.Exception
    {
        if (opnum < 0) {
            throw new java.rmi.UnmarshalException("invalid method hash");
        } else {
            if (hash != interfaceHash)
                throw new java.rmi.server.SkeletonMismatchException("interface hash mismatch");
        }
        NotExtending server = (NotExtending) obj;
        switch (opnum) {
        default:
            throw new java.rmi.UnmarshalException("invalid method number");
        }
    }
}
