public final class domimplementationcreatedocumenttype02 extends DOMTestCase {
   public domimplementationcreatedocumenttype02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DOMImplementation domImpl;
      DocumentType newDocType;
      Document ownerDocument;
      String publicId = "http:
      String systemId = "dom2.dtd";
      String qualifiedName;
      java.util.List qualifiedNames = new java.util.ArrayList();
      qualifiedNames.add("_:_");
      qualifiedNames.add("_:h0");
      qualifiedNames.add("_:test");
      qualifiedNames.add("_:_.");
      qualifiedNames.add("_:a-");
      qualifiedNames.add("l_:_");
      qualifiedNames.add("ns:_0");
      qualifiedNames.add("ns:a0");
      qualifiedNames.add("ns0:test");
      qualifiedNames.add("ns:EEE.");
      qualifiedNames.add("ns:_-");
      qualifiedNames.add("a.b:c");
      qualifiedNames.add("a-b:c.j");
      qualifiedNames.add("a-b:c");
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      for (int indexN10077 = 0; indexN10077 < qualifiedNames.size(); indexN10077++) {
          qualifiedName = (String) qualifiedNames.get(indexN10077);
    newDocType = domImpl.createDocumentType(qualifiedName, publicId, systemId);
      assertNotNull("domimplementationcreatedocumenttype02_newDocType", newDocType);
      ownerDocument = newDocType.getOwnerDocument();
      assertNull("domimplementationcreatedocumenttype02_ownerDocument", ownerDocument);
        }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(domimplementationcreatedocumenttype02.class, args);
   }
}
