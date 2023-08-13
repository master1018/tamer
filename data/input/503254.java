public final class createAttributeNS05 extends DOMTestCase {
   public createAttributeNS05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "http:
      String qualifiedName = "econm:local";
      Document doc;
      Attr newAttr;
      String attrName;
      doc = (Document) load("staffNS", false);
      newAttr = doc.createAttributeNS(namespaceURI, qualifiedName);
      attrName = newAttr.getName();
      assertEquals("throw_Equals", qualifiedName, attrName);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(createAttributeNS05.class, args);
   }
}
