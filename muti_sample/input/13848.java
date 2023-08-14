public class UndoManager extends CompoundEdit implements UndoableEditListener {
    int indexOfNextAdd;
    int limit;
    public UndoManager() {
        super();
        indexOfNextAdd = 0;
        limit = 100;
        edits.ensureCapacity(limit);
    }
    public synchronized int getLimit() {
        return limit;
    }
    public synchronized void discardAllEdits() {
        for (UndoableEdit e : edits) {
            e.die();
        }
        edits = new Vector<UndoableEdit>();
        indexOfNextAdd = 0;
    }
    protected void trimForLimit() {
        if (limit >= 0) {
            int size = edits.size();
            if (size > limit) {
                int halfLimit = limit/2;
                int keepFrom = indexOfNextAdd - 1 - halfLimit;
                int keepTo   = indexOfNextAdd - 1 + halfLimit;
                if (keepTo - keepFrom + 1 > limit) {
                    keepFrom++;
                }
                if (keepFrom < 0) {
                    keepTo -= keepFrom;
                    keepFrom = 0;
                }
                if (keepTo >= size) {
                    int delta = size - keepTo - 1;
                    keepTo += delta;
                    keepFrom += delta;
                }
                trimEdits(keepTo+1, size-1);
                trimEdits(0, keepFrom-1);
            }
        }
    }
    protected void trimEdits(int from, int to) {
        if (from <= to) {
            for (int i = to; from <= i; i--) {
                UndoableEdit e = edits.elementAt(i);
                e.die();
                edits.removeElementAt(i);
            }
            if (indexOfNextAdd > to) {
                indexOfNextAdd -= to-from+1;
            } else if (indexOfNextAdd >= from) {
                indexOfNextAdd = from;
            }
        }
    }
    public synchronized void setLimit(int l) {
        if (!inProgress) throw new RuntimeException("Attempt to call UndoManager.setLimit() after UndoManager.end() has been called");
        limit = l;
        trimForLimit();
    }
    protected UndoableEdit editToBeUndone() {
        int i = indexOfNextAdd;
        while (i > 0) {
            UndoableEdit edit = edits.elementAt(--i);
            if (edit.isSignificant()) {
                return edit;
            }
        }
        return null;
    }
    protected UndoableEdit editToBeRedone() {
        int count = edits.size();
        int i = indexOfNextAdd;
        while (i < count) {
            UndoableEdit edit = edits.elementAt(i++);
            if (edit.isSignificant()) {
                return edit;
            }
        }
        return null;
    }
    protected void undoTo(UndoableEdit edit) throws CannotUndoException {
        boolean done = false;
        while (!done) {
            UndoableEdit next = edits.elementAt(--indexOfNextAdd);
            next.undo();
            done = next == edit;
        }
    }
    protected void redoTo(UndoableEdit edit) throws CannotRedoException {
        boolean done = false;
        while (!done) {
            UndoableEdit next = edits.elementAt(indexOfNextAdd++);
            next.redo();
            done = next == edit;
        }
    }
    public synchronized void undoOrRedo() throws CannotRedoException,
        CannotUndoException {
        if (indexOfNextAdd == edits.size()) {
            undo();
        } else {
            redo();
        }
    }
    public synchronized boolean canUndoOrRedo() {
        if (indexOfNextAdd == edits.size()) {
            return canUndo();
        } else {
            return canRedo();
        }
    }
    public synchronized void undo() throws CannotUndoException {
        if (inProgress) {
            UndoableEdit edit = editToBeUndone();
            if (edit == null) {
                throw new CannotUndoException();
            }
            undoTo(edit);
        } else {
            super.undo();
        }
    }
    public synchronized boolean canUndo() {
        if (inProgress) {
            UndoableEdit edit = editToBeUndone();
            return edit != null && edit.canUndo();
        } else {
            return super.canUndo();
        }
    }
    public synchronized void redo() throws CannotRedoException {
        if (inProgress) {
            UndoableEdit edit = editToBeRedone();
            if (edit == null) {
                throw new CannotRedoException();
            }
            redoTo(edit);
        } else {
            super.redo();
        }
    }
    public synchronized boolean canRedo() {
        if (inProgress) {
            UndoableEdit edit = editToBeRedone();
            return edit != null && edit.canRedo();
        } else {
            return super.canRedo();
        }
    }
    public synchronized boolean addEdit(UndoableEdit anEdit) {
        boolean retVal;
        trimEdits(indexOfNextAdd, edits.size()-1);
        retVal = super.addEdit(anEdit);
        if (inProgress) {
          retVal = true;
        }
        indexOfNextAdd = edits.size();
        trimForLimit();
        return retVal;
    }
    public synchronized void end() {
        super.end();
        this.trimEdits(indexOfNextAdd, edits.size()-1);
    }
    public synchronized String getUndoOrRedoPresentationName() {
        if (indexOfNextAdd == edits.size()) {
            return getUndoPresentationName();
        } else {
            return getRedoPresentationName();
        }
    }
    public synchronized String getUndoPresentationName() {
        if (inProgress) {
            if (canUndo()) {
                return editToBeUndone().getUndoPresentationName();
            } else {
                return UIManager.getString("AbstractUndoableEdit.undoText");
            }
        } else {
            return super.getUndoPresentationName();
        }
    }
    public synchronized String getRedoPresentationName() {
        if (inProgress) {
            if (canRedo()) {
                return editToBeRedone().getRedoPresentationName();
            } else {
                return UIManager.getString("AbstractUndoableEdit.redoText");
            }
        } else {
            return super.getRedoPresentationName();
        }
    }
    public void undoableEditHappened(UndoableEditEvent e) {
        addEdit(e.getEdit());
    }
    public String toString() {
        return super.toString() + " limit: " + limit +
            " indexOfNextAdd: " + indexOfNextAdd;
    }
}
