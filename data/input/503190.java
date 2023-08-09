public final class removeAttributeNS01 extends DOMTestCase {
   public removeAttributeNS01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      org.w3c.domts.DocumentBuilderSetting[] settings = 
          new org.w3c.domts.DocumentBuilderSetting[] {
org.w3c.domts.DocumentBuilderSetting.namespaceAware
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
      Node gen;
      NodeList gList;
      Element genElement;
      int nodeType;
      doc = (Document) load("staffNS", true);
      genderList = doc.getElementsByTagName("gender");
      gender = genderList.item(2);
      gen = gender.getFirstChild();
      nodeType = (int) gen.getNodeType();
      if (equals(1, nodeType)) {
          gen = doc.createEntityReference("ent4");
      assertNotNull("createdEntRefNotNull", gen);
      }
    gList = gen.getChildNodes();
      genElement = (Element) gList.item(0);
      assertNotNull("notnull", genElement);
      {
         boolean success = false;
         try {
            genElement.removeAttributeNS("www.xyz.com", "local1");
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
        DOMTestCase.doMain(removeAttributeNS01.class, args);
   }
}
