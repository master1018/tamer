public class SingleThreadNamedTaskExecutorTest extends TestCase {
    private SingleThreadNamedTaskExecutor mExecutor;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mExecutor = new SingleThreadNamedTaskExecutor(Executors.defaultThreadFactory());
    }
    public void testExecute() throws Exception {
        MockTask a1 = addTask("a", 1);
        MockTask a2 = addTask("a", 2);
        a1.waitForCompletion();
        a2.waitForCompletion();
    }
    private MockTask addTask(String name, int id) {
        MockTask task = new MockTask(name, id);
        mExecutor.execute(task);
        return task;
    }
}
