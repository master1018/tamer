public class NoReadLogsPermissionTest extends AndroidTestCase {
    private static final String LOGTAG = "CTS";
    @MediumTest
    public void testSetMicrophoneMute() throws IOException {
        Process logcatProc = null;
        BufferedReader reader = null;
        try {
            logcatProc = Runtime.getRuntime().exec(new String[]
                    {"logcat", "-d", "AndroidRuntime:E :" + LOGTAG + ":V *:S" });
            Log.d(LOGTAG, "no read logs permission test");
            reader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()));
            String line;
            final StringBuilder log = new StringBuilder();
            String separator = System.getProperty("line.separator");
            while ((line = reader.readLine()) != null) {
                log.append(line);
                log.append(separator);
            }
            assertEquals(0, log.length());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
