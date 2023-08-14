public final class hc_namednodemapinuseattributeerr extends DOMTestCase {
   public hc_namednodemapinuseattributeerr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
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
      doc = (Document) load("hc_staff", true);
      elementList = doc.getElementsByTagName("acronym");
      firstNode = (Element) elementList.item(0);
      domesticAttr = doc.createAttribute("title");
      domesticAttr.setValue("Y\u03b1"); 
      setAttr = firstNode.setAttributeNode(domesticAttr);
      elementList = doc.getElementsByTagName("acronym");
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
        DOMTestCase.doMain(hc_namednodemapinuseattributeerr.class, args);
   }
}
