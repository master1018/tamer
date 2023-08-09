@TestTargetClass(DocumentBuilder.class) 
public class SimpleBuilderTest extends TestCase {
    private DocumentBuilder builder;
    protected void setUp() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);
        builder = factory.newDocumentBuilder();
    }
    protected void tearDown() throws Exception {
        builder = null;
    }
    private String getTextContent(Node node) {
        String result = (node instanceof Text ? ((Text) node).getData() : "");
        Node child = node.getFirstChild();
        while (child != null) {
            result = result + getTextContent(child);
            child = child.getNextSibling();
        }
        return result;
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL,
        notes = "Regression test.",
        method = "parse",
        args = {java.io.InputStream.class}
    )
    public void testGoodFile1() throws Exception {
        Document document = builder.parse(getClass().getResourceAsStream(
                "/SimpleBuilderTest.xml"));
        Element root = document.getDocumentElement();
        assertNotNull(root);
        assertEquals("http:
        assertEquals("t", root.getPrefix());
        assertEquals("stuff", root.getLocalName());
        NodeList list = root.getElementsByTagName("nestedStuff");
        assertNotNull(list);
        assertEquals(list.getLength(), 4);
        Element one = (Element) list.item(0);
        Element two = (Element) list.item(1);
        Element three = (Element) list.item(2);
        Element four = (Element) list.item(3);
        assertEquals("This space intentionally left blank.",
                getTextContent(one));
        assertEquals("Nothing to see here - please get along!",
                getTextContent(two));
        assertEquals("Rent this space!", getTextContent(three));
        assertEquals("", getTextContent(four));
        assertEquals("eins", one.getAttribute("one"));
        assertEquals("zwei", two.getAttribute("two"));
        assertEquals("drei", three.getAttribute("three"));
        assertEquals("vier", four.getAttribute("t:four"));
        assertEquals("vier", four.getAttributeNS("http:
        list = document.getChildNodes();
        assertNotNull(list);
        String proinst = "";
        String comment = "";
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node instanceof ProcessingInstruction) {
                proinst = proinst + node.getNodeValue();
            } else if (node instanceof Comment) {
                comment = comment + node.getNodeValue();
            }
        }
        assertEquals("The quick brown fox jumps over the lazy dog.", proinst);
        assertEquals(" Fragile!  Handle me with care! ", comment);
    }
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        method = "!todo parse",
        args = {java.io.InputStream.class}
    )
    @BrokenTest("Doesn't verify anything.")
    public void testGoodFile2() throws Exception {
        Document document = builder.parse(getClass().getResourceAsStream(
                "/staffNS.xml"));
        Element root = document.getDocumentElement();
        assertNotNull(root);
    }
    private void dump(String prefix, Element element) {
        System.out.print(prefix + "<" + element.getTagName());
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node item = attrs.item(i);
            System.out.print(" " + item.getNodeName() + "=" + item.getNodeValue());
        }
        System.out.println(">");
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node item = children.item(i);
            if (item instanceof Element) {
                dump(prefix + "  ", (Element)item);
            }
        }
        System.out.println(prefix + "</" + element.getTagName() + ">");
    }
}
