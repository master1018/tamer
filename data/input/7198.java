public final class ExtLoadedImpl_Stub
    extends java.rmi.server.RemoteStub
    implements CheckLoader
{
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_isCorrectContextLoader_0;
    static {
        try {
            $method_isCorrectContextLoader_0 = CheckLoader.class.getMethod("isCorrectContextLoader", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError(
                "stub class initialization failed");
        }
    }
    public ExtLoadedImpl_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public boolean isCorrectContextLoader()
        throws java.rmi.RemoteException
    {
        try {
            Object $result = ref.invoke(this, $method_isCorrectContextLoader_0, null, -5210790440944383968L);
            return ((java.lang.Boolean) $result).booleanValue();
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
}
