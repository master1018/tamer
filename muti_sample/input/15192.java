public class ClassLoaderTest {
    private final static String BASE = System.getProperty("test.src", "./");
    public static void main(String[] args) throws Exception {
        Transform.init();
        File file = new File(BASE);
        URL[] urls = new URL[1];
        urls[0] = file.toURI().toURL();
        URLClassLoader ucl = new URLClassLoader(urls);
        Class c = ucl.loadClass("MyTransform");
        Constructor cons = c.getConstructor();
        Object o = cons.newInstance();
        try {
            Transform.register(MyTransform.URI, "MyTransform");
            throw new Exception("ClassLoaderTest failed");
        } catch (AlgorithmAlreadyRegisteredException e) {
        }
    }
}
