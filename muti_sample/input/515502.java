public class JTidyDOMImplementation
    implements DOMImplementation {
  public boolean hasFeature(String feature,
                            String version) {
    if (feature.equals("Core")) {
      return version == null || version.equals("1.0") || version.equals("2.0");
    }
    return false;
  }
  public DocumentType createDocumentType(String qualifiedName,
                                         String publicId,
                                         String systemId) throws DOMException {
    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not Implemented");
  }
  public Document createDocument(String namespaceURI,
                                 String qualifiedName,
                                 DocumentType doctype) throws DOMException {
    throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "Not Implemented");
  }
  public DOMImplementation getInterface(String feature) {
    return this;
  }
  public Object getFeature(String feature, String version) {
    return null;
  }
}
