public class LogTest extends TestCase {
    private static final String PROPERTY_TAG = "log.tag.LogTest";
    private static final String LOG_TAG = "LogTest";
    public void testLogTestDummy() {
      return;
    }
    public static class PerformanceTest extends TestCase implements PerformanceTestCase {
        private static final int ITERATIONS = 1000;
        @Override
        public void setUp() {
            SystemProperties.set(LOG_TAG, "VERBOSE");
        }
        public boolean isPerformanceOnly() {
            return true;
        }
        public int startPerformance(PerformanceTestCase.Intermediates intermediates) {
            intermediates.setInternalIterations(ITERATIONS * 10);
            return 0;
        }
        public void testIsLoggable() {
            boolean canLog = false;
            for (int i = ITERATIONS - 1; i >= 0; i--) {
                canLog = Log.isLoggable(LOG_TAG, Log.VERBOSE);
                canLog = Log.isLoggable(LOG_TAG, Log.VERBOSE);
                canLog = Log.isLoggable(LOG_TAG, Log.VERBOSE);
                canLog = Log.isLoggable(LOG_TAG, Log.VERBOSE);
                canLog = Log.isLoggable(LOG_TAG, Log.VERBOSE);
                canLog = Log.isLoggable(LOG_TAG, Log.VERBOSE);
                canLog = Log.isLoggable(LOG_TAG, Log.VERBOSE);
                canLog = Log.isLoggable(LOG_TAG, Log.VERBOSE);
                canLog = Log.isLoggable(LOG_TAG, Log.VERBOSE);
                canLog = Log.isLoggable(LOG_TAG, Log.VERBOSE);
            }
        }
    }
}
