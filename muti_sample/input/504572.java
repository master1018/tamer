public class MethodProfilingHandler extends BaseFileHandler
        implements IMethodProfilingHandler {
    public MethodProfilingHandler(Shell parentShell) {
        super(parentShell);
    }
    @Override
    protected String getDialogTitle() {
        return "Method Profiling Error";
    }
    public void onStartFailure(final Client client, final String message) {
        displayErrorInUiThread(
                "Unable to create Method Profiling file for application '%1$s'\n\n%2$s" +
                "Check logcat for more information.",
                client.getClientData().getClientDescription(),
                message != null ? message + "\n\n" : "");
    }
    public void onEndFailure(final Client client, final String message) {
        displayErrorInUiThread(
                "Unable to finish Method Profiling for application '%1$s'\n\n%2$s" +
                "Check logcat for more information.",
                client.getClientData().getClientDescription(),
                message != null ? message + "\n\n" : "");
    }
    public void onSuccess(final String remoteFilePath, final Client client) {
        mParentShell.getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (remoteFilePath == null) {
                    displayErrorFromUiThread(
                            "Unable to download trace file: unknown file name.\n" +
                            "This can happen if you disconnected the device while recording the trace.");
                    return;
                }
                final IDevice device = client.getDevice();
                try {
                    final SyncService sync = client.getDevice().getSyncService();
                    if (sync != null) {
                        pullAndOpen(sync, remoteFilePath);
                    } else {
                        displayErrorFromUiThread("Unable to download trace file from device '%1$s'.",
                                device.getSerialNumber());
                    }
                } catch (Exception e) {
                    displayErrorFromUiThread("Unable to download trace file from device '%1$s'.",
                            device.getSerialNumber());
                }
            }
        });
    }
    public void onSuccess(byte[] data, final Client client) {
        try {
            File tempFile = saveTempFile(data);
            openInTraceview(tempFile.getAbsolutePath());
        } catch (IOException e) {
            String errorMsg = e.getMessage();
            displayErrorInUiThread(
                    "Failed to save trace data into temp file%1$s",
                    errorMsg != null ? ":\n" + errorMsg : ".");
        }
    }
    private void pullAndOpen(SyncService sync, String remoteFilePath)
            throws InvocationTargetException, InterruptedException, IOException {
        File temp = File.createTempFile("android", ".trace"); 
        String tempPath = temp.getAbsolutePath();
        SyncResult result = pull(sync, tempPath, remoteFilePath);
        if (result != null) {
            if (result.getCode() == SyncService.RESULT_OK) {
                openInTraceview(tempPath);
            } else {
                displayErrorFromUiThread("Unable to download trace file:\n\n%1$s",
                        result.getMessage());
            }
        } else {
            displayErrorFromUiThread("Unable to download trace file.");
        }
    }
    private void openInTraceview(String tempPath) {
        String[] command = new String[2];
        command[0] = DdmUiPreferences.getTraceview();
        command[1] = tempPath;
        try {
            final Process p = Runtime.getRuntime().exec(command);
            new Thread("Traceview output") {
                @Override
                public void run() {
                    InputStreamReader is = new InputStreamReader(p.getErrorStream());
                    BufferedReader resultReader = new BufferedReader(is);
                    try {
                        while (true) {
                            String line = resultReader.readLine();
                            if (line != null) {
                                DdmConsole.printErrorToConsole("Traceview: " + line);
                            } else {
                                break;
                            }
                        }
                        p.waitFor();
                    } catch (Exception e) {
                        Log.e("traceview", e);
                    }
                }
            }.start();
        } catch (IOException e) {
            Log.e("traceview", e);
        }
    }
}
