public class GraphicsTests {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(GraphicsTests.class.getName());
        suite.addTestSuite(BitmapTest.class);
        suite.addTestSuite(TypefaceTest.class);
        return suite;
    }
}
