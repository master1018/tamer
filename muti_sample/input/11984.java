public class SerialDriver implements Serializable {
    private static final long serialVersionUID = 1L;
    String name;
    SerialDriver next;
    public SerialDriver() {
        name = "<terminator>";
        next = null;
    }
    public SerialDriver(String name, SerialDriver next) {
        this.name = name;
        this.next = next;
    }
    static boolean serialize = false;
    static boolean deserialize = false;
    public static void main(String args[])  throws Exception  {
        SerialDriver obj = new SerialDriver("SerialDriver_1", new SerialDriver());
        SerialDriver[] array = new SerialDriver[5];
        for (int i = 0; i < array.length; i++)
            array[i] = new SerialDriver("SerialDriver_1_" + i, new SerialDriver());
        if (args.length == 1) {
            if (args[0].equals("-d")) {
                deserialize = true;
            } else if (args[0].equals("-s")) {
                serialize = true;
            } else {
                usage();
                throw new Exception("incorrect command line arguments");
            }
        } else {
            usage();
            throw new Exception("incorrect command line arguments");
        }
        File f = new File("stream.ser");
        if (serialize) {
            try (FileOutputStream fo = new FileOutputStream(f);
                 ObjectOutputStream so = new ObjectOutputStream(fo))
            {
                so.writeObject(obj);
                so.writeObject(array);
            } catch (Exception e) {
                System.out.println(e);
                throw e;
            }
        }
        if (deserialize) {
            try (FileInputStream fi = new FileInputStream(f);
                 ExtendedObjectInputStream si = new ExtendedObjectInputStream(fi))
            {
                si.addRenamedClassName("install.SerialDriver",
                                       "test.SerialDriver");
                si.addRenamedClassName("[Linstall.SerialDriver;",
                                       "[Ltest.SerialDriver");
                obj = (SerialDriver) si.readObject();
                array = (SerialDriver[]) si.readObject();
            } catch (Exception e) {
                System.out.println(e);
                throw e;
            }
            System.out.println();
            System.out.println("Printing deserialized class: ");
            System.out.println();
            System.out.println(obj.toString());
            System.out.println();
        }
    }
    public String toString() {
        String nextString = next != null ? next.toString() : "<null>";
        return "name =" + name + " next = <" + nextString + ">";
    }
    static void usage() {
        System.out.println("Usage:");
        System.out.println("      -s (in order to serialize)");
        System.out.println("      -d (in order to deserialize)");
    }
}
