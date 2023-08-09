public class WFileDialogPeer extends WWindowPeer implements FileDialogPeer {
    static {
        initIDs();
    }
    private WComponentPeer parent;
    private FilenameFilter fileFilter;
    private Vector<WWindowPeer> blockedWindows = new Vector<WWindowPeer>();
    private static native void setFilterString(String allFilter);
    public void setFilenameFilter(FilenameFilter filter) {
        this.fileFilter = filter;
    }
    boolean checkFilenameFilter(String filename) {
        FileDialog fileDialog = (FileDialog)target;
        if (fileFilter == null) {
            return true;
        }
        File file = new File(filename);
        return fileFilter.accept(new File(file.getParent()), file.getName());
    }
    WFileDialogPeer(FileDialog target) {
        super(target);
    }
    void create(WComponentPeer parent) {
        this.parent = parent;
    }
    protected void checkCreation() {
    }
    void initialize() {
        setFilenameFilter(((FileDialog) target).getFilenameFilter());
    }
    private native void _dispose();
    protected void disposeImpl() {
        WToolkit.targetDisposedPeer(target, this);
        _dispose();
    }
    private native void _show();
    private native void _hide();
    public void show() {
        new Thread(new Runnable() {
            public void run() {
                _show();
            }
        }).start();
    }
    public void hide() {
        _hide();
    }
    void setHWnd(long hwnd) {
        if (this.hwnd == hwnd) {
            return;
        }
        this.hwnd = hwnd;
        for (WWindowPeer window : blockedWindows) {
            if (hwnd != 0) {
                window.modalDisable((Dialog)target, hwnd);
            } else {
                window.modalEnable((Dialog)target);
            }
        }
    }
    void handleSelected(final char[] buffer)
    {
        String[] wFiles = (new String(buffer)).split("\0"); 
        boolean multiple = (wFiles.length > 1);
        String jDirectory = null;
        String jFile = null;
        String jFiles[] = null;
        if (multiple) {
            jDirectory = wFiles[0];
            jFiles = new String[wFiles.length - 1];
            System.arraycopy(wFiles, 1, jFiles, 0, jFiles.length);
            jFile = jFiles[1]; 
        } else {
            int index = wFiles[0].lastIndexOf(java.io.File.separatorChar);
            if (index == -1) {
                jDirectory = "."+java.io.File.separator;
                jFile = wFiles[0];
            } else {
                jDirectory = wFiles[0].substring(0, index + 1);
                jFile = wFiles[0].substring(index + 1);
            }
            jFiles = new String[] { jFile };
        }
        final FileDialog fileDialog = (FileDialog)target;
        AWTAccessor.FileDialogAccessor fileDialogAccessor = AWTAccessor.getFileDialogAccessor();
        fileDialogAccessor.setDirectory(fileDialog, jDirectory);
        fileDialogAccessor.setFile(fileDialog, jFile);
        fileDialogAccessor.setFiles(fileDialog, jDirectory, jFiles);
        WToolkit.executeOnEventHandlerThread(fileDialog, new Runnable() {
             public void run() {
                 fileDialog.hide();
             }
        });
    } 
    void handleCancel() {
        final FileDialog fileDialog = (FileDialog)target;
        AWTAccessor.getFileDialogAccessor().setFile(fileDialog, null);
        AWTAccessor.getFileDialogAccessor().setFiles(fileDialog, null, null);
        WToolkit.executeOnEventHandlerThread(fileDialog, new Runnable() {
             public void run() {
                 fileDialog.hide();
             }
        });
    } 
    static {
        String filterString = (String) AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    try {
                        ResourceBundle rb = ResourceBundle.getBundle("sun.awt.windows.awtLocalization");
                        return rb.getString("allFiles");
                    } catch (MissingResourceException e) {
                        return "All Files";
                    }
                }
            });
        setFilterString(filterString);
    }
    void blockWindow(WWindowPeer window) {
        blockedWindows.add(window);
        if (hwnd != 0) {
            window.modalDisable((Dialog)target, hwnd);
        }
    }
    void unblockWindow(WWindowPeer window) {
        blockedWindows.remove(window);
        if (hwnd != 0) {
            window.modalEnable((Dialog)target);
        }
    }
    public void blockWindows(java.util.List<Window> toBlock) {
        for (Window w : toBlock) {
            WWindowPeer wp = (WWindowPeer)AWTAccessor.getComponentAccessor().getPeer(w);
            if (wp != null) {
                blockWindow(wp);
            }
        }
    }
    public native void toFront();
    public native void toBack();
    public void setAlwaysOnTop(boolean value) {}
    public void setDirectory(String dir) {}
    public void setFile(String file) {}
    public void setTitle(String title) {}
    public void setResizable(boolean resizable) {}
    public void enable() {}
    public void disable() {}
    public void reshape(int x, int y, int width, int height) {}
    public boolean handleEvent(Event e) { return false; }
    public void setForeground(Color c) {}
    public void setBackground(Color c) {}
    public void setFont(Font f) {}
    public void updateMinimumSize() {}
    public void updateIconImages() {}
    public boolean requestFocus(boolean temporary,
                                boolean focusedWindowChangeAllowed) {
        return false;
    }
    public boolean requestFocus
         (Component lightweightChild, boolean temporary,
          boolean focusedWindowChangeAllowed, long time, CausedFocusEvent.Cause cause)
    {
        return false;
    }
    void start() {}
    public void beginValidate() {}
    public void endValidate() {}
    void invalidate(int x, int y, int width, int height) {}
    public void addDropTarget(DropTarget dt) {}
    public void removeDropTarget(DropTarget dt) {}
    public void updateFocusableWindowState() {}
    public void setZOrder(ComponentPeer above) {}
    private static native void initIDs();
    public void applyShape(sun.java2d.pipe.Region shape) {}
    public void setOpacity(float opacity) {}
    public void setOpaque(boolean isOpaque) {}
    public void updateWindow(java.awt.image.BufferedImage backBuffer) {}
    @Override
    public void createScreenSurface(boolean isResize) {}
    @Override
    public void replaceSurfaceData() {}
    public boolean isMultipleMode() {
        FileDialog fileDialog = (FileDialog)target;
        return AWTAccessor.getFileDialogAccessor().isMultipleMode(fileDialog);
    }
}
