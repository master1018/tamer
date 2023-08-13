public final class nodecdatasectionnodeattribute extends DOMTestCase {
   public nodecdatasectionnodeattribute(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element cdataName;
      Node cdataNode;
      NamedNodeMap attrList;
      int nodeType;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("name");
      cdataName = (Element) elementList.item(1);
      cdataNode = cdataName.getLastChild();
      nodeType = (int) cdataNode.getNodeType();
      if (!equals(4, nodeType)) {
          cdataNode = doc.createCDATASection("");
      }
    attrList = cdataNode.getAttributes();
      assertNull("cdataSection", attrList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodecdatasectionnodeattribute.class, args);
   }
}
