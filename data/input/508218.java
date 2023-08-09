public final class documenttypegetentities extends DOMTestCase {
   public documenttypegetentities(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentType docType;
      NamedNodeMap entityList;
      String name;
      java.util.Collection expectedResult = new java.util.ArrayList();
      expectedResult.add("ent1");
      expectedResult.add("ent2");
      expectedResult.add("ent3");
      expectedResult.add("ent4");
      expectedResult.add("ent5");
      java.util.Collection expectedResultSVG = new java.util.ArrayList();
      expectedResultSVG.add("ent1");
      expectedResultSVG.add("ent2");
      expectedResultSVG.add("ent3");
      expectedResultSVG.add("ent4");
      expectedResultSVG.add("ent5");
      expectedResultSVG.add("svgunit");
      expectedResultSVG.add("svgtest");
      java.util.Collection nameList = new java.util.ArrayList();
      Node entity;
      doc = (Document) load("staff", false);
      docType = doc.getDoctype();
      assertNotNull("docTypeNotNull", docType);
      entityList = docType.getEntities();
      assertNotNull("entitiesNotNull", entityList);
      for (int indexN1007B = 0; indexN1007B < entityList.getLength(); indexN1007B++) {
          entity = (Node) entityList.item(indexN1007B);
    name = entity.getNodeName();
      nameList.add(name);
        }
      if (("image/svg+xml".equals(getContentType()))) {
          assertEquals("entityNamesSVG", expectedResultSVG, nameList);
      } else {
          assertEquals("entityNames", expectedResult, nameList);
      }
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documenttypegetentities.class, args);
   }
}
