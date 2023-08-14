public final class hc_attrlastchild extends DOMTestCase {
   public hc_attrlastchild(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node otherChild;
      doc = (Document) load("hc_staff", true);
      acronymList = doc.getElementsByTagName("acronym");
      testNode = acronymList.item(3);
      attributes = testNode.getAttributes();
      titleAttr = (Attr) attributes.getNamedItem("title");
      textNode = (Text) titleAttr.getFirstChild();
      assertNotNull("textNodeNotNull", textNode);
      value = textNode.getNodeValue();
      assertEquals("child1IsYes", "Yes", value);
      otherChild = textNode.getNextSibling();
      assertNull("nextSiblingIsNull", otherChild);
      otherChild = textNode.getPreviousSibling();
      assertNull("previousSiblingIsNull", otherChild);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrlastchild.class, args);
   }
}
