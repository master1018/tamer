public class StateEdit
        extends AbstractUndoableEdit {
    protected static final String RCSID = "$Id: StateEdit.java,v 1.6 1997/10/01 20:05:51 sandipc Exp $";
    protected StateEditable object;
    protected Hashtable<Object,Object> preState;
    protected Hashtable<Object,Object> postState;
    protected String undoRedoName;
    public StateEdit(StateEditable anObject) {
        super();
        init (anObject,null);
    }
    public StateEdit(StateEditable anObject, String name) {
        super();
        init (anObject,name);
    }
    protected void init (StateEditable anObject, String name) {
        this.object = anObject;
        this.preState = new Hashtable<Object, Object>(11);
        this.object.storeState(this.preState);
        this.postState = null;
        this.undoRedoName = name;
    }
    public void end() {
        this.postState = new Hashtable<Object, Object>(11);
        this.object.storeState(this.postState);
        this.removeRedundantState();
    }
    public void undo() {
        super.undo();
        this.object.restoreState(preState);
    }
    public void redo() {
        super.redo();
        this.object.restoreState(postState);
    }
    public String getPresentationName() {
        return this.undoRedoName;
    }
    protected void removeRedundantState() {
        Vector<Object> uselessKeys = new Vector<Object>();
        Enumeration myKeys = preState.keys();
        while (myKeys.hasMoreElements()) {
            Object myKey = myKeys.nextElement();
            if (postState.containsKey(myKey) &&
                postState.get(myKey).equals(preState.get(myKey))) {
                uselessKeys.addElement(myKey);
            }
        }
        for (int i = uselessKeys.size()-1; i >= 0; i--) {
            Object myKey = uselessKeys.elementAt(i);
            preState.remove(myKey);
            postState.remove(myKey);
        }
    }
} 
