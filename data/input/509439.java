public final class namednodemapremovenameditem extends DOMTestCase {
   public namednodemapremovenameditem(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.validating
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node testAddress;
      NamedNodeMap attributes;
      Attr streetAttr;
      boolean specified;
      Node removedNode;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testAddress = elementList.item(2);
      attributes = testAddress.getAttributes();
      assertNotNull("attributesNotNull", attributes);
      removedNode = attributes.removeNamedItem("street");
      streetAttr = (Attr) attributes.getNamedItem("street");
      assertNotNull("streetAttrNotNull", streetAttr);
      specified = streetAttr.getSpecified();
      assertFalse("attrNotSpecified", specified);
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapremovenameditem.class, args);
   }
}
