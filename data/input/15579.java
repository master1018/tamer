public final class ForceLogSnapshot_Skel
    implements java.rmi.server.Skeleton
{
    private static final java.rmi.server.Operation[] operations = {
        new java.rmi.server.Operation("void crash()"),
        new java.rmi.server.Operation("void ping(int, java.lang.String)")
    };
    private static final long interfaceHash = -5865767584502007357L;
    public java.rmi.server.Operation[] getOperations() {
        return (java.rmi.server.Operation[]) operations.clone();
    }
    public void dispatch(java.rmi.Remote obj, java.rmi.server.RemoteCall call, int opnum, long hash)
        throws java.lang.Exception
    {
        if (opnum < 0) {
            if (hash == 8484760490859430950L) {
                opnum = 0;
            } else if (hash == -1519179153769139224L) {
                opnum = 1;
            } else {
                throw new java.rmi.UnmarshalException("invalid method hash");
            }
        } else {
            if (hash != interfaceHash)
                throw new java.rmi.server.SkeletonMismatchException("interface hash mismatch");
        }
        ForceLogSnapshot server = (ForceLogSnapshot) obj;
        switch (opnum) {
        case 0: 
        {
            call.releaseInputStream();
            server.crash();
            try {
                call.getResultStream(true);
            } catch (java.io.IOException e) {
                throw new java.rmi.MarshalException("error marshalling return", e);
            }
            break;
        }
        case 1: 
        {
            int $param_int_1;
            java.lang.String $param_String_2;
            try {
                java.io.ObjectInput in = call.getInputStream();
                $param_int_1 = in.readInt();
                $param_String_2 = (java.lang.String) in.readObject();
            } catch (java.io.IOException e) {
                throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
            } catch (java.lang.ClassNotFoundException e) {
                throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
            } finally {
                call.releaseInputStream();
            }
            server.ping($param_int_1, $param_String_2);
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
