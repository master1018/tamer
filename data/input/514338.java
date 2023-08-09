public class SerializationTest extends TestCase {
    static class MySerializable implements Serializable {}
    @SmallTest
    public void testSerialization() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(new MySerializable());
        oout.close();
        ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
        Object o = new ObjectInputStream(bin).readObject();
        assertTrue(o instanceof MySerializable);
    }
}
