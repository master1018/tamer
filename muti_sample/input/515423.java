public final class namednodemapremovenameditemgetvalue extends DOMTestCase {
   public namednodemapremovenameditemgetvalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Node testEmployee;
      NamedNodeMap attributes;
      Attr streetAttr;
      String value;
      Node removedNode;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      testEmployee = elementList.item(2);
      attributes = testEmployee.getAttributes();
      assertNotNull("attributesNotNull", attributes);
      removedNode = attributes.removeNamedItem("street");
      streetAttr = (Attr) attributes.getNamedItem("street");
      assertNotNull("streetAttrNotNull", streetAttr);
      value = streetAttr.getValue();
      assertEquals("namednodemapRemoveNamedItemGetValueAssert", "Yes", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapremovenameditemgetvalue.class, args);
   }
}
