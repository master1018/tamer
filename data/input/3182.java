public class TransformerManagementThreadAddTests extends ATestCaseScaffold
{
    public static void
    main (String[] args)
        throws Throwable {
        ATestCaseScaffold   test = new TransformerManagementThreadAddTests(args[0]);
        test.runTest();
    }
    protected void
    doRunTest()
        throws Throwable {
        testMultiThreadAdds();
    }
    protected static final int MIN_TRANS = 33;
    protected static final int MAX_TRANS = 45;
    protected static final int REMOVE_THREADS = 5;
    protected static final boolean LOG_TRANSFORMATIONS = false;
    protected static final int TOTAL_THREADS = MAX_TRANS - MIN_TRANS + 1;
    private byte[]          fDummyClassBytes;
    private Vector              fCheckedTransformers;
    private Instrumentation fInstrumentation;
    private int             fFinished;
    private ExecuteTransformersThread fExec;
    protected Vector            fAddedTransformers;
    private String          fDummyClassName;
    public TransformerManagementThreadAddTests(String name)
    {
        super(name);
        fCheckedTransformers = new Vector();
        fAddedTransformers = new Vector();
        fDummyClassName = "DummyClass";
        String resourceName = "DummyClass.class";
        File f = new File(System.getProperty("test.classes", "."), resourceName);
        System.out.println("Reading test class from " + f);
        try
        {
            InputStream redefineStream = new FileInputStream(f);
            fDummyClassBytes = NamedBuffer.loadBufferFromStream(redefineStream);
        }
        catch (IOException e)
        {
            fail("Could not load the class: "+resourceName);
        }
    }
    public void
    testMultiThreadAdds()
    {
        TransformerThread[] threads = new TransformerThread[TOTAL_THREADS];
        for (int i = MIN_TRANS; i <= MAX_TRANS; i++)
        {
            int index = i - MIN_TRANS;
            threads[index] = new TransformerThread("Trans"+prettyNum(index,2), i);
        }
        ExecuteTransformersThread exec = new ExecuteTransformersThread();
        exec.start();
        for (int i = threads.length - 1; i >= 0; i--)
        {
            threads[i].start();
        }
        while (!exec.isDone())
        {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
            }
        }
        assertTrue(finalCheck());
        if (LOG_TRANSFORMATIONS) {
            printTransformers();
        }
    }
    public Instrumentation getInstrumentation()
    {
        return fInstrumentation;
    }
    protected ExecuteTransformersThread getExecThread()
    {
        return fExec;
    }
    protected void setExecThread(ExecuteTransformersThread exec)
    {
        this.fExec = exec;
    }
    protected synchronized void
    threadFinished(Thread t)
    {
        fFinished++;
    }
    protected synchronized boolean
    threadsDone()
    {
        return fFinished == TOTAL_THREADS;
    }
    protected boolean
    testCompleted()
    {
        return getExecThread().isDone();
    }
    protected boolean
    finalCheck()
    {
        if (LOG_TRANSFORMATIONS) {
            for (int x = 0; x < fCheckedTransformers.size(); x++ ) {
                System.out.println(x + "\t\t" + fCheckedTransformers.get(x));
            }
            System.out.println();
            System.out.println();
            for (int x = 0; x < fCheckedTransformers.size(); x++ ) {
                Object current = fCheckedTransformers.get(x);
                for ( int y = x + 1; y < fCheckedTransformers.size(); y++) {
                    Object running = fCheckedTransformers.get(y);
                    if ( current.equals(running) ) {
                        System.out.println(x + "\t" + y + " \t" + "FOUND DUPLICATE: " + current);
                    }
                }
            }
        }
        for (int j = 1; j < fCheckedTransformers.size(); j++) {
            ThreadTransformer transformer = (ThreadTransformer)fCheckedTransformers.get(j);
            for (int i = 0; i < j; i++) {
                ThreadTransformer currTrans = (ThreadTransformer)fCheckedTransformers.get(i);
                assertTrue(currTrans + " incorrectly appeared before " +
                           transformer + " i=" + i + " j=" + j + " size=" +
                           fCheckedTransformers.size(),
                           !(
                             currTrans.getThread().equals(transformer.getThread()) &&
                             currTrans.getIndex() > transformer.getIndex()));
            }
        }
        return true;
    }
    protected void
    setUp()
        throws Exception
    {
        super.setUp();
        fFinished = 0;
        assertTrue(MIN_TRANS < MAX_TRANS);
        fInstrumentation = InstrumentationHandoff.getInstrumentationOrThrow();
    }
    protected void
    tearDown()
        throws Exception
    {
        super.tearDown();
    }
    private void
    executeTransform()
    {
        try
        {
            ClassDefinition cd = new ClassDefinition(DummyClass.class, fDummyClassBytes);
            fCheckedTransformers.clear();
            getInstrumentation().redefineClasses(new ClassDefinition[]{ cd });
        }
        catch (ClassNotFoundException e)
        {
            fail("Could not find the class: "+DummyClass.class.getName());
        }
        catch (UnmodifiableClassException e)
        {
            fail("Could not modify the class: "+DummyClass.class.getName());
        }
    }
    private void
    addTransformerToManager(ThreadTransformer threadTransformer)
    {
        getInstrumentation().addTransformer(threadTransformer);
        fAddedTransformers.add(threadTransformer);
    }
    private void
    checkInTransformer(ThreadTransformer transformer)
    {
        fCheckedTransformers.add(transformer);
    }
    private ThreadTransformer
    createTransformer(TransformerThread thread, int index)
    {
        ThreadTransformer tt = null;
        tt = new ThreadTransformer(thread, index);
        return tt;
    }
    protected class
    ExecuteTransformersThread
        extends Thread
    {
        private boolean fDone = false;
        private synchronized boolean isDone() {
            return fDone;
        }
        private synchronized void setIsDone() {
            fDone = true;
        }
        public void
        run()
        {
            while(!threadsDone())
            {
                executeTransform();
            }
            executeTransform();
            setIsDone();
        }
    }
    protected class
    TransformerThread
        extends Thread
    {
        private final ThreadTransformer[] fThreadTransformers;
        TransformerThread(String name, int numTransformers)
        {
            super(name);
            fThreadTransformers = makeTransformers(numTransformers);
        }
        private ThreadTransformer[]
        makeTransformers(int numTransformers)
        {
            ThreadTransformer[] trans = new ThreadTransformer[numTransformers];
            for (int i = 0; i < trans.length; i++)
            {
                trans[i] = createTransformer(TransformerThread.this, i);
            }
            return trans;
        }
        public void
        run()
        {
            for (int i = 0; i < fThreadTransformers.length; i++)
            {
                addTransformerToManager(fThreadTransformers[i]);
            }
            threadFinished(TransformerThread.this);
        }
    }
    protected class
    ThreadTransformer extends SimpleIdentityTransformer
    {
        private final String    fName;
        private final int       fIndex;
        private final Thread    fThread;
        public ThreadTransformer(Thread thread, int index) {
            super();
            fThread = thread;
            fIndex = index;
            fName = "TT["+fThread.getName()+"]["+prettyNum(fIndex,3)+"]";
        }
        public String toString()
        {
            return fName;
        }
        public byte[]
        transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain domain,
            byte[] classfileBuffer)
        {
            if ( className.equals(TransformerManagementThreadAddTests.this.fDummyClassName) ) {
                checkInTransformer(ThreadTransformer.this);
            }
            return super.transform(    loader,
                                        className,
                                        classBeingRedefined,
                                        domain,
                                        classfileBuffer);
        }
        public int getIndex()
        {
            return fIndex;
        }
        public Thread getThread()
        {
            return fThread;
        }
    }
    private int NUM_SWITCHES;
    protected void printTransformers()
    {
        NUM_SWITCHES = 0;
        Iterator trans = fCheckedTransformers.iterator();
        ThreadTransformer old = null;
        StringBuffer buf = new StringBuffer();
        for (int i = 1; trans.hasNext(); i++)
        {
            ThreadTransformer t = (ThreadTransformer)trans.next();
            buf.append(t.toString());
            if (old != null)
            {
                if (!old.getThread().equals(t.getThread()))
                {
                    NUM_SWITCHES++;
                    buf.append("*");
                }
                else
                { buf.append(" "); }
            }
            else
            { buf.append(" "); }
            if (i % 5 == 0)
            {
                buf.append("\n");
            }
            else
            { buf.append(" "); }
            old = t;
        }
        System.out.println(buf);
        System.out.println("\nNumber of transitions from one thread to another: "+NUM_SWITCHES);
    }
    protected String
    prettyNum(int n, int numSize)
    {
        StringBuffer num = new StringBuffer(Integer.toString(n));
        int size = num.length();
        for (int i = 0; i < numSize - size; i++)
        {
            num.insert(0, "0");
        }
        return num.toString();
    }
}
