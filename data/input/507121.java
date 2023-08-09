public class RepoSourceTest extends TestCase {
    private static class MockMonitor implements ITaskMonitor {
        public void setResult(String resultFormat, Object... args) {
        }
        public void setProgressMax(int max) {
        }
        public void setDescription(String descriptionFormat, Object... args) {
        }
        public boolean isCancelRequested() {
            return false;
        }
        public void incProgress(int delta) {
        }
        public int getProgress() {
            return 0;
        }
        public boolean displayPrompt(String title, String message) {
            return false;
        }
        public ITaskMonitor createSubMonitor(int tickCount) {
            return null;
        }
    }
    private static class MockRepoSource extends RepoSource {
        public MockRepoSource() {
            super("fake-url", false );
        }
        public Document _findAlternateToolsXml(InputStream xml) {
            return super.findAlternateToolsXml(xml);
        }
        public boolean _parsePackages(Document doc, String nsUri, ITaskMonitor monitor) {
            return super.parsePackages(doc, nsUri, monitor);
        }
    }
    private MockRepoSource mSource;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mSource = new MockRepoSource();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mSource = null;
    }
    public void testFindAlternateToolsXml_Errors() {
        Document result = mSource._findAlternateToolsXml(null);
        assertNull(result);
        String str = "";
        ByteArrayInputStream input = new ByteArrayInputStream(str.getBytes());
        result = mSource._findAlternateToolsXml(input);
        assertNull(result);
        str = "Some random string, not even HTML nor XML";
        input = new ByteArrayInputStream(str.getBytes());
        result = mSource._findAlternateToolsXml(input);
        assertNull(result);
        str = "<html><head> " +
        "<meta http-equiv=\"content-type\" content=\"text/html;charset=utf-8\"> " +
        "<title>404 Not Found</title> " + "<style><!--" + "body {font-family: arial,sans-serif}" +
        "div.nav { ... blah blah more css here ... color: green}" +
        "
        "<body text=#000000 bgcolor=#ffffff> " +
        "<table border=0 cellpadding=2 cellspacing=0 width=100%><tr><td rowspan=3 width=1% nowrap> " +
        "<b><font face=times color=#0039b6 size=10>G</font><font face=times color=#c41200 size=10>o</font><font face=times color=#f3c518 size=10>o</font><font face=times color=#0039b6 size=10>g</font><font face=times color=#30a72f size=10>l</font><font face=times color=#c41200 size=10>e</font>&nbsp;&nbsp;</b> " +
        "<td>&nbsp;</td></tr> " +
        "<tr><td bgcolor=\"#3366cc\"><font face=arial,sans-serif color=\"#ffffff\"><b>Error</b></td></tr> " +
        "<tr><td>&nbsp;</td></tr></table> " + "<blockquote> " + "<H1>Not Found</H1> " +
        "The requested URL <code>/404</code> was not found on this server." + " " + "<p> " +
        "</blockquote> " +
        "<table width=100% cellpadding=0 cellspacing=0><tr><td bgcolor=\"#3366cc\"><img alt=\"\" width=1 height=4></td></tr></table> " +
        "</body></html> ";
        input = new ByteArrayInputStream(str.getBytes());
        result = mSource._findAlternateToolsXml(input);
        assertNull(result);
        str = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
        "<manifest xmlns:android=\"http:
        "    package=\"some.cool.app\" android:versionName=\"1.6.04\" android:versionCode=\"1604\">" +
        "    <application android:label=\"@string/app_name\" android:icon=\"@drawable/icon\"/>" +
        "</manifest>";
        input = new ByteArrayInputStream(str.getBytes());
        result = mSource._findAlternateToolsXml(input);
        assertNull(result);
    }
    public void testFindAlternateToolsXml_1() {
        InputStream xmlStream = this.getClass().getResourceAsStream(
                    "/com/android/sdklib/testdata/repository_sample_1.xml");
        Document result = mSource._findAlternateToolsXml(xmlStream);
        assertNotNull(result);
        assertTrue(mSource._parsePackages(result,
                SdkRepository.NS_SDK_REPOSITORY, new MockMonitor()));
        Package[] pkgs = mSource.getPackages();
        assertEquals(2, pkgs.length);
        for (Package p : pkgs) {
            assertEquals(ToolPackage.class, p.getClass());
            assertEquals(1, p.getArchives().length);
        }
    }
}
