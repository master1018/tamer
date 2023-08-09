public final class hc_namednodemapinvalidtype1 extends DOMTestCase {
   public hc_namednodemapinvalidtype1(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NamedNodeMap attributes;
      Element docElem;
      Element newElem;
      Node retval;
      doc = (Document) load("hc_staff", true);
      docElem = doc.getDocumentElement();
      attributes = docElem.getAttributes();
      newElem = doc.createElement("html");
      {
         boolean success = false;
         try {
            retval = attributes.setNamedItem(newElem);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.HIERARCHY_REQUEST_ERR);
         }
         assertTrue("throw_HIERARCHY_REQUEST_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_namednodemapinvalidtype1.class, args);
   }
}
