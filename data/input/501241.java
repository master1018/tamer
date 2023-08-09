@TestTargetClass(LoggingMXBean.class) 
public class LoggingMXBeanTest extends TestCase {
    private MockLoggingMXBean m = null;
    protected void setUp() throws Exception {
        super.setUp();
         m = new MockLoggingMXBean();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLoggerLevel",
        args = {java.lang.String.class}
    )
    public void testGetLoggerLevel() {
        assertNull(m.getLoggerLevel(null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getLoggerNames",
        args = {}
    )
          public void testGetLoggerNames() {
                assertNull(m.getLoggerNames());
          }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getParentLoggerName",
        args = {java.lang.String.class}
    )
          public void testGetParentLoggerName() {
              assertNull(m.getParentLoggerName(null));
          }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "setLoggerLevel",
        args = {java.lang.String.class, java.lang.String.class}
    )
          public void testSetLoggerLevel() {
            try{
                m.setLoggerLevel(null,null);
            }
            catch (Exception e){
                throw new AssertionError();
            }
          }
    private class MockLoggingMXBean implements LoggingMXBean {
        public String getLoggerLevel(String loggerName) {
            return null;
        }
        public List<String> getLoggerNames() {
            return null;
        }
        public String getParentLoggerName(String loggerName) {
            return null;
        }
        public void setLoggerLevel(String loggerName, String levelName) {
        }
    }
}
