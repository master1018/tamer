final class WDragSourceContextPeer extends SunDragSourceContextPeer {
    public void startSecondaryEventLoop(){
        WToolkit.startSecondaryEventLoop();
    }
    public void quitSecondaryEventLoop(){
        WToolkit.quitSecondaryEventLoop();
    }
    private static final WDragSourceContextPeer theInstance =
        new WDragSourceContextPeer(null);
    private WDragSourceContextPeer(DragGestureEvent dge) {
        super(dge);
    }
    static WDragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge) throws InvalidDnDOperationException {
        theInstance.setTrigger(dge);
        return theInstance;
    }
    protected void startDrag(Transferable trans,
                             long[] formats, Map formatMap) {
        long nativeCtxtLocal = 0;
        nativeCtxtLocal = createDragSource(getTrigger().getComponent(),
                                           trans,
                                           getTrigger().getTriggerEvent(),
                                           getTrigger().getSourceAsDragGestureRecognizer().getSourceActions(),
                                           formats,
                                           formatMap);
        if (nativeCtxtLocal == 0) {
            throw new InvalidDnDOperationException("failed to create native peer");
        }
        int[] imageData = null;
        Point op = null;
        Image im = getDragImage();
        int imageWidth = -1;
        int imageHeight = -1;
        if (im != null) {
            try{
                imageWidth = im.getWidth(null);
                imageHeight = im.getHeight(null);
                if (imageWidth < 0 || imageHeight < 0) {
                    throw new InvalidDnDOperationException("drag image is not ready");
                }
                op = getDragImageOffset(); 
                BufferedImage bi = new BufferedImage(
                        imageWidth,
                        imageHeight,
                        BufferedImage.TYPE_INT_ARGB);
                bi.getGraphics().drawImage(im, 0, 0, null);
                imageData = ((DataBufferInt)bi.getData().getDataBuffer()).getData();
            } catch (Throwable ex) {
                throw new InvalidDnDOperationException("drag image creation problem: " + ex.getMessage());
            }
        }
        setNativeContext(nativeCtxtLocal);
        WDropTargetContextPeer.setCurrentJVMLocalSourceTransferable(trans);
        if (imageData != null) {
            doDragDrop(
                    getNativeContext(),
                    getCursor(),
                    imageData,
                    imageWidth, imageHeight,
                    op.x, op.y);
        } else {
            doDragDrop(
                    getNativeContext(),
                    getCursor(),
                    null,
                    -1, -1,
                    0, 0);
        }
    }
    native long createDragSource(Component component,
                                 Transferable transferable,
                                 InputEvent nativeTrigger,
                                 int actions,
                                 long[] formats,
                                 Map formatMap);
    native void doDragDrop(
            long nativeCtxt,
            Cursor cursor,
            int[] imageData,
            int imgWidth, int imgHight,
            int offsetX, int offsetY);
    protected native void setNativeCursor(long nativeCtxt, Cursor c, int cType);
}
