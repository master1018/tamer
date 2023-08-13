public final class hc_documentcreateelementcasesensitive extends DOMTestCase {
   public hc_documentcreateelementcasesensitive(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Element newElement1;
      Element newElement2;
      String attribute1;
      String attribute2;
      String nodeName1;
      String nodeName2;
      doc = (Document) load("hc_staff", true);
      newElement1 = doc.createElement("ACRONYM");
      newElement2 = doc.createElement("acronym");
      newElement1.setAttribute("lang", "EN");
      newElement2.setAttribute("title", "Dallas");
      attribute1 = newElement1.getAttribute("lang");
      attribute2 = newElement2.getAttribute("title");
      assertEquals("attrib1", "EN", attribute1);
      assertEquals("attrib2", "Dallas", attribute2);
      nodeName1 = newElement1.getNodeName();
      nodeName2 = newElement2.getNodeName();
      assertEqualsAutoCase("element", "nodeName1", "ACRONYM", nodeName1);
        assertEqualsAutoCase("element", "nodeName2", "acronym", nodeName2);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_documentcreateelementcasesensitive.class, args);
   }
}
