class TestClass implements Serializable {
    public static final Integer DEFAULT_OBJECT_I = new Integer(99);
    public static final Foo DEFAULT_OBJECT_F = new Foo();
    private static final long serialVersionUID=5748652654655279289L;
    private final static ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("objectI", Integer.class),
        new ObjectStreamField("primitiveI", Integer.TYPE),
        new ObjectStreamField("foo", Foo.class)
    };
    Integer objectI;
    int     primitiveI;
    Foo foo;
    public TestClass(Foo f, Integer I, int i) {
        foo = f;
        objectI = I;
        primitiveI = i;
    }
    private void readObject(ObjectInputStream in)
        throws ClassNotFoundException, IOException
    {
        ObjectInputStream.GetField pfields = in.readFields();
        primitiveI = pfields.get("primitiveI", 99);
        System.out.println("The primitiveI : " + primitiveI);
        objectI = (Integer)pfields.get("objectI", DEFAULT_OBJECT_I);
        System.out.println("The ObjectI : " + objectI);
        foo = (Foo)pfields.get("foo", DEFAULT_OBJECT_F);
        System.out.println("The foo : " + foo);
        try {
            boolean b = pfields.defaulted("primitiveI");
            System.out.println("Defaulted prim : " + b);
            if (b == false) {
                throw new Error("Bad return value for defaulted() with " +
                    "primitive type fields");
            }
            b = pfields.defaulted("objectI");
            System.out.println("Defaulted ObjectI : " + b);
            if (b == true) {
                throw new Error("Bad return value for defaulted() with " +
                    "object type fields");
            }
            b = pfields.defaulted("foo");
            System.out.println("Defaulted Foo : " + b);
            if (b == false) {
                throw new Error("Bad return value for defaulted() with " +
                    "object type fields");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Exception " + e.getMessage() +
                   ": handled calling " +
                   "GetField.defaulted(\"fieldName referring to an object\")");
            throw e;
        }
    }
};
public class GetFieldRead {
    public static void main(String[] args)
        throws ClassNotFoundException, IOException
    {
        FileInputStream fis = new FileInputStream("data.ser");
        ObjectInputStream in = new ObjectInputStream(fis);
        TestClass obj = (TestClass) in.readObject();
        in.close();
    }
};
