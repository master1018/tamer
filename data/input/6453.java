class LimitInputStream extends ByteArrayInputStream {
    int limit;
    LimitInputStream(byte[] b) {
        super(b);
        limit = b.length;
    }
    public int read() {
        if (limit < 1)
            throw new Error("limit exceeded");
        int c = super.read();
        if (c != -1)
            limit--;
        return c;
    }
    public int read(byte[] b) {
        return read(b, 0, b.length);
    }
    public int read(byte[] b, int off, int len) {
        if (limit < len)
            throw new Error("limit exceeded");
        int n = super.read(b, off, len);
        if (n != -1)
            limit -= n;
        return n;
    }
}
public class ReadPastObject {
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject("foo");
        LimitInputStream lin = new LimitInputStream(bout.toByteArray());
        ObjectInputStream oin = new ObjectInputStream(lin);
        System.out.println(oin.readObject());
    }
}
