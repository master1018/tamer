public final class hc_elementnotfounderr extends DOMTestCase {
   public hc_elementnotfounderr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Attr oldAttribute;
      NodeList addressElementList;
      Element testAddress;
      Attr attrAddress;
      doc = (Document) load("hc_staff", true);
      addressElementList = doc.getElementsByTagName("acronym");
      testAddress = (Element) addressElementList.item(4);
      oldAttribute = doc.createAttribute("title");
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
        DOMTestCase.doMain(hc_elementnotfounderr.class, args);
   }
}
