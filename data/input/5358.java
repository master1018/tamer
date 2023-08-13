class A implements Serializable {
    private void readObject(ObjectInputStream in)
        throws ClassNotFoundException, IOException
    {
        throw new ClassNotFoundException();
    }
}
class B implements Externalizable {
    public B() {}
    public void writeExternal(ObjectOutput out) throws IOException {}
    public void readExternal(ObjectInput in)
        throws ClassNotFoundException, IOException
    {
        throw new ClassNotFoundException();
    }
}
public class ExplicitCNFException {
    public static void main(String[] args) throws Exception {
        test(new A());
        test(new Object[]{ new A() });
        test(new B());
        test(new Object[]{ new B() });
    }
    static void test(Object obj) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.useProtocolVersion(ObjectStreamConstants.PROTOCOL_VERSION_2);
        oout.writeObject(obj);
        oout.close();
        ObjectInputStream oin = new ObjectInputStream(
            new ByteArrayInputStream(bout.toByteArray()));
        try {
            oin.readObject();
            throw new Error();  
        } catch (ClassNotFoundException ex) {
        }
    }
}
