public class LocalServiceTest extends ServiceTestCase<LocalService> {
    public LocalServiceTest() {
      super(LocalService.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @SmallTest
    public void testPreconditions() {
    }
    @SmallTest
    public void testStartable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), LocalService.class);
        startService(startIntent); 
    }
    @MediumTest
    public void testBindable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), LocalService.class);
        IBinder service = bindService(startIntent); 
    }
}
