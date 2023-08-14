public class T6422215 extends ToolTester {
    void test(String... args) {
        try {
            fm.setLocation(CLASS_OUTPUT,
                           Collections.singleton(new File(test_src, "no_such_dir")));
            throw new AssertionError("Expected exception not thrown");
        } catch (IOException e) {
            System.out.println("OK: caught expected exception: " + e.getLocalizedMessage());
        }
    }
    public static void main(String... args) {
        new T6422215().test(args);
    }
}
