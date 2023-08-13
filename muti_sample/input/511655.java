public final class hc_attrremovechild1 extends DOMTestCase {
   public hc_attrremovechild1(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node retval;
      Node firstChild;
      doc = (Document) load("hc_staff", true);
      acronymList = doc.getElementsByTagName("acronym");
      testNode = acronymList.item(3);
      attributes = testNode.getAttributes();
      titleAttr = (Attr) attributes.getNamedItem("title");
      textNode = (Text) titleAttr.getFirstChild();
      assertNotNull("attrChildNotNull", textNode);
      retval = titleAttr.removeChild(textNode);
      value = titleAttr.getValue();
      assertEquals("attrValue", "", value);
      value = titleAttr.getNodeValue();
      assertEquals("attrNodeValue", "", value);
      value = retval.getNodeValue();
      assertEquals("retvalValue", "Yes", value);
      firstChild = titleAttr.getFirstChild();
      assertNull("firstChildNull", firstChild);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrremovechild1.class, args);
   }
}
