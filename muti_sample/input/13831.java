public class WPageDialog extends WPrintDialog {
    static {
        initIDs();
    }
    PageFormat page;
    Printable painter;
    WPageDialog(Frame parent, PrinterJob control, PageFormat page, Printable painter) {
        super(parent, control);
        this.page = page;
        this.painter = painter;
    }
    WPageDialog(Dialog parent, PrinterJob control, PageFormat page, Printable painter) {
        super(parent, control);
        this.page = page;
        this.painter = painter;
    }
    public void addNotify() {
        synchronized(getTreeLock()) {
            Container parent = getParent();
            if (parent != null && parent.getPeer() == null) {
                parent.addNotify();
            }
            if (getPeer() == null) {
                ComponentPeer peer = ((WToolkit)Toolkit.getDefaultToolkit()).
                    createWPageDialog(this);
                setPeer(peer);
            }
            super.addNotify();
        }
    }
    private static native void initIDs();
}
