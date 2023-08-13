public final class attrsetvaluenomodificationallowederrEE extends DOMTestCase {
   public attrsetvaluenomodificationallowederrEE(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      EntityReference entRef;
      Element entElement;
      NamedNodeMap attrList;
      Node attrNode;
      Node gender;
      NodeList genderList;
      Node appendedChild;
      doc = (Document) load("staff", true);
      genderList = doc.getElementsByTagName("gender");
      gender = genderList.item(2);
      assertNotNull("genderNotNull", gender);
      entRef = doc.createEntityReference("ent4");
      assertNotNull("entRefNotNull", entRef);
      appendedChild = gender.appendChild(entRef);
      entElement = (Element) entRef.getFirstChild();
      assertNotNull("entElementNotNull", entElement);
      attrList = entElement.getAttributes();
      attrNode = attrList.getNamedItem("domestic");
      {
         boolean success = false;
         try {
            ((Attr) attrNode).setValue("newvalue");
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NO_MODIFICATION_ALLOWED_ERR);
         }
         assertTrue("setValue_throws_NO_MODIFICATION", success);
      }
      {
         boolean success = false;
         try {
            attrNode.setNodeValue("newvalue2");
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NO_MODIFICATION_ALLOWED_ERR);
         }
         assertTrue("setNodeValue_throws_NO_MODIFICATION", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(attrsetvaluenomodificationallowederrEE.class, args);
   }
}
