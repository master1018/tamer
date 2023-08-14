public class ExpatPerformanceTest extends AndroidTestCase {
    private static final String TAG = ExpatPerformanceTest.class.getSimpleName();
    private byte[] mXmlBytes;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        InputStream in = mContext.getResources().openRawResource(R.raw.youtube);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
        mXmlBytes = out.toByteArray();
        Log.i("***", "File size: " + (mXmlBytes.length / 1024) + "k");
    }
    @LargeTest
    public void testPerformance() throws Exception {
            runJavaPullParser();
            runSax();
            runExpatPullParser();
    }
    private InputStream newInputStream() {
        return new ByteArrayInputStream(mXmlBytes);
    }
    private void runSax() throws IOException, SAXException {
        long start = System.currentTimeMillis();
        Xml.parse(newInputStream(), Xml.Encoding.UTF_8, new DefaultHandler());
        long elapsed = System.currentTimeMillis() - start;
        Log.i(TAG, "expat SAX: " + elapsed + "ms");
    }
    private void runExpatPullParser() throws XmlPullParserException, IOException {
        long start = System.currentTimeMillis();
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(newInputStream(), "UTF-8");
        withPullParser(pullParser);
        long elapsed = System.currentTimeMillis() - start;
        Log.i(TAG, "expat pull: " + elapsed + "ms");
    }
    private void runJavaPullParser() throws XmlPullParserException, IOException {
        XmlPullParser pullParser;
        long start = System.currentTimeMillis();
        pullParser = new KXmlParser();
        pullParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
        pullParser.setInput(newInputStream(), "UTF-8");
        withPullParser(pullParser);
        long elapsed = System.currentTimeMillis() - start;
        Log.i(TAG, "java pull parser: " + elapsed + "ms");
    }
    private static void withPullParser(XmlPullParser pullParser)
            throws IOException, XmlPullParserException {
        int eventType = pullParser.next();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    pullParser.getName();
                    break;
                case XmlPullParser.END_TAG:
                    pullParser.getName();
                    break;
                case XmlPullParser.TEXT:
                    pullParser.getText();
                    break;
            }
            eventType = pullParser.next();
        }
    }
}
