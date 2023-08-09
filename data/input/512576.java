public final class DocumentBuilderSetting {
  private final String property;
  private final boolean value;
  private final DocumentBuilderSettingStrategy strategy;
  public static final DocumentBuilderSetting coalescing =
      new DocumentBuilderSetting(
      "coalescing",
      true,
      DocumentBuilderSettingStrategy.coalescing);
  public static final DocumentBuilderSetting notCoalescing =
      new DocumentBuilderSetting(
      "coalescing",
      false,
      DocumentBuilderSettingStrategy.coalescing);
  public static final DocumentBuilderSetting expandEntityReferences =
      new DocumentBuilderSetting(
      "expandEntityReferences",
      true,
      DocumentBuilderSettingStrategy.expandEntityReferences);
  public static final DocumentBuilderSetting notExpandEntityReferences =
      new DocumentBuilderSetting(
      "expandEntityReferences",
      false,
      DocumentBuilderSettingStrategy.expandEntityReferences);
  public static final DocumentBuilderSetting ignoringElementContentWhitespace =
      new DocumentBuilderSetting(
      "ignoringElementContentWhitespace",
      true,
      DocumentBuilderSettingStrategy.ignoringElementContentWhitespace);
  public static final DocumentBuilderSetting
      notIgnoringElementContentWhitespace =
      new DocumentBuilderSetting(
      "ignoringElementContentWhitespace",
      false,
      DocumentBuilderSettingStrategy.ignoringElementContentWhitespace);
  public static final DocumentBuilderSetting namespaceAware =
      new DocumentBuilderSetting(
      "namespaceAware",
      true,
      DocumentBuilderSettingStrategy.namespaceAware);
  public static final DocumentBuilderSetting notNamespaceAware =
      new DocumentBuilderSetting(
      "namespaceAware",
      false,
      DocumentBuilderSettingStrategy.namespaceAware);
  public static final DocumentBuilderSetting validating =
      new DocumentBuilderSetting(
      "validating",
      true,
      DocumentBuilderSettingStrategy.validating);
  public static final DocumentBuilderSetting notValidating =
      new DocumentBuilderSetting(
      "validating",
      false,
      DocumentBuilderSettingStrategy.validating);
  public static final DocumentBuilderSetting signed =
      new DocumentBuilderSetting(
      "signed",
      true,
      DocumentBuilderSettingStrategy.signed);
  public static final DocumentBuilderSetting notSigned =
      new DocumentBuilderSetting(
      "signed",
      false,
      DocumentBuilderSettingStrategy.signed);
  public static final DocumentBuilderSetting hasNullString =
      new DocumentBuilderSetting(
      "hasNullString",
      true,
      DocumentBuilderSettingStrategy.hasNullString);
  public static final DocumentBuilderSetting notHasNullString =
      new DocumentBuilderSetting(
      "hasNullString",
      false,
      DocumentBuilderSettingStrategy.hasNullString);
  public static final DocumentBuilderSetting schemaValidating =
      new DocumentBuilderSetting(
      "schemaValidating",
      true,
      DocumentBuilderSettingStrategy.schemaValidating);
  public static final DocumentBuilderSetting notSchemaValidating =
      new DocumentBuilderSetting(
      "schemaValidating",
      false,
      DocumentBuilderSettingStrategy.schemaValidating);
  public static final DocumentBuilderSetting ignoringComments =
      new DocumentBuilderSetting(
      "ignoringComments",
      true,
      DocumentBuilderSettingStrategy.ignoringComments);
  public static final DocumentBuilderSetting notIgnoringComments =
      new DocumentBuilderSetting(
      "ignoringComments",
      false,
      DocumentBuilderSettingStrategy.ignoringComments);
  protected DocumentBuilderSetting(
      String property,
      boolean value,
      DocumentBuilderSettingStrategy strategy) {
    if (property == null) {
      throw new NullPointerException("property");
    }
    this.property = property;
    this.value = value;
    this.strategy = strategy;
  }
  public final boolean hasConflict(DocumentBuilderSetting other) {
    if (other == null) {
      throw new NullPointerException("other");
    }
    if (other == this) {
      return true;
    }
    return strategy.hasConflict(other.strategy);
  }
  public final boolean hasSetting(DOMTestDocumentBuilderFactory factory) {
    return strategy.hasSetting(factory) == value;
  }
  public final void applySetting(DocumentBuilderFactory factory) throws
      DOMTestIncompatibleException {
    strategy.applySetting(factory, value);
  }
  public final String getProperty() {
    return property;
  }
  public final boolean getValue() {
    return value;
  }
  public final String toString() {
    StringBuffer builder = new StringBuffer(property);
    builder.append('=');
    builder.append(String.valueOf(value));
    return builder.toString();
  }
}
