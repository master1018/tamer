public final class setAttributeNodeNS02 extends DOMTestCase {
   public setAttributeNodeNS02(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.notNamespaceAware
        };
        DOMTestDocumentBuilderFactory testFactory = factory.newInstance(settings);
        setFactory(testFactory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList genderList;
      Node gender;
      NodeList genList;
      Node gen;
      NodeList gList;
      Element genElement;
      Attr newAttr;
      Attr setAttr1;
      doc = (Document) load("staffNS", true);
      if (!isExpandEntityReferences()) {
          genderList = doc.getElementsByTagName("gender");
      gender = genderList.item(2);
      genList = gender.getChildNodes();
      gen = genList.item(0);
      } else {
          gen = doc.createEntityReference("ent4");
      }
    gList = gen.getChildNodes();
      genElement = (Element) gList.item(0);
      assertNotNull("notnull", genElement);
      newAttr = doc.createAttributeNS("www.xyz.com", "emp:local1");
      {
         boolean success = false;
         try {
            setAttr1 = genElement.setAttributeNodeNS(newAttr);
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
        DOMTestCase.doMain(setAttributeNodeNS02.class, args);
   }
}
