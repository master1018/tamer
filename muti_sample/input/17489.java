class A implements Serializable {
    private static final long serialVersionUID = 1L;
    public int bar;
    public D zoo;
}
class D implements Serializable {
    public int x;
    D(int y) {
        x = y;
    }
}
public class ReadAddedField {
    public static void main(String args[])
        throws IOException, ClassNotFoundException
    {
        File f = new File("tmp.ser");
        ObjectInput in =
            new ObjectInputStream(new FileInputStream(f));
        A a = (A)in.readObject();
        A b = (A)in.readObject();
        if (a.bar != 4)
            throw new RuntimeException("a.bar does not equal 4, it equals " +
                                       a.bar);
        if (a.zoo.x != 22)
            throw new RuntimeException("a.zoo.x does not equal 22 equals " +
                                       a.zoo.x);
        if (b.bar != 4)
            throw new RuntimeException("b.bar does not equal 4, it equals " +
                                       b.bar);
        if (b.zoo.x != 22)
            throw new RuntimeException("b.zoo.x does not equal 22 equals " +
                                       b.zoo.x);
        in.close();
    }
}
