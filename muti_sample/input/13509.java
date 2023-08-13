public class ThreadTreeTool extends JPanel {
    private static final long serialVersionUID = 4168599992853038878L;
    private Environment env;
    private ExecutionManager runtime;
    private SourceManager sourceManager;
    private ClassManager classManager;
    private JTree tree;
    private DefaultTreeModel treeModel;
    private ThreadTreeNode root;
    private SearchPath sourcePath;
    private CommandInterpreter interpreter;
    private static String HEADING = "THREADS";
    public ThreadTreeTool(Environment env) {
        super(new BorderLayout());
        this.env = env;
        this.runtime = env.getExecutionManager();
        this.sourceManager = env.getSourceManager();
        this.interpreter = new CommandInterpreter(env);
        root = createThreadTree(HEADING);
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
                        ThreadTreeNode node =
                            (ThreadTreeNode)selPath.getLastPathComponent();
                        if (node.isLeaf()) {
                            tree.setSelectionPath(selPath);
                            interpreter.executeCommand("thread " +
                                                       node.getThreadId() +
                                                       "  (\"" +
                                                       node.getName() + "\")");
                        }
                    }
                }
            }
        };
        tree.addMouseListener(ml);
        JScrollPane treeView = new JScrollPane(tree);
        add(treeView);
        ThreadTreeToolListener listener = new ThreadTreeToolListener();
        runtime.addJDIListener(listener);
        runtime.addSessionListener(listener);
    }
    HashMap<ThreadReference, List<String>> threadTable = new HashMap<ThreadReference, List<String>>();
    private List<String> threadPath(ThreadReference thread) {
        List<String> l = new ArrayList<String>();
        l.add(0, thread.name());
        ThreadGroupReference group = thread.threadGroup();
        while (group != null) {
            l.add(0, group.name());
            group = group.parent();
        }
        return l;
    }
    private class ThreadTreeToolListener extends JDIAdapter
                              implements JDIListener, SessionListener {
        @Override
        public void sessionStart(EventObject e) {
            try {
                for (ThreadReference thread : runtime.allThreads()) {
                    root.addThread(thread);
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
        public void threadStart(ThreadStartEventSet e) {
            root.addThread(e.getThread());
        }
        @Override
        public void threadDeath(ThreadDeathEventSet e) {
            root.removeThread(e.getThread());
        }
        @Override
        public void vmDisconnect(VMDisconnectEventSet e) {
            root = createThreadTree(HEADING);
            treeModel = new DefaultTreeModel(root);
            tree.setModel(treeModel);
            threadTable = new HashMap<ThreadReference, List<String>>();
        }
    }
    ThreadTreeNode createThreadTree(String label) {
        return new ThreadTreeNode(label, null);
    }
    class ThreadTreeNode extends DefaultMutableTreeNode {
        String name;
        ThreadReference thread; 
        long uid;
        String description;
        ThreadTreeNode(String name, ThreadReference thread) {
            if (name == null) {
                name = "<unnamed>";
            }
            this.name = name;
            this.thread = thread;
            if (thread == null) {
                this.uid = -1;
                this.description = name;
            } else {
                this.uid = thread.uniqueID();
                this.description = name + " (t@" + Long.toHexString(uid) + ")";
            }
        }
        @Override
        public String toString() {
            return description;
        }
        public String getName() {
            return name;
        }
        public ThreadReference getThread() {
            return thread;
        }
        public String getThreadId() {
            return "t@" + Long.toHexString(uid);
        }
        private boolean isThreadGroup() {
            return (thread == null);
        }
        @Override
        public boolean isLeaf() {
            return !isThreadGroup();
        }
        public void addThread(ThreadReference thread) {
            if (threadTable.get(thread) == null) {
                try {
                    List<String> path = threadPath(thread);
                    try {
                        threadTable.put(thread, path);
                        addThread(path, thread);
                    } catch (Throwable tt) {
                        throw new RuntimeException("ThreadTree corrupted");
                    }
                } catch (VMDisconnectedException ee) {
                }
            }
        }
        private void addThread(List<String> threadPath, ThreadReference thread) {
            int size = threadPath.size();
            if (size == 0) {
                return;
            } else if (size == 1) {
                String name = threadPath.get(0);
                insertNode(name, thread);
            } else {
                String head = threadPath.get(0);
                List<String> tail = threadPath.subList(1, size);
                ThreadTreeNode child = insertNode(head, null);
                child.addThread(tail, thread);
            }
        }
        private ThreadTreeNode insertNode(String name, ThreadReference thread) {
            for (int i = 0; i < getChildCount(); i++) {
                ThreadTreeNode child = (ThreadTreeNode)getChildAt(i);
                int cmp = name.compareTo(child.getName());
                if (cmp == 0 && thread == null) {
                    return child;
                } else if (cmp < 0) {
                    ThreadTreeNode newChild = new ThreadTreeNode(name, thread);
                    treeModel.insertNodeInto(newChild, this, i);
                    return newChild;
                }
            }
            ThreadTreeNode newChild = new ThreadTreeNode(name, thread);
            treeModel.insertNodeInto(newChild, this, getChildCount());
            return newChild;
        }
        public void removeThread(ThreadReference thread) {
            List<String> threadPath = threadTable.get(thread);
            if (threadPath != null) {
                removeThread(threadPath, thread);
            }
        }
        private void removeThread(List<String> threadPath, ThreadReference thread) {
            int size = threadPath.size();
            if (size == 0) {
                return;
            } else if (size == 1) {
                String name = threadPath.get(0);
                ThreadTreeNode child = findLeafNode(thread, name);
                treeModel.removeNodeFromParent(child);
            } else {
                String head = threadPath.get(0);
                List<String> tail = threadPath.subList(1, size);
                ThreadTreeNode child = findInternalNode(head);
                child.removeThread(tail, thread);
                if (child.isThreadGroup() && child.getChildCount() < 1) {
                    treeModel.removeNodeFromParent(child);
                }
            }
        }
        private ThreadTreeNode findLeafNode(ThreadReference thread, String name) {
            for (int i = 0; i < getChildCount(); i++) {
                ThreadTreeNode child = (ThreadTreeNode)getChildAt(i);
                if (child.getThread() == thread) {
                    if (!name.equals(child.getName())) {
                        throw new RuntimeException("name mismatch");
                    }
                    return child;
                }
            }
            throw new RuntimeException("not found");
        }
        private ThreadTreeNode findInternalNode(String name) {
            for (int i = 0; i < getChildCount(); i++) {
                ThreadTreeNode child = (ThreadTreeNode)getChildAt(i);
                if (name.equals(child.getName())) {
                    return child;
                }
            }
            throw new RuntimeException("not found");
        }
    }
}
