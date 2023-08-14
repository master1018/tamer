public class IIOPOutputStream_1_3_1 extends com.sun.corba.se.impl.io.IIOPOutputStream
{
    private ObjectOutputStream.PutField putFields_1_3_1;
    public IIOPOutputStream_1_3_1()
        throws java.io.IOException {
        super();
    }
    public ObjectOutputStream.PutField putFields()
        throws IOException {
        putFields_1_3_1 = new LegacyHookPutFields();
        return putFields_1_3_1;
    }
    public void writeFields()
        throws IOException {
        putFields_1_3_1.write(this);
    }
}
