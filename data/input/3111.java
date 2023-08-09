public class ValidateClass {
    public static void main (String argv[]) {
        System.err.println("\nRegression test for validation of callbacks \n");
        FileInputStream istream = null;
        try {
            FileOutputStream ostream = new FileOutputStream("psiotest4.tmp");
            ObjectOutputStream p = new ObjectOutputStream(ostream);
            Validator vc = new Validator(0, null);
            vc = new Validator(2, vc);
            vc = new Validator(0, vc);
            vc = new Validator(3, vc);
            vc = new Validator(Integer.MIN_VALUE, vc);
            vc = new Validator(1, vc);
            vc = new Validator(1, vc);
            vc = new Validator(0, vc);
            p.writeObject(vc);
            p.flush();
            ostream.close();
            istream = new FileInputStream("psiotest4.tmp");
            ObjectInputStream q = new ObjectInputStream(istream);
            Validator vc_u;
            vc_u = (Validator)q.readObject();
            if (vc_u.validated != Integer.MIN_VALUE) {
                System.err.println("\nTEST FAILED: Validation callbacks did " +
                    "not complete.");
                throw new Error();
            }
            istream.close();
            System.err.println("\nTEST PASSED");
        } catch (Exception e) {
            System.err.print("TEST FAILED: ");
            e.printStackTrace();
            throw new Error();
        }
    }
}
class MissingWriterClass implements java.io.Serializable {
    int i = 77;
    private void writeObject(ObjectOutputStream pw) throws IOException {
        pw.writeInt(i);
    }
}
class MissingReaderClass implements java.io.Serializable {
    int i = 77;
    private void readObject(ObjectInputStream pr) throws IOException {
        i = pr.readInt();
    }
}
class Validator implements ObjectInputValidation, java.io.Serializable  {
    static int validated = Integer.MAX_VALUE; 
    int priority;
    Validator next = null;
    public Validator(int prio, Validator n) {
        priority = prio;
        next = n;
    }
    private void writeObject(ObjectOutputStream pw) throws IOException {
        pw.writeInt(priority);
        pw.writeObject(next);
    }
    private void readObject(ObjectInputStream pr)
        throws IOException, ClassNotFoundException
    {
        priority = pr.readInt();
        next = (Validator)pr.readObject();
        pr.registerValidation(this, priority);
    }
    public void validateObject() throws InvalidObjectException {
        if (validated < priority) {
            System.err.println("\nTEST FAILED: Validations called out " +
                "of order: Previous priority: " + validated + " < " +
                "new priority: " + priority);
            throw new Error();
        }
        validated = priority;
    }
}
