public abstract class DOMTest  {
  private DOMTestDocumentBuilderFactory factory;
  private int mutationCount = 0;
  public DOMTest(DOMTestDocumentBuilderFactory factory) {
    if (factory == null) {
      throw new NullPointerException("factory");
    }
    this.factory = factory;
  }
  public DOMTest() {
    factory = null;
  }
  protected void setFactory(DOMTestDocumentBuilderFactory factory) {
    this.factory = factory;
  }
  public boolean hasFeature(String feature, String version) {
    return factory.hasFeature(feature, version);
  }
  public boolean hasSetting(DocumentBuilderSetting setting) {
    return setting.hasSetting(factory);
  }
  protected DOMTestDocumentBuilderFactory getFactory() {
    return factory;
  }
  public DOMImplementation getImplementation() {
    return factory.getDOMImplementation();
  }
  private URL resolveURI(String baseURI) throws DOMTestLoadException {
    String docURI = factory.addExtension(baseURI);
    URL resolvedURI = null;
    try {
      resolvedURI = new URL(docURI);
      if (resolvedURI.getProtocol() != null) {
        return resolvedURI;
      }
    }
    catch (MalformedURLException ex) {
    }
    resolvedURI = getClass().getResource("/" + docURI);
    if (resolvedURI == null) {
      int firstSlash = docURI.indexOf('/');
      try {
        if (firstSlash == 0
            || (firstSlash >= 1
                && docURI.charAt(firstSlash - 1) == ':')) {
          resolvedURI = new URL(docURI);
        }
        else {
          String filename = getClass().getPackage().getName();
          filename =
              "tests/"
              + filename.substring(14).replace('.', '/')
              + "/files/"
              + docURI;
          resolvedURI = new java.io.File(filename).toURL();
        }
      }
      catch (MalformedURLException ex) {
        throw new DOMTestLoadException(ex);
      }
    }
    if (resolvedURI == null) {
      throw new DOMTestLoadException(
          new java.io.FileNotFoundException(docURI));
    }
    return resolvedURI;
  }
  public String getResourceURI(String href, String scheme, String contentType) throws
      DOMTestLoadException {
    if (scheme == null) {
      throw new NullPointerException("scheme");
    }
    if ("file".equals(scheme)) {
      return resolveURI(href).toString();
    }
    if ("http".equals(scheme)) {
      StringBuffer httpURL = new StringBuffer(
          System.getProperty("org.w3c.domts.httpbase",
                             "http:
      httpURL.append(href);
      if ("application/pdf".equals(contentType)) {
        httpURL.append(".pdf");
      }
      else {
        httpURL.append(".xml");
      }
      return httpURL.toString();
    }
    throw new DOMTestLoadException(new Exception("Unrecognized URI scheme " +
                                                 scheme));
  }
  public String createTempURI(String scheme) throws DOMTestLoadException {
    if (scheme == null) {
      throw new NullPointerException("scheme");
    }
    if ("file".equals(scheme)) {
      try {
        File tempFile = File.createTempFile("domts", ".xml");
        try {
          Method method = File.class.getMethod("toURI", (Class<?>) null);
          Object uri = method.invoke(tempFile, (Class<?>) null);
          return uri.toString();
        }
        catch (NoSuchMethodException ex) {
          URL url = tempFile.toURL();
          return url.toString();
        }
      }
      catch (Exception ex) {
        throw new DOMTestLoadException(ex);
      }
    }
    if ("http".equals(scheme)) {
      String httpBase = System.getProperty("org.w3c.domts.httpbase",
                                           "http:
      java.lang.StringBuffer buf = new StringBuffer(httpBase);
      if (!httpBase.endsWith("/")) {
          buf.append("/");
      }
      buf.append("tmp");
      buf.append( (new java.util.Random()).nextInt(Integer.MAX_VALUE));
      buf.append(".xml");
      return buf.toString();
    }
    throw new DOMTestLoadException(new Exception("Unrecognized URI scheme " +
                                                 scheme));
  }
  public Document load(String docURI, boolean willBeModified) throws
      DOMTestLoadException {
    Document doc = factory.load(resolveURI(docURI));
    return doc;
  }
  public void preload(String contentType, String docURI, boolean willBeModified) throws
      DOMTestIncompatibleException {
    if ("text/html".equals(contentType) ||
        "application/xhtml+xml".equals(contentType)) {
      if (docURI.startsWith("staff") || docURI.equals("datatype_normalization")) {
        throw DOMTestIncompatibleException.incompatibleLoad(docURI, contentType);
      }
    }
  }
  public Object createXPathEvaluator(Document doc) {
    return factory.createXPathEvaluator(doc);
  }
  public InputStream createStream(String bytes) throws DOMTestLoadException,
      IOException {
    int byteCount = bytes.length() / 2;
    byte[] array = new byte[byteCount];
    for (int i = 0; i < byteCount; i++) {
      array[i] = Byte.parseByte(bytes.substring(i * 2, i * 2 + 2), 16);
    }
    return new java.io.ByteArrayInputStream(array);
  }
  abstract public String getTargetURI();
  public final boolean isCoalescing() {
    return factory.isCoalescing();
  }
  public final boolean isExpandEntityReferences() {
    return factory.isExpandEntityReferences();
  }
  public final boolean isIgnoringElementContentWhitespace() {
    return factory.isIgnoringElementContentWhitespace();
  }
  public final boolean isNamespaceAware() {
    return factory.isNamespaceAware();
  }
  public final boolean isValidating() {
    return factory.isValidating();
  }
  public final boolean isSigned() {
    return true;
  }
  public final boolean isHasNullString() {
    return true;
  }
  public final String getContentType() {
    return factory.getContentType();
  }
  public final int getMutationCount() {
    return mutationCount;
  }
}
