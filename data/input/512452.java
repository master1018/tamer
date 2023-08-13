public final class namednodemapsetnameditemns11 extends DOMTestCase {
   public namednodemapsetnameditemns11(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      NamedNodeMap notations;
      NamedNodeMap attributes;
      Notation notation;
      Element element;
      NodeList elementList;
      Node newNode;
      String nullNS = null;
      doc = (Document) load("staffNS", true);
      docType = doc.getDoctype();
      notations = docType.getNotations();
      assertNotNull("notationsNotNull", notations);
      notation = (Notation) notations.getNamedItem("notation1");
      elementList = doc.getElementsByTagNameNS("http:
      element = (Element) elementList.item(0);
      attributes = element.getAttributes();
      {
         boolean success = false;
         try {
            newNode = attributes.setNamedItemNS(notation);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.HIERARCHY_REQUEST_ERR);
         }
         assertTrue("throw_HIERARCHY_REQUEST_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapsetnameditemns11.class, args);
   }
}
