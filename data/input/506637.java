public final class hc_attrchildnodes1 extends DOMTestCase {
   public hc_attrchildnodes1(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList acronymList;
      Node testNode;
      NamedNodeMap attributes;
      Attr titleAttr;
      String value;
      Text textNode;
      NodeList childNodes;
      doc = (Document) load("hc_staff", false);
      acronymList = doc.getElementsByTagName("acronym");
      testNode = acronymList.item(3);
      attributes = testNode.getAttributes();
      titleAttr = (Attr) attributes.getNamedItem("title");
      childNodes = titleAttr.getChildNodes();
      assertSize("childNodesSize", 1, childNodes);
      textNode = (Text) childNodes.item(0);
      value = textNode.getNodeValue();
      assertEquals("child1IsYes", "Yes", value);
      textNode = (Text) childNodes.item(1);
      assertNull("secondItemIsNull", textNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrchildnodes1.class, args);
   }
}
