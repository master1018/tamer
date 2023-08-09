@TestTargetClass(NodeChangeEvent.class)
public class NodeChangeEventTest extends TestCase {
    NodeChangeEvent event;
    protected void setUp() throws Exception {
        super.setUp();
        TestEnvironment.reset();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NodeChangeEvent",
        args = {java.util.prefs.Preferences.class, java.util.prefs.Preferences.class}
    )
    public void testConstructor() {
        event = new NodeChangeEvent(Preferences.systemRoot(), Preferences
                .userRoot());
        assertSame(Preferences.systemRoot(), event.getParent());
        assertSame(Preferences.userRoot(), event.getChild());
        assertSame(Preferences.systemRoot(), event.getSource());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "NodeChangeEvent",
        args = {java.util.prefs.Preferences.class, java.util.prefs.Preferences.class}
    )
    public void testConstructorNullParam() {
        try {
            event = new NodeChangeEvent(null, Preferences.userRoot());
            fail();
        } catch (IllegalArgumentException e) {
        }
        event = new NodeChangeEvent(Preferences.systemRoot(), null);
        assertSame(Preferences.systemRoot(), event.getParent());
        assertNull(event.getChild());
        assertSame(Preferences.systemRoot(), event.getSource());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Verifies serialization",
        method = "!Serialization",
        args = {}
    )
    public void testSerialization() throws Exception {
        event = new NodeChangeEvent(Preferences.systemRoot(), null);
        try {
            SerializationTest.copySerializable(event);
            fail("No expected NotSerializableException");
        } catch (NotSerializableException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test is correct, functionality checked in separate Mock class.",
        method = "getChild",
        args = {}
    )
    public void testGetChild() throws BackingStoreException {
        AbstractPreferences parent = (AbstractPreferences) Preferences
                .userNodeForPackage(Preferences.class);
        AbstractPreferences pref = (AbstractPreferences) parent.node("mock");
        MockNodeChangeListener nl = new MockNodeChangeListener(
                MockNodeChangeListener.TEST_GET_CHILD);
        try {
            pref.addNodeChangeListener(nl);
            Preferences child1 = pref.node("mock1");
            nl.waitForEvent();
            assertEquals(1, nl.getAdded());
            assertTrue(nl.getAddResult());
            nl.reset();
            child1.removeNode();
            nl.waitForEvent();
            assertEquals(1, nl.getRemoved());
            assertTrue(nl.getRemoveResult());
            nl.reset();
        } finally {
            pref.removeNodeChangeListener(nl);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test is correct, functionality checked in separate Mock class.",
        method = "getParent",
        args = {}
    )
    public void testGetParent() throws BackingStoreException {
        AbstractPreferences parent = (AbstractPreferences) Preferences
                .userNodeForPackage(Preferences.class);
        AbstractPreferences pref = (AbstractPreferences) parent.node("mock");
        MockNodeChangeListener nl = new MockNodeChangeListener(
                MockNodeChangeListener.TEST_GET_CHILD);
        try {
            pref.addNodeChangeListener(nl);
            Preferences child1 = pref.node("mock1");
            nl.waitForEvent();
            assertEquals(1, nl.getAdded());
            assertTrue(nl.getAddResult());
            nl.reset();
            child1.removeNode();
            nl.waitForEvent();
            assertEquals(1, nl.getRemoved());
            assertTrue(nl.getRemoveResult());
            nl.reset();
        } finally {
            pref.removeNodeChangeListener(nl);
        }
    }
}
