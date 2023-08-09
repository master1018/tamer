public class IIOPInputStream_1_3_1 extends com.sun.corba.se.impl.io.IIOPInputStream
{
    public IIOPInputStream_1_3_1()
        throws java.io.IOException {
        super();
    }
    public ObjectInputStream.GetField readFields()
        throws IOException, ClassNotFoundException, NotActiveException {
        Hashtable fields = (Hashtable)readObject();
        return new LegacyHookGetFields(fields);
    }
}
