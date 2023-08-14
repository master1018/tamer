public class OnThrowTest extends Object {
    private String touchFile;
    OnThrowTest() throws Exception {
        touchFile = System.getProperty("test.classes") +
                    File.separator + "OnThrowLaunchTouchFile";
        File f = new File(touchFile);
        f.delete();
        if ( f.exists() ) {
            throw new Exception("Test failed: Cannot remove old touch file: " +
                  touchFile);
        }
    }
    private boolean touchFileExists() {
        File f = new File(touchFile);
        return f.exists();
    }
    public void run(String[] cmdStrings) throws Exception {
        StringBuffer stdoutBuffer = new StringBuffer();
        StringBuffer stderrBuffer = new StringBuffer();
        String CR = System.getProperty("line.separator");
        int subprocessStatus = 1;
        System.out.print(CR + "runCommand method about to execute: ");
        for (int iNdx = 0; iNdx < cmdStrings.length; iNdx++) {
            System.out.print(" ");
            System.out.print(cmdStrings[iNdx]);
        }
        System.out.println(CR);
        try {
            Process process = Runtime.getRuntime().
                exec(VMConnection.insertDebuggeeVMOptions(cmdStrings));
            int BUFFERSIZE = 4096;
            BufferedInputStream is =
                new BufferedInputStream(process.getInputStream());
            int isLen = 0;
            byte[] isBuf = new byte[BUFFERSIZE];
            BufferedInputStream es =
                new BufferedInputStream(process.getErrorStream());
            int esLen = 0;
            byte[] esBuf = new byte[BUFFERSIZE];
            do {
                isLen = is.read(isBuf);
                if (isLen > 0) {
                    stdoutBuffer.append(new String(isBuf, 0, isLen));
                }
                esLen = es.read(esBuf);
                if (esLen > 0) {
                    stderrBuffer.append(new String(esBuf, 0, esLen));
                }
            } while ((isLen > -1) || (esLen > -1));
            try {
                process.waitFor();
                subprocessStatus = process.exitValue();
                process = null;
            } catch(java.lang.InterruptedException e) {
                System.err.println("InterruptedException: " + e);
                throw new Exception("Test failed: process interrupted");
            }
        } catch(IOException ex) {
            System.err.println("IO error: " + ex);
            throw new Exception("Test failed: IO error running process");
        }
        System.out.println(CR + "--- Return code was: " +
                           CR + Integer.toString(subprocessStatus));
        System.out.println(CR + "--- Return stdout was: " +
                           CR + stdoutBuffer.toString());
        System.out.println(CR + "--- Return stderr was: " +
                           CR + stderrBuffer.toString());
    }
    public static void main(String[] args) throws Exception {
        OnThrowTest myTest = new OnThrowTest();
        String launch = System.getProperty("test.classes") +
                        File.separator + "OnThrowLaunch.sh";
        File f = new File(launch);
        f.delete();
        FileWriter fw = new FileWriter(f);
        fw.write("#!/bin/sh\n echo OK $* > " +
                 myTest.touchFile.replace('\\','/') + "\n exit 0\n");
        fw.flush();
        fw.close();
        if ( ! f.exists() ) {
            throw new Exception("Test failed: sh file not created: " + launch);
        }
        String javaExe = System.getProperty("java.home") +
                         File.separator + "bin" + File.separator + "java";
        String targetClass = "OnThrowTarget";
        String cmds [] = {javaExe,
                          "-agentlib:jdwp=transport=dt_socket," +
                          "onthrow=OnThrowException,server=y,suspend=n," +
                          "launch=" + "sh " + launch.replace('\\','/'),
                          targetClass};
        myTest.run(cmds);
        if ( !myTest.touchFileExists() ) {
            throw new Exception("Test failed: touch file not found: " +
                  myTest.touchFile);
        }
        System.out.println("Test passed: launch create file");
    }
}
