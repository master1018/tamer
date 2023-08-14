public class UnbindIdempotent {
    public static void main(String[] args) throws Exception {
        try {
            java.rmi.registry.LocateRegistry.createRegistry(1099);
        } catch (java.rmi.RemoteException e) {
        }
        Context ictx = new InitialContext();
        Context rctx;
        try {
            rctx = (Context)ictx.lookup("rmi:
        } catch (NamingException e) {
            return;
        }
        try {
            rctx.unbind("_bogus_4278121_");
        } catch (NameNotFoundException e) {
            throw new Exception("Test failed:  unbind() call not idempotent");
        }
    }
}
