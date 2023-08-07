public class CastNode extends UnaryNode {
    public int brackets;
    public String type;
    public Class cls;
    public CastNode(Node child, int brackets, Object tp) {
        super(child);
        this.brackets = brackets;
        if (tp instanceof Class) cls = (Class) tp; else type = (String) tp;
    }
    public String toString() {
        StringBuffer s = new StringBuffer();
        s.append(super.toString());
        s.append(" (");
        if (cls != null) s.append(cls); else s.append(type);
        s.append(')');
        return s.toString();
    }
    public Field visit(MemVisitor visitor, Object obj) {
        return visitor.visitCastNode(this, obj);
    }
    protected void normalizeImp() {
        super.normalizeImp();
        if (next instanceof FieldNode || next instanceof FieldNavNode) {
            FieldNode toReplace = findFieldNode(childList);
            if (toReplace == null) return;
            Node toMove = next;
            Node toKeep = next.next;
            Node toRaise = childList;
            FieldNavNode castFnn = new FieldNavNode();
            castFnn.cast = type;
            castFnn.lexeme = toReplace.lexeme;
            castFnn.childList = toMove;
            toMove.parent = castFnn;
            toMove.next = null;
            toReplace.parent.childList = castFnn;
            castFnn.parent = toReplace.parent;
            if (toRaise == toReplace) toRaise = castFnn;
            parent.childList = toRaise;
            toRaise.parent = parent;
            toRaise.next = toKeep;
            if (toKeep != null) toKeep.parent = parent;
        }
    }
    private FieldNode findFieldNode(Node root) {
        for (; root != null && !(root instanceof FieldNode); root = root.childList) ;
        return (FieldNode) root;
    }
    public Object arrive(NodeVisitor v, Object msg) {
        return v.arriveCastNode(this, msg);
    }
}
