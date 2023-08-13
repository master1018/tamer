public class BatikTestDocumentBuilderFactory
    extends DOMTestDocumentBuilderFactory {
  private Object domFactory;
  private org.xml.sax.XMLReader xmlReader;
  private Method createDocument;
  private DOMImplementation domImpl;
  public BatikTestDocumentBuilderFactory(
      DocumentBuilderSetting[] settings) throws DOMTestIncompatibleException {
    super(settings);
    domImpl = null;
    SAXParserFactory saxFactory = SAXParserFactory.newInstance();
    try {
      SAXParser saxParser = saxFactory.newSAXParser();
      xmlReader = saxParser.getXMLReader();
    } catch (Exception ex) {
      throw new DOMTestIncompatibleException(ex, null);
    }
    String xmlReaderClassName = xmlReader.getClass().getName();
    try {
      ClassLoader classLoader = ClassLoader.getSystemClassLoader();
      Class domFactoryClass =
          classLoader.loadClass(
          "org.apache.batik.dom.svg.SAXSVGDocumentFactory");
      Constructor domFactoryConstructor =
          domFactoryClass.getConstructor(new Class[] {String.class});
      domFactory =
          domFactoryConstructor.newInstance(
          new Object[] {xmlReaderClassName});
      createDocument =
          domFactoryClass.getMethod(
          "createDocument",
          new Class[] {String.class, java.io.InputStream.class});
    } catch (InvocationTargetException ex) {
      throw new DOMTestIncompatibleException(
          ex.getTargetException(),
          null);
    } catch (Exception ex) {
      throw new DOMTestIncompatibleException(ex, null);
    }
  }
  public DOMTestDocumentBuilderFactory newInstance(
      DocumentBuilderSetting[] newSettings)
        throws DOMTestIncompatibleException {
    if (newSettings == null) {
      return this;
    }
    DocumentBuilderSetting[] mergedSettings = mergeSettings(newSettings);
    return new BatikTestDocumentBuilderFactory(mergedSettings);
  }
  public Document load(java.net.URL url) throws DOMTestLoadException {
    try {
      java.io.InputStream stream = url.openStream();
      return (org.w3c.dom.Document) createDocument.invoke(
          domFactory,
          new Object[] {url.toString(), stream});
    } catch (InvocationTargetException ex) {
      ex.printStackTrace();
      throw new DOMTestLoadException(ex.getTargetException());
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new DOMTestLoadException(ex);
    }
  }
  public DOMImplementation getDOMImplementation() {
    if (domImpl == null) {
      try {
        Class svgDomImplClass =
            ClassLoader.getSystemClassLoader().loadClass(
            "org.apache.batik.dom.svg.SVGDOMImplementation");
        Method getImpl =
            svgDomImplClass.getMethod(
            "getDOMImplementation",
            new Class[0]);
        domImpl =
            (DOMImplementation) getImpl.invoke(null, new Object[0]);
      } catch (Exception ex) {
        return null;
      }
    }
    return domImpl;
  }
  public boolean hasFeature(String feature, String version) {
    return getDOMImplementation().hasFeature(feature, version);
  }
  public String addExtension(String testFileName) {
    return testFileName + ".svg";
  }
  public boolean isCoalescing() {
    return false;
  }
  public boolean isExpandEntityReferences() {
    return false;
  }
  public boolean isIgnoringElementContentWhitespace() {
    return false;
  }
  public boolean isNamespaceAware() {
    return true;
  }
  public boolean isValidating() {
    return false;
  }
  public String getContentType() {
    return "image/svg+xml";
  }
}
