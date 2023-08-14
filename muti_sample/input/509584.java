public final class hc_attrchildnodes2 extends DOMTestCase {
   public hc_attrchildnodes2(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
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
      Node retval;
      doc = (Document) load("hc_staff", true);
      acronymList = doc.getElementsByTagName("acronym");
      testNode = acronymList.item(3);
      attributes = testNode.getAttributes();
      titleAttr = (Attr) attributes.getNamedItem("title");
      childNodes = titleAttr.getChildNodes();
      textNode = doc.createTextNode("terday");
      retval = titleAttr.appendChild(textNode);
      assertSize("childNodesSize", 2, childNodes);
      textNode = (Text) childNodes.item(0);
      value = textNode.getNodeValue();
      assertEquals("child1IsYes", "Yes", value);
      textNode = (Text) childNodes.item(1);
      value = textNode.getNodeValue();
      assertEquals("child2IsTerday", "terday", value);
      textNode = (Text) childNodes.item(2);
      assertNull("thirdItemIsNull", textNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrchildnodes2.class, args);
   }
}
