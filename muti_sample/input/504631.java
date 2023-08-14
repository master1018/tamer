public final class documentcreatetextnode extends DOMTestCase {
   public documentcreatetextnode(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Text newTextNode;
      String newTextName;
      String newTextValue;
      int newTextType;
      doc = (Document) load("staff", true);
      newTextNode = doc.createTextNode("This is a new Text node");
      newTextValue = newTextNode.getNodeValue();
      assertEquals("value", "This is a new Text node", newTextValue);
      newTextName = newTextNode.getNodeName();
      assertEquals("name", "#text", newTextName);
      newTextType = (int) newTextNode.getNodeType();
      assertEquals("type", 3, newTextType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreatetextnode.class, args);
   }
}
