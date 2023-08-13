public final class textparseintolistofelements extends DOMTestCase {
   public textparseintolistofelements(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node addressNode;
      NodeList childList;
      Node child;
      int length;
      String value;
      Node grandChild;
      java.util.List result = new java.util.ArrayList();
      java.util.List expectedNormal = new java.util.ArrayList();
      expectedNormal.add("1900 Dallas Road");
      expectedNormal.add(" Dallas, ");
      expectedNormal.add("Texas");
      expectedNormal.add("\n 98554");
      java.util.List expectedExpanded = new java.util.ArrayList();
      expectedExpanded.add("1900 Dallas Road Dallas, Texas\n 98554");
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("address");
      addressNode = elementList.item(1);
      childList = addressNode.getChildNodes();
      length = (int) childList.getLength();
      for (int indexN1007F = 0; indexN1007F < childList.getLength(); indexN1007F++) {
          child = (Node) childList.item(indexN1007F);
    value = child.getNodeValue();
      if ((value == null)) {
          grandChild = child.getFirstChild();
      assertNotNull("grandChildNotNull", grandChild);
      value = grandChild.getNodeValue();
      result.add(value);
      } else {
          result.add(value);
      }
      }
      if (equals(4, length)) {
          assertEquals("assertEqNormal", expectedNormal, result);
      } else {
          assertEquals("assertEqCoalescing", expectedExpanded, result);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(textparseintolistofelements.class, args);
   }
}
