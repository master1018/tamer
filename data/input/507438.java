public class ProviderPermissionTest extends AndroidTestCase {
    public void testReadContacts() {
        assertReadingContentUriRequiresPermission(Contacts.People.CONTENT_URI,
                android.Manifest.permission.READ_CONTACTS);
    }
    public void testWriteContacts() {
        assertWritingContentUriRequiresPermission(Contacts.People.CONTENT_URI,
                android.Manifest.permission.WRITE_CONTACTS);
    }
    public void testReadCallLog() {
        assertReadingContentUriRequiresPermission(CallLog.CONTENT_URI,
                android.Manifest.permission.READ_CONTACTS);
    }
    public void testWriteCallLog() {
        assertWritingContentUriRequiresPermission(CallLog.CONTENT_URI,
                android.Manifest.permission.WRITE_CONTACTS);
    }
    public void testWriteSettings() {
        assertWritingContentUriRequiresPermission(android.provider.Settings.System.CONTENT_URI,
                android.Manifest.permission.WRITE_SETTINGS);
    }
    public void testReadBookmarks() {
        assertReadingContentUriRequiresPermission(Browser.BOOKMARKS_URI,
                android.Manifest.permission.READ_HISTORY_BOOKMARKS);
    }
    public void testWriteBookmarks() {
        assertWritingContentUriRequiresPermission(Browser.BOOKMARKS_URI,
                android.Manifest.permission.WRITE_HISTORY_BOOKMARKS);
    }
    public void testReadBrowserHistory() {
        assertReadingContentUriRequiresPermission(Browser.SEARCHES_URI,
                android.Manifest.permission.READ_HISTORY_BOOKMARKS);
    }
    public void testWriteBrowserHistory() {
        assertWritingContentUriRequiresPermission(Browser.SEARCHES_URI,
                android.Manifest.permission.WRITE_HISTORY_BOOKMARKS);
    }
}
