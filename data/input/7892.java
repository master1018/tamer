class Foo implements Serializable {
    private static final long serialVersionUID = 0L;
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("s1", String.class),
        new ObjectStreamField("s2", String.class)
    };
    private void writeObject(ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField fields = out.putFields();
        fields.put("s1", "qwerty");
        fields.put("s2", "asdfg");
        fields.write(out);
    }
}
public class Write2 {
    public static void main(String[] args) throws Exception {
        ObjectOutputStream oout =
            new ObjectOutputStream(new FileOutputStream("tmp.ser"));
        oout.writeObject(new Foo());
        oout.close();
    }
}
