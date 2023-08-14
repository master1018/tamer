public final class hc_elementgetelementsbytagnamespecialvalue extends DOMTestCase {
   public hc_elementgetelementsbytagnamespecialvalue(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Element lastEmployee;
      NodeList lastempList;
      Node child;
      String childName;
      java.util.List result = new java.util.ArrayList();
      java.util.List expectedResult = new java.util.ArrayList();
      expectedResult.add("em");
      expectedResult.add("strong");
      expectedResult.add("code");
      expectedResult.add("sup");
      expectedResult.add("var");
      expectedResult.add("acronym");
      doc = (Document) load("hc_staff", false);
      elementList = doc.getElementsByTagName("p");
      lastEmployee = (Element) elementList.item(4);
      lastempList = lastEmployee.getElementsByTagName("*");
      for (int indexN10067 = 0; indexN10067 < lastempList.getLength(); indexN10067++) {
          child = (Node) lastempList.item(indexN10067);
    childName = child.getNodeName();
      result.add(childName);
        }
      assertEqualsAutoCase("element", "tagNames", expectedResult, result);
        }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_elementgetelementsbytagnamespecialvalue.class, args);
   }
}
