public class DragGestureEvent extends EventObject {
    private static final long serialVersionUID = 9080172649166731306L;
    public DragGestureEvent(DragGestureRecognizer dgr, int act, Point ori,
                            List<? extends InputEvent> evs)
    {
        super(dgr);
        if ((component = dgr.getComponent()) == null)
            throw new IllegalArgumentException("null component");
        if ((dragSource = dgr.getDragSource()) == null)
            throw new IllegalArgumentException("null DragSource");
        if (evs == null || evs.isEmpty())
            throw new IllegalArgumentException("null or empty list of events");
        if (act != DnDConstants.ACTION_COPY &&
            act != DnDConstants.ACTION_MOVE &&
            act != DnDConstants.ACTION_LINK)
            throw new IllegalArgumentException("bad action");
        if (ori == null) throw new IllegalArgumentException("null origin");
        events     = evs;
        action     = act;
        origin     = ori;
    }
    public DragGestureRecognizer getSourceAsDragGestureRecognizer() {
        return (DragGestureRecognizer)getSource();
    }
    public Component getComponent() { return component; }
    public DragSource getDragSource() { return dragSource; }
    public Point getDragOrigin() {
        return origin;
    }
    public Iterator<InputEvent> iterator() { return events.iterator(); }
    public Object[] toArray() { return events.toArray(); }
    public Object[] toArray(Object[] array) { return events.toArray(array); }
    public int getDragAction() { return action; }
    public InputEvent getTriggerEvent() {
        return getSourceAsDragGestureRecognizer().getTriggerEvent();
    }
    public void startDrag(Cursor dragCursor, Transferable transferable)
      throws InvalidDnDOperationException {
        dragSource.startDrag(this, dragCursor, transferable, null);
    }
    public void startDrag(Cursor dragCursor, Transferable transferable, DragSourceListener dsl) throws InvalidDnDOperationException {
        dragSource.startDrag(this, dragCursor, transferable, dsl);
    }
    public void startDrag(Cursor dragCursor, Image dragImage, Point imageOffset, Transferable transferable, DragSourceListener dsl) throws InvalidDnDOperationException {
        dragSource.startDrag(this,  dragCursor, dragImage, imageOffset, transferable, dsl);
    }
    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeObject(SerializationTester.test(events) ? events : null);
    }
    private void readObject(ObjectInputStream s)
        throws ClassNotFoundException, IOException
    {
        ObjectInputStream.GetField f = s.readFields();
        dragSource = (DragSource)f.get("dragSource", null);
        component = (Component)f.get("component", null);
        origin = (Point)f.get("origin", null);
        action = f.get("action", 0);
        try {
            events = (List)f.get("events", null);
        } catch (IllegalArgumentException e) {
            events = (List)s.readObject();
        }
        if (events == null) {
            events = Collections.EMPTY_LIST;
        }
    }
    private transient List events;
    private DragSource dragSource;
    private Component  component;
    private Point      origin;
    private int        action;
}
