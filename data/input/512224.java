public class DOMErrorMonitor
    implements DOMErrorHandler {
  private final List errors = new ArrayList();
  public DOMErrorMonitor() {
  }
  public boolean handleError(DOMError error) {
    errors.add(new DOMErrorImpl(error));
    return true;
  }
  public List getAllErrors() {
    return new ArrayList(errors);
  }
  public void assertLowerSeverity(DOMTestCase testCase, String id, int severity) {
    Iterator iter = errors.iterator();
    while (iter.hasNext()) {
      DOMError error = (DOMError) iter.next();
      if (error.getSeverity() >= severity) {
        testCase.fail(id + error.getMessage());
      }
    }
  }
}
