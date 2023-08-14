class TestSuiteReference extends AndroidTestReference {
    private final String mClassName;
    private List<TestCaseReference> mTests;
    TestSuiteReference(String className) {
         mClassName = className; 
         mTests = new ArrayList<TestCaseReference>();
    }
    public int countTestCases() {
        return mTests.size();
    }
    public void sendTree(IVisitsTestTrees notified) {
        notified.visitTreeEntry(getIdentifier(), true, countTestCases());
        for (TestCaseReference ref : mTests) {
            ref.sendTree(notified);
        }
    }
    public String getName() {
        return mClassName;
    }
    void addTest(TestCaseReference testRef) {
        mTests.add(testRef);
    }
}
