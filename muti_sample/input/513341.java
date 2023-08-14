public final class domimplementationcreatedocument03 extends DOMTestCase {
   public domimplementationcreatedocument03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DOMImplementation domImpl;
      Document newDoc;
      DocumentType docType = null;
      String namespaceURI = "http:
      String qualifiedName;
      java.util.List qualifiedNames = new java.util.ArrayList();
      qualifiedNames.add("_:_");
      qualifiedNames.add("_:h0");
      qualifiedNames.add("_:test");
      qualifiedNames.add("l_:_");
      qualifiedNames.add("ns:_0");
      qualifiedNames.add("ns:a0");
      qualifiedNames.add("ns0:test");
      qualifiedNames.add("a.b:c");
      qualifiedNames.add("a-b:c");
      qualifiedNames.add("a-b:c");
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      for (int indexN1006B = 0; indexN1006B < qualifiedNames.size(); indexN1006B++) {
          qualifiedName = (String) qualifiedNames.get(indexN1006B);
    newDoc = domImpl.createDocument(namespaceURI, qualifiedName, docType);
      assertNotNull("domimplementationcreatedocument03", newDoc);
        }
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(domimplementationcreatedocument03.class, args);
   }
}
