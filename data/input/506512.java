public class DOMLocatorImpl
    implements DOMLocator {
  private final int lineNumber;
  private final int columnNumber;
  private final int byteOffset;
  private final int utf16Offset;
  private final Node relatedNode;
  private final String uri;
  public DOMLocatorImpl(DOMLocator src) {
    this.lineNumber = src.getLineNumber();
    this.columnNumber = src.getColumnNumber();
    this.byteOffset = src.getByteOffset();
    this.utf16Offset = src.getUtf16Offset();
    this.relatedNode = src.getRelatedNode();
    this.uri = src.getUri();
  }
  public int getLineNumber() {
    return lineNumber;
  }
  public int getColumnNumber() {
    return columnNumber;
  }
  public int getByteOffset() {
    return byteOffset;
  }
  public int getUtf16Offset() {
    return utf16Offset;
  }
  public Node getRelatedNode() {
    return relatedNode;
  }
  public String getUri() {
    return uri;
  }
}
