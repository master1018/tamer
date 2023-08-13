public class AnnotateClass {
    public static void main (String argv[]) {
        System.err.println("\nRegression test for verification " +
                           "of invocation annotateClass/replaceObject " +
                           "methods \n");
        try {
            FileOutputStream ostream = new FileOutputStream("subtest1.tmp");
            try {
                TestOutputStream p = new TestOutputStream(ostream);
                p.writeObject(System.out);
                p.writeObject(System.err);
                p.writeObject(new PrintStream(ostream));
                p.flush();
            } finally {
                ostream.close();
            }
            FileInputStream istream = new FileInputStream("subtest1.tmp");
            try {
                TestInputStream q = new TestInputStream(istream);
                PrintStream out = (PrintStream)q.readObject();
                PrintStream err = (PrintStream)q.readObject();
                Object other = q.readObject();
                if (out != System.out) {
                    System.err.println(
                        "\nTEST FAILED: System.out not read correctly");
                    throw new Error();
                }
                if (err != System.err) {
                    System.err.println(
                        "\nTEST FAILED: System.err not read correctly");
                    throw new Error();
                }
                if (other != null) {
                    System.err.println(
                        "\nTEST FAILED: Non-system PrintStream should have " +
                        "been written/read as null");
                    throw new Error();
                }
            } finally {
                istream.close();
            }
            System.err.println("\nTEST PASSED");
        } catch (Exception e) {
            System.err.print("TEST FAILED: ");
            e.printStackTrace();
            throw new Error();
        }
    }
}
class TestOutputStream extends ObjectOutputStream {
    TestOutputStream(OutputStream out)  throws IOException {
        super(out);
        enableReplaceObject(true);
    }
    protected void annotateClass(Class cl) throws IOException {
        this.writeUTF("magic");
    }
    protected Object replaceObject(Object obj)
        throws IOException
    {
        if (obj instanceof PrintStream) {
            return new StdStream((PrintStream)obj);
        }
        return obj;
    }
}
class TestInputStream extends ObjectInputStream {
    TestInputStream(InputStream in)  throws IOException  {
        super(in);
        enableResolveObject(true);
    }
    protected Class<?> resolveClass(ObjectStreamClass classdesc)
        throws ClassNotFoundException, IOException
    {
        try {
            String s = readUTF();
            if (!(s.equals("magic"))) {
                System.err.println(
                    "\nTEST FAILED: Bad magic number");
                throw new Error();
            }
        } catch (IOException ee) {
            System.err.println(
                "\nTEST FAILED: I/O Exception");
            throw new Error();
        }
        return super.resolveClass(classdesc);
    }
    protected Object resolveObject(Object obj) {
        if (obj instanceof StdStream) {
            return ((StdStream)obj).getStream();
        }
        return obj;
    }
}
class StdStream implements java.io.Serializable {
    private int stream = 0;
    public StdStream(PrintStream s) {
        if (s == System.out) {
            stream = 1;
        } else if (s == System.err) {
            stream = 2;
        }
    }
    public PrintStream getStream() {
        if (stream == 1) {
            return System.out;
        } else if (stream == 2) {
            return System.err;
        } else {
            return null;
        }
    }
}
