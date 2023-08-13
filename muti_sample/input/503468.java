public final class documenttypegetentitieslength extends DOMTestCase {
   public documenttypegetentitieslength(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap entityList;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      entityList = docType.getEntities();
      assertNotNull("entitiesNotNull", entityList);
      if (("image/svg+xml".equals(getContentType()))) {
          assertSize("entitySizeSVG", 7, entityList);
      } else {
          assertSize("entitySize", 5, entityList);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documenttypegetentitieslength.class, args);
   }
}
