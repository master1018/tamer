public final class documentimportnode14 extends DOMTestCase {
   public documentimportnode14(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Document newDoc;
      DOMImplementation domImpl;
      DocumentType nullDocType = null;
      NodeList childList;
      Node imported;
      Node employeeElem;
      Attr attrNode;
      String attrValue;
      String nullNS = null;
      doc = (Document) load("staffNS", true);
      childList = doc.getElementsByTagNameNS("*", "employee");
      employeeElem = childList.item(3);
      domImpl = getImplementation();
      newDoc = domImpl.createDocument(nullNS, "staff", nullDocType);
      imported = newDoc.importNode(employeeElem, true);
      attrNode = ((Element) imported).getAttributeNodeNS(nullNS, "defaultAttr");
      assertNull("defaultAttrNotImported", attrNode);
      attrValue = ((Element) imported).getAttributeNS("http:
      assertEquals("explicitAttrImported", "http:
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentimportnode14.class, args);
   }
}
