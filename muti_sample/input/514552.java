public final class elementremoveattributenodenomodificationallowederr extends DOMTestCase {
   public elementremoveattributenodenomodificationallowederr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList genderList;
      Node gender;
      NodeList genList;
      Node gen;
      int nodeType;
      NodeList gList;
      Element genElement;
      NamedNodeMap attrList;
      Attr attrNode;
      Attr removedAttr;
      doc = (Document) load("staff", true);
      genderList = doc.getElementsByTagName("gender");
      gender = genderList.item(2);
      genList = gender.getChildNodes();
      gen = genList.item(0);
      assertNotNull("genNotNull", gen);
      nodeType = (int) gen.getNodeType();
      if (equals(1, nodeType)) {
          gen = doc.createEntityReference("ent4");
      assertNotNull("createdEntRefNotNull", gen);
      }
    gList = gen.getChildNodes();
      genElement = (Element) gList.item(0);
      assertNotNull("genElementNotNull", genElement);
      attrList = genElement.getAttributes();
      attrNode = (Attr) attrList.getNamedItem("domestic");
      {
         boolean success = false;
         try {
            removedAttr = genElement.removeAttributeNode(attrNode);
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
        DOMTestCase.doMain(elementremoveattributenodenomodificationallowederr.class, args);
   }
}
