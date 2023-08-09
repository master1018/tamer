public class Loader extends ClassLoader {
    public Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException {
        Class c = null;
        try {
            c = findSystemClass(name);
        } catch (ClassNotFoundException cnfe) {
        }
        if (c == null) {
            if (!name.equals("Loadee"))
                throw new Error("java.lang.ClassLoader.findSystemClass() " +
                                "did not find class " + name);
            byte[] b = locateBytes();
            c = defineClass(name, b, 0, b.length);
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }
    private byte[] locateBytes() {
        try {
            File f   = new File(System.getProperty("test.src", "."),
                                "Loadee.classfile");
            long l   = f.length();
            byte[] b = new byte[(int)l];
            DataInputStream in =
                new DataInputStream(new FileInputStream(f));
            in.readFully(b);
            return b;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new Error("Test failed due to IOException!");
        }
    }
    private static final int FIND      = 0x1;
    private static final int RESOURCE  = 0x2;
    private static final int RESOURCES = 0x4;
    public static void main(String[] args) throws Exception {
        int tests = FIND | RESOURCE | RESOURCES;
        if (args.length == 1 && args[0].equals("-1.1")) {
            tests &= ~RESOURCES; 
        }
        if ((tests & FIND) == FIND) {
            report("findSystemClass()");
            ClassLoader l = new Loader();
            Class       c = l.loadClass("Loadee");
            Object      o = c.newInstance();
        }
        if ((tests & RESOURCE) == RESOURCE) {
            report("getSystemResource()");
            URL u = getSystemResource("Loadee.resource");
            if (u == null)
                throw new Exception
                    ("java.lang.ClassLoader.getSystemResource() test failed!");
        }
        if ((tests & RESOURCES) == RESOURCES) {
            report("getSystemResources()");
            java.util.Enumeration e =
                getSystemResources("java/lang/Object.class");
            HashSet hs = new HashSet();
            while (e.hasMoreElements()) {
                URL u = (URL)e.nextElement();
                if (u == null)
                    break;
                System.out.println("url: " + u);
                hs.add(u);
            }
            if (hs.size() != 2) {
                throw
                    new Exception("java.lang.ClassLoader.getSystemResources()"+
                                  " did not find all resources");
            }
        }
    }
    private static void report(String s) {
        System.out.println("Testing java.lang.ClassLoader." + s + " ...");
    }
}
