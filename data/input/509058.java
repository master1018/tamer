@TestTargetClass(LauncherActivity.ListItem.class)
public class LauncherActivity_ListItemTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test ListItem",
        method = "LauncherActivity.ListItem",
        args = {}
    )
    public void testConstructor() {
        new LauncherActivity.ListItem();
    }
}
