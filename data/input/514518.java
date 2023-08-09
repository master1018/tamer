public abstract class DOMTestDocumentBuilderFactory {
  private final DocumentBuilderSetting[] settings;
  public DOMTestDocumentBuilderFactory(DocumentBuilderSetting[] settings) throws
      DOMTestIncompatibleException {
    if (settings == null) {
      this.settings = new DocumentBuilderSetting[0];
    }
    else {
      this.settings = (DocumentBuilderSetting[]) settings.clone();
    }
  }
  public abstract DOMTestDocumentBuilderFactory newInstance(
      DocumentBuilderSetting[] settings) throws DOMTestIncompatibleException;
  public abstract DOMImplementation getDOMImplementation();
  public abstract boolean hasFeature(String feature, String version);
  public abstract Document load(java.net.URL url) throws DOMTestLoadException;
  public Object createXPathEvaluator(Document doc) {
    try {
      Method getFeatureMethod = doc.getClass().getMethod("getFeature",
          new Class[] {String.class, String.class});
      if (getFeatureMethod != null) {
        return getFeatureMethod.invoke(doc, new Object[] {"XPath", null});
      }
    }
    catch (Exception ex) {
    }
    return doc;
  }
  protected DocumentBuilderSetting[] mergeSettings(DocumentBuilderSetting[]
      newSettings) {
    if (newSettings == null) {
      return (DocumentBuilderSetting[]) settings.clone();
    }
    List mergedSettings = new ArrayList(settings.length + newSettings.length);
    for (int i = 0; i < newSettings.length; i++) {
      mergedSettings.add(newSettings[i]);
    }
    for (int i = 0; i < settings.length; i++) {
      DocumentBuilderSetting setting = settings[i];
      boolean hasConflict = false;
      for (int j = 0; j < newSettings.length; j++) {
        DocumentBuilderSetting newSetting = newSettings[j];
        if (newSetting.hasConflict(setting) || setting.hasConflict(newSetting)) {
          hasConflict = true;
          break;
        }
      }
      if (!hasConflict) {
        mergedSettings.add(setting);
      }
    }
    DocumentBuilderSetting[] mergedArray =
        new DocumentBuilderSetting[mergedSettings.size()];
    for (int i = 0; i < mergedSettings.size(); i++) {
      mergedArray[i] = (DocumentBuilderSetting) mergedSettings.get(i);
    }
    return mergedArray;
  }
  public String addExtension(String testFileName) {
    String contentType = getContentType();
    if ("text/html".equals(contentType)) {
      return testFileName + ".html";
    }
    if ("image/svg+xml".equals(contentType)) {
      return testFileName + ".svg";
    }
    if ("application/xhtml+xml".equals(contentType)) {
      return testFileName + ".xhtml";
    }
    return testFileName + ".xml";
  }
  public abstract boolean isCoalescing();
  public abstract boolean isExpandEntityReferences();
  public abstract boolean isIgnoringElementContentWhitespace();
  public abstract boolean isNamespaceAware();
  public abstract boolean isValidating();
  public String getContentType() {
    return System.getProperty("org.w3c.domts.contentType", "text/xml");
  }
  public final DocumentBuilderSetting[] getActualSettings() {
    DocumentBuilderSetting[] allSettings = new DocumentBuilderSetting[] {
        DocumentBuilderSetting.coalescing,
        DocumentBuilderSetting.expandEntityReferences,
        DocumentBuilderSetting.hasNullString,
        DocumentBuilderSetting.ignoringElementContentWhitespace,
        DocumentBuilderSetting.namespaceAware,
        DocumentBuilderSetting.signed,
        DocumentBuilderSetting.validating,
        DocumentBuilderSetting.notCoalescing,
        DocumentBuilderSetting.notExpandEntityReferences,
        DocumentBuilderSetting.notHasNullString,
        DocumentBuilderSetting.notIgnoringElementContentWhitespace,
        DocumentBuilderSetting.notNamespaceAware,
        DocumentBuilderSetting.notSigned,
        DocumentBuilderSetting.notValidating
    };
    List list = new ArrayList(allSettings.length / 2);
    for (int i = 0; i < allSettings.length; i++) {
      if (allSettings[i].hasSetting(this)) {
        list.add(allSettings[i]);
      }
    }
    DocumentBuilderSetting[] settings = new DocumentBuilderSetting[list.size()];
    for (int i = 0; i < settings.length; i++) {
      settings[i] = (DocumentBuilderSetting) list.get(i);
    }
    return settings;
  }
}
