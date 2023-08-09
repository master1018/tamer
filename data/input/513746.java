public final class elementsetattributenodens04 extends DOMTestCase {
   public elementsetattributenodens04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element1;
      Element element2;
      Attr attribute;
      Attr newAttribute;
      doc = (Document) load("staffNS", true);
      element1 = doc.createElementNS("http:
      element2 = doc.createElementNS("http:
      attribute = doc.createAttributeNS("http:
      newAttribute = element1.setAttributeNodeNS(attribute);
      {
         boolean success = false;
         try {
            newAttribute = element2.setAttributeNodeNS(attribute);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INUSE_ATTRIBUTE_ERR);
         }
         assertTrue("elementsetattributenodens04", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementsetattributenodens04.class, args);
   }
}
