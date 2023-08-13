public class AccessPermissionWithDiffSigTest extends AndroidTestCase {
    public void testReadProviderWithDiff() {
        assertReadingContentUriRequiresPermission(Uri.parse("content:
                "com.android.cts.permissionWithSignature");
    }
}
