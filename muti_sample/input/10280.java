class A implements Serializable {
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("nonexistent", int.class)
    };
}
class B implements Serializable {
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("mismatched", int.class)
    };
    private float mismatched;
}
class C implements Serializable {
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("existent", int.class)
    };
    private int existent;
}
public class BadSerialPersistentField {
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout;
        oout = new ObjectOutputStream(bout);
        try {
            oout.writeObject(new A());
            throw new Error();
        } catch (InvalidClassException ex) {
        }
        oout = new ObjectOutputStream(bout);
        try {
            oout.writeObject(new B());
            throw new Error();
        } catch (InvalidClassException ex) {
        }
        oout = new ObjectOutputStream(bout);
        oout.writeObject(new C());
    }
}
