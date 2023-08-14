public class PerNameExecutorTest extends TestCase {
    private MockNamedTaskExecutorFactory mExecutorFactory;
    private NamedTaskExecutor mExecutor;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mExecutorFactory = new MockNamedTaskExecutorFactory();
        mExecutor = new PerNameExecutor(mExecutorFactory);
    }
    public void testExecute() {
        MockTask a1 = addTask("a", 1);
        MockTask a2 = addTask("a", 2);
        MockTask a3 = addTask("a", 3);
        MockTask b1 = addTask("b", 1);
        assertRanNever("step 0", a1, a2, a3, b1);
        step(); 
        assertRanOnce("step 1", a1, b1);
        assertRanNever("step 1", a2, a3);
        step(); 
        assertRanOnce("step 2", a1, b1, a2);
        assertRanNever("step 2", a3);
        step(); 
        assertRanOnce("step 3", a1, b1, a2, a3);
        step(); 
        assertRanOnce("step 4", a1, b1, a2, a3);
    }
    public void testCancelPendingTasks() {
        MockTask a1 = addTask("a", 1);
        MockTask a2 = addTask("a", 2);
        MockTask b1 = addTask("b", 1);
        step(); 
        assertRanOnce("step 1", a1, b1);
        assertRanNever("step 1", a2);
        mExecutor.cancelPendingTasks(); 
        assertRanOnce("step 1, after cancel", a1, b1);
        assertRanNever("step 1, after cancel", a2);
        step(); 
        assertRanOnce("step 2", a1, b1);
        assertRanNever("step 2", a2);
        MockTask a3 = addTask("a" , 3);
        MockTask c1 = addTask("c" , 1);
        assertRanNever("step 2, new tasks", a3, c1, a2);
        step(); 
        assertRanOnce("step 3", a1, b1, a3, c1);
        assertRanNever("step 3", a2);
        step(); 
        assertRanOnce("step 4", a1, b1, a3, c1);
        assertRanNever("step 4", a2);
    }
    public void testClose() {
        MockTask a1 = new MockTask("a", 1);
        mExecutor.execute(a1);
        assertRanNever("before close()", a1);
        mExecutor.close();
        assertRanNever("after close() 1", a1);
        mExecutor.close();
        assertRanNever("after close() 2", a1);
    }
    private MockTask addTask(String name, int id) {
        MockTask task = new MockTask(name, id);
        mExecutor.execute(task);
        return task;
    }
    private void step() {
        mExecutorFactory.runNext();
    }
}
