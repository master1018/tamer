public class TestDecorator extends Assert implements Test {
	protected Test fTest;
	public TestDecorator(Test test) {
		fTest= test;
	}
	public void basicRun(TestResult result) {
		fTest.run(result);
	}
	public int countTestCases() {
		return fTest.countTestCases();
	}
	public void run(TestResult result) {
		basicRun(result);
	}
	public String toString() {
		return fTest.toString();
	}
	public Test getTest() {
		return fTest;
	}
}