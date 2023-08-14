public class UndoableEditSupport {
    protected int updateLevel;
    protected CompoundEdit compoundEdit;
    protected Vector<UndoableEditListener> listeners;
    protected Object realSource;
    public UndoableEditSupport() {
        this(null);
    }
    public UndoableEditSupport(Object r) {
        realSource = r == null ? this : r;
        updateLevel = 0;
        compoundEdit = null;
        listeners = new Vector<UndoableEditListener>();
    }
    public synchronized void addUndoableEditListener(UndoableEditListener l) {
        listeners.addElement(l);
    }
    public synchronized void removeUndoableEditListener(UndoableEditListener l)
    {
        listeners.removeElement(l);
    }
    public synchronized UndoableEditListener[] getUndoableEditListeners() {
        return listeners.toArray(new UndoableEditListener[0]);
    }
    protected void _postEdit(UndoableEdit e) {
        UndoableEditEvent ev = new UndoableEditEvent(realSource, e);
        Enumeration cursor = ((Vector)listeners.clone()).elements();
        while (cursor.hasMoreElements()) {
            ((UndoableEditListener)cursor.nextElement()).
                undoableEditHappened(ev);
        }
    }
    public synchronized void postEdit(UndoableEdit e) {
        if (updateLevel == 0) {
            _postEdit(e);
        } else {
            compoundEdit.addEdit(e);
        }
    }
    public int getUpdateLevel() {
        return updateLevel;
    }
    public synchronized void beginUpdate() {
        if (updateLevel == 0) {
            compoundEdit = createCompoundEdit();
        }
        updateLevel++;
    }
    protected CompoundEdit createCompoundEdit() {
        return new CompoundEdit();
    }
    public synchronized void endUpdate() {
        updateLevel--;
        if (updateLevel == 0) {
            compoundEdit.end();
            _postEdit(compoundEdit);
            compoundEdit = null;
        }
    }
    public String toString() {
        return super.toString() +
            " updateLevel: " + updateLevel +
            " listeners: " + listeners +
            " compoundEdit: " + compoundEdit;
    }
}
