class CNFInputStream extends ObjectInputStream {
    CNFInputStream(InputStream in) throws IOException {
        super(in);
    }
    protected ObjectStreamClass readClassDescriptor()
        throws IOException, ClassNotFoundException
    {
        throw new ClassNotFoundException("foobar");
    }
}
public class CNFException {
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(new Integer(5));
        oout.close();
        ObjectInputStream oin =
            new CNFInputStream(new ByteArrayInputStream(bout.toByteArray()));
        try {
            oin.readObject();
            throw new Error("expected InvalidClassException");
        } catch (InvalidClassException e) {
            Throwable cause = e.getCause();
            if (!(cause instanceof ClassNotFoundException)) {
                throw new Error(
                    "expected ClassNotFoundException as cause, not " + cause);
            }
        }
    }
}
