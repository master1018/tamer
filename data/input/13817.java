public final class NotExtending_Stub
    extends java.rmi.server.RemoteStub
    implements java.rmi.Remote
{
    private static java.rmi.server.Operation[] operations = {
    };
    private static final long interfaceHash = 3103311997983563335L;
    private static final long serialVersionUID = 2;
    private static boolean useNewInvoke;
    static {
        try {
            java.rmi.server.RemoteRef.class.getMethod("invoke",
                new java.lang.Class[] {
                    java.rmi.Remote.class,
                    java.lang.reflect.Method.class,
                    java.lang.Object[].class,
                    long.class
                });
            useNewInvoke = true;
        } catch (java.lang.NoSuchMethodException e) {
            useNewInvoke = false;
        }
    }
    public NotExtending_Stub() {
        super();
    }
    public NotExtending_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
}
