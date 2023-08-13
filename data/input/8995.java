public class DefinesWriteObject implements Externalizable {
    private int    intData = 4;
    private Object objData = new String("hello");
    public DefinesWriteObject() {
    }
    public DefinesWriteObject(int i, Object o) {
        intData = i;
        objData = o;
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
    }
    public void writeExternal(ObjectOutput out)
        throws IOException
    {
        out.writeInt(intData);
        out.writeObject(objData);
    }
    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException
    {
        intData = in.readInt();
        objData = in.readObject();
    }
    public static void main(String args[])
        throws IOException, ClassNotFoundException
    {
        DefinesWriteObject obj1 = new DefinesWriteObject(5, "GoodBye");
        DefinesWriteObject obj2 = new DefinesWriteObject(6, "AuRevoir");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj1);
        oos.writeObject(obj2);
        oos.close();
        ByteArrayInputStream bais =
            new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        DefinesWriteObject readObject1 = (DefinesWriteObject)ois.readObject();
        DefinesWriteObject readObject2 = (DefinesWriteObject)ois.readObject();
        ois.close();
        if (obj1.intData != readObject1.intData ||
            obj2.intData != readObject2.intData) {
            throw new Error("Unexpected mismatch between integer data written and read.");
        }
        if ( ! ((String)obj1.objData).equals((String)readObject1.objData) ||
             ! ((String)obj2.objData).equals((String)readObject2.objData)) {
            throw new Error("Unexpected mismatch between String data written and read.");
        }
    }
};
