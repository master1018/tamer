public final class hc_documentgetelementsbytagnametotallength extends DOMTestCase {
   public hc_documentgetelementsbytagnametotallength(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "hc_staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList nameList;
      java.util.List expectedNames = new java.util.ArrayList();
      expectedNames.add("html");
      expectedNames.add("head");
      expectedNames.add("meta");
      expectedNames.add("title");
      expectedNames.add("script");
      expectedNames.add("script");
      expectedNames.add("script");
      expectedNames.add("body");
      expectedNames.add("p");
      expectedNames.add("em");
      expectedNames.add("strong");
      expectedNames.add("code");
      expectedNames.add("sup");
      expectedNames.add("var");
      expectedNames.add("acronym");
      expectedNames.add("p");
      expectedNames.add("em");
      expectedNames.add("strong");
      expectedNames.add("code");
      expectedNames.add("sup");
      expectedNames.add("var");
      expectedNames.add("acronym");
      expectedNames.add("p");
      expectedNames.add("em");
      expectedNames.add("strong");
      expectedNames.add("code");
      expectedNames.add("sup");
      expectedNames.add("var");
      expectedNames.add("acronym");
      expectedNames.add("p");
      expectedNames.add("em");
      expectedNames.add("strong");
      expectedNames.add("code");
      expectedNames.add("sup");
      expectedNames.add("var");
      expectedNames.add("acronym");
      expectedNames.add("p");
      expectedNames.add("em");
      expectedNames.add("strong");
      expectedNames.add("code");
      expectedNames.add("sup");
      expectedNames.add("var");
      expectedNames.add("acronym");
      java.util.List svgExpectedNames = new java.util.ArrayList();
      svgExpectedNames.add("svg");
      svgExpectedNames.add("rect");
      svgExpectedNames.add("script");
      svgExpectedNames.add("head");
      svgExpectedNames.add("meta");
      svgExpectedNames.add("title");
      svgExpectedNames.add("body");
      svgExpectedNames.add("p");
      svgExpectedNames.add("em");
      svgExpectedNames.add("strong");
      svgExpectedNames.add("code");
      svgExpectedNames.add("sup");
      svgExpectedNames.add("var");
      svgExpectedNames.add("acronym");
      svgExpectedNames.add("p");
      svgExpectedNames.add("em");
      svgExpectedNames.add("strong");
      svgExpectedNames.add("code");
      svgExpectedNames.add("sup");
      svgExpectedNames.add("var");
      svgExpectedNames.add("acronym");
      svgExpectedNames.add("p");
      svgExpectedNames.add("em");
      svgExpectedNames.add("strong");
      svgExpectedNames.add("code");
      svgExpectedNames.add("sup");
      svgExpectedNames.add("var");
      svgExpectedNames.add("acronym");
      svgExpectedNames.add("p");
      svgExpectedNames.add("em");
      svgExpectedNames.add("strong");
      svgExpectedNames.add("code");
      svgExpectedNames.add("sup");
      svgExpectedNames.add("var");
      svgExpectedNames.add("acronym");
      svgExpectedNames.add("p");
      svgExpectedNames.add("em");
      svgExpectedNames.add("strong");
      svgExpectedNames.add("code");
      svgExpectedNames.add("sup");
      svgExpectedNames.add("var");
      svgExpectedNames.add("acronym");
      java.util.List actualNames = new java.util.ArrayList();
      Element thisElement;
      String thisTag;
      doc = (Document) load("hc_staff", false);
      nameList = doc.getElementsByTagName("*");
      for (int indexN10148 = 0; indexN10148 < nameList.getLength(); indexN10148++) {
          thisElement = (Element) nameList.item(indexN10148);
    thisTag = thisElement.getTagName();
      actualNames.add(thisTag);
        }
      if (("image/svg+xml".equals(getContentType()))) {
          assertEqualsAutoCase("element", "svgTagNames", svgExpectedNames, actualNames);
        } else {
          assertEqualsAutoCase("element", "tagNames", expectedNames, actualNames);
        }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_documentgetelementsbytagnametotallength.class, args);
   }
}
