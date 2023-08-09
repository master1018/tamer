@TestTargetClass(android.provider.Contacts.Settings.class)
public class Contacts_SettingsTest extends InstrumentationTestCase {
    private ContentResolver mContentResolver;
    private IContentProvider mProvider;
    private ArrayList<ContentValues> mSettingBackup;
    @ToBeFixed(explanation = "The URL: content:
            " deleting operation, that makes the table's status can't be recovered.")
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContentResolver = getInstrumentation().getTargetContext().getContentResolver();
        mProvider = mContentResolver.acquireProvider(Contacts.AUTHORITY);
        mSettingBackup = new ArrayList<ContentValues>();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which access setting",
            method = "getSetting",
            args = {android.content.ContentResolver.class, java.lang.String.class, 
                    java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test methods which access setting",
            method = "setSetting",
            args = {android.content.ContentResolver.class, java.lang.String.class, 
                    java.lang.String.class, java.lang.String.class}
        )
    })
    public void testAccessSetting() {
        String key1 = "key 1";
        String value1 = "value 1";
        String key2 = "key 2";
        String value2 = "value 2";
        Settings.setSetting(mContentResolver, "account", key1, value1);
        Settings.setSetting(mContentResolver, "account", key2, value2);
        assertEquals(value1, Settings.getSetting(mContentResolver, "account", key1));
        assertEquals(value2, Settings.getSetting(mContentResolver, "account", key2));
        assertNull(Settings.getSetting(mContentResolver, "account", "key not exist"));
        Settings.setSetting(mContentResolver, "account", key1, value2);
        assertEquals(value2, Settings.getSetting(mContentResolver, "account", key1));
    }
}
