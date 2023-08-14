@TestTargetClass(NodeChangeListener.class)
public class NodeChangeListenerTest extends TestCase {
    NodeChangeListener l;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestEnvironment.reset();
        l = new NodeChangeListenerImpl();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Testing Interface",
        method = "childAdded",
        args = {java.util.prefs.NodeChangeEvent.class}
    )
    public void testChildAdded() {
        l.childAdded(new NodeChangeEvent(Preferences.userRoot(), Preferences
                .userRoot()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Testing Interface",
        method = "childRemoved",
        args = {java.util.prefs.NodeChangeEvent.class}
    )
    public void testChildRemoved() {
        l.childRemoved(new NodeChangeEvent(Preferences.userRoot(), Preferences
                .userRoot()));
    }
    public static class NodeChangeListenerImpl implements NodeChangeListener {
        public void childAdded(NodeChangeEvent e) {
        }
        public void childRemoved(NodeChangeEvent e) {
        }
    }
}
