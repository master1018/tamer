public final class importNode11 extends DOMTestCase {
   public importNode11(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document aNewDoc;
      EntityReference entRef;
      Node aNode;
      String name;
      Node child;
      String childValue;
      doc = (Document) load("staff", true);
      aNewDoc = (Document) load("staff", true);
      entRef = aNewDoc.createEntityReference("ent3");
      assertNotNull("createdEntRefNotNull", entRef);
      aNode = doc.importNode(entRef, true);
      name = aNode.getNodeName();
      assertEquals("entityName", "ent3", name);
      child = aNode.getFirstChild();
      assertNotNull("child", child);
      childValue = child.getNodeValue();
      assertEquals("childValue", "Texas", childValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(importNode11.class, args);
   }
}
