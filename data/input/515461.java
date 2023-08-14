public final class namednodemapinuseattributeerr extends DOMTestCase {
   public namednodemapinuseattributeerr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element firstNode;
      Node testNode;
      NamedNodeMap attributes;
      Attr domesticAttr;
      Attr setAttr;
      Node setNode;
      doc = (Document) load("staff", true);
      elementList = doc.getElementsByTagName("address");
      firstNode = (Element) elementList.item(0);
      domesticAttr = doc.createAttribute("domestic");
      domesticAttr.setValue("Yes");
      setAttr = firstNode.setAttributeNode(domesticAttr);
      elementList = doc.getElementsByTagName("address");
      testNode = elementList.item(2);
      attributes = testNode.getAttributes();
      {
         boolean success = false;
         try {
            setNode = attributes.setNamedItem(domesticAttr);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.INUSE_ATTRIBUTE_ERR);
         }
         assertTrue("throw_INUSE_ATTRIBUTE_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(namednodemapinuseattributeerr.class, args);
   }
}
