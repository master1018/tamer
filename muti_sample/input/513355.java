public final class hc_namednodemapnotfounderr extends DOMTestCase {
   public hc_namednodemapnotfounderr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element testEmployee;
      NamedNodeMap attributes;
      Node removedNode;
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      testEmployee = (Element) elementList.item(2);
      attributes = testEmployee.getAttributes();
      {
         boolean success = false;
         try {
            removedNode = attributes.removeNamedItem("lang");
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
        DOMTestCase.doMain(hc_namednodemapnotfounderr.class, args);
   }
}
