public class TestSetup extends TestDecorator {
	public TestSetup(Test test) {
		super(test);
	}
	public void run(final TestResult result) {
		Protectable p= new Protectable() {
			public void protect() throws Exception {
				setUp();
				basicRun(result);
				tearDown();
			}
		};
		result.runProtected(this, p);
	}
	protected void setUp() throws Exception {
	}
	protected void tearDown() throws Exception {
	}
}