public final class elementsetattributenodenomodificationallowederr extends DOMTestCase {
   public elementsetattributenodenomodificationallowederr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList genderList;
      Node gender;
      Node entRef;
      Element entElement;
      Attr newAttr;
      int nodeType;
      Attr badAttr;
      doc = (Document) load("staff", true);
      genderList = doc.getElementsByTagName("gender");
      gender = genderList.item(2);
      entRef = gender.getFirstChild();
      assertNotNull("entRefNotNull", entRef);
      nodeType = (int) entRef.getNodeType();
      if (equals(1, nodeType)) {
          entRef = doc.createEntityReference("ent4");
      assertNotNull("createdEntRefNotNull", entRef);
      }
    entElement = (Element) entRef.getFirstChild();
      assertNotNull("entElementNotNull", entElement);
      newAttr = doc.createAttribute("newAttr");
      {
         boolean success = false;
         try {
            badAttr = entElement.setAttributeNode(newAttr);
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
        DOMTestCase.doMain(elementsetattributenodenomodificationallowederr.class, args);
   }
}
