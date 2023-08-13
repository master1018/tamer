public final class attrsetvaluenomodificationallowederr extends DOMTestCase {
   public attrsetvaluenomodificationallowederr(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
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
      NodeList genList;
      Node gen;
      NodeList gList;
      Node g;
      NamedNodeMap attrList;
      Attr attrNode;
      doc = (Document) load("staff", true);
      genderList = doc.getElementsByTagName("gender");
      gender = genderList.item(2);
      assertNotNull("genderNotNull", gender);
      genList = gender.getChildNodes();
      gen = genList.item(0);
      assertNotNull("genderFirstChildNotNull", gen);
      gList = gen.getChildNodes();
      g = gList.item(0);
      assertNotNull("genderFirstGrandchildNotNull", g);
      attrList = g.getAttributes();
      assertNotNull("attributesNotNull", attrList);
      attrNode = (Attr) attrList.getNamedItem("domestic");
      assertNotNull("attrNotNull", attrNode);
      {
         boolean success = false;
         try {
            attrNode.setValue("newvalue");
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
        DOMTestCase.doMain(attrsetvaluenomodificationallowederr.class, args);
   }
}
