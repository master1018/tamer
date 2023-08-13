public class DirectoryTree {
    private Node rootNode;
    private Vector subdirsToIgnore;
    private Hashtable nameToNodeListTable;
    private boolean verbose;
    public DirectoryTree() {
        subdirsToIgnore = new Vector();
        verbose = false;
    }
    public void addSubdirToIgnore(String subdir) {
        subdirsToIgnore.add(subdir);
    }
    private class FileIterator implements Iterator {
        private Vector nodes = new Vector();
        public FileIterator(Node rootNode) {
            if(rootNode == null) {
                return;
            }
            nodes.add(rootNode);
            prune();
        }
        public boolean hasNext() {
            return nodes.size() > 0;
        }
        public Object next() {
            Node last = (Node)nodes.remove(nodes.size() - 1);
            prune();
            return new File(last.getName());
        }
        public void remove() {
            throw new RuntimeException();
        }
        private void prune() {
            while (nodes.size() > 0) {
                Node last = (Node)nodes.get(nodes.size() - 1);
                if (last.isDirectory()) {
                    nodes.remove(nodes.size() - 1);
                    nodes.addAll(last.children);
                } else {
                    return;
                }
            }
        }
    }
    public Iterator getFileIterator() {
        return new FileIterator(rootNode);
    }
    public void setVerbose(boolean newValue) {
        verbose = newValue;
    }
    public boolean getVerbose() {
        return verbose;
    }
    public String getRootNodeName() {
        return rootNode.getName();
    }
    public void readDirectory(String baseDirectory)
        throws IllegalArgumentException {
        File root = new File(Util.normalize(baseDirectory));
        if (!root.isDirectory()) {
            return;
        }
        try {
            root = root.getCanonicalFile();
        }
        catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
        rootNode = new Node(root);
        readDirectory(rootNode, root);
    }
    public List findFile(String name) {
        if (rootNode == null) {
            return null;
        }
        if (nameToNodeListTable == null) {
            nameToNodeListTable = new Hashtable();
            try {
                buildNameToNodeListTable(rootNode);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return (List) nameToNodeListTable.get(name);
    }
    private void buildNameToNodeListTable(Node curNode)
      throws IOException {
        String fullName = curNode.getName();
        String parent = curNode.getParent();
        String separator = System.getProperty("file.separator");
        if (parent != null) {
          if (!fullName.startsWith(parent)) {
            throw new RuntimeException(
                "Internal error: parent of file name \"" + fullName +
                "\" does not match file name \"" + parent + "\""
            );
          }
          int len = parent.length();
          if (!parent.endsWith(separator)) {
            len += separator.length();
          }
          String fileName = fullName.substring(len);
          if (fileName == null) {
            throw new RuntimeException(
                "Internal error: file name was empty"
            );
          }
          List nodeList = (List) nameToNodeListTable.get(fileName);
          if (nodeList == null) {
            nodeList = new Vector();
            nameToNodeListTable.put(fileName, nodeList);
          }
          nodeList.add(curNode);
        } else {
          if (curNode != rootNode) {
            throw new RuntimeException(
                "Internal error: parent of file + \"" + fullName + "\"" +
                " was null"
            );
          }
        }
        if (curNode.isDirectory()) {
          Iterator iter = curNode.getChildren();
          if (iter != null) {
            while (iter.hasNext()) {
              buildNameToNodeListTable((Node) iter.next());
            }
          }
        }
    }
    private void readDirectory(Node parentNode, File parentDir) {
        File[] children = parentDir.listFiles();
        if (children == null)
            return;
        if (verbose) {
            System.out.print(".");
            System.out.flush();
        }
        for (int i = 0; i < children.length; i++) {
            File child = children[i];
            children[i] = null;
            boolean isDir = child.isDirectory();
            boolean mustSkip = false;
            if (isDir) {
                for (Iterator iter = subdirsToIgnore.iterator();
                     iter.hasNext(); ) {
                    if (child.getName().equals((String) iter.next())) {
                        mustSkip = true;
                        break;
                    }
                }
            }
            if (!mustSkip) {
                Node childNode = new Node(child);
                parentNode.addChild(childNode);
                if (isDir) {
                    readDirectory(childNode, child);
                }
            }
        }
    }
    private class Node implements DirectoryTreeNode {
        private File file;
        private Vector children;
        Node(File file) {
            this.file = file;
            children = new Vector();
        }
        public boolean isFile() {
            return file.isFile();
        }
        public boolean isDirectory() {
            return file.isDirectory();
        }
        public String getName() {
            return file.getPath();
        }
        public String getParent() {
            return file.getParent();
        }
        public void addChild(Node n) {
            children.add(n);
        }
        public Iterator getChildren() throws IllegalArgumentException {
            return children.iterator();
        }
        public int getNumChildren() throws IllegalArgumentException {
            return children.size();
        }
        public DirectoryTreeNode getChild(int i)
            throws IllegalArgumentException, ArrayIndexOutOfBoundsException {
            return (DirectoryTreeNode) children.get(i);
        }
    }
}
