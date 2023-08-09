public abstract class BaseFileHandler {
    protected final Shell mParentShell;
    public BaseFileHandler(Shell parentShell) {
        mParentShell = parentShell;
    }
    protected abstract String getDialogTitle();
    protected SyncResult promptAndPull(SyncService sync,
            String localFileName, String remoteFilePath, String title)
            throws InvocationTargetException, InterruptedException {
        FileDialog fileDialog = new FileDialog(mParentShell, SWT.SAVE);
        fileDialog.setText(title);
        fileDialog.setFileName(localFileName);
        String localFilePath = fileDialog.open();
        if (localFilePath != null) {
            return pull(sync, localFilePath, remoteFilePath);
        }
        return null;
    }
    protected boolean promptAndSave(String localFileName, byte[] data, String title) {
        FileDialog fileDialog = new FileDialog(mParentShell, SWT.SAVE);
        fileDialog.setText(title);
        fileDialog.setFileName(localFileName);
        String localFilePath = fileDialog.open();
        if (localFilePath != null) {
            try {
                saveFile(data, new File(localFilePath));
                return true;
            } catch (IOException e) {
                String errorMsg = e.getMessage();
                displayErrorInUiThread(
                        "Failed to save file '%1$s'%2$s",
                        localFilePath,
                        errorMsg != null ? ":\n" + errorMsg : ".");
            }
        }
        return false;
    }
    protected SyncResult pull(final SyncService sync, final String localFilePath,
            final String remoteFilePath)
            throws InvocationTargetException, InterruptedException {
        final SyncResult[] res = new SyncResult[1];
        new ProgressMonitorDialog(mParentShell).run(true, true, new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) {
                try {
                    res[0] = sync.pullFile(remoteFilePath, localFilePath,
                            new SyncProgressMonitor(monitor, String.format(
                                    "Pulling %1$s from the device", remoteFilePath)));
                } finally {
                    sync.close();
                }
            }
        });
        return res[0];
    }
    protected void displayErrorInUiThread(final String format, final Object... args) {
        mParentShell.getDisplay().asyncExec(new Runnable() {
            public void run() {
                MessageDialog.openError(mParentShell, getDialogTitle(),
                        String.format(format, args));
            }
        });
    }
    protected void displayErrorFromUiThread(final String format, final Object... args) {
        MessageDialog.openError(mParentShell, getDialogTitle(),
                String.format(format, args));
    }
    protected File saveTempFile(byte[] data) throws IOException {
        File f = File.createTempFile("ddms", null);
        saveFile(data, f);
        return f;
    }
    protected void saveFile(byte[] data, File output) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(output);
            fos.write(data);
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }
}
