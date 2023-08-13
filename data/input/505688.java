public final class nodehasattributes04 extends DOMTestCase {
   public nodehasattributes04(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Document newDoc;
      DocumentType docType = null;
      DOMImplementation domImpl;
      Element element;
      Element elementTest;
      Element elementDoc;
      Attr attribute;
      Node setNode;
      Node appendedChild;
      NodeList elementList;
      boolean hasAttributes;
      doc = (Document) load("staffNS", false);
      domImpl = doc.getImplementation();
      newDoc = domImpl.createDocument("http:
      element = newDoc.createElementNS("http:
      attribute = newDoc.createAttribute("attr");
      setNode = element.setAttributeNode(attribute);
      elementDoc = newDoc.getDocumentElement();
      appendedChild = elementDoc.appendChild(element);
      elementList = newDoc.getElementsByTagNameNS("http:
      elementTest = (Element) elementList.item(0);
      hasAttributes = elementTest.hasAttributes();
      assertTrue("nodehasattributes04", hasAttributes);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodehasattributes04.class, args);
   }
}
