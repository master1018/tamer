public class NoWriteSecureSettingsPermissionTest extends AndroidTestCase {
    public void testWriteSecureSettings() {
        assertWritingContentUriRequiresPermission(android.provider.Settings.Secure.CONTENT_URI,
                android.Manifest.permission.WRITE_SECURE_SETTINGS);
    }
}
