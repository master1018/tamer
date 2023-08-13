public class WPrintDialog extends Dialog {
    static {
        initIDs();
    }
    protected PrintJob job;
    protected PrinterJob pjob;
    public WPrintDialog(Frame parent, PrinterJob control) {
        super(parent, true);
        this.pjob = control;
        setLayout(null);
    }
    public WPrintDialog(Dialog parent, PrinterJob control) {
        super(parent, "", true);
        this.pjob = control;
        setLayout(null);
    }
    protected native void setPeer(ComponentPeer peer);
    public void addNotify() {
        synchronized(getTreeLock()) {
            Container parent = getParent();
            if (parent != null && parent.getPeer() == null) {
                parent.addNotify();
            }
            if (getPeer() == null) {
                ComponentPeer peer = ((WToolkit)Toolkit.getDefaultToolkit()).
                    createWPrintDialog(this);
                setPeer(peer);
            }
            super.addNotify();
        }
    }
    private boolean retval = false;
    public void setRetVal(boolean ret) {
        retval = ret;
    }
    public boolean getRetVal() {
        return retval;
    }
    private static native void initIDs();
}
