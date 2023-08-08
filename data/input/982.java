public class SMILESNode implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    private Atom _atom;
    private List _nextbond;
    private List _nextnode;
    private Atom _parent;
    public SMILESNode(Atom atom) {
        _atom = atom;
        _parent = null;
        _nextnode = new Vector();
        _nextbond = new Vector();
    }
    public Atom getAtom() {
        return (_atom);
    }
    public Atom getNextAtom(int i) {
        return ((SMILESNode) _nextnode.get(i)).getAtom();
    }
    public Bond getNextBond(int i) {
        return ((Bond) _nextbond.get(i));
    }
    public SMILESNode getNextNode(int i) {
        return ((SMILESNode) _nextnode.get(i));
    }
    public Atom getParent() {
        return (_parent);
    }
    public void setNextNode(SMILESNode node, Bond bond) {
        _nextnode.add(node);
        _nextbond.add(bond);
    }
    public void setParent(Atom a) {
        _parent = a;
    }
    public int size() {
        return _nextnode.size();
    }
    protected void finalize() throws Throwable {
        _nextnode.clear();
        super.finalize();
    }
}
