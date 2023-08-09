public final class documentcreateattribute extends DOMTestCase {
   public documentcreateattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Attr newAttrNode;
      String attrValue;
      String attrName;
      int attrType;
      doc = (Document) load("staff", true);
      newAttrNode = doc.createAttribute("district");
      attrValue = newAttrNode.getNodeValue();
      assertEquals("value", "", attrValue);
      attrName = newAttrNode.getNodeName();
      assertEquals("name", "district", attrName);
      attrType = (int) newAttrNode.getNodeType();
      assertEquals("type", 2, attrType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateattribute.class, args);
   }
}
