public class OptionTest extends Object {
    private Process subprocess;
    private int subprocessStatus;
    private static final String CR = System.getProperty("line.separator");
    private static final int BUFFERSIZE = 4096;
    public static final int RETSTAT = 0;
    public static final int STDOUT = 1;
    public static final int STDERR = 2;
    public String[] run (String[] cmdStrings) {
        StringBuffer stdoutBuffer = new StringBuffer();
        StringBuffer stderrBuffer = new StringBuffer();
        System.out.print(CR + "runCommand method about to execute: ");
        for (int iNdx = 0; iNdx < cmdStrings.length; iNdx++) {
            System.out.print(" ");
            System.out.print(cmdStrings[iNdx]);
        }
        System.out.println(CR);
        try {
            Process process = Runtime.getRuntime().exec(cmdStrings);
            java.io.BufferedInputStream is =
                new java.io.BufferedInputStream(process.getInputStream());
            int isLen = 0;
            byte[] isBuf = new byte[BUFFERSIZE];
            java.io.BufferedInputStream es =
                new java.io.BufferedInputStream(process.getErrorStream());
            int esLen = 0;
            byte[] esBuf = new byte[BUFFERSIZE];
            do {
                isLen = is.read(isBuf);
                if (isLen > 0) {
                    stdoutBuffer.append(
                                        new String(isBuf, 0, isLen));
                }
                esLen = es.read(esBuf);
                if (esLen > 0) {
                    stderrBuffer.append(
                                        new String(esBuf, 0, esLen));
                }
            } while ((isLen > -1) || (esLen > -1));
            try {
                process.waitFor();
                subprocessStatus = process.exitValue();
                process = null;
            } catch(java.lang.InterruptedException e) {
                System.err.println("InterruptedException: " + e);
            }
        } catch(java.io.IOException ex) {
            System.err.println("IO error: " + ex);
        }
        String[] result =
            new String[] {
                Integer.toString(subprocessStatus),
                stdoutBuffer.toString(),
                stderrBuffer.toString()
        };
        System.out.println(CR + "--- Return code was: " +
                           CR + result[RETSTAT]);
        System.out.println(CR + "--- Return stdout was: " +
                           CR + result[STDOUT]);
        System.out.println(CR + "--- Return stderr was: " +
                           CR + result[STDERR]);
        return result;
    }
    public static void main(String[] args) throws Exception {
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        ss.close();
        String address = String.valueOf(port);
        String javaExe = System.getProperty("java.home") +
            java.io.File.separator + "bin" +
            java.io.File.separator + "java";
        String targetClass = "HelloWorld";
        String baseOptions = "transport=dt_socket" +
                              ",address=" + address +
                              ",server=y" +
                              ",suspend=n";
        String options[] =  {
                "timeout=0,mutf8=y,quiet=y,stdalloc=y,strict=n",
                "timeout=200000,mutf8=n,quiet=n,stdalloc=n,strict=y"
                };
        for ( String option : options) {
            String cmds [] = {javaExe,
                              "-agentlib:jdwp=" + baseOptions + "," + option,
                              targetClass};
            OptionTest myTest = new OptionTest();
            String results [] = myTest.run(VMConnection.insertDebuggeeVMOptions(cmds));
            if (!(results[RETSTAT].equals("0")) ||
                (results[STDERR].startsWith("ERROR:"))) {
                throw new Exception("Test failed: jdwp doesn't like " + cmds[1]);
            }
        }
        System.out.println("Test passed: status = 0");
    }
}
