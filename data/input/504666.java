public class CalendarTests extends TestBrowserActivity {
    @Override
    public final TestSuite getTopTestSuite() {
        return suite();
    }
    public static TestSuite suite() {
        TestSuite suite = new TestSuite(CalendarTests.class.getName());
        suite.addTestSuite(FormatDateRangeTest.class);
        suite.addTestSuite(WeekNumberTest.class);
        return suite;
    }
}
