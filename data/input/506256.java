public final class hc_notationsremovenameditemns1 extends DOMTestCase {
   public hc_notationsremovenameditemns1(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NamedNodeMap notations;
      DocumentType docType;
      Node retval;
      doc = (Document) load("hc_staff", true);
      docType = doc.getDoctype();
      if (
    !(("text/html".equals(getContentType())))
) {
          assertNotNull("docTypeNotNull", docType);
      notations = docType.getNotations();
      assertNotNull("notationsNotNull", notations);
      try {
      retval = notations.removeNamedItemNS("http:
      fail("throw_NO_MOD_OR_NOT_FOUND_ERR");
      } catch (DOMException ex) {
           switch (ex.code) {
      case 7 : 
       break;
      case 8 : 
       break;
          default:
          throw ex;
          }
      } 
}
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_notationsremovenameditemns1.class, args);
   }
}
