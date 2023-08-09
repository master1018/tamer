public final class setAttributeNS02 extends DOMTestCase {
   public setAttributeNS02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName = "emp:";
      Document doc;
      NodeList elementList;
      Node testAddr;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("emp:employee");
      testAddr = elementList.item(0);
      {
         try {
            ((Element) testAddr).setAttributeNS(namespaceURI, qualifiedName, "newValue");
             fail("throw_NAMESPACE_ERR");
          } catch (DOMException ex) {
         }
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(setAttributeNS02.class, args);
   }
}
