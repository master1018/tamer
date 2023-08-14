public final class documentcreatecdatasection extends DOMTestCase {
   public documentcreatecdatasection(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      CDATASection newCDATASectionNode;
      String newCDATASectionValue;
      String newCDATASectionName;
      int newCDATASectionType;
      doc = (Document) load("staff", true);
      newCDATASectionNode = doc.createCDATASection("This is a new CDATASection node");
      newCDATASectionValue = newCDATASectionNode.getNodeValue();
      assertEquals("nodeValue", "This is a new CDATASection node", newCDATASectionValue);
      newCDATASectionName = newCDATASectionNode.getNodeName();
      assertEquals("nodeName", "#cdata-section", newCDATASectionName);
      newCDATASectionType = (int) newCDATASectionNode.getNodeType();
      assertEquals("nodeType", 4, newCDATASectionType);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreatecdatasection.class, args);
   }
}
