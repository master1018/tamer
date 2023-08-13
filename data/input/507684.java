public final class elementsetattributenomodificationallowederr extends DOMTestCase {
   public elementsetattributenomodificationallowederr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.notExpandEntityReferences
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList genderList;
      Node gender;
      EntityReference entRef;
      Element entElement;
      doc = (Document) load("staff", true);
      genderList = doc.getElementsByTagName("gender");
      gender = genderList.item(2);
      entRef = (EntityReference) gender.getFirstChild();
      assertNotNull("entRefNotNull", entRef);
      entElement = (Element) entRef.getFirstChild();
      assertNotNull("entElementNotNull", entElement);
      {
         boolean success = false;
         try {
            entElement.setAttribute("newAttr", "newValue");
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
        DOMTestCase.doMain(elementsetattributenomodificationallowederr.class, args);
   }
}
