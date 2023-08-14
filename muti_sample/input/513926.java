public class ExceptionTestCase extends TestCase {
	Class fExpected;
	public ExceptionTestCase(String name, Class exception) {
		super(name);
		fExpected= exception;
	}
	protected void runTest() throws Throwable {
		try {
			super.runTest();
		}
		catch (Exception e) {
			if (fExpected.isAssignableFrom(e.getClass()))
				return;
			else
				throw e;
		}
		fail("Expected exception " + fExpected);
	}
}