public class GoMessageAction extends AbstractGoRule {
    public boolean isLeaf(Object node) {
        return !(node instanceof MMessage && getChildCount(node) > 0);
    }
    public Collection getChildren(Object parent) {
        if (parent instanceof MMessage) {
            MMessage mes = (MMessage) parent;
            MAction action = mes.getAction();
            if (action != null) {
                Vector vec = new Vector();
                vec.add(((MMessage) parent).getAction());
                return vec;
            }
        }
        return null;
    }
}
