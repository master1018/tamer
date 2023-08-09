public class FolderNode extends AbstractNode {
    private GroupOrganizer organizer;
    private InstanceContent content;
    private List<Pair<String, List<Group>>> structure;
    private List<String> subFolders;
    private FolderChildren children;
    private static class FolderChildren extends Children.Keys implements ChangedListener<Group> {
        private FolderNode parent;
        private List<Group> registeredGroups;
        public void setParent(FolderNode parent) {
            this.parent = parent;
            this.registeredGroups = new ArrayList<Group>();
        }
        @Override
        protected Node[] createNodes(Object arg0) {
            for(Group g : registeredGroups) {
                g.getChangedEvent().removeListener(this);
            }
            registeredGroups.clear();
            Pair<String, List<Group>> p = (Pair<String, List<Group>>) arg0;
            if (p.getLeft().length() == 0) {
                List<Node> curNodes = new ArrayList<Node>();
                for (Group g : p.getRight()) {
                    for (InputGraph graph : g.getGraphs()) {
                        curNodes.add(new GraphNode(graph));
                    }
                    g.getChangedEvent().addListener(this);
                    registeredGroups.add(g);
                }
                Node[] result = new Node[curNodes.size()];
                for (int i = 0; i < curNodes.size(); i++) {
                    result[i] = curNodes.get(i);
                }
                return result;
            } else {
                return new Node[]{new FolderNode(p.getLeft(), parent.organizer, parent.subFolders, p.getRight())};
            }
        }
        @Override
        public void addNotify() {
            this.setKeys(parent.structure);
        }
        public void changed(Group source) {
            List<Pair<String, List<Group>>> newStructure = new ArrayList<Pair<String, List<Group>>>();
            for(Pair<String, List<Group>> p : parent.structure) {
                refreshKey(p);
            }
        }
    }
    protected InstanceContent getContent() {
        return content;
    }
    @Override
    public Image getIcon(int i) {
        return Utilities.loadImage("com/sun/hotspot/igv/coordinator/images/folder.gif");
    }
    protected FolderNode(String name, GroupOrganizer organizer, List<String> subFolders, List<Group> groups) {
        this(name, organizer, subFolders, groups, new FolderChildren(), new InstanceContent());
    }
    private FolderNode(String name, GroupOrganizer organizer, List<String> oldSubFolders, final List<Group> groups, FolderChildren children, InstanceContent content) {
        super(children, new AbstractLookup(content));
        children.setParent(this);
        this.content = content;
        this.children = children;
        content.add(new RemoveCookie() {
            public void remove() {
                for (Group g : groups) {
                    if (g.getDocument() != null) {
                        g.getDocument().removeGroup(g);
                    }
                }
            }
        });
        init(name, organizer, oldSubFolders, groups);
    }
    public void init(String name, GroupOrganizer organizer, List<String> oldSubFolders, List<Group> groups) {
        this.setDisplayName(name);
        this.organizer = organizer;
        this.subFolders = new ArrayList<String>(oldSubFolders);
        if (name.length() > 0) {
            this.subFolders.add(name);
        }
        structure = organizer.organize(subFolders, groups);
        assert structure != null;
        children.addNotify();
    }
    @Override
    public Image getOpenedIcon(int i) {
        return getIcon(i);
    }
}
