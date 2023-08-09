class Foo implements Externalizable {
    private static final long serialVersionUID = 0xbabel;
    int x;
    int y;
    Object obj;
    public Foo() {
    }
    public Foo(int x, int y, Object obj) {
        this.x = x;
        this.y = y;
        this.obj = obj;
    }
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(x);
        out.writeInt(y);
        out.writeObject(obj);
    }
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        x = in.readInt();
        y = in.readInt();
        obj = in.readObject();
    }
    public boolean equals(Object other) {
        if (other instanceof Foo) {
            Foo f = (Foo) other;
            return ((x == f.x) && (y == f.y) &&
                    ((obj != null) ? obj.equals(f.obj) : (f.obj == null)));
        }
        return false;
    }
}
public class ExternalizableBlockData {
    public static void main(String[] args) throws Exception {
        byte[] oldExternalizableBytes = getFileBytes(
                new File(System.getProperty("test.src", "."), "old.ser"));
        Foo foo = new Foo(0xbad, 0xbeef, "burrito");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.useProtocolVersion(ObjectStreamConstants.PROTOCOL_VERSION_1);
        oout.writeObject(foo);
        oout.close();
        if (! Arrays.equals(bout.toByteArray(), oldExternalizableBytes)) {
            throw new Error();
        }
        ObjectInputStream oin = new ObjectInputStream(
                new ByteArrayInputStream(oldExternalizableBytes));
        if (! foo.equals(oin.readObject())) {
            throw new Error();
        }
        bout = new ByteArrayOutputStream();
        oout = new ObjectOutputStream(bout);
        oout.writeObject(foo);
        oout.close();
        if (Arrays.equals(bout.toByteArray(), oldExternalizableBytes)) {
            throw new Error();
        }
    }
    static byte[] getFileBytes(File file) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buf = new byte[256];
        int n;
        while ((n = fin.read(buf)) != -1) {
            bout.write(buf, 0, n);
        }
        fin.close();
        return bout.toByteArray();
    }
}
