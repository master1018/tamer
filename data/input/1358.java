class A implements Serializable {
    static HashSet writeObjectExtent = new HashSet();
    private void writeObject(ObjectOutputStream out) throws IOException {
        if (writeObjectExtent.contains(this)) {
            throw new InvalidObjectException("writeObject: object " +
                                             this.toString() + " has already "
                                             + "been serialized and should " +
                                             "have be serialized by reference.");
        } else {
            writeObjectExtent.add(this);
        }
        out.defaultWriteObject();
    }
    A() {
    }
}
public class VerifyDynamicObjHandleTable {
    public static void main(String args[])
        throws IOException, ClassNotFoundException
    {
        ObjectOutputStream out =
            new ObjectOutputStream(new ByteArrayOutputStream(3000));
        for (int i = 0; i < 1000; i++) {
            out.writeObject(new A());
        }
        Iterator iter = A.writeObjectExtent.iterator();
        while (iter.hasNext()) {
            out.writeObject(iter.next());
        }
        out.close();
    }
}
