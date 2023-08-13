public class ParallelTransformerLoaderApp
{
        private static final int kNumIterations = 1000;
        public static void
        main(   String[] args)
                throws Exception
        {
                System.out.println();
                System.out.print("Starting test with " + kNumIterations + " iterations");
                for (int i = 0; i < kNumIterations; i++)
                {
                        Thread testThread = new TestThread(2);
                        testThread.start();
                        loadClasses(1);
                        testThread.join();
                        System.out.print(".");
                        ParallelTransformerLoaderAgent.generateNewClassLoader();
                }
                System.out.println();
                System.out.println("Test completed successfully");
        }
        private static class TestThread
                extends Thread
        {
                private final int fIndex;
                public
                TestThread(     int index)
                {
                        super("TestThread");
                        fIndex = index;
                }
                public void
                run()
                {
                        loadClasses(fIndex);
                }
        }
        public static void
        loadClasses( int index)
        {
                ClassLoader loader = ParallelTransformerLoaderAgent.getClassLoader();
                try
                {
                        Class.forName("TestClass" + index, true, loader);
                }
                catch (Exception e)
                {
                        e.printStackTrace();
                }
        }
}
