@TestTargetClass(Process.class) 
public class ProcessManagerTest extends TestCase {
    Thread thread = null;
    Process process = null;
    boolean isThrown = false;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getOutputStream",
        args = {}
    )
    public void testCat() throws IOException, InterruptedException {
        String[] commands = { "cat" };
        Process process = Runtime.getRuntime().exec(commands, null, null);
        OutputStream out = process.getOutputStream();
        String greeting = "Hello, World!";
        out.write(greeting.getBytes());
        out.write('\n');
        out.close();
        assertEquals(greeting, readLine(process));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "waitFor",
        args = {}
    )
    @BrokenTest("Sporadic failures in CTS, but not in CoreTestRunner")
    public void testSleep() throws IOException {
        String[] commands = { "sleep", "1" };
        process = Runtime.getRuntime().exec(commands, null, null);
        try { 
            assertEquals(0, process.waitFor());
        } catch(InterruptedException ie) {
            fail("InterruptedException was thrown.");
        }
        isThrown = false;
        thread = new Thread() {
            public void run() {
                String[] commands = { "sleep", "1000"};
                try {
                    process = Runtime.getRuntime().exec(commands, null, null);
                } catch (IOException e1) {
                    fail("IOException was thrown.");
                }
                try {
                    process.waitFor();
                    fail("InterruptedException was not thrown.");
                } catch(InterruptedException ie) {
                    isThrown = true;
                }
            }
        };
        Thread interruptThread = new Thread() { 
            public void run() {
                try {
                    sleep(10);
                } catch(InterruptedException ie) {
                    fail("InterruptedException was thrown in " +
                            "the interruptThread.");
                }
                thread.interrupt();
            }
        };
        thread.start();
        interruptThread.start();
        try {
            interruptThread.join();
        } catch (InterruptedException e) {
            fail("InterruptedException was thrown.");
        }
        try {
            Thread.sleep(100);
        } catch(InterruptedException ie) {
        }
        thread.interrupt();
        try {
            Thread.sleep(100);
        } catch(InterruptedException ie) {
        }
        assertTrue(isThrown);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInputStream",
        args = {}
    )
    public void testPwd() throws IOException, InterruptedException {
        String[] commands = { "sh", "-c", "pwd" };
        Process process = Runtime.getRuntime().exec(
                commands, null, new File("/"));
        logErrors(process);
        assertEquals("/", readLine(process));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "getInputStream",
        args = {}
    )
    public void testEnvironment() throws IOException, InterruptedException {
        String[] commands = { "sh", "-c", "echo $FOO" };
        String[] environment = { "FOO=foo", "PATH=" + System.getenv("PATH") };
        Process process = Runtime.getRuntime().exec(
                commands, environment, null);
        logErrors(process);
        assertEquals("foo", readLine(process));
    }
    String readLine(Process process) throws IOException {
        InputStream in = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        return reader.readLine();
    }
    void logErrors(final Process process) throws IOException {
        Thread thread = new Thread() {
            public void run() {
                InputStream in = process.getErrorStream();
                BufferedReader reader
                        = new BufferedReader(new InputStreamReader(in));
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        System.err.println(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Stress test.",
        method = "waitFor",
        args = {}
    )
    public void testHeavyLoad() {
        int i;
        for (i = 0; i < 100; i++)
            stuff();
    }
    private static void stuff() {
        Runtime rt = Runtime.getRuntime();
        try {
            Process proc = rt.exec("ls");
            proc.waitFor();
            proc = null;
        } catch (Exception ex) {
            System.err.println("Failure: " + ex);
            throw new RuntimeException(ex);
        }
        rt.gc();
        rt = null;
    }
    InputStream in;
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        notes = "Check non standard fd behavior",
        clazz = Runtime.class,
        method = "exec",
        args = {String[].class, String[].class, java.io.File.class}
    )
    public void testCloseNonStandardFds()
            throws IOException, InterruptedException {
        String[] commands = { "ls", "/proc/self/fd" };
        Process process = Runtime.getRuntime().exec(commands, null, null);
        int before = countLines(process);
        this.in = new FileInputStream("/proc/version");
        try {
            process = Runtime.getRuntime().exec(commands, null, null);
            int after = countLines(process);
            assertEquals(before, after);
        } finally {
            this.in = null;
        }
    }
    private int countLines(Process process) throws IOException {
        logErrors(process);
        InputStream in = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        int count = 0;
        while (reader.readLine() != null) {
            count++;
        }
        return count;
    }
    @TestTargetNew(
        level = TestLevel.ADDITIONAL,
        notes = "Check non standard fd behavior",
        clazz = Runtime.class,
        method = "exec",
        args = {String[].class, String[].class, java.io.File.class}
    )
    public void testInvalidCommand()
            throws IOException, InterruptedException {
        try {
            String[] commands = { "doesnotexist" };
            Runtime.getRuntime().exec(commands, null, null);
        } catch (IOException e) {  }
    }
}
