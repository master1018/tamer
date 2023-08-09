public class LSDocumentBuilderFactory
    extends DOMTestDocumentBuilderFactory {
  private final Object parser;
  private final Method parseURIMethod;
  private final DOMImplementation impl;
  private static abstract class LSStrategy {
    protected LSStrategy() {
    }
    public abstract void applySetting(DocumentBuilderSetting setting,
                                      Object parser) throws
        DOMTestIncompatibleException;
    public abstract boolean hasSetting(Object parser);
  }
  private static class LSFixedStrategy
      extends LSStrategy {
    private final boolean fixedValue;
    public LSFixedStrategy(boolean fixedValue) {
      this.fixedValue = fixedValue;
    }
    public void applySetting(DocumentBuilderSetting setting, Object parser) throws
        DOMTestIncompatibleException {
      if (setting.getValue() != fixedValue) {
        throw new DOMTestIncompatibleException(null, setting);
      }
    }
    public boolean hasSetting(Object parser) {
      return fixedValue;
    }
  }
  private static class LSParameterStrategy
      extends LSStrategy {
    private final String lsParameter;
    private final boolean inverse;
    public LSParameterStrategy(String lsParameter, boolean inverse) {
      this.lsParameter = lsParameter;
      this.inverse = inverse;
    }
    protected static void setParameter(DocumentBuilderSetting setting,
                                       Object parser,
                                       String parameter,
                                       Object value) throws
        DOMTestIncompatibleException {
      try {
        Method domConfigMethod = parser.getClass().getMethod("getDomConfig",
            new Class[0]);
        Object domConfig = domConfigMethod.invoke(parser, new Object[0]);
        Method setParameterMethod = domConfig.getClass().getMethod(
            "setParameter", new Class[] {String.class, Object.class});
        setParameterMethod.invoke(domConfig, new Object[] {parameter, value});
      }
      catch (InvocationTargetException ex) {
        throw new DOMTestIncompatibleException(ex.getTargetException(), setting);
      }
      catch (Exception ex) {
        throw new DOMTestIncompatibleException(ex, setting);
      }
    }
    protected static Object getParameter(Object parser,
                                         String parameter) throws Exception {
      Method domConfigMethod = parser.getClass().getMethod("getDomConfig",
          new Class[0]);
      Object domConfig = domConfigMethod.invoke(parser, new Object[0]);
      Method getParameterMethod = domConfig.getClass().getMethod("getParameter",
          new Class[] {String.class});
      return getParameterMethod.invoke(domConfig, new Object[] {parameter});
    }
    public void applySetting(DocumentBuilderSetting setting, Object parser) throws
        DOMTestIncompatibleException {
      if (inverse) {
        setParameter(setting, parser, lsParameter,
                     new Boolean(!setting.getValue()));
      }
      else {
        setParameter(setting, parser, lsParameter, new Boolean(setting.getValue()));
      }
    }
    public boolean hasSetting(Object parser) {
      try {
        if (inverse) {
          return! ( (Boolean) getParameter(parser, lsParameter)).booleanValue();
        }
        else {
          return ( (Boolean) getParameter(parser, lsParameter)).booleanValue();
        }
      }
      catch (Exception ex) {
        return false;
      }
    }
  }
  private static class LSValidateStrategy
      extends LSParameterStrategy {
    private final String schemaType;
    public LSValidateStrategy(String schemaType) {
      super("validate", false);
      this.schemaType = schemaType;
    }
    public void applySetting(DocumentBuilderSetting setting, Object parser) throws
        DOMTestIncompatibleException {
      super.applySetting(setting, parser);
      setParameter(null, parser, "schema-type", schemaType);
    }
    public boolean hasSetting(Object parser) {
      if (super.hasSetting(parser)) {
        try {
          String parserSchemaType = (String) getParameter(parser, "schema-type");
          if (schemaType == null || schemaType.equals(parserSchemaType)) {
            return true;
          }
        }
        catch (Exception ex) {
        }
      }
      return false;
    }
  }
  private static final Map strategies;
  static {
    strategies = new HashMap();
    strategies.put("coalescing", new LSParameterStrategy("cdata-sections", true));
    strategies.put("expandEntityReferences", new LSParameterStrategy("entities", true));
    strategies.put("ignoringElementContentWhitespace",
                   new LSParameterStrategy("element-content-whitespace", true));
    strategies.put("namespaceAware", new LSParameterStrategy("namespaces", false));
    strategies.put("validating",
                   new LSValidateStrategy("http:
    strategies.put("schemaValidating",
                   new LSValidateStrategy("http:
    strategies.put("ignoringComments", new LSParameterStrategy("comments", true));
    strategies.put("signed", new LSFixedStrategy(true));
    strategies.put("hasNullString", new LSFixedStrategy(true));
  }
  public LSDocumentBuilderFactory(DocumentBuilderSetting[] settings) throws
      DOMTestIncompatibleException {
    super(settings);
    try {
      Class domImplRegistryClass = Class.forName(
          "org.w3c.dom.bootstrap.DOMImplementationRegistry");
      Method newInstanceMethod = domImplRegistryClass.getMethod("newInstance", (Class<?>) null);
      Object domRegistry = newInstanceMethod.invoke(null, (Class<?>) null);
      Method getDOMImplementationMethod = domImplRegistryClass.getMethod(
          "getDOMImplementation", new Class[] {String.class});
      impl = (DOMImplementation) getDOMImplementationMethod.invoke(domRegistry,
          new Object[] {"LS"});
      Method createLSParserMethod = impl.getClass().getMethod("createLSParser",
          new Class[] {short.class, String.class});
      parser = createLSParserMethod.invoke(impl,
                                           new Object[] {new Short( (short) 1), null});
      parseURIMethod = parser.getClass().getMethod("parseURI",
          new Class[] {String.class});
    }
    catch (InvocationTargetException ex) {
      throw new DOMTestIncompatibleException(ex.getTargetException(), null);
    }
    catch (Exception ex) {
      throw new DOMTestIncompatibleException(ex, null);
    }
    if (settings != null) {
      for (int i = 0; i < settings.length; i++) {
        Object strategy = strategies.get(settings[i].getProperty());
        if (strategy == null) {
          throw new DOMTestIncompatibleException(null, settings[i]);
        }
        else {
          ( (LSStrategy) strategy).applySetting(settings[i], parser);
        }
      }
    }
  }
  public DOMTestDocumentBuilderFactory newInstance(
      DocumentBuilderSetting[] newSettings) throws DOMTestIncompatibleException {
    if (newSettings == null) {
      return this;
    }
    DocumentBuilderSetting[] mergedSettings = mergeSettings(newSettings);
    return new LSDocumentBuilderFactory(mergedSettings);
  }
  public Document load(java.net.URL url) throws DOMTestLoadException {
    try {
      return (Document) parseURIMethod.invoke(parser,
                                              new Object[] {url.toString()});
    }
    catch (InvocationTargetException ex) {
      throw new DOMTestLoadException(ex.getTargetException());
    }
    catch (Exception ex) {
      throw new DOMTestLoadException(ex);
    }
  }
  public DOMImplementation getDOMImplementation() {
    return impl;
  }
  public boolean hasFeature(String feature, String version) {
    return getDOMImplementation().hasFeature(feature, version);
  }
  private boolean hasProperty(String parameter) {
    try {
      return ( (Boolean) LSParameterStrategy.getParameter(parser, parameter)).
          booleanValue();
    }
    catch (Exception ex) {
      return true;
    }
  }
  public boolean isCoalescing() {
    return!hasProperty("cdata-sections");
  }
  public boolean isExpandEntityReferences() {
    return!hasProperty("entities");
  }
  public boolean isIgnoringElementContentWhitespace() {
    return!hasProperty("element-content-whitespace");
  }
  public boolean isNamespaceAware() {
    return hasProperty("namespaces");
  }
  public boolean isValidating() {
    return hasProperty("validate");
  }
}
