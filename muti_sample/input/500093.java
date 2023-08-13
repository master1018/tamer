@TestTargetClass(android.webkit.MimeTypeMap.class)
public class MimeTypeMapTest extends AndroidTestCase {
    private MimeTypeMap mMimeTypeMap;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMimeTypeMap = MimeTypeMap.getSingleton();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFileExtensionFromUrl",
            args = {String.class}
        )
    })
    @ToBeFixed(explanation="Returns part of the domain name if the optional URL path is missing")
    public void testGetFileExtensionFromUrl() {
        assertEquals("html", MimeTypeMap.getFileExtensionFromUrl("http:
        assertEquals("html", MimeTypeMap.getFileExtensionFromUrl("http:
        assertEquals("", MimeTypeMap.getFileExtensionFromUrl("http:
        assertEquals("", MimeTypeMap.getFileExtensionFromUrl("https:
        assertEquals("", MimeTypeMap.getFileExtensionFromUrl(null));
        assertEquals("", MimeTypeMap.getFileExtensionFromUrl(""));
        assertEquals("", MimeTypeMap.getFileExtensionFromUrl("http:
}
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasMimeType",
            args = {String.class}
        )
    })
    public void testHasMimeType() {
        assertTrue(mMimeTypeMap.hasMimeType("audio/mpeg"));
        assertTrue(mMimeTypeMap.hasMimeType("text/plain"));
        assertFalse(mMimeTypeMap.hasMimeType("some_random_string"));
        assertFalse(mMimeTypeMap.hasMimeType(""));
        assertFalse(mMimeTypeMap.hasMimeType(null));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMimeTypeFromExtension",
            args = {String.class}
        )
    })
    public void testGetMimeTypeFromExtension() {
        assertEquals("audio/mpeg", mMimeTypeMap.getMimeTypeFromExtension("mp3"));
        assertEquals("application/zip", mMimeTypeMap.getMimeTypeFromExtension("zip"));
        assertNull(mMimeTypeMap.getMimeTypeFromExtension("some_random_string"));
        assertNull(mMimeTypeMap.getMimeTypeFromExtension(null));
        assertNull(mMimeTypeMap.getMimeTypeFromExtension(""));
}
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasExtension",
            args = {String.class}
        )
    })
    public void testHasExtension() {
        assertTrue(mMimeTypeMap.hasExtension("mp3"));
        assertTrue(mMimeTypeMap.hasExtension("zip"));
        assertFalse(mMimeTypeMap.hasExtension("some_random_string"));
        assertFalse(mMimeTypeMap.hasExtension(""));
        assertFalse(mMimeTypeMap.hasExtension(null));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getExtensionFromMimeType",
            args = {String.class}
        )
    })
    public void testGetExtensionFromMimeType() {
        assertEquals("png", mMimeTypeMap.getExtensionFromMimeType("image/png"));
        assertEquals("zip", mMimeTypeMap.getExtensionFromMimeType("application/zip"));
        assertNull(mMimeTypeMap.getExtensionFromMimeType("some_random_string"));
        assertNull(mMimeTypeMap.getExtensionFromMimeType(null));
        assertNull(mMimeTypeMap.getExtensionFromMimeType(""));
}
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getSingleton",
            args = {}
        )
    })
    public void testGetSingleton() {
        MimeTypeMap firstMimeTypeMap = MimeTypeMap.getSingleton();
        MimeTypeMap secondMimeTypeMap = MimeTypeMap.getSingleton();
        assertSame(firstMimeTypeMap, secondMimeTypeMap);
    }
}
