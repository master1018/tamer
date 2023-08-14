public final class elementsetattributenodens05 extends DOMTestCase {
   public elementsetattributenodens05(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document docAlt;
      Element element;
      Attr attribute;
      Attr newAttribute;
      doc = (Document) load("staffNS", true);
      docAlt = (Document) load("staffNS", true);
      element = doc.createElementNS("http:
      attribute = docAlt.createAttributeNS("http:
      {
         boolean success = false;
         try {
            newAttribute = element.setAttributeNodeNS(attribute);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.WRONG_DOCUMENT_ERR);
         }
         assertTrue("throw_WRONG_DOCUMENT_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementsetattributenodens05.class, args);
   }
}
