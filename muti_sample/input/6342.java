class Foo implements Serializable {
    private static final long serialVersionUID = 0L;
    String s1, s2;
}
public class Read2 {
    public static void main(String[] args) throws Exception {
        ObjectInputStream oin =
            new ObjectInputStream(new FileInputStream("tmp.ser"));
        Foo foo = (Foo) oin.readObject();
        oin.close();
        if (!(foo.s1.equals("qwerty") && foo.s2.equals("asdfg"))) {
            throw new Error();
        }
    }
}
