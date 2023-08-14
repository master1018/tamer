public class XalanDOMTestDocumentBuilderFactory
    extends JAXPDOMTestDocumentBuilderFactory {
  public XalanDOMTestDocumentBuilderFactory(
      DocumentBuilderFactory baseFactory,
      DocumentBuilderSetting[] settings) throws DOMTestIncompatibleException {
    super(baseFactory, settings);
  }
  protected DOMTestDocumentBuilderFactory createInstance(DocumentBuilderFactory
      newFactory,
      DocumentBuilderSetting[] mergedSettings) throws
      DOMTestIncompatibleException {
    return new XalanDOMTestDocumentBuilderFactory(newFactory, mergedSettings);
  }
  public Object createXPathEvaluator(Document doc) {
    try {
      Class xpathClass = Class.forName(
          "org.apache.xpath.domapi.XPathEvaluatorImpl");
      Constructor constructor = xpathClass.getConstructor(new Class[] {Document.class});
      return constructor.newInstance(new Object[] {doc});
    }
    catch (Exception ex) {
    }
    return doc;
  }
}
