public class AdbWrapper {
    private final String mAdbOsLocation;
    private final ITaskMonitor mMonitor;
    public AdbWrapper(String osSdkPath, ITaskMonitor monitor) {
        mMonitor = monitor;
        if (!osSdkPath.endsWith(File.separator)) {
            osSdkPath += File.separator;
        }
        mAdbOsLocation = osSdkPath + SdkConstants.OS_SDK_TOOLS_FOLDER + SdkConstants.FN_ADB;
    }
    private void display(String format, Object...args) {
        mMonitor.setResult(format, args);
    }
    public synchronized boolean startAdb() {
        if (mAdbOsLocation == null) {
            display("Error: missing path to ADB."); 
            return false;
        }
        Process proc;
        int status = -1;
        try {
            String[] command = new String[2];
            command[0] = mAdbOsLocation;
            command[1] = "start-server"; 
            proc = Runtime.getRuntime().exec(command);
            ArrayList<String> errorOutput = new ArrayList<String>();
            ArrayList<String> stdOutput = new ArrayList<String>();
            status = grabProcessOutput(proc, errorOutput, stdOutput,
                    false );
        } catch (IOException ioe) {
            display("Unable to run 'adb': %1$s.", ioe.getMessage()); 
        } catch (InterruptedException ie) {
            display("Unable to run 'adb': %1$s.", ie.getMessage()); 
        }
        if (status != 0) {
            display("'adb start-server' failed."); 
            return false;
        }
        display("'adb start-server' succeeded."); 
        return true;
    }
    public synchronized boolean stopAdb() {
        if (mAdbOsLocation == null) {
            display("Error: missing path to ADB."); 
            return false;
        }
        Process proc;
        int status = -1;
        try {
            String[] command = new String[2];
            command[0] = mAdbOsLocation;
            command[1] = "kill-server"; 
            proc = Runtime.getRuntime().exec(command);
            status = proc.waitFor();
        }
        catch (IOException ioe) {
        }
        catch (InterruptedException ie) {
        }
        if (status != 0) {
            display("'adb kill-server' failed -- run manually if necessary."); 
            return false;
        }
        display("'adb kill-server' succeeded."); 
        return true;
    }
    private int grabProcessOutput(final Process process, final ArrayList<String> errorOutput,
            final ArrayList<String> stdOutput, boolean waitforReaders)
            throws InterruptedException {
        assert errorOutput != null;
        assert stdOutput != null;
        Thread t1 = new Thread("") { 
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getErrorStream());
                BufferedReader errReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = errReader.readLine();
                        if (line != null) {
                            display("ADB Error: %1$s", line);
                            errorOutput.add(line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
            }
        };
        Thread t2 = new Thread("") { 
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getInputStream());
                BufferedReader outReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = outReader.readLine();
                        if (line != null) {
                            display("ADB: %1$s", line);
                            stdOutput.add(line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
            }
        };
        t1.start();
        t2.start();
        if (waitforReaders) {
            try {
                t1.join();
            } catch (InterruptedException e) {
            }
            try {
                t2.join();
            } catch (InterruptedException e) {
            }
        }
        return process.waitFor();
    }
}
