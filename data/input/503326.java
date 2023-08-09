public class NamespacedAttributesLookupTest extends TestCase {
    private static final String SAX_PROPERTY_NS =
            "http:
    private static final String SAX_PROPERTY_NS_PREFIXES =
            "http:
    private static String xml = "<?xml version='1.0' encoding='UTF-8'?>" +
            "<test xmlns='http:
            "<b c='w' bar:c='x'/>" +
            "<bar:e baz:c='y' bar:c='z'/>" +
            "</test>";
    public void testNamespace() throws Exception {
        List<String> expected = Arrays.asList(
                "http:
                "  http:
                "  http:
                "  bar:c=null\n",
                "http:
                "  ,c\n" +
                "  http:
                "  http:
                "  bar:c=x\n",
                "http:
                "  http:
                "  http:
                "  http:
                "  bar:c=z\n");
        boolean namespace = true;
        boolean namespacePrefixes = false;
        assertEquals(expected, getStartElements(xml, namespace, namespacePrefixes));
    }
    public void testNamespacePrefixes() throws Exception {
        List<String> expected = Arrays.asList(
                "test\n" +
                "  xmlns\n" +
                "  xmlns:bar\n" +
                "  xmlns:baz\n" +
                "  baz:c\n" +
                "  http:
                "  bar:c=null\n",
                "b\n" +
                "  c\n" +
                "  bar:c\n" +
                "  http:
                "  bar:c=x\n",
                "bar:e\n" +
                "  baz:c\n" +
                "  bar:c\n" +
                "  http:
                "  bar:c=z\n");
        boolean namespace = false;
        boolean namespacePrefixes = true;
        assertEquals(expected, getStartElements(xml, namespace, namespacePrefixes));
    }
    public List<String> getStartElements(String xml, final boolean namespace, boolean namespacePrefixes)
            throws Exception {
        final List<String> result = new ArrayList<String>();
        XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
        reader.setFeature(SAX_PROPERTY_NS, namespace);
        reader.setFeature(SAX_PROPERTY_NS_PREFIXES, namespacePrefixes);
        reader.setContentHandler(new DefaultHandler() {
            @Override public final void startElement(
                    String uri, String localName, String qName, Attributes attributes) {
                StringBuilder serialized = new StringBuilder();
                if (namespace) {
                    serialized.append(uri).append(",");
                    serialized.append(localName);
                } else {
                    serialized.append(qName);
                }
                for (int i = 0; i < attributes.getLength(); i++) {
                    serialized.append("\n  ");
                    if (namespace) {
                        serialized.append(attributes.getURI(i)).append(",");
                        serialized.append(attributes.getLocalName(i));
                    } else {
                        serialized.append(attributes.getQName(i));
                    }
                }
                serialized.append("\n  http:
                        .append(attributes.getValue("http:
                        .append("\n  bar:c=")
                        .append(attributes.getValue("bar:c"))
                        .append("\n");
                result.add(serialized.toString());
            }
        });
        reader.parse(new InputSource(new StringReader(xml)));
        return result;
    }
}
