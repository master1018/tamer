class A implements Externalizable {
    public A() {}
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(0);
        out.close();
    }
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        in.readInt();
        in.close();
    }
}
class B implements Serializable {
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.close();
    }
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        in.close();
    }
}
public class SurvivePrematureClose {
    public static void main(String[] args) throws Exception {
        writeRead(new A());
        writeRead(new B());
    }
    static void writeRead(Object obj) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(obj);
        oout.close();
        ObjectInputStream oin = new ObjectInputStream(
            new ByteArrayInputStream(bout.toByteArray()));
        oin.readObject();
    }
}
