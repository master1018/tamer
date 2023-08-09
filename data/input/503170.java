public final class namednodemapsetnameditemns04 extends DOMTestCase {
   public namednodemapsetnameditemns04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      DOMImplementation domImpl;
      Document docAlt;
      DocumentType docType = null;
      NamedNodeMap attributes;
      NodeList elementList;
      Element element;
      Attr attrAlt;
      Node newNode;
      String nullNS = null;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagNameNS("*", "address");
      element = (Element) elementList.item(1);
      attributes = element.getAttributes();
      domImpl = doc.getImplementation();
      docAlt = domImpl.createDocument(nullNS, "newDoc", docType);
      attrAlt = docAlt.createAttributeNS(nullNS, "street");
      {
         boolean success = false;
         try {
            newNode = attributes.setNamedItemNS(attrAlt);
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
        DOMTestCase.doMain(namednodemapsetnameditemns04.class, args);
   }
}
