class NoExecTestResult extends TestResult {
    @Override
    protected void run(final TestCase test) {
        startTest(test);
        endTest(test);
    }
}
