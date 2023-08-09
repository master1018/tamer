public final class hc_entitiesremovenameditem1 extends DOMTestCase {
   public hc_entitiesremovenameditem1(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
      if (factory.hasFeature("XML", null) != true) {
         throw org.w3c.domts.DOMTestIncompatibleException.incompatibleFeature("XML", null);
      }
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NamedNodeMap entities;
      DocumentType docType;
      Node retval;
      doc = (Document) load("hc_staff", true);
      docType = doc.getDoctype();
      if (
    !(("text/html".equals(getContentType())))
) {
          assertNotNull("docTypeNotNull", docType);
      entities = docType.getEntities();
      assertNotNull("entitiesNotNull", entities);
      {
         boolean success = false;
         try {
            retval = entities.removeNamedItem("alpha");
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NO_MODIFICATION_ALLOWED_ERR);
         }
         assertTrue("throw_NO_MODIFICATION_ALLOWED_ERR", success);
      }
}
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_entitiesremovenameditem1.class, args);
   }
}
