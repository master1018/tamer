class Foo implements Serializable {
    int reps;
    Foo(int reps) {
        this.reps = reps;
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        for (int i = 0; i < reps; i++) {
            out.writeObject(new Integer(i));
        }
    }
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        for (int i = 0; i < reps; i++) {
            in.readObject();
        }
        try {
            in.readObject();
            throw new Error();
        } catch (OptionalDataException ex) {
            if (! (ex.eof && (ex.length == 0))) {
                throw new Error();
            }
        }
    }
}
public class OptionalDataEnd {
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(new Foo(5));
        oout.writeObject(new Foo(0));
        oout.close();
        ObjectInputStream oin = new ObjectInputStream(
            new ByteArrayInputStream(bout.toByteArray()));
        oin.readObject();
        oin.readObject();
        oin.close();
    }
}
