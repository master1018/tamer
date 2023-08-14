class TestCaseReference extends AndroidTestReference {
    private final String mClassName;
    private final String mTestName;
    TestCaseReference(String className, String testName) {
        mClassName = className;
        mTestName = testName;
    }
    TestCaseReference(TestIdentifier test) {
        mClassName = test.getClassName();
        mTestName = test.getTestName();
    }
    public int countTestCases() {
        return 1;
    }
    public void sendTree(IVisitsTestTrees notified) {
        notified.visitTreeEntry(getIdentifier(), false, countTestCases());
    }
    public String getName() {
        return MessageFormat.format(MessageIds.TEST_IDENTIFIER_MESSAGE_FORMAT, 
                new Object[] { mTestName, mClassName});
    }
}
