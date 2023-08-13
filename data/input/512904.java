public class OperationSchedulerTest extends AndroidTestCase {
    private class TimeTravelScheduler extends OperationScheduler {
        static final long DEFAULT_TIME = 1250146800000L;  
        public long timeMillis = DEFAULT_TIME;
        @Override
        protected long currentTimeMillis() { return timeMillis; }
        public TimeTravelScheduler() { super(getFreshStorage()); }
    }
    private SharedPreferences getFreshStorage() {
        SharedPreferences sp = getContext().getSharedPreferences("OperationSchedulerTest", 0);
        sp.edit().clear().commit();
        return sp;
    }
    @MediumTest
    public void testScheduler() throws Exception {
        TimeTravelScheduler scheduler = new TimeTravelScheduler();
        OperationScheduler.Options options = new OperationScheduler.Options();
        assertEquals(Long.MAX_VALUE, scheduler.getNextTimeMillis(options));
        assertEquals(0, scheduler.getLastSuccessTimeMillis());
        assertEquals(0, scheduler.getLastAttemptTimeMillis());
        long beforeTrigger = scheduler.timeMillis;
        scheduler.setTriggerTimeMillis(beforeTrigger + 1000000);
        assertEquals(beforeTrigger + 1000000, scheduler.getNextTimeMillis(options));
        scheduler.setMoratoriumTimeMillis(beforeTrigger + 500000);
        assertEquals(beforeTrigger + 1000000, scheduler.getNextTimeMillis(options));
        scheduler.setMoratoriumTimeMillis(beforeTrigger + 1500000);
        assertEquals(beforeTrigger + 1500000, scheduler.getNextTimeMillis(options));
        scheduler.setEnabledState(false);
        assertEquals(Long.MAX_VALUE, scheduler.getNextTimeMillis(options));
        scheduler.setEnabledState(true);
        assertEquals(beforeTrigger + 1500000, scheduler.getNextTimeMillis(options));
        long beforeError = (scheduler.timeMillis += 100);
        scheduler.onTransientError();
        assertEquals(0, scheduler.getLastSuccessTimeMillis());
        assertEquals(beforeError, scheduler.getLastAttemptTimeMillis());
        assertEquals(beforeTrigger + 1500000, scheduler.getNextTimeMillis(options));
        options.backoffFixedMillis = 1000000;
        options.backoffIncrementalMillis = 500000;
        assertEquals(beforeError + 1500000, scheduler.getNextTimeMillis(options));
        beforeError = (scheduler.timeMillis += 100);
        scheduler.onTransientError();
        assertEquals(beforeError, scheduler.getLastAttemptTimeMillis());
        assertEquals(beforeError + 2000000, scheduler.getNextTimeMillis(options));
        scheduler.resetTransientError();
        assertEquals(0, scheduler.getLastSuccessTimeMillis());
        assertEquals(beforeTrigger + 1500000, scheduler.getNextTimeMillis(options));
        assertEquals(beforeError, scheduler.getLastAttemptTimeMillis());
        scheduler.onPermanentError();
        assertEquals(Long.MAX_VALUE, scheduler.getNextTimeMillis(options));
        scheduler.resetTransientError();
        assertEquals(Long.MAX_VALUE, scheduler.getNextTimeMillis(options));
        scheduler.resetPermanentError();
        assertEquals(beforeTrigger + 1500000, scheduler.getNextTimeMillis(options));
        long beforeSuccess = (scheduler.timeMillis += 100);
        scheduler.onSuccess();
        assertEquals(beforeSuccess, scheduler.getLastAttemptTimeMillis());
        assertEquals(beforeSuccess, scheduler.getLastSuccessTimeMillis());
        assertEquals(Long.MAX_VALUE, scheduler.getNextTimeMillis(options));
        scheduler.setTriggerTimeMillis(0);
        assertEquals(beforeTrigger + 1500000, scheduler.getNextTimeMillis(options));
        scheduler.setMoratoriumTimeMillis(0);
        assertEquals(beforeSuccess, scheduler.getNextTimeMillis(options));
        options.periodicIntervalMillis = 250000;
        scheduler.setTriggerTimeMillis(Long.MAX_VALUE);
        assertEquals(beforeSuccess + 250000, scheduler.getNextTimeMillis(options));
        options.minTriggerMillis = 1000000;
        assertEquals(beforeSuccess + 1000000, scheduler.getNextTimeMillis(options));
    }
    @SmallTest
    public void testParseOptions() throws Exception {
         OperationScheduler.Options options = new OperationScheduler.Options();
         assertEquals(
                 "OperationScheduler.Options[backoff=0.0+5.0 max=86400.0 min=0.0 period=3600.0]",
                 OperationScheduler.parseOptions("3600", options).toString());
         assertEquals(
                 "OperationScheduler.Options[backoff=0.0+2.5 max=86400.0 min=0.0 period=3700.0]",
                 OperationScheduler.parseOptions("backoff=+2.5 3700", options).toString());
         assertEquals(
                 "OperationScheduler.Options[backoff=10.0+2.5 max=12345.6 min=7.0 period=3800.0]",
                 OperationScheduler.parseOptions("max=12345.6 min=7 backoff=10 period=3800",
                         options).toString());
         assertEquals(
                "OperationScheduler.Options[backoff=10.0+2.5 max=12345.6 min=7.0 period=3800.0]",
                 OperationScheduler.parseOptions("", options).toString());
    }
    @SmallTest
    public void testMoratoriumWithHttpDate() throws Exception {
        TimeTravelScheduler scheduler = new TimeTravelScheduler();
        OperationScheduler.Options options = new OperationScheduler.Options();
        long beforeTrigger = scheduler.timeMillis;
        scheduler.setTriggerTimeMillis(beforeTrigger + 1000000);
        assertEquals(beforeTrigger + 1000000, scheduler.getNextTimeMillis(options));
        scheduler.setMoratoriumTimeMillis(beforeTrigger + 2000000);
        assertEquals(beforeTrigger + 2000000, scheduler.getNextTimeMillis(options));
        long beforeMoratorium = scheduler.timeMillis;
        assertTrue(scheduler.setMoratoriumTimeHttp("3000"));
        long afterMoratorium = scheduler.timeMillis;
        assertTrue(beforeMoratorium + 3000000 <= scheduler.getNextTimeMillis(options));
        assertTrue(afterMoratorium + 3000000 >= scheduler.getNextTimeMillis(options));
        options.maxMoratoriumMillis = Long.MAX_VALUE / 2;
        assertTrue(scheduler.setMoratoriumTimeHttp("Fri, 31 Dec 2030 23:59:59 GMT"));
        assertEquals(1924991999000L, scheduler.getNextTimeMillis(options));
        assertFalse(scheduler.setMoratoriumTimeHttp("not actually a date"));
    }
    @SmallTest
    public void testClockRollbackScenario() throws Exception {
        TimeTravelScheduler scheduler = new TimeTravelScheduler();
        OperationScheduler.Options options = new OperationScheduler.Options();
        options.minTriggerMillis = 2000;
        long beforeTrigger = scheduler.timeMillis;
        long triggerTime = beforeTrigger - 10000000;
        scheduler.setTriggerTimeMillis(triggerTime);
        assertEquals(triggerTime, scheduler.getNextTimeMillis(options));
        assertEquals(0, scheduler.getLastAttemptTimeMillis());
        long beforeSuccess = (scheduler.timeMillis += 100);
        scheduler.onSuccess();
        scheduler.setTriggerTimeMillis(triggerTime);
        assertEquals(beforeSuccess, scheduler.getLastAttemptTimeMillis());
        assertEquals(beforeSuccess + 2000, scheduler.getNextTimeMillis(options));
        long beforeError = (scheduler.timeMillis += 100);
        scheduler.onTransientError();
        assertEquals(beforeError, scheduler.getLastAttemptTimeMillis());
        assertEquals(beforeError + 5000, scheduler.getNextTimeMillis(options));
        long beforeMoratorium = (scheduler.timeMillis += 100);
        scheduler.setMoratoriumTimeMillis(beforeTrigger + 1000000);
        assertEquals(beforeTrigger + 1000000, scheduler.getNextTimeMillis(options));
        long beforeRollback = (scheduler.timeMillis = beforeTrigger - 10000);
        assertEquals(beforeTrigger + 1000000, scheduler.getNextTimeMillis(options));
        assertEquals(scheduler.timeMillis, scheduler.getLastAttemptTimeMillis());
        beforeRollback = (scheduler.timeMillis = beforeTrigger - 100000000);
        assertEquals(triggerTime, scheduler.getNextTimeMillis(options));
        assertEquals(beforeRollback, scheduler.getLastAttemptTimeMillis());
        scheduler.timeMillis = triggerTime + 5000000;
        assertEquals(triggerTime, scheduler.getNextTimeMillis(options));
        assertEquals(beforeRollback, scheduler.getLastAttemptTimeMillis());
        assertEquals(beforeRollback, scheduler.getLastSuccessTimeMillis());
    }
}
