@TestTargetClass(PreferenceChangeListener.class)
public class PreferenceChangeListenerTest extends TestCase {
    PreferenceChangeListener l;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        l = new PreferenceChangeListenerImpl();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Testing Interface",
        method = "preferenceChange",
        args = {java.util.prefs.PreferenceChangeEvent.class}
    )
    public void testPreferenceChange() {
        l.preferenceChange(new PreferenceChangeEvent(Preferences.userRoot(),
                "", ""));
    }
    public static class PreferenceChangeListenerImpl implements
            PreferenceChangeListener {
        public void preferenceChange(PreferenceChangeEvent pce) {
        }
    }
}
