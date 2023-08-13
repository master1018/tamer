public class TransformerManagementThreadRemoveTests
    extends TransformerManagementThreadAddTests
{
    public TransformerManagementThreadRemoveTests(String name)
    {
        super(name);
    }
    public static void
    main (String[] args)
        throws Throwable {
        ATestCaseScaffold   test = new TransformerManagementThreadRemoveTests(args[0]);
        test.runTest();
    }
    protected final void
    doRunTest()
        throws Throwable {
        testMultiThreadAddsAndRemoves();
    }
    public void
    testMultiThreadAddsAndRemoves()
    {
        int size = TOTAL_THREADS + REMOVE_THREADS;
        ArrayList threadList = new ArrayList(size);
        for (int i = MIN_TRANS; i <= MAX_TRANS; i++)
        {
            int index = i - MIN_TRANS;
            threadList.add(new TransformerThread("Trans"+prettyNum(index,2), i));
        }
        int factor = (int)Math.floor(TOTAL_THREADS / REMOVE_THREADS);
        for (int i = 0; i < REMOVE_THREADS; i++)
        {
            threadList.add(factor * i, new RemoveThread("Remove"+i));
        }
        Thread[] threads = (Thread[])threadList.toArray(new Thread[size]);
        setExecThread(new ExecuteTransformersThread());
        getExecThread().start();
        for (int i = threads.length - 1; i >= 0; i--)
        {
            threads[i].start();
        }
        while (!testCompleted())
        {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
            }
        }
        assertTrue(finalCheck());
    }
    private void
    removeTransformer(Thread t)
    {
        ThreadTransformer tt = null;
        synchronized (fAddedTransformers)
        {
            int size = fAddedTransformers.size();
            if (size > 0)
            {
                int choose = (int)Math.floor(Math.random() * size);
                tt = (ThreadTransformer)fAddedTransformers.remove(choose);
            }
        }
        if (tt != null)
        {
            getInstrumentation().removeTransformer(tt);
        }
    }
    private class
    RemoveThread
        extends Thread
    {
        RemoveThread(String name)
        {
            super(name);
        }
        public void
        run()
        {
            while (!threadsDone())
            {
                removeTransformer(RemoveThread.this);
            }
        }
    }
}
