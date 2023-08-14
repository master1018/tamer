public class BridgeXmlBlockParserTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    public void testXmlBlockParser() throws Exception {
        XmlPullParser parser = new KXmlParser();
        parser = new BridgeXmlBlockParser(parser, null, false );
        InputStream input = this.getClass().getClassLoader().getResourceAsStream(
            "com/android/layoutlib/testdata/layout1.xml");
        parser.setInput(input, null );
        assertEquals(XmlPullParser.START_DOCUMENT, parser.next());
        assertEquals(XmlPullParser.START_TAG, parser.next());
        assertEquals("LinearLayout", parser.getName());
        assertEquals(XmlPullParser.TEXT, parser.next());
        assertEquals(XmlPullParser.START_TAG, parser.next());
        assertEquals("Button", parser.getName());
        assertEquals(XmlPullParser.TEXT, parser.next());
        assertEquals(XmlPullParser.END_TAG, parser.next());
        assertEquals(XmlPullParser.TEXT, parser.next());
        assertEquals(XmlPullParser.START_TAG, parser.next());
        assertEquals("View", parser.getName());
        assertEquals(XmlPullParser.END_TAG, parser.next());
        assertEquals(XmlPullParser.TEXT, parser.next());
        assertEquals(XmlPullParser.START_TAG, parser.next());
        assertEquals("TextView", parser.getName());
        assertEquals(XmlPullParser.END_TAG, parser.next());
        assertEquals(XmlPullParser.TEXT, parser.next());
        assertEquals(XmlPullParser.END_TAG, parser.next());
        assertEquals(XmlPullParser.END_DOCUMENT, parser.next());
    }
    @SuppressWarnings("unused")
    private void dump(Node node, String prefix) {
        Node n;
        String[] types = {
                "unknown",
                "ELEMENT_NODE",
                "ATTRIBUTE_NODE",
                "TEXT_NODE",
                "CDATA_SECTION_NODE",
                "ENTITY_REFERENCE_NODE",
                "ENTITY_NODE",
                "PROCESSING_INSTRUCTION_NODE",
                "COMMENT_NODE",
                "DOCUMENT_NODE",
                "DOCUMENT_TYPE_NODE",
                "DOCUMENT_FRAGMENT_NODE",
                "NOTATION_NODE"
        };
        String s = String.format("%s<%s> %s %s",
                prefix,
                types[node.getNodeType()],
                node.getNodeName(),
                node.getNodeValue() == null ? "" : node.getNodeValue().trim());
        System.out.println(s);
        n = node.getFirstChild();
        if (n != null) {
            dump(n, prefix + "- ");
        }
        n = node.getNextSibling();
        if (n != null) {
            dump(n, prefix);
        }
    }
}
