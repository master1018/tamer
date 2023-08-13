@TestTargetClass(Intent.class)
public class AvailableIntentsTest extends AndroidTestCase {
    private static final String NORMAL_URL = "http:
    private static final String SECURE_URL = "https:
    private void assertCanBeHandled(final Intent intent) {
        PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);
        assertNotNull(resolveInfoList);
        assertTrue(resolveInfoList.size() > 0);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Intent",
        args = {java.lang.String.class, android.net.Uri.class}
    )
    public void testViewNormalUrl() {
        Uri uri = Uri.parse(NORMAL_URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        assertCanBeHandled(intent);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Intent",
        args = {java.lang.String.class, android.net.Uri.class}
    )
    public void testViewSecureUrl() {
        Uri uri = Uri.parse(SECURE_URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        assertCanBeHandled(intent);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Intent",
        args = {java.lang.String.class, android.net.Uri.class}
    )
    public void testWebSearchNormalUrl() {
        Uri uri = Uri.parse(NORMAL_URL);
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, uri);
        assertCanBeHandled(intent);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Intent",
        args = {java.lang.String.class, android.net.Uri.class}
    )
    public void testWebSearchSecureUrl() {
        Uri uri = Uri.parse(SECURE_URL);
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, uri);
        assertCanBeHandled(intent);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Intent",
        args = {java.lang.String.class, android.net.Uri.class}
    )
    public void testWebSearchPlainText() {
        String searchString = "where am I?";
        Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        intent.putExtra(SearchManager.QUERY, searchString);
        assertCanBeHandled(intent);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Intent",
        args = {java.lang.String.class, android.net.Uri.class}
    )
    public void testCallPhoneNumber() {
        Uri uri = Uri.parse("tel:2125551212");
        Intent intent = new Intent(Intent.ACTION_CALL, uri);
        assertCanBeHandled(intent);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Intent",
        args = {java.lang.String.class, android.net.Uri.class}
    )
    public void testDialPhoneNumber() {
        Uri uri = Uri.parse("tel:(212)5551212");
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        assertCanBeHandled(intent);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "Intent",
        args = {java.lang.String.class, android.net.Uri.class}
    )
    public void testDialVoicemail() {
        Uri uri = Uri.parse("voicemail:");
        Intent intent = new Intent(Intent.ACTION_DIAL, uri);
        assertCanBeHandled(intent);
    }
}
