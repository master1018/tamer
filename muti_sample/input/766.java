public class DeleteOnExitNPE implements Runnable
{
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            runTest();
        } else {
            doTest();
        }
    }
    public static void runTest() throws Exception {
        String cmd = System.getProperty("java.home") + File.separator +
                     "bin" + File.separator + "java" +
                     " -classpath " + System.getProperty("test.classes");
        Process process = Runtime.getRuntime().exec(cmd +  " DeleteOnExitNPE -test");
        BufferedReader isReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader esReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        process.waitFor();
        boolean failed = false;
        String str;
        while ((str = isReader.readLine()) != null) {
            failed = true;
            System.out.println(str);
        }
        while ((str = esReader.readLine()) != null) {
            failed = true;
            System.err.println(str);
        }
        if (failed)
            throw new RuntimeException("Failed: No output should have been received from the process");
    }
    public static void doTest() {
        try {
            File file = File.createTempFile("DeleteOnExitNPE", null);
            file.deleteOnExit();
            Thread thread = new Thread(new DeleteOnExitNPE());
            thread.start();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            System.exit(0);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
    public void run() {
        File file = new File("xxyyzz");
        try {
            for (;;) {
                file.deleteOnExit();
            }
        } catch (IllegalStateException ise) {
        }
    }
}
