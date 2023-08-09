class Foo implements Serializable {
    final int i;
    Foo(int i) {
        this.i = i;
    }
    public boolean equals(Object obj) {
        if (! (obj instanceof Foo))
            return false;
        Foo f = (Foo) obj;
        return (i == f.i);
    }
}
public class FinalFields {
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bout;
        ByteArrayInputStream bin;
        ObjectOutputStream oout;
        ObjectInputStream oin;
        Foo f1, f2, f1copy, f2copy;
        bout = new ByteArrayOutputStream();
        oout = new ObjectOutputStream(bout);
        f1 = new Foo(1);
        f2 = new Foo(2);
        oout.writeObject(f1);
        oout.writeObject(f2);
        oout.flush();
        bin = new ByteArrayInputStream(bout.toByteArray());
        oin = new ObjectInputStream(bin);
        f1copy = (Foo) oin.readObject();
        f2copy = (Foo) oin.readObject();
        if (! (f1.equals(f1copy) && f2.equals(f2copy)))
            throw new Error("copies don't match originals");
    }
}
