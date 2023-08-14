class OriginalIOException extends IOException {
}
class BrokenOutputStream extends OutputStream {
    boolean broken = false;
    public void write(int b) throws IOException {
        if (broken) {
            throw new OriginalIOException();
        }
    }
}
public class UnderlyingOutputStreamException {
    public static void main(String[] args) throws Exception {
        BrokenOutputStream bout = new BrokenOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        bout.broken = true;
        try {
            oout.writeObject("foo");
            throw new Error();
        } catch (OriginalIOException ex) {
        }
    }
}
