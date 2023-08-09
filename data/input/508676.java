public final class elementinuseattributeerr extends DOMTestCase {
   public elementinuseattributeerr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Attr newAttribute;
      NodeList addressElementList;
      Element testAddress;
      Element newElement;
      Node appendedChild;
      Attr setAttr1;
      Attr setAttr2;
      doc = (Document) load("staff", true);
      addressElementList = doc.getElementsByTagName("address");
      testAddress = (Element) addressElementList.item(1);
      newElement = doc.createElement("newElement");
      appendedChild = testAddress.appendChild(newElement);
      newAttribute = doc.createAttribute("newAttribute");
      setAttr1 = newElement.setAttributeNode(newAttribute);
      {
         boolean success = false;
         try {
            setAttr2 = testAddress.setAttributeNode(newAttribute);
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
        DOMTestCase.doMain(elementinuseattributeerr.class, args);
   }
}
