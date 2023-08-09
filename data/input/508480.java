public final class removeNamedItemNS03 extends DOMTestCase {
   public removeNamedItemNS03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      String namespaceURI = "http:
      String localName = "local1";
      Document doc;
      NodeList elementList;
      Node testAddress;
      NodeList nList;
      Node child;
      NodeList n2List;
      Node child2;
      NamedNodeMap attributes;
      Node removedNode;
      int nodeType;
      doc = (Document) load("staffNS", true);
      elementList = doc.getElementsByTagName("gender");
      testAddress = elementList.item(2);
      nList = testAddress.getChildNodes();
      child = nList.item(0);
      nodeType = (int) child.getNodeType();
      if (equals(1, nodeType)) {
          child = doc.createEntityReference("ent4");
      assertNotNull("createdEntRefNotNull", child);
      }
    n2List = child.getChildNodes();
      child2 = n2List.item(0);
      assertNotNull("notnull", child2);
      attributes = child2.getAttributes();
      {
         boolean success = false;
         try {
            removedNode = attributes.removeNamedItemNS(namespaceURI, localName);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NO_MODIFICATION_ALLOWED_ERR);
         }
         assertTrue("throw_NO_MODIFICATION_ALLOWED_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(removeNamedItemNS03.class, args);
   }
}
