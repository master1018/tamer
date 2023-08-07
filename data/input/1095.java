public class QueryNode extends AbstractNode implements IRootNode {
    private String name;
    private ContextRoot[] children;
    private int childIdx;
    public QueryNode(String name) {
        RefClass[] roots = new ProxyHandler().getRootClasses();
        children = new ContextRoot[roots.length];
        for (int i = 0; i < roots.length; i++) {
            children[i] = new ContextRoot(roots[i], name, DiscoveryService.getProxy().getMetamodelName(i));
            children[i].setSelected(false);
        }
        childIdx = -1;
    }
    public QueryNode(Collection roots, String name, String metamodel) {
        children = new ContextRoot[roots.size()];
        for (int i = 0; i < roots.size(); i++) {
            children[i] = new ContextRoot((RefBaseObject) roots.toArray()[i], name, metamodel);
            children[i].setSelected(false);
        }
        childIdx = -1;
    }
    public boolean hasChildren() {
        return true;
    }
    public Object[] getChildren() {
        return children;
    }
    public Object getObject() {
        return null;
    }
    public String getName() {
        return name;
    }
    public int getNodesFlag() {
        if (childIdx > 0 && childIdx < children.length) return children[childIdx].getNodesFlag();
        int result = NONE;
        for (int i = 0; i < children.length; i++) {
            result = result | children[i].getNodesFlag();
        }
        return result;
    }
    public Iterator getNodes(boolean isHard) {
        if (childIdx > 0 && childIdx < children.length) return children[childIdx].getNodes(isHard);
        Vector result = new Vector();
        for (int i = 0; i < children.length; i++) {
            result.addAll(children[i].getNodesAsVector(isHard));
        }
        return result.iterator();
    }
    public String getQueryName() {
        return name;
    }
    public boolean hasNodes() {
        if (childIdx > 0 && childIdx < children.length) return children[childIdx].hasNodes();
        for (int i = 0; i < children.length; i++) {
            if (children[i].hasNodes()) return true;
        }
        return false;
    }
    public Iterator getNodes() {
        if (childIdx > 0 && childIdx < children.length) return children[childIdx].getNodes();
        Vector result = new Vector();
        for (int i = 0; i < children.length; i++) {
            result.addAll(children[i].getNodesAsCollection());
        }
        return result.iterator();
    }
    public boolean isInstance() {
        return false;
    }
    public void setSelectedMetamodel(int idx) {
        childIdx = idx;
    }
    public void setSelectedMetamodel(ContextRoot node) {
        for (int i = 0; i < children.length; i++) {
            if (children[i] == node) {
                if (childIdx >= 0) children[childIdx].setSelected(false);
                childIdx = i;
                children[childIdx].setSelected(true);
                return;
            }
        }
    }
    public ContextRoot getSelectedChild() {
        if (childIdx < 0 && childIdx > children.length) throw new RuntimeException("No metamodel selected.");
        return children[childIdx];
    }
    public String getMetamodel() {
        if (childIdx < 0 && childIdx > children.length) throw new RuntimeException("No metamodel selected.");
        return children[childIdx].getMetamodel();
    }
    public Vector getPath() {
        if (childIdx < 0 && childIdx > children.length) throw new RuntimeException("No metamodel selected.");
        return children[childIdx].getPath();
    }
    public String getResultType() {
        if (childIdx < 0 && childIdx > children.length) throw new RuntimeException("No metamodel selected.");
        return children[childIdx].getResultType();
    }
}
