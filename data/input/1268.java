final class SWGSchematicTreeModel implements TreeModel, UpdateSubscriber {
    boolean hideEmptyCategoryNodes;
    private List<SWGSchematic> matcher;
    private Vector<TreeModelListener> modelListeners;
    SWGSchematicTreeModel() {
        hideEmptyCategoryNodes = ((Boolean) SWGFrame.getPrefsKeeper().get("schemDraftHideEmptyNodes", Boolean.TRUE)).booleanValue();
        modelListeners = new Vector<TreeModelListener>();
        SWGSchematicsManager.addSubscriber(this);
    }
    private void addElements(List<? extends Object> src, List<TNode> target) {
        if (src.size() > 0) {
            Object[] a = src.toArray();
            for (Object o : a) target.add(new TNode(o));
        }
    }
    public void addTreeModelListener(TreeModelListener l) {
        if (!modelListeners.contains(l)) modelListeners.add(l);
    }
    private List<SWGSchematic> filterSchematics(List<SWGSchematic> schems) {
        if (schems.size() <= 0) return schems;
        List<SWGSchematic> ret = new ArrayList<SWGSchematic>(schems);
        synchronized (this) {
            if (matcher != null) ret.retainAll(matcher);
            return ret;
        }
    }
    public Object getChild(Object parent, int index) {
        if (parent instanceof TNode) return getChildElements((TNode) parent).get(index);
        return null;
    }
    public int getChildCount(Object parent) {
        if (parent instanceof TNode && ((TNode) parent).getContent() instanceof SWGCategory) return getChildElements((TNode) parent).size();
        return 0;
    }
    private List<TNode> getChildElements(TNode node) {
        if (node != null && node.getContent() instanceof SWGCategory) {
            ArrayList<TNode> ret = new ArrayList<TNode>();
            SWGCategory cat = (SWGCategory) node.getContent();
            addElements(cat.getCategories(), ret);
            addElements(filterSchematics(cat.getSchematics()), ret);
            addElements(cat.getItems(), ret);
            if (!hideEmptyCategoryNodes) return ret;
            ArrayList<TNode> ret2 = new ArrayList<TNode>();
            for (TNode tn : ret) {
                if (tn.getContent() instanceof SWGSchematic || tn.getContent() instanceof String || getChildElements(tn).size() > 0 || (tn.getContent() instanceof SWGCategory && SWGSchematicsManager.isSpecial((SWGCategory) tn.getContent()))) ret2.add(tn);
            }
            return ret2;
        }
        return Collections.emptyList();
    }
    public int getIndexOfChild(Object parent, Object child) {
        if (parent instanceof TNode && child instanceof TNode) {
            TNode p = (TNode) parent;
            TNode c = (TNode) child;
            return getChildElements(p).indexOf(c);
        }
        return -1;
    }
    public Object getRoot() {
        SWGCategory c = SWGSchematicsManager.getCategory(0);
        return c == null ? null : new TNode(c);
    }
    @Override
    public void handleUpdate(UpdateNotification u) {
        if (u instanceof CacheUpdate && ((CacheUpdate) u).type == UpdateType.SCHEMATICS) notifyListeners();
    }
    public boolean isLeaf(Object node) {
        if (node instanceof TNode && ((TNode) node).getContent() instanceof SWGCategory) return false;
        return true;
    }
    boolean isVisible(TNode node) {
        if (node.getContent() instanceof SWGCategory) return getChildElements(node).size() > 0;
        if (node.getContent() instanceof SWGSchematic) {
            SWGSchematic s = (SWGSchematic) node.getContent();
            return pathFromSchematicID(s.getID()) != null;
        }
        return false;
    }
    private void notifyListeners() {
        Object root = getRoot();
        if (root != null) {
            TreeModelEvent evt = new TreeModelEvent(this, new TreePath(root));
            for (TreeModelListener listener : modelListeners) listener.treeStructureChanged(evt);
        }
    }
    private List<TNode> pathFromCategory(SWGCategory c) {
        SWGCategory cc = c;
        List<TNode> nodes = new ArrayList<TNode>();
        while (cc != null) {
            nodes.add(new TNode(cc));
            if (cc.getID() < 0) return null;
            if (cc.getID() == 0) break;
            cc = SWGSchematicsManager.getCategory(cc.getParentID());
        }
        if (cc == null) return null;
        Collections.reverse(nodes);
        return nodes;
    }
    TreePath pathFromCategoryID(SWGCategory c) {
        if (c == null) return null;
        List<TNode> nodes = pathFromCategory(c);
        if (nodes == null) return null;
        return new TreePath(nodes.toArray());
    }
    TreePath pathFromSchematicID(int sid) {
        if (sid < 0 || sid > SWGSchematicsManager.maxSchematicID()) return null;
        SWGSchematic s = SWGSchematicsManager.getSchematic(sid);
        if (s == null || (matcher != null && !matcher.contains(s))) return null;
        SWGCategory c = SWGSchematicsManager.getCategory(s.getCategory());
        List<TNode> nodes = pathFromCategory(c);
        if (nodes == null) return null;
        nodes.add(new TNode(s));
        return new TreePath(nodes.toArray());
    }
    public void removeTreeModelListener(TreeModelListener l) {
        modelListeners.remove(l);
    }
    void setSchematics(List<SWGSchematic> schems) {
        synchronized (this) {
            matcher = schems;
        }
        notifyListeners();
    }
    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("Editing nodes in this tree is unsupported");
    }
    final class TNode {
        private Object object;
        TNode(Object o) {
            object = o;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TNode) return object.equals(((TNode) obj).object);
            return false;
        }
        Object getContent() {
            return object;
        }
        @Override
        public int hashCode() {
            return object.hashCode();
        }
        @Override
        public String toString() {
            if (object instanceof SWGCategory) return ((SWGCategory) object).getName();
            if (object instanceof SWGSchematic) return ((SWGSchematic) object).getName();
            return object.toString();
        }
    }
}
