public class VariableHeightLayoutCache extends AbstractLayoutCache {
    private Vector<Object> visibleNodes;
    private boolean           updateNodeSizes;
    private TreeStateNode     root;
    private Rectangle         boundsBuffer;
    private Hashtable<TreePath, TreeStateNode> treePathMapping;
    private Stack<Stack<TreePath>> tempStacks;
    public VariableHeightLayoutCache() {
        super();
        tempStacks = new Stack<Stack<TreePath>>();
        visibleNodes = new Vector<Object>();
        boundsBuffer = new Rectangle();
        treePathMapping = new Hashtable<TreePath, TreeStateNode>();
    }
    public void setModel(TreeModel newModel) {
        super.setModel(newModel);
        rebuild(false);
    }
    public void setRootVisible(boolean rootVisible) {
        if(isRootVisible() != rootVisible && root != null) {
            if(rootVisible) {
                root.updatePreferredSize(0);
                visibleNodes.insertElementAt(root, 0);
            }
            else if(visibleNodes.size() > 0) {
                visibleNodes.removeElementAt(0);
                if(treeSelectionModel != null)
                    treeSelectionModel.removeSelectionPath
                        (root.getTreePath());
            }
            if(treeSelectionModel != null)
                treeSelectionModel.resetRowSelection();
            if(getRowCount() > 0)
                getNode(0).setYOrigin(0);
            updateYLocationsFrom(0);
            visibleNodesChanged();
        }
        super.setRootVisible(rootVisible);
    }
    public void setRowHeight(int rowHeight) {
        if(rowHeight != getRowHeight()) {
            super.setRowHeight(rowHeight);
            invalidateSizes();
            this.visibleNodesChanged();
        }
    }
    public void setNodeDimensions(NodeDimensions nd) {
        super.setNodeDimensions(nd);
        invalidateSizes();
        visibleNodesChanged();
    }
    public void setExpandedState(TreePath path, boolean isExpanded) {
        if(path != null) {
            if(isExpanded)
                ensurePathIsExpanded(path, true);
            else {
                TreeStateNode        node = getNodeForPath(path, false, true);
                if(node != null) {
                    node.makeVisible();
                    node.collapse();
                }
            }
        }
    }
    public boolean getExpandedState(TreePath path) {
        TreeStateNode       node = getNodeForPath(path, true, false);
        return (node != null) ? (node.isVisible() && node.isExpanded()) :
                                 false;
    }
    public Rectangle getBounds(TreePath path, Rectangle placeIn) {
        TreeStateNode       node = getNodeForPath(path, true, false);
        if(node != null) {
            if(updateNodeSizes)
                updateNodeSizes(false);
            return node.getNodeBounds(placeIn);
        }
        return null;
    }
    public TreePath getPathForRow(int row) {
        if(row >= 0 && row < getRowCount()) {
            return getNode(row).getTreePath();
        }
        return null;
    }
    public int getRowForPath(TreePath path) {
        if(path == null)
            return -1;
        TreeStateNode    visNode = getNodeForPath(path, true, false);
        if(visNode != null)
            return visNode.getRow();
        return -1;
    }
    public int getRowCount() {
        return visibleNodes.size();
    }
    public void invalidatePathBounds(TreePath path) {
        TreeStateNode       node = getNodeForPath(path, true, false);
        if(node != null) {
            node.markSizeInvalid();
            if(node.isVisible())
                updateYLocationsFrom(node.getRow());
        }
    }
    public int getPreferredHeight() {
        int           rowCount = getRowCount();
        if(rowCount > 0) {
            TreeStateNode  node = getNode(rowCount - 1);
            return node.getYOrigin() + node.getPreferredHeight();
        }
        return 0;
    }
    public int getPreferredWidth(Rectangle bounds) {
        if(updateNodeSizes)
            updateNodeSizes(false);
        return getMaxNodeWidth();
    }
    public TreePath getPathClosestTo(int x, int y) {
        if(getRowCount() == 0)
            return null;
        if(updateNodeSizes)
            updateNodeSizes(false);
        int                row = getRowContainingYLocation(y);
        return getNode(row).getTreePath();
    }
    public Enumeration<TreePath> getVisiblePathsFrom(TreePath path) {
        TreeStateNode       node = getNodeForPath(path, true, false);
        if(node != null) {
            return new VisibleTreeStateNodeEnumeration(node);
        }
        return null;
    }
    public int getVisibleChildCount(TreePath path) {
        TreeStateNode         node = getNodeForPath(path, true, false);
        return (node != null) ? node.getVisibleChildCount() : 0;
    }
    public void invalidateSizes() {
        if(root != null)
            root.deepMarkSizeInvalid();
        if(!isFixedRowHeight() && visibleNodes.size() > 0) {
            updateNodeSizes(true);
        }
    }
    public boolean isExpanded(TreePath path) {
        if(path != null) {
            TreeStateNode     lastNode = getNodeForPath(path, true, false);
            return (lastNode != null && lastNode.isExpanded());
        }
        return false;
    }
    public void treeNodesChanged(TreeModelEvent e) {
        if(e != null) {
            int               changedIndexs[];
            TreeStateNode     changedNode;
            changedIndexs = e.getChildIndices();
            changedNode = getNodeForPath(e.getTreePath(), false, false);
            if(changedNode != null) {
                Object            changedValue = changedNode.getValue();
                changedNode.updatePreferredSize();
                if(changedNode.hasBeenExpanded() && changedIndexs != null) {
                    int                counter;
                    TreeStateNode      changedChildNode;
                    for(counter = 0; counter < changedIndexs.length;
                        counter++) {
                        changedChildNode = (TreeStateNode)changedNode
                                    .getChildAt(changedIndexs[counter]);
                        changedChildNode.setUserObject
                                    (treeModel.getChild(changedValue,
                                                     changedIndexs[counter]));
                        changedChildNode.updatePreferredSize();
                    }
                }
                else if (changedNode == root) {
                    changedNode.updatePreferredSize();
                }
                if(!isFixedRowHeight()) {
                    int          aRow = changedNode.getRow();
                    if(aRow != -1)
                        this.updateYLocationsFrom(aRow);
                }
                this.visibleNodesChanged();
            }
        }
    }
    public void treeNodesInserted(TreeModelEvent e) {
        if(e != null) {
            int               changedIndexs[];
            TreeStateNode     changedParentNode;
            changedIndexs = e.getChildIndices();
            changedParentNode = getNodeForPath(e.getTreePath(), false, false);
            if(changedParentNode != null && changedIndexs != null &&
               changedIndexs.length > 0) {
                if(changedParentNode.hasBeenExpanded()) {
                    boolean            makeVisible;
                    int                counter;
                    Object             changedParent;
                    TreeStateNode      newNode;
                    int                oldChildCount = changedParentNode.
                                          getChildCount();
                    changedParent = changedParentNode.getValue();
                    makeVisible = ((changedParentNode == root &&
                                    !rootVisible) ||
                                   (changedParentNode.getRow() != -1 &&
                                    changedParentNode.isExpanded()));
                    for(counter = 0;counter < changedIndexs.length;counter++)
                    {
                        newNode = this.createNodeAt(changedParentNode,
                                                    changedIndexs[counter]);
                    }
                    if(oldChildCount == 0) {
                        changedParentNode.updatePreferredSize();
                    }
                    if(treeSelectionModel != null)
                        treeSelectionModel.resetRowSelection();
                    if(!isFixedRowHeight() && (makeVisible ||
                                               (oldChildCount == 0 &&
                                        changedParentNode.isVisible()))) {
                        if(changedParentNode == root)
                            this.updateYLocationsFrom(0);
                        else
                            this.updateYLocationsFrom(changedParentNode.
                                                      getRow());
                        this.visibleNodesChanged();
                    }
                    else if(makeVisible)
                        this.visibleNodesChanged();
                }
                else if(treeModel.getChildCount(changedParentNode.getValue())
                        - changedIndexs.length == 0) {
                    changedParentNode.updatePreferredSize();
                    if(!isFixedRowHeight() && changedParentNode.isVisible())
                        updateYLocationsFrom(changedParentNode.getRow());
                }
            }
        }
    }
    public void treeNodesRemoved(TreeModelEvent e) {
        if(e != null) {
            int               changedIndexs[];
            TreeStateNode     changedParentNode;
            changedIndexs = e.getChildIndices();
            changedParentNode = getNodeForPath(e.getTreePath(), false, false);
            if(changedParentNode != null && changedIndexs != null &&
               changedIndexs.length > 0) {
                if(changedParentNode.hasBeenExpanded()) {
                    boolean            makeInvisible;
                    int                counter;
                    int                removedRow;
                    TreeStateNode      removedNode;
                    makeInvisible = ((changedParentNode == root &&
                                      !rootVisible) ||
                                     (changedParentNode.getRow() != -1 &&
                                      changedParentNode.isExpanded()));
                    for(counter = changedIndexs.length - 1;counter >= 0;
                        counter--) {
                        removedNode = (TreeStateNode)changedParentNode.
                                getChildAt(changedIndexs[counter]);
                        if(removedNode.isExpanded()) {
                            removedNode.collapse(false);
                        }
                        if(makeInvisible) {
                            removedRow = removedNode.getRow();
                            if(removedRow != -1) {
                                visibleNodes.removeElementAt(removedRow);
                            }
                        }
                        changedParentNode.remove(changedIndexs[counter]);
                    }
                    if(changedParentNode.getChildCount() == 0) {
                        changedParentNode.updatePreferredSize();
                        if (changedParentNode.isExpanded() &&
                                   changedParentNode.isLeaf()) {
                            changedParentNode.collapse(false);
                        }
                    }
                    if(treeSelectionModel != null)
                        treeSelectionModel.resetRowSelection();
                    if(!isFixedRowHeight() && (makeInvisible ||
                               (changedParentNode.getChildCount() == 0 &&
                                changedParentNode.isVisible()))) {
                        if(changedParentNode == root) {
                            if(getRowCount() > 0)
                                getNode(0).setYOrigin(0);
                            updateYLocationsFrom(0);
                        }
                        else
                            updateYLocationsFrom(changedParentNode.getRow());
                        this.visibleNodesChanged();
                    }
                    else if(makeInvisible)
                        this.visibleNodesChanged();
                }
                else if(treeModel.getChildCount(changedParentNode.getValue())
                        == 0) {
                    changedParentNode.updatePreferredSize();
                    if(!isFixedRowHeight() && changedParentNode.isVisible())
                        this.updateYLocationsFrom(changedParentNode.getRow());
                }
            }
        }
    }
    public void treeStructureChanged(TreeModelEvent e) {
        if(e != null)
        {
            TreePath          changedPath = e.getTreePath();
            TreeStateNode     changedNode;
            changedNode = getNodeForPath(changedPath, false, false);
            if(changedNode == root ||
               (changedNode == null &&
                ((changedPath == null && treeModel != null &&
                  treeModel.getRoot() == null) ||
                 (changedPath != null && changedPath.getPathCount() == 1)))) {
                rebuild(true);
            }
            else if(changedNode != null) {
                int                              nodeIndex, oldRow;
                TreeStateNode                    newNode, parent;
                boolean                          wasExpanded, wasVisible;
                int                              newIndex;
                wasExpanded = changedNode.isExpanded();
                wasVisible = (changedNode.getRow() != -1);
                parent = (TreeStateNode)changedNode.getParent();
                nodeIndex = parent.getIndex(changedNode);
                if(wasVisible && wasExpanded) {
                    changedNode.collapse(false);
                }
                if(wasVisible)
                    visibleNodes.removeElement(changedNode);
                changedNode.removeFromParent();
                createNodeAt(parent, nodeIndex);
                newNode = (TreeStateNode)parent.getChildAt(nodeIndex);
                if(wasVisible && wasExpanded)
                    newNode.expand(false);
                newIndex = newNode.getRow();
                if(!isFixedRowHeight() && wasVisible) {
                    if(newIndex == 0)
                        updateYLocationsFrom(newIndex);
                    else
                        updateYLocationsFrom(newIndex - 1);
                    this.visibleNodesChanged();
                }
                else if(wasVisible)
                    this.visibleNodesChanged();
            }
        }
    }
    private void visibleNodesChanged() {
    }
    private void addMapping(TreeStateNode node) {
        treePathMapping.put(node.getTreePath(), node);
    }
    private void removeMapping(TreeStateNode node) {
        treePathMapping.remove(node.getTreePath());
    }
    private TreeStateNode getMapping(TreePath path) {
        return treePathMapping.get(path);
    }
    private Rectangle getBounds(int row, Rectangle placeIn) {
        if(updateNodeSizes)
            updateNodeSizes(false);
        if(row >= 0 && row < getRowCount()) {
            return getNode(row).getNodeBounds(placeIn);
        }
        return null;
    }
    private void rebuild(boolean clearSelection) {
        Object rootObject;
        treePathMapping.clear();
        if(treeModel != null && (rootObject = treeModel.getRoot()) != null) {
            root = createNodeForValue(rootObject);
            root.path = new TreePath(rootObject);
            addMapping(root);
            root.updatePreferredSize(0);
            visibleNodes.removeAllElements();
            if (isRootVisible())
                visibleNodes.addElement(root);
            if(!root.isExpanded())
                root.expand();
            else {
                Enumeration cursor = root.children();
                while(cursor.hasMoreElements()) {
                    visibleNodes.addElement(cursor.nextElement());
                }
                if(!isFixedRowHeight())
                    updateYLocationsFrom(0);
            }
        }
        else {
            visibleNodes.removeAllElements();
            root = null;
        }
        if(clearSelection && treeSelectionModel != null) {
            treeSelectionModel.clearSelection();
        }
        this.visibleNodesChanged();
    }
    private TreeStateNode createNodeAt(TreeStateNode parent,
                                         int childIndex) {
        boolean                isParentRoot;
        Object                 newValue;
        TreeStateNode          newChildNode;
        newValue = treeModel.getChild(parent.getValue(), childIndex);
        newChildNode = createNodeForValue(newValue);
        parent.insert(newChildNode, childIndex);
        newChildNode.updatePreferredSize(-1);
        isParentRoot = (parent == root);
        if(newChildNode != null && parent.isExpanded() &&
           (parent.getRow() != -1 || isParentRoot)) {
            int                 newRow;
            if(childIndex == 0) {
                if(isParentRoot && !isRootVisible())
                    newRow = 0;
                else
                    newRow = parent.getRow() + 1;
            }
            else if(childIndex == parent.getChildCount())
                newRow = parent.getLastVisibleNode().getRow() + 1;
            else {
                TreeStateNode          previousNode;
                previousNode = (TreeStateNode)parent.
                    getChildAt(childIndex - 1);
                newRow = previousNode.getLastVisibleNode().getRow() + 1;
            }
            visibleNodes.insertElementAt(newChildNode, newRow);
        }
        return newChildNode;
    }
    private TreeStateNode getNodeForPath(TreePath path,
                                           boolean onlyIfVisible,
                                           boolean shouldCreate) {
        if(path != null) {
            TreeStateNode      node;
            node = getMapping(path);
            if(node != null) {
                if(onlyIfVisible && !node.isVisible())
                    return null;
                return node;
            }
            Stack<TreePath> paths;
            if(tempStacks.size() == 0) {
                paths = new Stack<TreePath>();
            }
            else {
                paths = tempStacks.pop();
            }
            try {
                paths.push(path);
                path = path.getParentPath();
                node = null;
                while(path != null) {
                    node = getMapping(path);
                    if(node != null) {
                        while(node != null && paths.size() > 0) {
                            path = paths.pop();
                            node.getLoadedChildren(shouldCreate);
                            int            childIndex = treeModel.
                                      getIndexOfChild(node.getUserObject(),
                                                  path.getLastPathComponent());
                            if(childIndex == -1 ||
                               childIndex >= node.getChildCount() ||
                               (onlyIfVisible && !node.isVisible())) {
                                node = null;
                            }
                            else
                                node = (TreeStateNode)node.getChildAt
                                               (childIndex);
                        }
                        return node;
                    }
                    paths.push(path);
                    path = path.getParentPath();
                }
            }
            finally {
                paths.removeAllElements();
                tempStacks.push(paths);
            }
        }
        return null;
    }
    private void updateYLocationsFrom(int location) {
        if(location >= 0 && location < getRowCount()) {
            int                    counter, maxCounter, newYOrigin;
            TreeStateNode          aNode;
            aNode = getNode(location);
            newYOrigin = aNode.getYOrigin() + aNode.getPreferredHeight();
            for(counter = location + 1, maxCounter = visibleNodes.size();
                counter < maxCounter;counter++) {
                aNode = (TreeStateNode)visibleNodes.
                    elementAt(counter);
                aNode.setYOrigin(newYOrigin);
                newYOrigin += aNode.getPreferredHeight();
            }
        }
    }
    private void updateNodeSizes(boolean updateAll) {
        int                      aY, counter, maxCounter;
        TreeStateNode            node;
        updateNodeSizes = false;
        for(aY = counter = 0, maxCounter = visibleNodes.size();
            counter < maxCounter; counter++) {
            node = (TreeStateNode)visibleNodes.elementAt(counter);
            node.setYOrigin(aY);
            if(updateAll || !node.hasValidSize())
                node.updatePreferredSize(counter);
            aY += node.getPreferredHeight();
        }
    }
    private int getRowContainingYLocation(int location) {
        if(isFixedRowHeight()) {
            if(getRowCount() == 0)
                return -1;
            return Math.max(0, Math.min(getRowCount() - 1,
                                        location / getRowHeight()));
        }
        int                    max, maxY, mid, min, minY;
        TreeStateNode          node;
        if((max = getRowCount()) <= 0)
            return -1;
        mid = min = 0;
        while(min < max) {
            mid = (max - min) / 2 + min;
            node = (TreeStateNode)visibleNodes.elementAt(mid);
            minY = node.getYOrigin();
            maxY = minY + node.getPreferredHeight();
            if(location < minY) {
                max = mid - 1;
            }
            else if(location >= maxY) {
                min = mid + 1;
            }
            else
                break;
        }
        if(min == max) {
            mid = min;
            if(mid >= getRowCount())
                mid = getRowCount() - 1;
        }
        return mid;
    }
    private void ensurePathIsExpanded(TreePath aPath, boolean expandLast) {
        if(aPath != null) {
            if(treeModel.isLeaf(aPath.getLastPathComponent())) {
                aPath = aPath.getParentPath();
                expandLast = true;
            }
            if(aPath != null) {
                TreeStateNode     lastNode = getNodeForPath(aPath, false,
                                                            true);
                if(lastNode != null) {
                    lastNode.makeVisible();
                    if(expandLast)
                        lastNode.expand();
                }
            }
        }
    }
    private TreeStateNode getNode(int row) {
        return (TreeStateNode)visibleNodes.elementAt(row);
    }
    private int getMaxNodeWidth() {
        int                     maxWidth = 0;
        int                     nodeWidth;
        int                     counter;
        TreeStateNode           node;
        for(counter = getRowCount() - 1;counter >= 0;counter--) {
            node = this.getNode(counter);
            nodeWidth = node.getPreferredWidth() + node.getXOrigin();
            if(nodeWidth > maxWidth)
                maxWidth = nodeWidth;
        }
        return maxWidth;
    }
    private TreeStateNode createNodeForValue(Object value) {
        return new TreeStateNode(value);
    }
    private class TreeStateNode extends DefaultMutableTreeNode {
        protected int             preferredWidth;
        protected int             preferredHeight;
        protected int             xOrigin;
        protected int             yOrigin;
        protected boolean         expanded;
        protected boolean         hasBeenExpanded;
        protected TreePath        path;
        public TreeStateNode(Object value) {
            super(value);
        }
        public void setParent(MutableTreeNode parent) {
            super.setParent(parent);
            if(parent != null) {
                path = ((TreeStateNode)parent).getTreePath().
                                       pathByAddingChild(getUserObject());
                addMapping(this);
            }
        }
        public void remove(int childIndex) {
            TreeStateNode     node = (TreeStateNode)getChildAt(childIndex);
            node.removeFromMapping();
            super.remove(childIndex);
        }
        public void setUserObject(Object o) {
            super.setUserObject(o);
            if(path != null) {
                TreeStateNode      parent = (TreeStateNode)getParent();
                if(parent != null)
                    resetChildrenPaths(parent.getTreePath());
                else
                    resetChildrenPaths(null);
            }
        }
        public Enumeration children() {
            if (!this.isExpanded()) {
                return DefaultMutableTreeNode.EMPTY_ENUMERATION;
            } else {
                return super.children();
            }
        }
        public boolean isLeaf() {
            return getModel().isLeaf(this.getValue());
        }
        public Rectangle getNodeBounds(Rectangle placeIn) {
            if(placeIn == null)
                placeIn = new Rectangle(getXOrigin(), getYOrigin(),
                                        getPreferredWidth(),
                                        getPreferredHeight());
            else {
                placeIn.x = getXOrigin();
                placeIn.y = getYOrigin();
                placeIn.width = getPreferredWidth();
                placeIn.height = getPreferredHeight();
            }
            return placeIn;
        }
        public int getXOrigin() {
            if(!hasValidSize())
                updatePreferredSize(getRow());
            return xOrigin;
        }
        public int getYOrigin() {
            if(isFixedRowHeight()) {
                int      aRow = getRow();
                if(aRow == -1)
                    return -1;
                return getRowHeight() * aRow;
            }
            return yOrigin;
        }
        public int getPreferredHeight() {
            if(isFixedRowHeight())
                return getRowHeight();
            else if(!hasValidSize())
                updatePreferredSize(getRow());
            return preferredHeight;
        }
        public int getPreferredWidth() {
            if(!hasValidSize())
                updatePreferredSize(getRow());
            return preferredWidth;
        }
        public boolean hasValidSize() {
            return (preferredHeight != 0);
        }
        public int getRow() {
            return visibleNodes.indexOf(this);
        }
        public boolean hasBeenExpanded() {
            return hasBeenExpanded;
        }
        public boolean isExpanded() {
            return expanded;
        }
        public TreeStateNode getLastVisibleNode() {
            TreeStateNode                node = this;
            while(node.isExpanded() && node.getChildCount() > 0)
                node = (TreeStateNode)node.getLastChild();
            return node;
        }
        public boolean isVisible() {
            if(this == root)
                return true;
            TreeStateNode        parent = (TreeStateNode)getParent();
            return (parent != null && parent.isExpanded() &&
                    parent.isVisible());
        }
        public int getModelChildCount() {
            if(hasBeenExpanded)
                return super.getChildCount();
            return getModel().getChildCount(getValue());
        }
        public int getVisibleChildCount() {
            int               childCount = 0;
            if(isExpanded()) {
                int         maxCounter = getChildCount();
                childCount += maxCounter;
                for(int counter = 0; counter < maxCounter; counter++)
                    childCount += ((TreeStateNode)getChildAt(counter)).
                                    getVisibleChildCount();
            }
            return childCount;
        }
        public void toggleExpanded() {
            if (isExpanded()) {
                collapse();
            } else {
                expand();
            }
        }
        public void makeVisible() {
            TreeStateNode       parent = (TreeStateNode)getParent();
            if(parent != null)
                parent.expandParentAndReceiver();
        }
        public void expand() {
            expand(true);
        }
        public void collapse() {
            collapse(true);
        }
        public Object getValue() {
            return getUserObject();
        }
        public TreePath getTreePath() {
            return path;
        }
        protected void resetChildrenPaths(TreePath parentPath) {
            removeMapping(this);
            if(parentPath == null)
                path = new TreePath(getUserObject());
            else
                path = parentPath.pathByAddingChild(getUserObject());
            addMapping(this);
            for(int counter = getChildCount() - 1; counter >= 0; counter--)
                ((TreeStateNode)getChildAt(counter)).resetChildrenPaths(path);
        }
        protected void setYOrigin(int newYOrigin) {
            yOrigin = newYOrigin;
        }
        protected void shiftYOriginBy(int offset) {
            yOrigin += offset;
        }
        protected void updatePreferredSize() {
            updatePreferredSize(getRow());
        }
        protected void updatePreferredSize(int index) {
            Rectangle       bounds = getNodeDimensions(this.getUserObject(),
                                                       index, getLevel(),
                                                       isExpanded(),
                                                       boundsBuffer);
            if(bounds == null) {
                xOrigin = 0;
                preferredWidth = preferredHeight = 0;
                updateNodeSizes = true;
            }
            else if(bounds.height == 0) {
                xOrigin = 0;
                preferredWidth = preferredHeight = 0;
                updateNodeSizes = true;
            }
            else {
                xOrigin = bounds.x;
                preferredWidth = bounds.width;
                if(isFixedRowHeight())
                    preferredHeight = getRowHeight();
                else
                    preferredHeight = bounds.height;
            }
        }
        protected void markSizeInvalid() {
            preferredHeight = 0;
        }
        protected void deepMarkSizeInvalid() {
            markSizeInvalid();
            for(int counter = getChildCount() - 1; counter >= 0; counter--)
                ((TreeStateNode)getChildAt(counter)).deepMarkSizeInvalid();
        }
        protected Enumeration getLoadedChildren(boolean createIfNeeded) {
            if(!createIfNeeded || hasBeenExpanded)
                return super.children();
            TreeStateNode   newNode;
            Object          realNode = getValue();
            TreeModel       treeModel = getModel();
            int             count = treeModel.getChildCount(realNode);
            hasBeenExpanded = true;
            int    childRow = getRow();
            if(childRow == -1) {
                for (int i = 0; i < count; i++) {
                    newNode = createNodeForValue
                        (treeModel.getChild(realNode, i));
                    this.add(newNode);
                    newNode.updatePreferredSize(-1);
                }
            }
            else {
                childRow++;
                for (int i = 0; i < count; i++) {
                    newNode = createNodeForValue
                        (treeModel.getChild(realNode, i));
                    this.add(newNode);
                    newNode.updatePreferredSize(childRow++);
                }
            }
            return super.children();
        }
        protected void didAdjustTree() {
        }
        protected void expandParentAndReceiver() {
            TreeStateNode       parent = (TreeStateNode)getParent();
            if(parent != null)
                parent.expandParentAndReceiver();
            expand();
        }
        protected void expand(boolean adjustTree) {
            if (!isExpanded() && !isLeaf()) {
                boolean         isFixed = isFixedRowHeight();
                int             startHeight = getPreferredHeight();
                int             originalRow = getRow();
                expanded = true;
                updatePreferredSize(originalRow);
                if (!hasBeenExpanded) {
                    TreeStateNode  newNode;
                    Object         realNode = getValue();
                    TreeModel      treeModel = getModel();
                    int            count = treeModel.getChildCount(realNode);
                    hasBeenExpanded = true;
                    if(originalRow == -1) {
                        for (int i = 0; i < count; i++) {
                            newNode = createNodeForValue(treeModel.getChild
                                                            (realNode, i));
                            this.add(newNode);
                            newNode.updatePreferredSize(-1);
                        }
                    }
                    else {
                        int offset = originalRow + 1;
                        for (int i = 0; i < count; i++) {
                            newNode = createNodeForValue(treeModel.getChild
                                                       (realNode, i));
                            this.add(newNode);
                            newNode.updatePreferredSize(offset);
                        }
                    }
                }
                int i = originalRow;
                Enumeration cursor = preorderEnumeration();
                cursor.nextElement(); 
                int newYOrigin;
                if(isFixed)
                    newYOrigin = 0;
                else if(this == root && !isRootVisible())
                    newYOrigin = 0;
                else
                    newYOrigin = getYOrigin() + this.getPreferredHeight();
                TreeStateNode   aNode;
                if(!isFixed) {
                    while (cursor.hasMoreElements()) {
                        aNode = (TreeStateNode)cursor.nextElement();
                        if(!updateNodeSizes && !aNode.hasValidSize())
                            aNode.updatePreferredSize(i + 1);
                        aNode.setYOrigin(newYOrigin);
                        newYOrigin += aNode.getPreferredHeight();
                        visibleNodes.insertElementAt(aNode, ++i);
                    }
                }
                else {
                    while (cursor.hasMoreElements()) {
                        aNode = (TreeStateNode)cursor.nextElement();
                        visibleNodes.insertElementAt(aNode, ++i);
                    }
                }
                if(adjustTree && (originalRow != i ||
                                  getPreferredHeight() != startHeight)) {
                    if(!isFixed && ++i < getRowCount()) {
                        int              counter;
                        int              heightDiff = newYOrigin -
                            (getYOrigin() + getPreferredHeight()) +
                            (getPreferredHeight() - startHeight);
                        for(counter = visibleNodes.size() - 1;counter >= i;
                            counter--)
                            ((TreeStateNode)visibleNodes.elementAt(counter)).
                                shiftYOriginBy(heightDiff);
                    }
                    didAdjustTree();
                    visibleNodesChanged();
                }
                if(treeSelectionModel != null) {
                    treeSelectionModel.resetRowSelection();
                }
            }
        }
        protected void collapse(boolean adjustTree) {
            if (isExpanded()) {
                Enumeration cursor = preorderEnumeration();
                cursor.nextElement(); 
                int rowsDeleted = 0;
                boolean isFixed = isFixedRowHeight();
                int lastYEnd;
                if(isFixed)
                    lastYEnd = 0;
                else
                    lastYEnd = getPreferredHeight() + getYOrigin();
                int startHeight = getPreferredHeight();
                int startYEnd = lastYEnd;
                int myRow = getRow();
                if(!isFixed) {
                    while(cursor.hasMoreElements()) {
                        TreeStateNode node = (TreeStateNode)cursor.
                            nextElement();
                        if (node.isVisible()) {
                            rowsDeleted++;
                            lastYEnd = node.getYOrigin() +
                                node.getPreferredHeight();
                        }
                    }
                }
                else {
                    while(cursor.hasMoreElements()) {
                        TreeStateNode node = (TreeStateNode)cursor.
                            nextElement();
                        if (node.isVisible()) {
                            rowsDeleted++;
                        }
                    }
                }
                for (int counter = rowsDeleted + myRow; counter > myRow;
                     counter--) {
                    visibleNodes.removeElementAt(counter);
                }
                expanded = false;
                if(myRow == -1)
                    markSizeInvalid();
                else if (adjustTree)
                    updatePreferredSize(myRow);
                if(myRow != -1 && adjustTree &&
                   (rowsDeleted > 0 || startHeight != getPreferredHeight())) {
                    startYEnd += (getPreferredHeight() - startHeight);
                    if(!isFixed && (myRow + 1) < getRowCount() &&
                       startYEnd != lastYEnd) {
                        int                 counter, maxCounter, shiftAmount;
                        shiftAmount = startYEnd - lastYEnd;
                        for(counter = myRow + 1, maxCounter =
                                visibleNodes.size();
                            counter < maxCounter;counter++)
                            ((TreeStateNode)visibleNodes.elementAt(counter))
                                .shiftYOriginBy(shiftAmount);
                    }
                    didAdjustTree();
                    visibleNodesChanged();
                }
                if(treeSelectionModel != null && rowsDeleted > 0 &&
                   myRow != -1) {
                    treeSelectionModel.resetRowSelection();
                }
            }
        }
        protected void removeFromMapping() {
            if(path != null) {
                removeMapping(this);
                for(int counter = getChildCount() - 1; counter >= 0; counter--)
                    ((TreeStateNode)getChildAt(counter)).removeFromMapping();
            }
        }
    } 
    private class VisibleTreeStateNodeEnumeration implements
                     Enumeration<TreePath> {
        protected TreeStateNode       parent;
        protected int                 nextIndex;
        protected int                 childCount;
        protected VisibleTreeStateNodeEnumeration(TreeStateNode node) {
            this(node, -1);
        }
        protected VisibleTreeStateNodeEnumeration(TreeStateNode parent,
                                                  int startIndex) {
            this.parent = parent;
            this.nextIndex = startIndex;
            this.childCount = this.parent.getChildCount();
        }
        public boolean hasMoreElements() {
            return (parent != null);
        }
        public TreePath nextElement() {
            if(!hasMoreElements())
                throw new NoSuchElementException("No more visible paths");
            TreePath                retObject;
            if(nextIndex == -1) {
                retObject = parent.getTreePath();
            }
            else {
                TreeStateNode   node = (TreeStateNode)parent.
                                        getChildAt(nextIndex);
                retObject = node.getTreePath();
            }
            updateNextObject();
            return retObject;
        }
        protected void updateNextObject() {
            if(!updateNextIndex()) {
                findNextValidParent();
            }
        }
        protected boolean findNextValidParent() {
            if(parent == root) {
                parent = null;
                return false;
            }
            while(parent != null) {
                TreeStateNode      newParent = (TreeStateNode)parent.
                                                  getParent();
                if(newParent != null) {
                    nextIndex = newParent.getIndex(parent);
                    parent = newParent;
                    childCount = parent.getChildCount();
                    if(updateNextIndex())
                        return true;
                }
                else
                    parent = null;
            }
            return false;
        }
        protected boolean updateNextIndex() {
            if(nextIndex == -1 && !parent.isExpanded())
                return false;
            if(childCount == 0)
                return false;
            else if(++nextIndex >= childCount)
                return false;
            TreeStateNode       child = (TreeStateNode)parent.
                                        getChildAt(nextIndex);
            if(child != null && child.isExpanded()) {
                parent = child;
                nextIndex = -1;
                childCount = child.getChildCount();
            }
            return true;
        }
    } 
}
