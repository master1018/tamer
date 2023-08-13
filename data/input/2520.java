public class TestShutdown
{
    private static AppContext targetAppContext;
    private static JFrame f;
    private static JTextField tf;
    private static volatile boolean exceptionsOccurred = false;
    private static volatile boolean appcontextInitDone = false;
    private static int timerValue = 0;
    public static void main(String[] args)
        throws Exception
    {
        ThreadGroup tg = new TestThreadGroup("TTG");
        Thread t = new Thread(tg, new TestRunnable(), "InitThread");
        t.start();
        while (!appcontextInitDone)
        {
            Thread.sleep(1000);
        }
        targetAppContext.dispose();
        if (exceptionsOccurred)
        {
            throw new RuntimeException("Test FAILED: some exceptions occurred");
        }
    }
    static void initGUI()
    {
        f = new JFrame("F");
        f.setBounds(100, 100, 200, 100);
        tf = new JTextField("Test");
        f.add(tf);
        f.setVisible(true);
    }
    static void startGUI()
    {
        tf.requestFocusInWindow();
        ActionListener al = new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                System.out.println("Timer tick: " + timerValue++);
            }
        };
        new javax.swing.Timer(30, al).start();
    }
    static class TestThreadGroup extends ThreadGroup
    {
        public TestThreadGroup(String name)
        {
            super(name);
        }
        @Override
        public synchronized void uncaughtException(Thread thread, Throwable t)
        {
            if (t instanceof ThreadDeath)
            {
                throw (ThreadDeath)t;
            }
            System.err.println("Test FAILED: an exception is caught in the " +
                               "target thread group on thread " + thread.getName());
            t.printStackTrace(System.err);
            exceptionsOccurred = true;
        }
    }
    static class TestRunnable implements Runnable
    {
        @Override
        public void run()
        {
            SunToolkit stk = (SunToolkit)Toolkit.getDefaultToolkit();
            targetAppContext = stk.createNewAppContext();
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    initGUI();
                }
            });
            stk.realSync();
            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    startGUI();
                }
            });
            while (!Thread.interrupted())
            {
                try
                {
                    new TestSwingWorker().execute();
                    Thread.sleep(40);
                }
                catch (Exception e)
                {
                    break;
                }
            }
        }
    }
    static class TestSwingWorker extends SwingWorker<String, Integer>
    {
        @Override
        public String doInBackground()
        {
            Random r = new Random();
            for (int i = 0; i < 10; i++)
            {
                try
                {
                    int delay = r.nextInt() % 50;
                    Thread.sleep(delay);
                    publish(delay);
                }
                catch (Exception z)
                {
                    break;
                }
            }
            if (!appcontextInitDone)
            {
                appcontextInitDone = true;
            }
            return "Done";
        }
        @Override
        public void process(java.util.List<Integer> chunks)
        {
            for (Integer i : chunks)
            {
                System.err.println("Processed: " + i);
            }
        }
    }
}
