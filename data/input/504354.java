public class JAXPDOMTestDocumentBuilderFactory
    extends DOMTestDocumentBuilderFactory {
  private DocumentBuilderFactory factory;
  private DocumentBuilder builder;
  public JAXPDOMTestDocumentBuilderFactory(
      DocumentBuilderFactory baseFactory,
      DocumentBuilderSetting[] settings) throws DOMTestIncompatibleException {
    super(settings);
    if (baseFactory == null) {
      factory = DocumentBuilderFactory.newInstance();
    }
    else {
      factory = baseFactory;
    }
    if (settings != null) {
      for (int i = 0; i < settings.length; i++) {
        settings[i].applySetting(factory);
      }
    }
    try {
      this.builder = factory.newDocumentBuilder();
    }
    catch (ParserConfigurationException ex) {
      throw new DOMTestIncompatibleException(ex, null);
    }
  }
  protected DOMTestDocumentBuilderFactory createInstance(DocumentBuilderFactory
      newFactory,
      DocumentBuilderSetting[] mergedSettings) throws
      DOMTestIncompatibleException {
    return new JAXPDOMTestDocumentBuilderFactory(newFactory, mergedSettings);
  }
  public DOMTestDocumentBuilderFactory newInstance(DocumentBuilderSetting[]
      newSettings) throws DOMTestIncompatibleException {
    if (newSettings == null) {
      return this;
    }
    DocumentBuilderSetting[] mergedSettings = mergeSettings(newSettings);
    DocumentBuilderFactory newFactory = factory.newInstance();
    return createInstance(newFactory, mergedSettings);
  }
  private class LoadErrorHandler
      implements org.xml.sax.ErrorHandler {
    private SAXException parseException;
    private int errorCount;
    private int warningCount;
    public LoadErrorHandler() {
      parseException = null;
      errorCount = 0;
      warningCount = 0;
    }
    public void error(SAXParseException ex) {
      errorCount++;
      if (parseException == null) {
        parseException = ex;
      }
    }
    public void warning(SAXParseException ex) {
      warningCount++;
    }
    public void fatalError(SAXParseException ex) {
      if (parseException == null) {
        parseException = ex;
      }
    }
    public SAXException getFirstException() {
      return parseException;
    }
  }
  public Document load(java.net.URL url) throws DOMTestLoadException {
    Document doc = null;
    Exception parseException = null;
    try {
      LoadErrorHandler errorHandler = new LoadErrorHandler();
      builder.setErrorHandler(errorHandler);
      doc = builder.parse(url.openStream(), url.toString());
      parseException = errorHandler.getFirstException();
    }
    catch (Exception ex) {
      parseException = ex;
    }
    builder.setErrorHandler(null);
    if (parseException != null) {
      throw new DOMTestLoadException(parseException);
    }
    return doc;
  }
  public DOMImplementation getDOMImplementation() {
    return builder.getDOMImplementation();
  }
  public boolean hasFeature(String feature, String version) {
    return builder.getDOMImplementation().hasFeature(feature, version);
  }
  public boolean isCoalescing() {
    return factory.isCoalescing();
  }
  public boolean isExpandEntityReferences() {
    return factory.isExpandEntityReferences();
  }
  public boolean isIgnoringElementContentWhitespace() {
    return factory.isIgnoringElementContentWhitespace();
  }
  public boolean isNamespaceAware() {
    return factory.isNamespaceAware();
  }
  public boolean isValidating() {
    return factory.isValidating();
  }
  public static DocumentBuilderSetting[] getConfiguration1() {
    return new DocumentBuilderSetting[] {
        DocumentBuilderSetting.notCoalescing,
        DocumentBuilderSetting.notExpandEntityReferences,
        DocumentBuilderSetting.notIgnoringElementContentWhitespace,
        DocumentBuilderSetting.notNamespaceAware,
        DocumentBuilderSetting.notValidating};
  }
  public static DocumentBuilderSetting[] getConfiguration2() {
    return new DocumentBuilderSetting[] {
        DocumentBuilderSetting.notCoalescing,
        DocumentBuilderSetting.expandEntityReferences,
        DocumentBuilderSetting.ignoringElementContentWhitespace,
        DocumentBuilderSetting.namespaceAware,
        DocumentBuilderSetting.validating};
  }
}
