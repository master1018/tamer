class AddedSuperClass implements Serializable {
    int field;
}
class A extends AddedSuperClass implements Serializable  {
    private static final long serialVersionUID = 1L;
}
public class WriteAddedSuperClass {
    public static void main(String args[]) throws IOException {
        A a = new A();
        File f = new File("tmp.ser");
        ObjectOutput out =
            new ObjectOutputStream(new FileOutputStream(f));
        out.writeObject(a);
        out.close();
    }
}
