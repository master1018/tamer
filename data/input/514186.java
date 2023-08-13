public class AllTests_Level1 {
    public static final Test suite() {
        DOMTestSuite domSuite;
        try {
            DOMTestDocumentBuilderFactory factory1 =
                new JAXPDOMTestDocumentBuilderFactory(null,
                JAXPDOMTestDocumentBuilderFactory.getConfiguration1());
            domSuite = new org.w3c.domts.level1.core.alltests(factory1);
        } catch (Exception e) {
            throw new RuntimeException("problem creating dom test suite, "+e.getClass().getName()+", "+e.getMessage(), e);
        }
        TestSuite suite = new JUnitTestSuiteAdapter(domSuite);
        return suite;
    }
}
