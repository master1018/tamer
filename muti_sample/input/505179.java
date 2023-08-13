public final class hc_documentcreateattribute extends DOMTestCase {
   public hc_documentcreateattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Attr newAttrNode;
      String attrValue;
      String attrName;
      int attrType;
      doc = (Document) load("hc_staff", true);
      newAttrNode = doc.createAttribute("title");
      attrValue = newAttrNode.getNodeValue();
      assertEquals("value", "", attrValue);
      attrName = newAttrNode.getNodeName();
      assertEqualsAutoCase("attribute", "name", "title", attrName);
        attrType = (int) newAttrNode.getNodeType();
      assertEquals("type", 2, attrType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_documentcreateattribute.class, args);
   }
}
