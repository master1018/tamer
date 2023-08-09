public class DynamicTest {
    public static void main(String args[]) throws Exception {
        Application app = new Application();
        if (app.doSomething() != 1) {
            throw new RuntimeException("Test configuration error - doSomething should return 1");
        }
        JarFile jf = new JarFile("Tracer.jar");
        Agent.getInstrumentation().appendToBootstrapClassLoaderSearch(jf);
        File f = new File("InstrumentedApplication.bytes");
        int len = (int)f.length();
        byte[] def = new byte[len];
        FileInputStream fis = new FileInputStream(f);
        int nread = 0;
        do {
            int n = fis.read(def, nread, len-nread);
            if (n > 0) {
                nread += n;
            }
        } while (nread < len);
        ClassDefinition classDefs = new ClassDefinition(Application.class, def);
        Agent.getInstrumentation().redefineClasses(new ClassDefinition[] { classDefs } );
        int res = app.doSomething();
        if (res != 3) {
            throw new RuntimeException("FAIL: redefined Application returned: " + res);
        }
        System.out.println("PASS: Test passed.");
    }
}
