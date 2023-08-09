public class DOMErrorImpl
    implements DOMError {
  private final short severity;
  private final String message;
  private final String type;
  private final Object relatedException;
  private final Object relatedData;
  private final DOMLocator location;
  public DOMErrorImpl(DOMError src) {
    this.severity = src.getSeverity();
    this.message = src.getMessage();
    this.type = src.getType();
    this.relatedException = src.getRelatedException();
    this.relatedData = src.getRelatedData();
    this.location = new DOMLocatorImpl(src.getLocation());
  }
  public final short getSeverity() {
    return severity;
  }
  public final String getMessage() {
    return message;
  }
  public final String getType() {
    return type;
  }
  public final Object getRelatedException() {
    return relatedException;
  }
  public final Object getRelatedData() {
    return relatedData;
  }
  public final DOMLocator getLocation() {
    return location;
  }
}
