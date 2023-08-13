public class Validate {
    public static void main(String[] args) throws Exception {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutput out = new ObjectOutputStream(baos);
            Class1 c1 = new Class1(11, 22);
            out.writeObject(c1);
            out.writeObject(new Class1(22,33));
            out.writeObject(new Date());
            out.writeObject(new Date());
            out.writeObject(new Date());
            out.writeObject(new Date());
            out.flush();
            out.close();
            ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(bais);
            Class1 cc1 = (Class1) in.readObject();
            Class1 cc2 = (Class1) in.readObject();
            System.out.println("date: " + in.readObject());
            System.out.println("date: " + in.readObject());
            System.out.println("date: " + in.readObject());
            System.out.println("date: " + in.readObject());
            in.close();
            System.out.println(cc1.a + " " + cc1.b);
            System.out.println(cc2.a + " " + cc2.b);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
class Class1 implements Serializable, ObjectInputValidation {
    int a, b;
    transient int validates;
    public Class1(int aa, int bb) {
        a = aa;
        b = bb;
    }
    public void validateObject() throws InvalidObjectException {
        if (validates > 0)
            throw new Error("Implementation error: Re-validating object " + this.toString());
        validates++;
        System.out.println("Validating " + this.toString());
        if (a > b) {
            throw new InvalidObjectException("Fields cannot be negative");
        }
    }
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
            in.registerValidation(this, 1);
            in.defaultReadObject();
    }
}
