public final class documentcreateelementcasesensitive extends DOMTestCase {
   public documentcreateelementcasesensitive(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element newElement1;
      Element newElement2;
      String attribute1;
      String attribute2;
      doc = (Document) load("staff", true);
      newElement1 = doc.createElement("ADDRESS");
      newElement2 = doc.createElement("address");
      newElement1.setAttribute("district", "Fort Worth");
      newElement2.setAttribute("county", "Dallas");
      attribute1 = newElement1.getAttribute("district");
      attribute2 = newElement2.getAttribute("county");
      assertEquals("attrib1", "Fort Worth", attribute1);
      assertEquals("attrib2", "Dallas", attribute2);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreateelementcasesensitive.class, args);
   }
}
