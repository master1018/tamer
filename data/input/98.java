public class IIOPInputStream_1_3 extends com.sun.corba.se.impl.io.IIOPInputStream
{
    protected String internalReadUTF(org.omg.CORBA.portable.InputStream stream)
    {
        return stream.read_string();
    }
    public ObjectInputStream.GetField readFields()
        throws IOException, ClassNotFoundException, NotActiveException {
        Hashtable fields = (Hashtable)readObject();
        return new LegacyHookGetFields(fields);
    }
    public IIOPInputStream_1_3()
        throws java.io.IOException {
        super();
    }
}
