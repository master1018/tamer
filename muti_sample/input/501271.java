public class AllTests_Level2 {
    public static final Test suite() {
        DOMTestSuite domSuite;
        try {
            DOMTestDocumentBuilderFactory factory1 =
                new JAXPDOMTestDocumentBuilderFactory(null,
                JAXPDOMTestDocumentBuilderFactory.getConfiguration1());
            domSuite = new org.w3c.domts.level2.core.alltests(factory1);
        } catch (Exception e) {
            throw new RuntimeException("problem creating dom test suite", e);
        }
        TestSuite suite = new JUnitTestSuiteAdapter(domSuite);
        return suite;
    }
}
