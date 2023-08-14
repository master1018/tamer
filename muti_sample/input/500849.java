@TestTargetClass(AssetManager.class)
public class AssetManagerTest extends AndroidTestCase{
    private AssetManager mAssets;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mAssets = mContext.getAssets();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "open",
            args = {java.lang.String.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "open",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openFd",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openNonAssetFd",
            args = {int.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openNonAssetFd",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openXmlResourceParser",
            args = {int.class, java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "openXmlResourceParser",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLocales",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "list",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.NOT_FEASIBLE,
            notes = "Cannot close. Because when it is closed, it won't be opened again."
                     + " This will cause other testcases fail",
            method = "close",
            args = {}
        )
    })
    public void testAssetOperations() throws IOException, XmlPullParserException {
        final Resources res = getContext().getResources();
        final TypedValue value = new TypedValue();
        res.getValue(R.raw.text, value, true);
        final String fileName = "text.txt";
        InputStream inputStream = mAssets.open(fileName);
        assertNotNull(inputStream);
        final String expect = "OneTwoThreeFourFiveSixSevenEightNineTen";
        assertContextEquals(expect, inputStream);
        inputStream = mAssets.open(fileName, AssetManager.ACCESS_BUFFER);
        assertNotNull(inputStream);
        assertContextEquals(expect, inputStream);
        AssetFileDescriptor assetFileDes = mAssets.openFd(fileName);
        assertNotNull(assetFileDes);
        assertContextEquals(expect, assetFileDes.createInputStream());
        assetFileDes = mAssets.openNonAssetFd(value.string.toString());
        assertNotNull(assetFileDes);
        assertContextEquals(expect, assetFileDes.createInputStream());
        assetFileDes = mAssets.openNonAssetFd(value.assetCookie, value.string.toString());
        assertNotNull(assetFileDes);
        assertContextEquals(expect, assetFileDes.createInputStream());
        XmlResourceParser parser = mAssets.openXmlResourceParser("AndroidManifest.xml");
        assertNotNull(parser);
        XmlUtils.beginDocument(parser, "manifest");
        parser = mAssets.openXmlResourceParser(0, "AndroidManifest.xml");
        assertNotNull(parser);
        beginDocument(parser, "manifest");
        String[] files = mAssets.list("");
        boolean result = false;
        for (int i = 0; i < files.length; i++) {
            if (files[i].equals(fileName)) {
                result = true;
                break;
            }
        }
        assertTrue(result);
        try {
            mAssets.open("notExistFile.txt", AssetManager.ACCESS_BUFFER);
            fail("test open(String, int) failed");
        } catch (IOException e) {
        }
        try {
            mAssets.openFd("notExistFile.txt");
            fail("test openFd(String) failed");
        } catch (IOException e) {
        }
        try {
            mAssets.openNonAssetFd(0, "notExistFile.txt");
            fail("test openNonAssetFd(int, String) failed");
        } catch (IOException e) {
        }
        try {
            mAssets.openXmlResourceParser(0, "notExistFile.txt");
            fail("test openXmlResourceParser(int, String) failed");
        } catch (IOException e) {
        }
        assertNotNull(mAssets.getLocales());
    }
    private void assertContextEquals(final String expect, final InputStream inputStream)
            throws IOException {
        final BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream));
        final String result = bf.readLine();
        inputStream.close();
        assertNotNull(result);
        assertEquals(expect, result);
    }
    private void beginDocument(final XmlPullParser parser,final  String firstElementName)
            throws XmlPullParserException, IOException {
        int type;
        while ((type = parser.next()) != XmlPullParser.START_TAG) {
        }
        if (type != XmlPullParser.START_TAG) {
            fail("No start tag found");
        }
        assertEquals(firstElementName, parser.getName());
    }
}
