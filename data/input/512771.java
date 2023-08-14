public class JUnitTestSuiteAdapter extends TestSuite implements DOMTestSink  {
  private DOMTestSuite test;
  public JUnitTestSuiteAdapter(DOMTestSuite test) {
    super(test.getTargetURI());
    this.test = test;
    test.build(this);
  }
  public void addTest(Class testclass) {
    DOMTestDocumentBuilderFactory factory = test.getFactory();
    try {
      Constructor testConstructor = testclass.getConstructor(
          new Class[] { DOMTestDocumentBuilderFactory.class } );
      Object domtest;
      try {
        domtest = testConstructor.newInstance(new Object[] { factory });
      } catch(InvocationTargetException ex) {
        throw ex.getTargetException();
      }
      if(domtest instanceof DOMTestCase) {
        TestCase test = new JUnitTestCaseAdapter((DOMTestCase) domtest);
        addTest(test);
      }
      else {
        if(domtest instanceof DOMTestSuite) {
          TestSuite test = new JUnitTestSuiteAdapter((DOMTestSuite) domtest);
          addTest(test);
        }
      }
    }
    catch(Throwable ex) {
      if(!(ex instanceof DOMTestIncompatibleException)) {
        ex.printStackTrace();
      }
    }
  }
}
