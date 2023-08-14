public class SaxTest extends TestCase {
    public void testNoPrefixesNoNamespaces() throws Exception {
        parse(false, false, "<foo bar=\"baz\"/>", new DefaultHandler() {
            @Override public void startElement(String uri, String localName,
                    String qName, Attributes attributes) {
                assertEquals("", uri);
                assertEquals("", localName);
                assertEquals("foo", qName);
                assertEquals(1, attributes.getLength());
                assertEquals("", attributes.getURI(0));
                assertOneOf("bar", "", attributes.getLocalName(0));
                assertEquals("bar", attributes.getQName(0));
            }
        });
        parse(false, false, "<a:foo a:bar=\"baz\"/>", new DefaultHandler() {
            @Override public void startElement(String uri, String localName,
                    String qName, Attributes attributes) {
                assertEquals("", uri);
                assertEquals("", localName);
                assertEquals("a:foo", qName);
                assertEquals(1, attributes.getLength());
                assertEquals("", attributes.getURI(0));
                assertOneOf("a:bar", "", attributes.getLocalName(0));
                assertEquals("a:bar", attributes.getQName(0));
            }
        });
    }
    public void testNoPrefixesYesNamespaces() throws Exception {
        parse(false, true, "<foo bar=\"baz\"/>", new DefaultHandler() {
            @Override public void startElement(String uri, String localName,
                    String qName, Attributes attributes) {
                assertEquals("", uri);
                assertEquals("foo", localName);
                assertEquals("foo", qName);
                assertEquals(1, attributes.getLength());
                assertEquals("", attributes.getURI(0));
                assertEquals("bar", attributes.getLocalName(0));
                assertEquals("bar", attributes.getQName(0));
            }
        });
        parse(false, true, "<a:foo a:bar=\"baz\" xmlns:a=\"http:
            @Override public void startElement(String uri, String localName,
                    String qName, Attributes attributes) {
                assertEquals("http:
                assertEquals("foo", localName);
                assertEquals("a:foo", qName);
                assertEquals(1, attributes.getLength());
                assertEquals("http:
                assertEquals("bar", attributes.getLocalName(0));
                assertEquals("a:bar", attributes.getQName(0));
            }
        });
    }
    @KnownFailure("No xmlns attributes from Expat")
    public void testYesPrefixesYesNamespaces() throws Exception {
        parse(true, true, "<foo bar=\"baz\"/>", new DefaultHandler() {
            @Override public void startElement(String uri, String localName,
                    String qName, Attributes attributes) {
                assertEquals("", uri);
                assertEquals("foo", localName);
                assertEquals("foo", qName);
                assertEquals(1, attributes.getLength());
                assertEquals("", attributes.getURI(0));
                assertEquals("bar", attributes.getLocalName(0));
                assertEquals("bar", attributes.getQName(0));
            }
        });
        parse(true, true, "<a:foo a:bar=\"baz\" xmlns:a=\"http:
            @Override public void startElement(String uri, String localName,
                    String qName, Attributes attributes) {
                assertEquals("http:
                assertEquals("foo", localName);
                assertEquals("a:foo", qName);
                assertEquals(2, attributes.getLength());
                assertEquals("http:
                assertEquals("bar", attributes.getLocalName(0));
                assertEquals("a:bar", attributes.getQName(0));
                assertEquals("", attributes.getURI(1));
                assertEquals("", attributes.getLocalName(1));
                assertEquals("xmlns:a", attributes.getQName(1));
            }
        });
    }
    public void testYesPrefixesNoNamespaces() throws Exception {
        parse(true, false, "<foo bar=\"baz\"/>", new DefaultHandler() {
            @Override public void startElement(String uri, String localName,
                    String qName, Attributes attributes) {
                assertEquals("", uri);
                assertEquals("", localName);
                assertEquals("foo", qName);
                assertEquals(1, attributes.getLength());
                assertEquals("", attributes.getURI(0));
                assertOneOf("bar", "", attributes.getLocalName(0));
                assertEquals("bar", attributes.getQName(0));
            }
        });
        parse(true, false, "<a:foo a:bar=\"baz\"/>", new DefaultHandler() {
            @Override public void startElement(String uri, String localName,
                    String qName, Attributes attributes) {
                assertEquals("", uri);
                assertEquals("", localName);
                assertEquals("a:foo", qName);
                assertEquals(1, attributes.getLength());
                assertEquals("", attributes.getURI(0));
                assertOneOf("a:bar", "", attributes.getLocalName(0));
                assertEquals("a:bar", attributes.getQName(0));
            }
        });
    }
    private void parse(boolean prefixes, boolean namespaces, String xml,
            ContentHandler handler) throws Exception {
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
        XMLReader reader = parser.getXMLReader();
        reader.setFeature("http:
        reader.setFeature("http:
        reader.setContentHandler(handler);
        reader.parse(new InputSource(new StringReader(xml)));
    }
    private void assertOneOf(String expected, String sentinel, String actual) {
        List<String> optionsList = Arrays.asList(sentinel, expected);
        assertTrue("Expected one of " + optionsList + " but was " + actual,
                optionsList.contains(actual));
    }
}
