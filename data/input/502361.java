public class AndroidPerformanceTests extends TestListActivity {
    @Override
    public String getTestSuite() {
        return "com.android.unit_tests.AndroidPerformanceTests$Suite";
    }
    public static class Suite {
        public static String[] children() {
            return new String[] {
                JavaPerformanceTests.class.getName(),
                PerformanceTests.class.getName(),
            };
        }
    }
}
