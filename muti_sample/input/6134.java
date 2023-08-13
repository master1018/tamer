public final class UseCustomRef_Skel
    implements java.rmi.server.Skeleton
{
    private static final java.rmi.server.Operation[] operations = {
        new java.rmi.server.Operation("void ping()"),
        new java.rmi.server.Operation("void receiveAndPing(Ping)")
    };
    private static final long interfaceHash = 8457085322718440165L;
    public java.rmi.server.Operation[] getOperations() {
        return (java.rmi.server.Operation[]) operations.clone();
    }
    public void dispatch(java.rmi.Remote obj, java.rmi.server.RemoteCall call, int opnum, long hash)
        throws java.lang.Exception
    {
        if (opnum < 0) {
            if (hash == 5866401369815527589L) {
                opnum = 0;
            } else if (hash == -5964458762870933304L) {
                opnum = 1;
            } else {
                throw new java.rmi.UnmarshalException("invalid method hash");
            }
        } else {
            if (hash != interfaceHash)
                throw new java.rmi.server.SkeletonMismatchException("interface hash mismatch");
        }
        UseCustomRef server = (UseCustomRef) obj;
        switch (opnum) {
        case 0: 
        {
            call.releaseInputStream();
            server.ping();
            try {
                call.getResultStream(true);
            } catch (java.io.IOException e) {
                throw new java.rmi.MarshalException("error marshalling return", e);
            }
            break;
        }
        case 1: 
        {
            Ping $param_Ping_1;
            try {
                java.io.ObjectInput in = call.getInputStream();
                $param_Ping_1 = (Ping) in.readObject();
            } catch (java.io.IOException e) {
                throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
            } catch (java.lang.ClassNotFoundException e) {
                throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
            } finally {
                call.releaseInputStream();
            }
            server.receiveAndPing($param_Ping_1);
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
