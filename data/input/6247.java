class BrokenObjectInputStream extends ObjectInputStream {
    static final String message = "bodega";
    BrokenObjectInputStream(InputStream in) throws IOException {
        super(in);
    }
    protected Class resolveClass(ObjectStreamClass desc)
        throws IOException, ClassNotFoundException
    {
        throw new ClassNotFoundException(message);
    }
}
public class ResolveClassException {
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bout;
        ObjectOutputStream oout;
        ByteArrayInputStream bin;
        BrokenObjectInputStream oin;
        Object obj;
        obj = new Integer(5);
        bout = new ByteArrayOutputStream();
        oout = new ObjectOutputStream(bout);
        oout.writeObject(obj);
        bin = new ByteArrayInputStream(bout.toByteArray());
        oin = new BrokenObjectInputStream(bin);
        try {
            oin.readObject();
        } catch (ClassNotFoundException e) {
            if (! BrokenObjectInputStream.message.equals(e.getMessage()))
                throw new Error("Original exception not preserved");
        }
        obj = new Integer[] { new Integer(5) };
        bout = new ByteArrayOutputStream();
        oout = new ObjectOutputStream(bout);
        oout.writeObject(obj);
        bin = new ByteArrayInputStream(bout.toByteArray());
        oin = new BrokenObjectInputStream(bin);
        try {
            oin.readObject();
        } catch (ClassNotFoundException e) {
            if (! BrokenObjectInputStream.message.equals(e.getMessage()))
                throw new Error("Original exception not preserved");
        }
    }
}
