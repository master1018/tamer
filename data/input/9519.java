public class OriginalClass {
    public static void main(String args[]) throws Exception{
        ASubClass corg = new ASubClass(1);
        ASubClass cnew = null;
        FileOutputStream fo = new FileOutputStream("parents.ser");
        try {
            ObjectOutputStream so = new ObjectOutputStream(fo);
            so.writeObject(corg);
            so.flush();
        } finally {
            fo.close();
        }
        System.out.println("Printing the serialized class: ");
        System.out.println();
        System.out.println(corg);
    }
}
class ASubClass implements Serializable {
    int num;
    ASubClass(int num) {
        this.num = num;
    }
    public String toString() {
        return ("\nNum:  " + num);
    }
}
