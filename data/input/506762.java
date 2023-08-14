@TestTargetClass(android.provider.Settings.SettingNotFoundException.class)
public class Settings_SettingNotFoundExceptionTest extends AndroidTestCase {
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Settings.SettingNotFoundException",
        args = {java.lang.String.class}
    )
    public void testConstructor() {
        new SettingNotFoundException("Setting not found exception.");
        new SettingNotFoundException(null);
    }
}
