public class ClassTreeTool extends JPanel {
    private static final long serialVersionUID = 526178912591739259L;
    private Environment env;
    private ExecutionManager runtime;
    private SourceManager sourceManager;
    private ClassManager classManager;
    private JTree tree;
    private DefaultTreeModel treeModel;
    private ClassTreeNode root;
    private CommandInterpreter interpreter;
    private static String HEADING = "CLASSES";
    public ClassTreeTool(Environment env) {
        super(new BorderLayout());
        this.env = env;
        this.runtime = env.getExecutionManager();
        this.sourceManager = env.getSourceManager();
        this.interpreter = new CommandInterpreter(env);
        root = createClassTree(HEADING);
        treeModel = new DefaultTreeModel(root);
        tree = new JTree(treeModel);
        tree.setSelectionModel(new SingleLeafTreeSelectionModel());
        MouseListener ml = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if(selRow != -1) {
                    if(e.getClickCount() == 1) {
                        ClassTreeNode node =
                            (ClassTreeNode)selPath.getLastPathComponent();
                        if (node.isLeaf()) {
                            tree.setSelectionPath(selPath);
                            interpreter.executeCommand("view " + node.getReferenceTypeName());
                        }
                    }
                }
            }
        };
        tree.addMouseListener(ml);
        JScrollPane treeView = new JScrollPane(tree);
        add(treeView);
        ClassTreeToolListener listener = new ClassTreeToolListener();
        runtime.addJDIListener(listener);
        runtime.addSessionListener(listener);
    }
    private class ClassTreeToolListener extends JDIAdapter
                       implements JDIListener, SessionListener {
        @Override
        public void sessionStart(EventObject e) {
            try {
                for (ReferenceType type : runtime.allClasses()) {
                    root.addClass(type);
                }
            } catch (VMDisconnectedException ee) {
            } catch (NoSessionException ee) {
            }
        }
        @Override
        public void sessionInterrupt(EventObject e) {}
        @Override
        public void sessionContinue(EventObject e) {}
        @Override
        public void classPrepare(ClassPrepareEventSet e) {
            root.addClass(e.getReferenceType());
        }
        @Override
        public void classUnload(ClassUnloadEventSet e) {
            root.removeClass(e.getClassName());
        }
        @Override
        public void vmDisconnect(VMDisconnectEventSet e) {
            root = createClassTree(HEADING);
            treeModel = new DefaultTreeModel(root);
            tree.setModel(treeModel);
        }
    }
    ClassTreeNode createClassTree(String label) {
        return new ClassTreeNode(label, null);
    }
    class ClassTreeNode extends DefaultMutableTreeNode {
        private String name;
        private ReferenceType refTy;  
        ClassTreeNode(String name, ReferenceType refTy) {
            this.name = name;
            this.refTy = refTy;
        }
        @Override
        public String toString() {
            return name;
        }
        public ReferenceType getReferenceType() {
            return refTy;
        }
        public String getReferenceTypeName() {
            return refTy.name();
        }
        private boolean isPackage() {
            return (refTy == null);
        }
        @Override
        public boolean isLeaf() {
            return !isPackage();
        }
        public void addClass(ReferenceType refTy) {
            addClass(refTy.name(), refTy);
        }
        private void addClass(String className, ReferenceType refTy) {
            if (className.equals("")) {
                return;
            }
            int pos = className.indexOf('.');
            if (pos < 0) {
                insertNode(className, refTy);
            } else {
                String head = className.substring(0, pos);
                String tail = className.substring(pos + 1);
                ClassTreeNode child = insertNode(head, null);
                child.addClass(tail, refTy);
            }
        }
        private ClassTreeNode insertNode(String name, ReferenceType refTy) {
            for (int i = 0; i < getChildCount(); i++) {
                ClassTreeNode child = (ClassTreeNode)getChildAt(i);
                int cmp = name.compareTo(child.toString());
                if (cmp == 0) {
                    return child;
                } else if (cmp < 0) {
                    ClassTreeNode newChild = new ClassTreeNode(name, refTy);
                    treeModel.insertNodeInto(newChild, this, i);
                    return newChild;
                }
            }
            ClassTreeNode newChild = new ClassTreeNode(name, refTy);
            treeModel.insertNodeInto(newChild, this, getChildCount());
            return newChild;
        }
        public void removeClass(String className) {
            if (className.equals("")) {
                return;
            }
            int pos = className.indexOf('.');
            if (pos < 0) {
                ClassTreeNode child = findNode(className);
                if (!isPackage()) {
                    treeModel.removeNodeFromParent(child);
                }
            } else {
                String head = className.substring(0, pos);
                String tail = className.substring(pos + 1);
                ClassTreeNode child = findNode(head);
                child.removeClass(tail);
                if (isPackage() && child.getChildCount() < 1) {
                    treeModel.removeNodeFromParent(child);
                }
            }
        }
        private ClassTreeNode findNode(String name) {
            for (int i = 0; i < getChildCount(); i++) {
                ClassTreeNode child = (ClassTreeNode)getChildAt(i);
                int cmp = name.compareTo(child.toString());
                if (cmp == 0) {
                    return child;
                } else if (cmp > 0) {
                    return null;
                }
            }
            return null;
        }
    }
}
