public abstract class DOMTestSuite
    extends DOMTest {
  protected DOMTestSuite() {
  }
  protected DOMTestSuite(DOMTestDocumentBuilderFactory factory) {
    super(factory);
  }
  abstract public void build(DOMTestSink sink);
}
