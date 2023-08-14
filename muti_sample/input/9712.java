public class SimpleSerialization {
    public static void main(final String[] args) throws Exception {
        final Vector<String> v1 = new Vector<>();
        v1.add("entry1");
        v1.add("entry2");
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(v1);
        oos.close();
        final byte[] data = baos.toByteArray();
        final ByteArrayInputStream bais = new ByteArrayInputStream(data);
        final ObjectInputStream ois = new ObjectInputStream(bais);
        final Object deserializedObject = ois.readObject();
        ois.close();
        if (false == v1.equals(deserializedObject)) {
            throw new RuntimeException(getFailureText(v1, deserializedObject));
        }
    }
    private static String getFailureText(final Object orig, final Object copy) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pw.println("Test FAILED: Deserialized object is not equal to the original object");
        pw.print("\tOriginal: ");
        printObject(pw, orig).println();
        pw.print("\tCopy:     ");
        printObject(pw, copy).println();
        pw.close();
        return sw.toString();
    }
    private static PrintWriter printObject(final PrintWriter pw, final Object o) {
        pw.printf("%s@%08x", o.getClass().getName(), System.identityHashCode(o));
        return pw;
    }
}
