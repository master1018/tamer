public final class documentcreateentityreferenceknown extends DOMTestCase {
   public documentcreateentityreferenceknown(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      EntityReference newEntRefNode;
      NodeList newEntRefList;
      Node child;
      String name;
      String value;
      doc = (Document) load("staff", true);
      newEntRefNode = doc.createEntityReference("ent3");
      assertNotNull("createdEntRefNotNull", newEntRefNode);
      newEntRefList = newEntRefNode.getChildNodes();
      assertSize("size", 1, newEntRefList);
      child = newEntRefNode.getFirstChild();
      name = child.getNodeName();
      assertEquals("name", "#text", name);
      value = child.getNodeValue();
      assertEquals("value", "Texas", value);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateentityreferenceknown.class, args);
   }
}
