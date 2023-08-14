public class Support_Xml {
    public static Document domOf(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setCoalescing(true);
        dbf.setExpandEntityReferences(true);
        ByteArrayInputStream stream = new ByteArrayInputStream(xml.getBytes());
        DocumentBuilder builder = dbf.newDocumentBuilder();
        return builder.parse(stream);
    }
    public static String firstChildTextOf(Document doc) throws Exception {
        NodeList children = doc.getFirstChild().getChildNodes();
        assertEquals(1, children.getLength());
        return children.item(0).getNodeValue();
    }
    public static Element firstElementOf(Document doc) throws Exception {
        return (Element) doc.getFirstChild();
    }
    public static String attrOf(Element e) throws Exception {
        return e.getAttribute("attr");
    }
}
