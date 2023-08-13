public class ValueHandlerImpl_1_3_1
    extends com.sun.corba.se.impl.io.ValueHandlerImpl
{
    public ValueHandlerImpl_1_3_1() {}
    public ValueHandlerImpl_1_3_1(boolean isInputStream) {
        super(isInputStream);
    }
    protected TCKind getJavaCharTCKind() {
        return TCKind.tk_char;
    }
    public boolean useFullValueDescription(Class clazz, String repositoryID)
        throws java.io.IOException
    {
        return RepositoryId_1_3_1.useFullValueDescription(clazz, repositoryID);
    }
    protected final String getOutputStreamClassName() {
        return "com.sun.corba.se.impl.orbutil.IIOPOutputStream_1_3_1";
    }
    protected final String getInputStreamClassName() {
        return "com.sun.corba.se.impl.orbutil.IIOPInputStream_1_3_1";
    }
}
