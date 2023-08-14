public final class documentcreateelementdefaultattr extends DOMTestCase {
   public documentcreateelementdefaultattr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      Element newElement;
      NamedNodeMap defaultAttr;
      Node child;
      String name;
      String value;
      doc = (Document) load("staff", true);
      newElement = doc.createElement("address");
      defaultAttr = newElement.getAttributes();
      child = defaultAttr.item(0);
      assertNotNull("defaultAttrNotNull", child);
      name = child.getNodeName();
      assertEquals("attrName", "street", name);
      value = child.getNodeValue();
      assertEquals("attrValue", "Yes", value);
      assertSize("attrCount", 1, defaultAttr);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateelementdefaultattr.class, args);
   }
}
