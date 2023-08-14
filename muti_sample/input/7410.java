class GtkFileDialogPeer extends XDialogPeer implements FileDialogPeer {
    private FileDialog fd;
    private volatile long widget = 0L;
    public GtkFileDialogPeer(FileDialog fd) {
        super((Dialog) fd);
        this.fd = fd;
    }
    private static native void initIDs();
    static {
        initIDs();
    }
    private native void run(String title, int mode, String dir, String file,
                            FilenameFilter filter, boolean isMultipleMode, int x, int y);
    private native void quit();
    @Override
    public native void toFront();
    @Override
    public native void setBounds(int x, int y, int width, int height, int op);
    private void setFileInternal(String directory, String[] filenames) {
        AWTAccessor.FileDialogAccessor accessor = AWTAccessor
                .getFileDialogAccessor();
        if (filenames == null) {
            accessor.setDirectory(fd, null);
            accessor.setFile(fd, null);
            accessor.setFiles(fd, null, null);
        } else {
            accessor.setDirectory(fd, directory +
                    (directory.endsWith(File.separator) ?
                     "" : File.separator));
            accessor.setFile(fd, filenames[0]);
            accessor.setFiles(fd, directory, filenames);
        }
    }
    private boolean filenameFilterCallback(String fullname) {
        if (fd.getFilenameFilter() == null) {
            return true;
        }
        File filen = new File(fullname);
        return fd.getFilenameFilter().accept(new File(filen.getParent()),
                filen.getName());
    }
    @Override
    public void setVisible(boolean b) {
        XToolkit.awtLock();
        try {
            if (b) {
                Thread t = new Thread() {
                    public void run() {
                        showNativeDialog();
                        fd.setVisible(false);
                    }
                };
                t.start();
            } else {
                quit();
                fd.setVisible(false);
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    @Override
    public void dispose() {
        quit();
        super.dispose();
    }
    @Override
    public void setDirectory(String dir) {
    }
    @Override
    public void setFile(String file) {
    }
    @Override
    public void setFilenameFilter(FilenameFilter filter) {
    }
    private void showNativeDialog() {
        String dirname = fd.getDirectory();
        String filename = fd.getFile();
        if (filename != null) {
            final File file = new File(filename);
            if (fd.getMode() == FileDialog.LOAD
                && dirname != null
                && file.getParent() == null) {
                filename = dirname + (dirname.endsWith(File.separator) ? "" :
                                              File.separator) + filename;
            }
            if (fd.getMode() == FileDialog.SAVE && file.getParent() != null) {
                filename = file.getName();
                dirname = file.getParent();
            }
        }
        GtkFileDialogPeer.this.run(fd.getTitle(), fd.getMode(), dirname,
                                   filename, fd.getFilenameFilter(),
                                   fd.isMultipleMode(), fd.getX(), fd.getY());
    }
}
