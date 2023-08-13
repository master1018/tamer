public final class hc_attrgetvalue2 extends DOMTestCase {
   public hc_attrgetvalue2(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
      if (factory.hasFeature("XML", null) != true) {
         throw org.w3c.domts.DOMTestIncompatibleException.incompatibleFeature("XML", null);
      }
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList acronymList;
      Node testNode;
      NamedNodeMap attributes;
      Attr titleAttr;
      String value;
      Text textNode;
      Node retval;
      Node firstChild;
      EntityReference alphaRef;
      doc = (Document) load("hc_staff", true);
      acronymList = doc.getElementsByTagName("acronym");
      testNode = acronymList.item(3);
      attributes = testNode.getAttributes();
      titleAttr = (Attr) attributes.getNamedItem("class");
      if (("text/html".equals(getContentType()))) {
      {
         boolean success = false;
         try {
            alphaRef = doc.createEntityReference("alpha");
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NOT_SUPPORTED_ERR);
         }
         assertTrue("throw_NOT_SUPPORTED_ERR", success);
      }
} else {
          alphaRef = doc.createEntityReference("alpha");
      firstChild = titleAttr.getFirstChild();
      retval = titleAttr.insertBefore(alphaRef, firstChild);
      value = titleAttr.getValue();
      assertEquals("attrValue1", "\u03b1Y\u03b1", value); 
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_attrgetvalue2.class, args);
   }
}
