public final class hc_entitiessetnameditemns1 extends DOMTestCase {
   public hc_entitiessetnameditemns1(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NamedNodeMap entities;
      DocumentType docType;
      Node retval;
      Element elem;
      doc = (Document) load("hc_staff", true);
      docType = doc.getDoctype();
      if (
    !(("text/html".equals(getContentType())))
) {
          assertNotNull("docTypeNotNull", docType);
      entities = docType.getEntities();
      assertNotNull("entitiesNotNull", entities);
      elem = doc.createElementNS("http:
      try {
      retval = entities.setNamedItemNS(elem);
      fail("throw_HIER_OR_NO_MOD_ERR");
      } catch (DOMException ex) {
           switch (ex.code) {
      case 3 : 
       break;
      case 7 : 
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
        DOMTestCase.doMain(hc_entitiessetnameditemns1.class, args);
   }
}
