public class TestFailure extends Object {
	protected Test fFailedTest;
	protected Throwable fThrownException;
	public TestFailure(Test failedTest, Throwable thrownException) {
		fFailedTest= failedTest;
		fThrownException= thrownException;
	}
	public Test failedTest() {
	    return fFailedTest;
	}
	public Throwable thrownException() {
	    return fThrownException;
	}
	public String toString() {
	    StringBuffer buffer= new StringBuffer();
	    buffer.append(fFailedTest+": "+fThrownException.getMessage());
	    return buffer.toString();
	}
	public String trace() {
		StringWriter stringWriter= new StringWriter();
		PrintWriter writer= new PrintWriter(stringWriter);
		thrownException().printStackTrace(writer);
		StringBuffer buffer= stringWriter.getBuffer();
		return buffer.toString();
	}
	public String exceptionMessage() {
		return thrownException().getMessage();
	}
	public boolean isFailure() {
		return thrownException() instanceof AssertionFailedError;
	}
}