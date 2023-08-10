public class TestNameCollector extends BaseTestListener {
    private final Set<String> testNames = new HashSet<String>();
    @Override
    public void startTest(Test test) {
        super.startTest(test);
        testNames.add(JUnit3TestHarness.getShortName(test));
    }
    public Set<String> getTestNames() {
        return testNames;
    }
}
