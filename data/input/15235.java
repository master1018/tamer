public class LoadNullClass {
    public static void main(String[] args) throws Exception {
        File f = new File(System.getProperty("test.src", "."));
        FileClassLoader cl = new FileClassLoader
            (new URL[]{new URL("file:"+ f.getAbsolutePath())});
        cl.testFindLoadedClass(null);
    }
}
class FileClassLoader extends URLClassLoader {
    public FileClassLoader(URL[] urls) {
        super(urls);
    }
    public void testFindLoadedClass(String name) throws Exception {
        super.findLoadedClass(name);
    }
}
