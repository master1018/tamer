public final class nodenormalize01 extends DOMTestCase {
   public nodenormalize01(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staffNS", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Document newDoc;
      DOMImplementation domImpl;
      DocumentType docType;
      DocumentType docTypeNull = null;
      Element documentElement;
      Element element1;
      Element element2;
      Element element3;
      Element element4;
      Element element5;
      Element element6;
      Element element7;
      Text text1;
      Text text2;
      Text text3;
      ProcessingInstruction pi;
      CDATASection cData;
      Comment comment;
      EntityReference entRef;
      NodeList elementList;
      Node appendedChild;
      doc = (Document) load("staffNS", true);
      domImpl = doc.getImplementation();
      newDoc = domImpl.createDocument("http:
      element1 = newDoc.createElement("element1");
      element2 = newDoc.createElement("element2");
      element3 = newDoc.createElement("element3");
      element4 = newDoc.createElement("element4");
      element5 = newDoc.createElement("element5");
      element6 = newDoc.createElement("element6");
      element7 = newDoc.createElement("element7");
      text1 = newDoc.createTextNode("text1");
      text2 = newDoc.createTextNode("text2");
      text3 = newDoc.createTextNode("text3");
      cData = newDoc.createCDATASection("Cdata");
      comment = newDoc.createComment("comment");
      pi = newDoc.createProcessingInstruction("PITarget", "PIData");
      entRef = newDoc.createEntityReference("EntRef");
      assertNotNull("createdEntRefNotNull", entRef);
      documentElement = newDoc.getDocumentElement();
      appendedChild = documentElement.appendChild(element1);
      appendedChild = element2.appendChild(text1);
      appendedChild = element2.appendChild(text2);
      appendedChild = element2.appendChild(text3);
      appendedChild = element1.appendChild(element2);
      text1 = (Text) text1.cloneNode(false);
      text2 = (Text) text2.cloneNode(false);
      appendedChild = element3.appendChild(entRef);
      appendedChild = element3.appendChild(text1);
      appendedChild = element3.appendChild(text2);
      appendedChild = element1.appendChild(element3);
      text1 = (Text) text1.cloneNode(false);
      text2 = (Text) text2.cloneNode(false);
      appendedChild = element4.appendChild(cData);
      appendedChild = element4.appendChild(text1);
      appendedChild = element4.appendChild(text2);
      appendedChild = element1.appendChild(element4);
      text2 = (Text) text2.cloneNode(false);
      text3 = (Text) text3.cloneNode(false);
      appendedChild = element5.appendChild(comment);
      appendedChild = element5.appendChild(text2);
      appendedChild = element5.appendChild(text3);
      appendedChild = element1.appendChild(element5);
      text2 = (Text) text2.cloneNode(false);
      text3 = (Text) text3.cloneNode(false);
      appendedChild = element6.appendChild(pi);
      appendedChild = element6.appendChild(text2);
      appendedChild = element6.appendChild(text3);
      appendedChild = element1.appendChild(element6);
      entRef = (EntityReference) entRef.cloneNode(false);
      text1 = (Text) text1.cloneNode(false);
      text2 = (Text) text2.cloneNode(false);
      text3 = (Text) text3.cloneNode(false);
      appendedChild = element7.appendChild(entRef);
      appendedChild = element7.appendChild(text1);
      appendedChild = element7.appendChild(text2);
      appendedChild = element7.appendChild(text3);
      appendedChild = element1.appendChild(element7);
      elementList = element1.getChildNodes();
      assertSize("nodeNormalize01_1Bef", 6, elementList);
      elementList = element2.getChildNodes();
      assertSize("nodeNormalize01_2Bef", 3, elementList);
      elementList = element3.getChildNodes();
      assertSize("nodeNormalize01_3Bef", 3, elementList);
      elementList = element4.getChildNodes();
      assertSize("nodeNormalize01_4Bef", 3, elementList);
      elementList = element5.getChildNodes();
      assertSize("nodeNormalize01_5Bef", 3, elementList);
      elementList = element6.getChildNodes();
      assertSize("nodeNormalize01_6Bef", 3, elementList);
      elementList = element7.getChildNodes();
      assertSize("nodeNormalize01_7Bef", 4, elementList);
      newDoc.normalize();
      elementList = element1.getChildNodes();
      assertSize("nodeNormalize01_1Aft", 6, elementList);
      elementList = element2.getChildNodes();
      assertSize("nodeNormalize01_2Aft", 1, elementList);
      elementList = element3.getChildNodes();
      assertSize("nodeNormalize01_3Aft", 2, elementList);
      elementList = element4.getChildNodes();
      assertSize("nodeNormalize01_4Aft", 2, elementList);
      elementList = element5.getChildNodes();
      assertSize("nodeNormalize01_5Aft", 2, elementList);
      elementList = element6.getChildNodes();
      assertSize("nodeNormalize01_6Aft", 2, elementList);
      elementList = element7.getChildNodes();
      assertSize("nodeNormalize01_7Aft", 2, elementList);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodenormalize01.class, args);
   }
}
