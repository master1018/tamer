class NewExternFieldClass implements Externalizable {
    byte l;
    public NewExternFieldClass() {
        l = 0;
    }
    public NewExternFieldClass(byte value) {
        l = value;
    }
    public void readExternal(ObjectInput s)
        throws IOException, ClassNotFoundException
    {
        l = s.readByte();
        System.out.println("readExternal read " + l);
    }
    public void writeExternal(ObjectOutput s) throws IOException
    {
        s.writeByte(l);
    }
}
class D implements Serializable {
    public int x;
    D(int y) {
        x = y;
    }
}
class A implements Serializable  {
    private static final long serialVersionUID = 1L;
    NewExternFieldClass foo;
    D zoo;
    int bar;
    A() {
        bar = 4;
        foo = new NewExternFieldClass((byte)66);
        zoo = new D(22);
    }
}
public class WriteAddedField {
    public static void main(String args[]) throws IOException {
        A a = new A();
        File f = new File("tmp.ser");
        ObjectOutput out =
            new ObjectOutputStream(new FileOutputStream(f));
        out.writeObject(a);
        out.writeObject(new A());
        out.close();
    }
}
