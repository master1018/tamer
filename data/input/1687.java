public abstract class DragSourceAdapter
    implements DragSourceListener, DragSourceMotionListener {
    public void dragEnter(DragSourceDragEvent dsde) {}
    public void dragOver(DragSourceDragEvent dsde) {}
    public void dragMouseMoved(DragSourceDragEvent dsde) {}
    public void dropActionChanged(DragSourceDragEvent dsde) {}
    public void dragExit(DragSourceEvent dse) {}
    public void dragDropEnd(DragSourceDropEvent dsde) {}
}
