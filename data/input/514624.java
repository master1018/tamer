public class ContentTests {
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(ContentTests.class.getName());
        suite.addTestSuite(AssetTest.class);
        return suite;
    }
}
