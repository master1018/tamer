public final class ToStub_Stub
    extends java.rmi.server.RemoteStub
    implements RemoteInterface
{
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_passObject_0;
    static {
        try {
            $method_passObject_0 = RemoteInterface.class.getMethod("passObject", new java.lang.Class[] {java.lang.Object.class});
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError(
                "stub class initialization failed");
        }
    }
    public ToStub_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public java.lang.Object passObject(java.lang.Object $param_Object_1)
        throws java.io.IOException
    {
        try {
            Object $result = ref.invoke(this, $method_passObject_0, new java.lang.Object[] {$param_Object_1}, 3074202549763602823L);
            return ((java.lang.Object) $result);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.io.IOException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
}
