public class DOM4JTestDocumentBuilderFactory
    extends DOMTestDocumentBuilderFactory {
  private final Object domFactory;
  private final Object saxReader;
  private final org.xml.sax.XMLReader xmlReader;
  private org.w3c.dom.DOMImplementation domImpl;
  private final Method readMethod;
  public DOM4JTestDocumentBuilderFactory(DocumentBuilderSetting[] settings) throws
      DOMTestIncompatibleException {
    super(settings);
    try {
      ClassLoader classLoader = ClassLoader.getSystemClassLoader();
      Class domFactoryClass = classLoader.loadClass(
          "org.dom4j.dom.DOMDocumentFactory");
      Method getInstance = domFactoryClass.getMethod("getInstance", new Class[] {});
      domFactory = getInstance.invoke(null, new Object[] {});
      domImpl = (DOMImplementation) domFactory;
      Class saxReaderClass = classLoader.loadClass("org.dom4j.io.SAXReader");
      Constructor saxReaderConstructor = saxReaderClass.getConstructor(
          new Class[] {classLoader.loadClass("org.dom4j.DocumentFactory")});
      saxReader = saxReaderConstructor.newInstance(new Object[] {domFactory});
      Method getReaderMethod = saxReaderClass.getMethod("getXMLReader",
          new Class[] {});
      xmlReader = (XMLReader) getReaderMethod.invoke(saxReader, new Object[0]);
      readMethod = saxReaderClass.getMethod("read", new Class[] {java.net.URL.class});
    }
    catch (InvocationTargetException ex) {
      throw new DOMTestIncompatibleException(ex.getTargetException(), null);
    }
    catch (Exception ex) {
      throw new DOMTestIncompatibleException(ex, null);
    }
  }
  public DOMTestDocumentBuilderFactory newInstance(DocumentBuilderSetting[]
      newSettings) throws DOMTestIncompatibleException {
    if (newSettings == null) {
      return this;
    }
    DocumentBuilderSetting[] mergedSettings = mergeSettings(newSettings);
    return new DOM4JTestDocumentBuilderFactory(mergedSettings);
  }
  public Document load(java.net.URL url) throws DOMTestLoadException {
    if (url == null) {
      throw new NullPointerException("url");
    }
    if (saxReader == null) {
      throw new NullPointerException("saxReader");
    }
    try {
      return (org.w3c.dom.Document) readMethod.invoke(saxReader,
          new Object[] {url});
    }
    catch (InvocationTargetException ex) {
      ex.getTargetException().printStackTrace();
      throw new DOMTestLoadException(ex.getTargetException());
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw new DOMTestLoadException(ex);
    }
  }
  public DOMImplementation getDOMImplementation() {
    return domImpl;
  }
  public boolean hasFeature(String feature, String version) {
    return domImpl.hasFeature(feature, version);
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
}
