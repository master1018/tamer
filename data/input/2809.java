class TestClass implements Serializable {
    private static final long serialVersionUID = 5748652654655279289L;
    private final static ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("objectI", Integer.class)};
    Integer objectI;
    int     primitiveI;
    Foo foo;
    public TestClass(Foo f, Integer I, int i) {
        foo = f;
        objectI = I;
        primitiveI = i;
    }
};
public class GetFieldWrite {
    public static void main(String[] args)
        throws ClassNotFoundException, IOException
    {
        FileOutputStream fos = new FileOutputStream("data.ser");
        ObjectOutput out = new ObjectOutputStream(fos);
        out.writeObject(new TestClass(new Foo(100, 200), new Integer(100),
            200));
        out.close();
    }
};
class Foo implements Serializable{
    int a;
    int b;
    public Foo() {
        a = 10; b= 20;
    }
    public Foo(int a1, int b1)
    {
        a = a1; b = b1;
    }
    public String toString() {
        return new String("a = " + a + " b = " + b);
    }
}
