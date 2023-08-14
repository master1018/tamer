class AddedSuperClass implements Serializable {
    int field;
}
class A implements Serializable {
    private static final long serialVersionUID = 1L;
}
public class ReadAddedSuperClass2 {
    public static void main(String args[]) throws IOException, ClassNotFoundException {
        File f = new File("tmp.ser");
        ObjectInput in =
            new ObjectInputStream(new FileInputStream(f));
        A a = (A)in.readObject();
        in.close();
    }
}
