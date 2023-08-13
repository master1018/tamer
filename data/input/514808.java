public abstract class DocumentBuilderSettingStrategy {
  protected DocumentBuilderSettingStrategy() {
  }
  private static final String JAXP_SCHEMA_LANGUAGE =
      "http:
  private static final String W3C_XML_SCHEMA =
      "http:
  public boolean hasConflict(DocumentBuilderSettingStrategy other) {
    return (other == this);
  }
  public abstract void applySetting(
      DocumentBuilderFactory factory,
      boolean value) throws DOMTestIncompatibleException;
  public abstract boolean hasSetting(DOMTestDocumentBuilderFactory factory);
  public static final DocumentBuilderSettingStrategy coalescing =
      new DocumentBuilderSettingStrategy() {
    public void applySetting(DocumentBuilderFactory factory, boolean value)
        throws DOMTestIncompatibleException {
      factory.setCoalescing(value);
    }
    public boolean hasSetting(DOMTestDocumentBuilderFactory factory) {
      return factory.isCoalescing();
    }
  };
  public static final DocumentBuilderSettingStrategy
      expandEntityReferences =
      new DocumentBuilderSettingStrategy() {
    public void applySetting(DocumentBuilderFactory factory, boolean value)
        throws DOMTestIncompatibleException {
      factory.setExpandEntityReferences(value);
    }
    public boolean hasSetting(DOMTestDocumentBuilderFactory factory) {
      return factory.isExpandEntityReferences();
    }
  };
  public static final DocumentBuilderSettingStrategy
      ignoringElementContentWhitespace =
      new DocumentBuilderSettingStrategy() {
    public void applySetting(DocumentBuilderFactory factory, boolean value)
        throws DOMTestIncompatibleException {
      factory.setIgnoringElementContentWhitespace(value);
    }
    public boolean hasSetting(DOMTestDocumentBuilderFactory factory) {
      return factory.isIgnoringElementContentWhitespace();
    }
  };
  public static final DocumentBuilderSettingStrategy ignoringComments =
      new DocumentBuilderSettingStrategy() {
    public void applySetting(DocumentBuilderFactory factory, boolean value)
        throws DOMTestIncompatibleException {
      if (value) {
        throw new DOMTestIncompatibleException(
            new Exception("ignoreComments=true not supported"),
            DocumentBuilderSetting.ignoringComments);
      }
    }
    public boolean hasSetting(DOMTestDocumentBuilderFactory factory) {
      return false;
    }
  };
  public static final DocumentBuilderSettingStrategy namespaceAware =
      new DocumentBuilderSettingStrategy() {
    public void applySetting(DocumentBuilderFactory factory, boolean value) throws
        DOMTestIncompatibleException {
      factory.setNamespaceAware(value);
    }
    public boolean hasSetting(DOMTestDocumentBuilderFactory factory) {
      return factory.isNamespaceAware();
    }
  };
  public static final DocumentBuilderSettingStrategy validating =
      new DocumentBuilderSettingStrategy() {
    public void applySetting(DocumentBuilderFactory factory, boolean value) throws
        DOMTestIncompatibleException {
      factory.setValidating(value);
    }
    public boolean hasSetting(DOMTestDocumentBuilderFactory factory) {
      return factory.isValidating();
    }
  };
  public static final DocumentBuilderSettingStrategy signed =
      new DocumentBuilderSettingStrategy() {
    public void applySetting(DocumentBuilderFactory factory, boolean value) throws
        DOMTestIncompatibleException {
      if (!value) {
        throw new DOMTestIncompatibleException(
            null,
            DocumentBuilderSetting.notSigned);
      }
    }
    public boolean hasSetting(DOMTestDocumentBuilderFactory factory) {
      return true;
    }
  };
  public static final DocumentBuilderSettingStrategy hasNullString =
      new DocumentBuilderSettingStrategy() {
    public void applySetting(DocumentBuilderFactory factory, boolean value) throws
        DOMTestIncompatibleException {
      if (!value) {
        throw new DOMTestIncompatibleException(
            null,
            DocumentBuilderSetting.notHasNullString);
      }
    }
    public boolean hasSetting(DOMTestDocumentBuilderFactory factory) {
      return true;
    }
  };
  public static final DocumentBuilderSettingStrategy schemaValidating =
      new DocumentBuilderSettingStrategy() {
    public void applySetting(DocumentBuilderFactory factory, boolean value) throws
        DOMTestIncompatibleException {
      if (value) {
        factory.setNamespaceAware(true);
        factory.setValidating(true);
        factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
      }
      else {
        factory.setAttribute(JAXP_SCHEMA_LANGUAGE,
                             "http:
      }
    }
    public boolean hasSetting(DOMTestDocumentBuilderFactory factory) {
      try {
        if (factory.isValidating()) {
          Method getAttrMethod = factory.getClass().getMethod("getAttribute",
              new Class[] {String.class});
          String val = (String) getAttrMethod.invoke(factory,
              new Object[] {JAXP_SCHEMA_LANGUAGE});
          return W3C_XML_SCHEMA.equals(val);
        }
      }
      catch (Exception ex) {
      }
      return false;
    }
    public boolean hasConflict(DocumentBuilderSettingStrategy other) {
      if (other == this ||
          other == DocumentBuilderSettingStrategy.namespaceAware ||
          other == DocumentBuilderSettingStrategy.validating) {
        return true;
      }
      return false;
    }
  };
}
