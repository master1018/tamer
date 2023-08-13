public class TestResult extends Object {
	protected Vector fFailures;
	protected Vector fErrors;
	protected Vector fListeners;
	protected int fRunTests;
	private boolean fStop;
	public TestResult() {
		fFailures= new Vector();
		fErrors= new Vector();
		fListeners= new Vector();
		fRunTests= 0;
		fStop= false;
	}
	public synchronized void addError(Test test, Throwable t) {
		fErrors.addElement(new TestFailure(test, t));
		for (Enumeration e= cloneListeners().elements(); e.hasMoreElements(); ) {
			((TestListener)e.nextElement()).addError(test, t);
		}
	}
	public synchronized void addFailure(Test test, AssertionFailedError t) {
		fFailures.addElement(new TestFailure(test, t));
		for (Enumeration e= cloneListeners().elements(); e.hasMoreElements(); ) {
			((TestListener)e.nextElement()).addFailure(test, t);
		}
	}
	public synchronized void addListener(TestListener listener) {
		fListeners.addElement(listener);
	}
	public synchronized void removeListener(TestListener listener) {
		fListeners.removeElement(listener);
	}
	private synchronized Vector cloneListeners() {
		return (Vector)fListeners.clone();
	}
	public void endTest(Test test) {
		for (Enumeration e= cloneListeners().elements(); e.hasMoreElements(); ) {
			((TestListener)e.nextElement()).endTest(test);
		}
	}
	public synchronized int errorCount() {
		return fErrors.size();
	}
	public synchronized Enumeration errors() {
		return fErrors.elements();
	}
	public synchronized int failureCount() {
		return fFailures.size();
	}
	public synchronized Enumeration failures() {
		return fFailures.elements();
	}
	protected void run(final TestCase test) {
		startTest(test);
		Protectable p= new Protectable() {
			public void protect() throws Throwable {
				test.runBare();
			}
		};
		runProtected(test, p);
		endTest(test);
	}
	public synchronized int runCount() {
		return fRunTests;
	}
	public void runProtected(final Test test, Protectable p) {
		try {
			p.protect();
		} 
		catch (AssertionFailedError e) {
			addFailure(test, e);
		}
		catch (ThreadDeath e) { 
			throw e;
		}
		catch (Throwable e) {
			addError(test, e);
		}
	}
	public synchronized boolean shouldStop() {
		return fStop;
	}
	public void startTest(Test test) {
		final int count= test.countTestCases();
		synchronized(this) {
			fRunTests+= count;
		}
		for (Enumeration e= cloneListeners().elements(); e.hasMoreElements(); ) {
			((TestListener)e.nextElement()).startTest(test);
		}
	}
	public synchronized void stop() {
		fStop= true;
	}
	public synchronized boolean wasSuccessful() {
		return failureCount() == 0 && errorCount() == 0;
	}
}