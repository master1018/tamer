public final class ShutdownGracefully_Stub
    extends java.rmi.server.RemoteStub
    implements RegisteringActivatable, java.rmi.Remote
{
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_shutdown_0;
    static {
        try {
            $method_shutdown_0 = RegisteringActivatable.class.getMethod("shutdown", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError(
                "stub class initialization failed");
        }
    }
    public ShutdownGracefully_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public void shutdown()
        throws java.lang.Exception
    {
        ref.invoke(this, $method_shutdown_0, null, -7207851917985848402L);
    }
}
