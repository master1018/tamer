public class UninitializedDisplayModeChangeTest {
    public static volatile boolean failed = false;
    public static void main(String[] args) {
        Toolkit.getDefaultToolkit();
        try {
            EventQueue.invokeAndWait(new Runnable() {
                public void run() {
                    Thread.currentThread().setDefaultUncaughtExceptionHandler(
                        new Thread.UncaughtExceptionHandler() {
                            public void uncaughtException(Thread t,
                                                          Throwable e)
                            {
                                System.err.println("Exception Detected:");
                                e.printStackTrace();
                                failed = true;
                            }
                        }
                    );
                }
            });
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        } catch (InvocationTargetException ex) {
            ex.printStackTrace();
        }
        Process childProc;
        String classPath = System.getProperty("java.class.path" , ".");
        String cmd = new String(System.getProperty("java.home") +
                File.separator +
                "bin" +
                File.separator +
                "java -cp " + classPath +
                " DisplayModeChanger");
        System.out.println("Launching the display mode changer process");
        System.out.println("cmd="+cmd);
        try {
            childProc = Runtime.getRuntime().exec(cmd);
            StreamProcessor err =
                new StreamProcessor("stderr", childProc.getErrorStream());
            StreamProcessor out =
                new StreamProcessor("stdout", childProc.getInputStream());
            err.start();
            out.start();
            childProc.waitFor();
        } catch (Exception e) {
            failed = true;
            e.printStackTrace();
        }
        if (failed) {
            throw new RuntimeException("Test Failed: exception detected");
        }
        System.out.println("Test Passed.");
    }
    static class StreamProcessor extends Thread {
        InputStream is;
        String inputType;
        StreamProcessor(String inputType, InputStream is) {
            this.inputType = inputType;
            this.is = is;
        }
        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;
                while ( (line = br.readLine()) != null) {
                    System.out.println("Display Changer "+inputType+
                                       " output > " + line);
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
