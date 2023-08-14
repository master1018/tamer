public class DefineClass extends ClassLoader {
    public static void main(String args[]) {
        DefineClass t = new DefineClass();
        t.run(args);
    }
    public void run(String args[]) {
        Class n;
        byte b[] = new byte[10000];
        int len = 0;
        String cdir;
        String cfile;
        cdir = System.getProperty("test.classes", ".");
        cfile = cdir + java.io.File.separator + "HelloWorld.class";
        try {
            FileInputStream fis = new FileInputStream(cfile);
            int nbytes;
            do {
                nbytes = fis.read(b, len, b.length-len);
                if ( nbytes > 0 ) {
                    len += nbytes;
                }
            } while ( nbytes > 0 );
        } catch ( Throwable x ) {
            System.err.println("Cannot find " + cfile);
            x.printStackTrace();
        }
        n = defineClass(null, b, 0, len);
        try {
            n.newInstance();
        } catch ( Throwable x ) {
            x.printStackTrace();
        }
    }
}
