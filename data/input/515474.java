public class ActiveTestSuite extends TestSuite {
	private volatile int fActiveTestDeathCount;
	public ActiveTestSuite() {
	}
	public ActiveTestSuite(Class theClass) {
		super(theClass);
	}
	public ActiveTestSuite(String name) {
		super (name);
	}
	public ActiveTestSuite(Class theClass, String name) {
		super(theClass, name);
	}
	public void run(TestResult result) {
		fActiveTestDeathCount= 0;
		super.run(result);
		waitUntilFinished();
	}
	public void runTest(final Test test, final TestResult result) {
		Thread t= new Thread() {
			public void run() {
				try {
					test.run(result);
				} finally {
					ActiveTestSuite.this.runFinished();
				}
			}
		};
		t.start();
	}
	synchronized void waitUntilFinished() {
		while (fActiveTestDeathCount < testCount()) {
			try {
				wait();
			} catch (InterruptedException e) {
				return; 
			}
		}
	}
	synchronized public void runFinished() {
		fActiveTestDeathCount++;
		notifyAll();
	}
}