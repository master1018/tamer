public class IIOPOutputStream_1_3 extends com.sun.corba.se.impl.io.IIOPOutputStream
{
    private ObjectOutputStream.PutField putFields_1_3;
    protected void internalWriteUTF(org.omg.CORBA.portable.OutputStream stream,
                                    String data)
    {
        stream.write_string(data);
    }
    public IIOPOutputStream_1_3()
        throws java.io.IOException {
        super();
    }
    public ObjectOutputStream.PutField putFields()
        throws IOException {
        putFields_1_3 = new LegacyHookPutFields();
        return putFields_1_3;
    }
    public void writeFields()
        throws IOException {
        putFields_1_3.write(this);
    }
}
