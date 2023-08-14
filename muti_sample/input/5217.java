public class CheckForException {
    public static void main (String argv[]) {
        System.err.println("\nRegression test of " +
                           "serialization/deserialization of " +
                           "complex objects which raise " +
                           "NotSerializableException inside " +
                           "writeObject() and readObject() methods.\n");
        FileInputStream istream = null;
        try {
            FileOutputStream ostream = new FileOutputStream("psiotest3.tmp");
            ObjectOutputStream p = new ObjectOutputStream(ostream);
            TryPickleClass npc = new TryPickleClass();
            NotSerializableException we = null;
            try {
                p.writeObject("test");
                p.writeObject("test2");
                p.writeObject(npc);
            } catch (NotSerializableException e) {
                we = e;
            }
            if (we == null) {
                System.err.println("\nTEST FAILED: Write of NoPickleClass " +
                    "should have raised an exception");
                throw new Error();
            }
            p.flush();
            ostream.close();
            istream = new FileInputStream("psiotest3.tmp");
            ObjectInputStream q = new ObjectInputStream(istream);
            TryPickleClass npc_u;
            NotSerializableException re = null;
            try {
                q.readObject();
                q.readObject();
                npc_u = (TryPickleClass)q.readObject();
            } catch (NotSerializableException e) {
                re = e;
            }
            if (re == null) {
                System.err.println("\nTEST FAILED: Read of NoPickleClass " +
                   "should have raised an exception");
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
class PickleClass implements java.io.Serializable {
    int ii = 17;
    transient int tmp[];
    private void writeObject(ObjectOutputStream pw) throws IOException {
        pw.writeUTF("PickleClass");
        pw.writeInt(ii);
    }
    private void readObject(ObjectInputStream pr) throws IOException {
        tmp = new int[32];
        pr.readUTF();
        ii = pr.readInt();
    }
    private void readObjectCleanup(ObjectInputStream pr) {
        System.err.println("\nPickleClass cleanup correctly called on abort.");
        if (tmp != null) {
            tmp = null;
        }
    }
}
class NoPickleClass extends PickleClass {
    private void writeObject(ObjectOutputStream pw)
        throws NotSerializableException
    {
        throw new NotSerializableException("NoPickleClass");
    }
    private void readObject(ObjectInputStream pr)
            throws NotSerializableException
    {
            throw new NotSerializableException("NoPickleClass");
    }
}
class TryPickleClass  extends NoPickleClass {
    int i = 7;
    transient int tmp[];
    private void writeObject(ObjectOutputStream pw) throws IOException {
            pw.writeInt(i);
    }
    private void readObject(ObjectInputStream pr) throws IOException {
            tmp = new int[32];
            i = pr.readInt();
    }
    private void readObjectCleanup(ObjectInputStream pr) {
            System.err.println("\nCleanup called on abort");
            if (tmp != null) {
                tmp = null;
        }
    }
}
