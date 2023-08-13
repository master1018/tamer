public final class hc_elementinuseattributeerr extends DOMTestCase {
   public hc_elementinuseattributeerr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Attr newAttribute;
      NodeList addressElementList;
      Element testAddress;
      Element newElement;
      Attr attrAddress;
      Node appendedChild;
      Attr setAttr1;
      Attr setAttr2;
      doc = (Document) load("hc_staff", true);
      addressElementList = doc.getElementsByTagName("body");
      testAddress = (Element) addressElementList.item(0);
      newElement = doc.createElement("p");
      appendedChild = testAddress.appendChild(newElement);
      newAttribute = doc.createAttribute("title");
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
        DOMTestCase.doMain(hc_elementinuseattributeerr.class, args);
   }
}
