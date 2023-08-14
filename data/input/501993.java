public final class namednodemapsetnameditemns09 extends DOMTestCase {
   public namednodemapsetnameditemns09(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      DocumentType docType;
      NamedNodeMap entities;
      NamedNodeMap notations;
      Attr attr;
      Node newNode;
      doc = (Document) load("staffNS", true);
      docType = doc.getDoctype();
      entities = docType.getEntities();
      notations = docType.getNotations();
      attr = doc.createAttributeNS("http:
      {
         boolean success = false;
         try {
            newNode = entities.setNamedItemNS(attr);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NO_MODIFICATION_ALLOWED_ERR);
         }
         assertTrue("throw_NO_MODIFICATION_ALLOWED_ERR_entities", success);
      }
      {
         boolean success = false;
         try {
            newNode = notations.setNamedItemNS(attr);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NO_MODIFICATION_ALLOWED_ERR);
         }
         assertTrue("throw_NO_MODIFICATION_ALLOWED_ERR_notations", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapsetnameditemns09.class, args);
   }
}
