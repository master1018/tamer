public class SetDefaultProvider {
    public static void main(String[] args) throws Exception {
        Class<?> c = FileSystems.getDefault().provider().getClass();
        Class<?> expected = Class.forName("TestProvider", false,
            ClassLoader.getSystemClassLoader());
        if (c != expected)
            throw new RuntimeException();
    }
}
