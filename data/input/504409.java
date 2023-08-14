public class DatabaseLockTest extends AndroidTestCase {
    private static final int NUM_ITERATIONS = 100;
    private static final int SLEEP_TIME = 30;
    private static final int MAX_ALLOWED_LATENCY_TIME = 30;
    private SQLiteDatabase mDatabase;
    private File mDatabaseFile;
    private AtomicInteger mCounter = new AtomicInteger();
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        File parentDir = getContext().getFilesDir();
        mDatabaseFile = new File(parentDir, "database_test.db");
        if (mDatabaseFile.exists()) {
            mDatabaseFile.delete();
        }
        mDatabase = SQLiteDatabase.openOrCreateDatabase(mDatabaseFile.getPath(), null);
        assertNotNull(mDatabase);
    }
    @Override
    protected void tearDown() throws Exception {
        mDatabase.close();
        mDatabaseFile.delete();
        super.tearDown();
    }
    @Suppress
    public void testLockFairness() {
        startDatabaseFairnessThread();
        int previous = 0;
        for (int i = 0; i < NUM_ITERATIONS; i++) { 
            mDatabase.beginTransaction();
            int val = mCounter.get();
            if (i == 0) {
                previous = val - i;
            }
            assertTrue(previous == (val - i));
            try {
                Thread.currentThread().sleep(SLEEP_TIME); 
            } catch (InterruptedException e) {
            }
            mDatabase.endTransaction();
        }
    }
    private void startDatabaseFairnessThread() {
        Thread thread = new DatabaseFairnessThread();
        thread.start();
    }
    private class DatabaseFairnessThread extends Thread {
        @Override
        public void run() {
            for (int i = 0; i < NUM_ITERATIONS; i++) {
                mDatabase.beginTransaction();
                int val = mCounter.incrementAndGet();
                try {
                    Thread.currentThread().sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                }
                mDatabase.endTransaction();
            }
        }
    }    
    @Suppress
    public void testLockLatency() {
        startDatabaseLatencyThread();
        int previous = 0;
        long sumTime = 0;
        long maxTime = 0;
        for (int i = 0; i < NUM_ITERATIONS; i++) { 
            long startTime = System.currentTimeMillis();
            mDatabase.beginTransaction();
            long endTime = System.currentTimeMillis();
            long elapsedTime = endTime - startTime;
            if (maxTime < elapsedTime) {
                maxTime = elapsedTime;
            }
            sumTime += elapsedTime;
            try {
                Thread.currentThread().sleep(SLEEP_TIME); 
            } catch (InterruptedException e) {
            }   
            mDatabase.endTransaction();
        }
        long averageTime = sumTime/NUM_ITERATIONS;
        Log.i("DatabaseLockLatency", "AverageTime: " + averageTime);
        Log.i("DatabaseLockLatency", "MaxTime: " + maxTime);
        assertTrue( (averageTime - SLEEP_TIME) <= MAX_ALLOWED_LATENCY_TIME);
    }
    private void startDatabaseLatencyThread() {
        Thread thread = new DatabaseLatencyThread();
        thread.start();
    }
    private class DatabaseLatencyThread extends Thread {
        @Override
        public void run() {
            for (int i = 0; i < NUM_ITERATIONS; i++) 
            {
                mDatabase.beginTransaction();
                try {
                    Thread.currentThread().sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                } 
                mDatabase.endTransaction();
            }
        }
    }        
}
