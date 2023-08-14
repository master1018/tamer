public class DropTargetContext implements Serializable {
    private static final long serialVersionUID = -634158968993743371L;
    DropTargetContext(DropTarget dt) {
        super();
        dropTarget = dt;
    }
    public DropTarget getDropTarget() { return dropTarget; }
    public Component getComponent() { return dropTarget.getComponent(); }
    public void addNotify(DropTargetContextPeer dtcp) {
        dropTargetContextPeer = dtcp;
    }
    public void removeNotify() {
        dropTargetContextPeer = null;
        transferable          = null;
    }
    protected void setTargetActions(int actions) {
        DropTargetContextPeer peer = getDropTargetContextPeer();
        if (peer != null) {
            synchronized (peer) {
                peer.setTargetActions(actions);
                getDropTarget().doSetDefaultActions(actions);
            }
        } else {
            getDropTarget().doSetDefaultActions(actions);
        }
    }
    protected int getTargetActions() {
        DropTargetContextPeer peer = getDropTargetContextPeer();
        return ((peer != null)
                        ? peer.getTargetActions()
                        : dropTarget.getDefaultActions()
        );
    }
    public void dropComplete(boolean success) throws InvalidDnDOperationException{
        DropTargetContextPeer peer = getDropTargetContextPeer();
        if (peer != null) {
            peer.dropComplete(success);
        }
    }
    protected void acceptDrag(int dragOperation) {
        DropTargetContextPeer peer = getDropTargetContextPeer();
        if (peer != null) {
            peer.acceptDrag(dragOperation);
        }
    }
    protected void rejectDrag() {
        DropTargetContextPeer peer = getDropTargetContextPeer();
        if (peer != null) {
            peer.rejectDrag();
        }
    }
    protected void acceptDrop(int dropOperation) {
        DropTargetContextPeer peer = getDropTargetContextPeer();
        if (peer != null) {
            peer.acceptDrop(dropOperation);
        }
    }
    protected void rejectDrop() {
        DropTargetContextPeer peer = getDropTargetContextPeer();
        if (peer != null) {
            peer.rejectDrop();
        }
    }
    protected DataFlavor[] getCurrentDataFlavors() {
        DropTargetContextPeer peer = getDropTargetContextPeer();
        return peer != null ? peer.getTransferDataFlavors() : new DataFlavor[0];
    }
    protected List<DataFlavor> getCurrentDataFlavorsAsList() {
        return Arrays.asList(getCurrentDataFlavors());
    }
    protected boolean isDataFlavorSupported(DataFlavor df) {
        return getCurrentDataFlavorsAsList().contains(df);
    }
    protected Transferable getTransferable() throws InvalidDnDOperationException {
        DropTargetContextPeer peer = getDropTargetContextPeer();
        if (peer == null) {
            throw new InvalidDnDOperationException();
        } else {
            if (transferable == null) {
                Transferable t = peer.getTransferable();
                boolean isLocal = peer.isTransferableJVMLocal();
                synchronized (this) {
                    if (transferable == null) {
                        transferable = createTransferableProxy(t, isLocal);
                    }
                }
            }
            return transferable;
        }
    }
    DropTargetContextPeer getDropTargetContextPeer() {
        return dropTargetContextPeer;
    }
    protected Transferable createTransferableProxy(Transferable t, boolean local) {
        return new TransferableProxy(t, local);
    }
    protected class TransferableProxy implements Transferable {
        TransferableProxy(Transferable t, boolean local) {
            proxy = new sun.awt.datatransfer.TransferableProxy(t, local);
            transferable = t;
            isLocal      = local;
        }
        public DataFlavor[] getTransferDataFlavors() {
            return proxy.getTransferDataFlavors();
        }
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return proxy.isDataFlavorSupported(flavor);
        }
        public Object getTransferData(DataFlavor df)
            throws UnsupportedFlavorException, IOException
        {
            return proxy.getTransferData(df);
        }
        protected Transferable  transferable;
        protected boolean       isLocal;
        private sun.awt.datatransfer.TransferableProxy proxy;
    }
    private DropTarget dropTarget;
    private transient DropTargetContextPeer dropTargetContextPeer;
    private transient Transferable transferable;
}
