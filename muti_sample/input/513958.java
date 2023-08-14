@TestTargetClass(ThreadDeath.class) 
public class ThreadDeathTest extends junit.framework.TestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "ThreadDeath",
        args = {}
    )
    public void test_Constructor() {
        ThreadDeath td = new ThreadDeath();
        assertNull(td.getCause());
        assertNull(td.getMessage());
    }
}
