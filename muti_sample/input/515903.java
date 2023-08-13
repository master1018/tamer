public final class documentcreateentityreference extends DOMTestCase {
   public documentcreateentityreference(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      EntityReference newEntRefNode;
      String entRefValue;
      String entRefName;
      int entRefType;
      doc = (Document) load("staff", true);
      newEntRefNode = doc.createEntityReference("ent1");
      assertNotNull("createdEntRefNotNull", newEntRefNode);
      entRefValue = newEntRefNode.getNodeValue();
      assertNull("value", entRefValue);
      entRefName = newEntRefNode.getNodeName();
      assertEquals("name", "ent1", entRefName);
      entRefType = (int) newEntRefNode.getNodeType();
      assertEquals("type", 5, entRefType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateentityreference.class, args);
   }
}
