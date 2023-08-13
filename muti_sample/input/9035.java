public class ModifyStaticFields implements Serializable {
    private static final ObjectStreamField[] serialPersistentFields =
        new ObjectStreamField[] { new ObjectStreamField("str", String.class) };
    static String str = "foo";
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(new ModifyStaticFields());
        oout.close();
        String origStr = str;
        ObjectInputStream oin = new ObjectInputStream(
            new ByteArrayInputStream(bout.toByteArray()));
        oin.readObject();
        if (str != origStr) {
            throw new Error("deserialization modified static field");
        }
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        ObjectOutputStream.PutField pf = out.putFields();
        pf.put("str", "bar");
        out.writeFields();
    }
}
