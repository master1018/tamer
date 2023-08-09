public class TestXalan extends TestSuite {
  public static TestSuite suite() throws Exception
  {
    Class testClass = ClassLoader.getSystemClassLoader().loadClass("org.w3c.domts.level3.xpath.alltests");
    Constructor testConstructor = testClass.getConstructor(new Class[] { DOMTestDocumentBuilderFactory.class });
    DocumentBuilderFactory jaxpFactory = DocumentBuilderFactory.newInstance();
    DOMTestDocumentBuilderFactory factory =
        new XalanDOMTestDocumentBuilderFactory(jaxpFactory,
          XalanDOMTestDocumentBuilderFactory.getConfiguration1());
    Object test = testConstructor.newInstance(new Object[] { factory });
    return new JUnitTestSuiteAdapter((DOMTestSuite) test);
  }
}
