public final class setAttributeNS03 extends DOMTestCase {
   public setAttributeNS03(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      String namespaceURI = "www.xyz.com";
      String qualifiedName = "emp:local1";
      Document doc;
      NodeList genderList;
      Node gender;
      NodeList genList;
      Node gen;
      NodeList gList;
      Element genElement;
      int nodeType;
      doc = (Document) load("staffNS", true);
      genderList = doc.getElementsByTagName("gender");
      gender = genderList.item(2);
      genList = gender.getChildNodes();
      gen = genList.item(0);
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
            genElement.setAttributeNS(namespaceURI, qualifiedName, "newValue");
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
        DOMTestCase.doMain(setAttributeNS03.class, args);
   }
}
