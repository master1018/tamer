class A implements Serializable {
    int i1 = 1, i2 = 2;
    String s1 = "foo", s2 = "bar";
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        if (in.read() != -1) {
            throw new Error();
        }
        try {
            in.readInt();
            throw new Error();
        } catch (EOFException ex) {
        }
        try {
            in.readObject();
            throw new Error();
        } catch (OptionalDataException ex) {
            if (!ex.eof) {
                throw new Error();
            }
        }
        try {
            in.readUnshared();
            throw new Error();
        } catch (OptionalDataException ex) {
            if (!ex.eof) {
                throw new Error();
            }
        }
    }
}
class B implements Serializable {
    int i1 = 1, i2 = 2;
    String s1 = "foo", s2 = "bar";
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.readFields();
        try {
            in.readObject();
            throw new Error();
        } catch (OptionalDataException ex) {
            if (!ex.eof) {
                throw new Error();
            }
        }
        try {
            in.readUnshared();
            throw new Error();
        } catch (OptionalDataException ex) {
            if (!ex.eof) {
                throw new Error();
            }
        }
        if (in.read() != -1) {
            throw new Error();
        }
        try {
            in.readInt();
            throw new Error();
        } catch (EOFException ex) {
        }
    }
}
class C implements Serializable {
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        try {
            in.readObject();
            throw new Error();
        } catch (OptionalDataException ex) {
            if (!ex.eof) {
                throw new Error();
            }
        }
        try {
            in.readUnshared();
            throw new Error();
        } catch (OptionalDataException ex) {
            if (!ex.eof) {
                throw new Error();
            }
        }
        if (in.read() != -1) {
            throw new Error();
        }
        try {
            in.readInt();
            throw new Error();
        } catch (EOFException ex) {
        }
    }
}
public class DefaultDataEnd {
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oout = new ObjectOutputStream(bout);
        oout.writeObject(new A());
        oout.writeObject(new B());
        oout.writeObject(new C());
        oout.close();
        ObjectInputStream oin = new ObjectInputStream(
            new ByteArrayInputStream(bout.toByteArray()));
        oin.readObject();
        oin.readObject();
        oin.readObject();
    }
}
