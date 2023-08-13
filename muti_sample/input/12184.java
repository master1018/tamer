class A implements Serializable {
    Object writeReplace() throws ObjectStreamException {
        return new B();
    }
}
class B implements Serializable {
    Object writeReplace() throws ObjectStreamException {
        return new C();
    }
}
class C implements Serializable {
    static int writeReplaceCalled = 0;
    Object writeReplace() throws ObjectStreamException {
        writeReplaceCalled++;
        return new C();
    }
    Object readResolve() throws ObjectStreamException {
        return new D();
    }
}
class D implements Serializable {
    Object readResolve() throws ObjectStreamException {
        throw new Error("readResolve() called more than once");
    }
}
public class NestedReplace {
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bout;
        ObjectOutputStream oout;
        ByteArrayInputStream bin;
        ObjectInputStream oin;
        Object obj;
        bout = new ByteArrayOutputStream();
        oout = new ObjectOutputStream(bout);
        oout.writeObject(new A());
        oout.flush();
        bin = new ByteArrayInputStream(bout.toByteArray());
        oin = new ObjectInputStream(bin);
        obj = oin.readObject();
        if (! (obj instanceof D))
            throw new Error("Deserialized object is of wrong class");
        if (C.writeReplaceCalled != 1)
            throw new Error("C.writeReplace() should only get called once");
    }
}
