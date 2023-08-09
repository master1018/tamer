public class EvolvedClass {
    public static void main(String args[]) throws Exception{
        ASubClass corg = new ASubClass(1, "SerializedByEvolvedClass");
        ASubClass cnew = null;
        FileInputStream fi = new FileInputStream("parents.ser");
        try {
            ObjectInputStream si = new ObjectInputStream(fi);
            cnew = (ASubClass) si.readObject();
        } finally {
            fi.close();
        }
        System.out.println("Printing the deserialized class: ");
        System.out.println();
        System.out.println(cnew);
    }
}
class ASuperClass implements Serializable {
    String name;
    ASuperClass() {
        throw new Error("ASuperClass: Wrong no-arg constructor invoked");
    }
    ASuperClass(String name) {
        this.name = new String(name);
    }
    public String toString() {
        return("Name:  " + name);
    }
}
class ASubClass extends ASuperClass implements Serializable {
    int num;
    private static final long serialVersionUID =6341246181948372513L;
    ASubClass(int num, String name) {
        super(name);
        this.num = num;
    }
    public String toString() {
        return (super.toString() + "\nNum:  " + num);
    }
}
