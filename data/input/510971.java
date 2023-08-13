public final class elementnotfounderr extends DOMTestCase {
   public elementnotfounderr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Attr oldAttribute;
      NodeList addressElementList;
      Element testAddress;
      Attr attrAddress;
      doc = (Document) load("staff", true);
      addressElementList = doc.getElementsByTagName("address");
      testAddress = (Element) addressElementList.item(4);
      oldAttribute = doc.createAttribute("oldAttribute");
      {
         boolean success = false;
         try {
            attrAddress = testAddress.removeAttributeNode(oldAttribute);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NOT_FOUND_ERR);
         }
         assertTrue("throw_NOT_FOUND_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementnotfounderr.class, args);
   }
}
