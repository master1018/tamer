public final class elementremoveattributenodenomodificationallowederrEE extends DOMTestCase {
   public elementremoveattributenodenomodificationallowederrEE(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList genderList;
      Node gender;
      EntityReference entRef;
      Element entElement;
      NamedNodeMap attrList;
      Attr attrNode;
      int nodeType;
      Attr removedAttr;
      Node appendedChild;
      doc = (Document) load("staff", true);
      genderList = doc.getElementsByTagName("gender");
      gender = genderList.item(2);
      entRef = doc.createEntityReference("ent4");
      assertNotNull("createdEntRefNotNull", entRef);
      appendedChild = gender.appendChild(entRef);
      entElement = (Element) entRef.getFirstChild();
      assertNotNull("entElementNotNull", entElement);
      attrList = entElement.getAttributes();
      attrNode = (Attr) attrList.getNamedItem("domestic");
      assertNotNull("attrNodeNotNull", attrNode);
      {
         boolean success = false;
         try {
            removedAttr = entElement.removeAttributeNode(attrNode);
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
        DOMTestCase.doMain(elementremoveattributenodenomodificationallowederrEE.class, args);
   }
}
