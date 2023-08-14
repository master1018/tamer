public final class elementsetattributenodens06 extends DOMTestCase {
   public elementsetattributenodens06(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.notExpandEntityReferences
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element element;
      Attr attribute;
      Attr attribute2;
      EntityReference entRef;
      NodeList elementList;
      Node newAttribute;
      Node newChild;
      doc = (Document) load("staffNS", true);
      element = doc.createElementNS("http:
      attribute = doc.createAttributeNS("http:
      entRef = doc.createEntityReference("ent4");
      newChild = attribute.appendChild(entRef);
      newAttribute = element.setAttributeNodeNS(attribute);
      elementList = entRef.getChildNodes();
      element = (Element) elementList.item(0);
      attribute2 = doc.createAttributeNS("http:
      {
         boolean success = false;
         try {
            newAttribute = element.setAttributeNodeNS(attribute2);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NO_MODIFICATION_ALLOWED_ERR);
         }
         assertTrue("elementsetattributenodens06", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(elementsetattributenodens06.class, args);
   }
}
