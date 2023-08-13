@TestTargetClass(PreferencesFactory.class)
public class PreferencesFactoryTest extends TestCase {
    PreferencesFactory f;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        f = new PreferencesFactoryImpl();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Testing Interface",
        method = "userRoot",
        args = {}
    )
    public void testUserRoot() {
        assertNull(f.userRoot());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Testing Interface",
        method = "systemRoot",
        args = {}
    )
    public void testSystemRoot() {
        assertNull(f.systemRoot());
    }
    public static class PreferencesFactoryImpl implements PreferencesFactory {
        public Preferences userRoot() {
            return null;
        }
        public Preferences systemRoot() {
            return null;
        }
    }
}
