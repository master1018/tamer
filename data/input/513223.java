public class JTidyDocumentBuilderFactory
    extends DOMTestDocumentBuilderFactory {
  private final Constructor tidyConstructor;
  private final Method parseDOMMethod;
  private final DOMImplementation domImpl;
  private static final Class[] NO_CLASSES = new Class[0];
  private static final Object[] NO_OBJECTS = new Object[0];
  public JTidyDocumentBuilderFactory(
      DocumentBuilderSetting[] settings) throws DOMTestIncompatibleException {
    super(settings);
    try {
      ClassLoader classLoader = ClassLoader.getSystemClassLoader();
      Class tidyClass = classLoader.loadClass("org.w3c.tidy.Tidy");
      tidyConstructor =
          tidyClass.getConstructor(NO_CLASSES);
      parseDOMMethod =
          tidyClass.getMethod("parseDOM",
                              new Class[] {java.io.InputStream.class,
                              java.io.OutputStream.class});
      domImpl = new JTidyDOMImplementation();
    }
    catch (Exception ex) {
      throw new DOMTestIncompatibleException(ex, null);
    }
    if (settings != null) {
      for (int i = 0; i < settings.length; i++) {
      }
    }
  }
  public DOMTestDocumentBuilderFactory newInstance(DocumentBuilderSetting[]
      newSettings) throws DOMTestIncompatibleException {
    if (newSettings == null) {
      return this;
    }
    DocumentBuilderSetting[] mergedSettings = mergeSettings(newSettings);
    return new JTidyDocumentBuilderFactory(mergedSettings);
  }
  public Document load(java.net.URL url) throws DOMTestLoadException {
    Document doc = null;
    try {
      java.io.InputStream stream = url.openStream();
      Object tidyObj = tidyConstructor.newInstance(new Object[0]);
      doc = (Document) parseDOMMethod.invoke(tidyObj,
                                             new Object[] {stream, null});
    }
    catch (InvocationTargetException ex) {
      throw new DOMTestLoadException(ex.getTargetException());
    }
    catch (Exception ex) {
      throw new DOMTestLoadException(ex);
    }
    return doc;
  }
  public DOMImplementation getDOMImplementation() {
    return domImpl;
  }
  public boolean hasFeature(String feature, String version) {
    return domImpl.hasFeature(feature, version);
  }
  public String getContentType() {
    return "text/html";
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
    return false;
  }
  public boolean isValidating() {
    return false;
  }
}
