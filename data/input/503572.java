public final class attrreplacechild1 extends DOMTestCase {
   public attrreplacechild1(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      EntityReference entRef;
      Element entElement;
      Node attrNode;
      Text textNode;
      Node removedNode;
      Node newChild;
      doc = (Document) load("staff", true);
      entRef = doc.createEntityReference("ent4");
      assertNotNull("createdEntRefNotNull", entRef);
      entElement = (Element) entRef.getFirstChild();
      assertNotNull("entElementNotNull", entElement);
      attrNode = entElement.getAttributeNode("domestic");
      textNode = (Text) attrNode.getFirstChild();
      assertNotNull("attrChildNotNull", textNode);
      newChild = doc.createTextNode("Yesterday");
      {
         boolean success = false;
         try {
            removedNode = attrNode.replaceChild(newChild, textNode);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NO_MODIFICATION_ALLOWED_ERR);
         }
         assertTrue("setValue_throws_NO_MODIFICATION_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(attrreplacechild1.class, args);
   }
}
