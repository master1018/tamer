class LoopbackOutputStream extends ObjectOutputStream {
    LinkedList descs;
    LoopbackOutputStream(OutputStream out, LinkedList descs)
        throws IOException
    {
        super(out);
        this.descs = descs;
    }
    protected void writeClassDescriptor(ObjectStreamClass desc)
        throws IOException
    {
        descs.add(desc);
    }
}
class LoopbackInputStream extends ObjectInputStream {
    LinkedList descs;
    LoopbackInputStream(InputStream in, LinkedList descs) throws IOException {
        super(in);
        this.descs = descs;
    }
    protected ObjectStreamClass readClassDescriptor()
        throws IOException, ClassNotFoundException
    {
        return (ObjectStreamClass) descs.removeFirst();
    }
}
public class Loopback implements Serializable {
    String str;
    Loopback(String str) {
        this.str = str;
    }
    public static void main(String[] args) throws Exception {
        Loopback lb = new Loopback("foo");
        LinkedList descs = new LinkedList();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        LoopbackOutputStream lout = new LoopbackOutputStream(bout, descs);
        lout.writeObject(lb);
        lout.close();
        LoopbackInputStream lin = new LoopbackInputStream(
            new ByteArrayInputStream(bout.toByteArray()), descs);
        Loopback lbcopy = (Loopback) lin.readObject();
        if (!lb.str.equals(lbcopy.str)) {
            throw new Error();
        }
    }
}
