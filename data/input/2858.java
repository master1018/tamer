public class GetResourceAsStream extends Common {
    public static void main (String args[]) throws Exception {
        String workdir = System.getProperty("test.classes");
        if (workdir == null) {
            workdir = args[0];
        }
        File srcfile = new File (workdir, "foo.jar");
        File testfile = new File (workdir, "test.jar");
        copyFile (srcfile, testfile);
        test (testfile, false, false);
        copyFile (srcfile, testfile);
        test (testfile, true, false);
        copyFile (srcfile, testfile);
        test (testfile, true, true);
        File testdir= new File (workdir, "testdir");
        File srcdir= new File (workdir, "test3");
        copyDir (srcdir, testdir);
        test (testdir, true, false);
    }
    static void test (File file, boolean loadclass, boolean readall)
        throws Exception
    {
        URL[] urls = new URL[] {file.toURL()};
        System.out.println ("Doing tests with URL: " + urls[0]);
        URLClassLoader loader = new URLClassLoader (urls);
        if (loadclass) {
            Class testclass = loadClass ("com.foo.TestClass", loader, true);
        }
        InputStream s = loader.getResourceAsStream ("hello.txt");
        s.read();
        if (readall) {
            while (s.read() != -1) ;
            s.close();
        }
        loader.close ();
        InputStream s1 = loader.getResourceAsStream("bye.txt");
        if (s1 != null) {
            throw new RuntimeException ("closed loader returned resource");
        }
        rm_minus_rf (file);
        System.out.println (" ... OK");
    }
}
