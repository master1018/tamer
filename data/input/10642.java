public final class Doctor_Stub
    extends java.rmi.server.RemoteStub
    implements Eliza, Retireable, java.rmi.Remote
{
    private static final long serialVersionUID = 2;
    private static java.lang.reflect.Method $method_complain_0;
    private static java.lang.reflect.Method $method_retire_1;
    static {
        try {
            $method_complain_0 = Eliza.class.getMethod("complain", new java.lang.Class[] {java.lang.String.class});
            $method_retire_1 = Retireable.class.getMethod("retire", new java.lang.Class[] {});
        } catch (java.lang.NoSuchMethodException e) {
            throw new java.lang.NoSuchMethodError(
                "stub class initialization failed");
        }
    }
    public Doctor_Stub(java.rmi.server.RemoteRef ref) {
        super(ref);
    }
    public java.lang.String complain(java.lang.String $param_String_1)
        throws java.rmi.RemoteException
    {
        try {
            Object $result = ref.invoke(this, $method_complain_0, new java.lang.Object[] {$param_String_1}, -6341882871094951445L);
            return ((java.lang.String) $result);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
    public void retire()
        throws java.rmi.RemoteException
    {
        try {
            ref.invoke(this, $method_retire_1, null, 1748571935738034018L);
        } catch (java.lang.RuntimeException e) {
            throw e;
        } catch (java.rmi.RemoteException e) {
            throw e;
        } catch (java.lang.Exception e) {
            throw new java.rmi.UnexpectedException("undeclared checked exception", e);
        }
    }
}
