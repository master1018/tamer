public class ImmutableDescriptorSerialTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Test that ImmutableDescriptor.EMPTY_DESCRIPTOR " +
                "deserializes identically");
        if (serialize(ImmutableDescriptor.EMPTY_DESCRIPTOR) !=
                ImmutableDescriptor.EMPTY_DESCRIPTOR) {
            throw new Exception("ImmutableDescriptor.EMPTY_DESCRIPTOR did not " +
                    "deserialize identically");
        }
        System.out.println("...OK");
        System.out.println("Test that serialization preserves case and " +
                "that deserialized object is case-insensitive");
        Descriptor d = new ImmutableDescriptor("a=aval", "B=Bval", "cC=cCval");
        Descriptor d1 = serialize(d);
        Set<String> keys = new HashSet(Arrays.asList(d1.getFieldNames()));
        if (keys.size() != 3 ||
                !keys.containsAll(Arrays.asList("a", "B", "cC"))) {
            throw new Exception("Keys don't match: " + keys);
        }
        for (String key : keys) {
            String value = (String) d.getFieldValue(key);
            for (String t :
                    Arrays.asList(key, key.toLowerCase(), key.toUpperCase())) {
                String tvalue = (String) d1.getFieldValue(t);
                if (!tvalue.equals(value)) {
                    throw new Exception("Value of " + key + " for " +
                            "deserialized object does not match: " +
                            tvalue + " should be " + value);
                }
            }
        }
        System.out.println("...OK");
    }
    private static <T> T serialize(T x) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(x);
        oout.close();
        byte[] bytes = bout.toByteArray();
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        ObjectInputStream oin = new ObjectInputStream(bin);
        return (T) oin.readObject();
    }
}
