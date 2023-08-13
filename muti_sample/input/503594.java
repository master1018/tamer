@TestTargetClass(Process.class) 
public class Process2Test extends junit.framework.TestCase {
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getErrorStream",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getInputStream",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.PARTIAL_COMPLETE,
            notes = "",
            method = "getOutputStream",
            args = {}
        )
    })
    @AndroidOnly("dalvikvm specific")
    public void test_streams()
            throws IOException, InterruptedException {
        Process p = javaProcessBuilder().start();
        assertNotNull(p.getInputStream());
        assertNotNull(p.getErrorStream());
        assertNotNull(p.getOutputStream());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "getErrorStream",
        args = {}
    )
    public void test_getErrorStream() {
        String[] commands = {"ls"};      
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(commands, null, null);
            InputStream is = process.getErrorStream();
            StringBuffer msg = new StringBuffer("");
            while (true) {
                int c = is.read();
                if (c == -1)
                    break;
                msg.append((char) c);
            }
            assertEquals("", msg.toString());
        } catch (IOException e) {
            fail("IOException was thrown.");
        } finally {
            process.destroy();
        }
        String[] unknownCommands = {"mkdir", "-u", "test"};      
        Process erProcess = null;
        try {
            erProcess = Runtime.getRuntime().exec(unknownCommands, null, null);
            InputStream is = erProcess.getErrorStream();
            StringBuffer msg = new StringBuffer("");
            while (true) {
                int c = is.read();
                if (c == -1)
                    break;
                msg.append((char) c);
            }
            assertTrue("Error stream should not be empty", 
                                                !"".equals(msg.toString()));
        } catch (IOException e) {
            fail("IOException was thrown.");
        } finally {
            erProcess.destroy();
        }
    }
}
