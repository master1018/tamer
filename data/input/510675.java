public final class domimplementationcreatedocumenttype01 extends DOMTestCase {
   public domimplementationcreatedocumenttype01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DOMImplementation domImpl;
      DocumentType newDocType;
      Document ownerDocument;
      String qualifiedName = "test:root";
      String publicId;
      String systemId;
      java.util.List publicIds = new java.util.ArrayList();
      publicIds.add("1234");
      publicIds.add("test");
      java.util.List systemIds = new java.util.ArrayList();
      systemIds.add("");
      systemIds.add("test");
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      for (int indexN1005D = 0; indexN1005D < publicIds.size(); indexN1005D++) {
          publicId = (String) publicIds.get(indexN1005D);
    for (int indexN10061 = 0; indexN10061 < systemIds.size(); indexN10061++) {
          systemId = (String) systemIds.get(indexN10061);
    newDocType = domImpl.createDocumentType(qualifiedName, publicId, systemId);
      assertNotNull("domimplementationcreatedocumenttype01_newDocType", newDocType);
      ownerDocument = newDocType.getOwnerDocument();
      assertNull("domimplementationcreatedocumenttype01_ownerDocument", ownerDocument);
        }
        }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(domimplementationcreatedocumenttype01.class, args);
   }
}
