@TestTargetClass(Set.class)
public class Support_SetTest extends junit.framework.TestCase {
    Set<Integer> set; 
    public Support_SetTest(String p1) {
        super(p1);
    }
    public Support_SetTest(String p1, Set<Integer> s) {
        super(p1);
        set = s;
    }
    @Override
    public void runTest() {
        assertTrue("Set Test - Adding a duplicate element changed the set",
                !set.add(new Integer(50)));
        assertTrue("Set Test - Removing an element did not change the set", set
                .remove(new Integer(50)));
        assertTrue(
                "Set Test - Adding and removing a duplicate element failed to remove it",
                !set.contains(new Integer(50)));
        set.add(new Integer(50));
        new Support_CollectionTest("", set).runTest();
    }
}
