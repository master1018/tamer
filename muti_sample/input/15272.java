public class ParallelTransformerLoaderAgent
{
        private static URL sURL;
        private static ClassLoader sClassLoader;
        public static synchronized ClassLoader
        getClassLoader()
        {
                return sClassLoader;
        }
        public static synchronized void
        generateNewClassLoader()
        {
                sClassLoader = new URLClassLoader(new URL[] {sURL});
        }
        public static void
        premain(        String agentArgs,
                        Instrumentation instrumentation)
                throws Exception
        {
                if (agentArgs == null || agentArgs == "")
                {
                        System.err.println("Error: No jar file name provided, test will not run.");
                        return;
                }
                sURL = (new File(agentArgs)).toURL();
                System.out.println("Using jar file: " + sURL);
                generateNewClassLoader();
                instrumentation.addTransformer(new TestTransformer());
        }
        private static class TestTransformer
                implements ClassFileTransformer
        {
                public byte[]
                transform(      ClassLoader loader,
                                String className,
                                Class classBeingRedefined,
                                ProtectionDomain protectionDomain,
                                byte[] classfileBuffer)
                        throws IllegalClassFormatException
                {
                        String tName = Thread.currentThread().getName();
                        if (!tName.equals("main") && !tName.equals("TestThread")) {
                            System.out.println("Thread '" + tName +
                                "' has called transform()");
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException ie) {
                            }
                        }
                        if (!tName.equals("main"))
                        {
                                loadClasses(3);
                        }
                        return null;
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
}
