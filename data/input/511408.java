public class TestOracle extends TestSuite {
  public static TestSuite suite() throws Exception
  {
    Class testClass = ClassLoader.getSystemClassLoader().loadClass("org.w3c.domts.level1.core.alltests");
    Constructor testConstructor = testClass.getConstructor(new Class[] { DOMTestDocumentBuilderFactory.class });
    DocumentBuilderFactory oracleFactory = (DocumentBuilderFactory)
      ClassLoader.getSystemClassLoader().
        loadClass("oracle.xml.jaxp.JXDocumentBuilderFactory").newInstance();
    DOMTestDocumentBuilderFactory factory =
        new JAXPDOMTestDocumentBuilderFactory(oracleFactory,
          JAXPDOMTestDocumentBuilderFactory.getConfiguration1());
    Object test = testConstructor.newInstance(new Object[] { factory });
    return new JUnitTestSuiteAdapter((DOMTestSuite) test);
  }
}
