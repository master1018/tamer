public class FixedHeightLayoutCache extends AbstractLayoutCache {
    private FHTreeStateNode    root;
    private int                rowCount;
    private Rectangle          boundsBuffer;
    private Hashtable<TreePath, FHTreeStateNode> treePathMapping;
    private SearchInfo         info;
    private Stack<Stack<TreePath>> tempStacks;
    public FixedHeightLayoutCache() {
        super();
        tempStacks = new Stack<Stack<TreePath>>();
        boundsBuffer = new Rectangle();
        treePathMapping = new Hashtable<TreePath, FHTreeStateNode>();
        info = new SearchInfo();
        setRowHeight(1);
    }
    public void setModel(TreeModel newModel) {
        super.setModel(newModel);
        rebuild(false);
    }
    public void setRootVisible(boolean rootVisible) {
        if(isRootVisible() != rootVisible) {
            super.setRootVisible(rootVisible);
            if(root != null) {
                if(rootVisible) {
                    rowCount++;
                    root.adjustRowBy(1);
                }
                else {
                    rowCount--;
                    root.adjustRowBy(-1);
                }
                visibleNodesChanged();
            }
        }
    }
    public void setRowHeight(int rowHeight) {
        if(rowHeight <= 0)
            throw new IllegalArgumentException("FixedHeightLayoutCache only supports row heights greater than 0");
        if(getRowHeight() != rowHeight) {
            super.setRowHeight(rowHeight);
            visibleNodesChanged();
        }
    }
    public int getRowCount() {
        return rowCount;
    }
    public void invalidatePathBounds(TreePath path) {
    }
    public void invalidateSizes() {
        visibleNodesChanged();
    }
    public boolean isExpanded(TreePath path) {
        if(path != null) {
            FHTreeStateNode     lastNode = getNodeForPath(path, true, false);
            return (lastNode != null && lastNode.isExpanded());
        }
        return false;
    }
    public Rectangle getBounds(TreePath path, Rectangle placeIn) {
        if(path == null)
            return null;
        FHTreeStateNode      node = getNodeForPath(path, true, false);
        if(node != null)
            return getBounds(node, -1, placeIn);
        TreePath       parentPath = path.getParentPath();
        node = getNodeForPath(parentPath, true, false);
        if (node != null && node.isExpanded()) {
            int              childIndex = treeModel.getIndexOfChild
                                 (parentPath.getLastPathComponent(),
                                  path.getLastPathComponent());
            if(childIndex != -1)
                return getBounds(node, childIndex, placeIn);
        }
        return null;
    }
    public TreePath getPathForRow(int row) {
        if(row >= 0 && row < getRowCount()) {
            if(root.getPathForRow(row, getRowCount(), info)) {
                return info.getPath();
            }
        }
        return null;
    }
    public int getRowForPath(TreePath path) {
        if(path == null || root == null)
            return -1;
        FHTreeStateNode         node = getNodeForPath(path, true, false);
        if(node != null)
            return node.getRow();
        TreePath       parentPath = path.getParentPath();
        node = getNodeForPath(parentPath, true, false);
        if(node != null && node.isExpanded()) {
            return node.getRowToModelIndex(treeModel.getIndexOfChild
                                           (parentPath.getLastPathComponent(),
                                            path.getLastPathComponent()));
        }
        return -1;
    }
    public TreePath getPathClosestTo(int x, int y) {
        if(getRowCount() == 0)
            return null;
        int                row = getRowContainingYLocation(y);
        return getPathForRow(row);
    }
    public int getVisibleChildCount(TreePath path) {
        FHTreeStateNode         node = getNodeForPath(path, true, false);
        if(node == null)
            return 0;
        return node.getTotalChildCount();
    }
    public Enumeration<TreePath> getVisiblePathsFrom(TreePath path) {
        if(path == null)
            return null;
        FHTreeStateNode         node = getNodeForPath(path, true, false);
        if(node != null) {
            return new VisibleFHTreeStateNodeEnumeration(node);
        }
        TreePath            parentPath = path.getParentPath();
        node = getNodeForPath(parentPath, true, false);
        if(node != null && node.isExpanded()) {
            return new VisibleFHTreeStateNodeEnumeration(node,
                  treeModel.getIndexOfChild(parentPath.getLastPathComponent(),
                                            path.getLastPathComponent()));
        }
        return null;
    }
    public void setExpandedState(TreePath path, boolean isExpanded) {
        if(isExpanded)
            ensurePathIsExpanded(path, true);
        else if(path != null) {
            TreePath              parentPath = path.getParentPath();
            if(parentPath != null) {
                FHTreeStateNode     parentNode = getNodeForPath(parentPath,
                                                                false, true);
                if(parentNode != null)
                    parentNode.makeVisible();
            }
            FHTreeStateNode         childNode = getNodeForPath(path, true,
                                                               false);
            if(childNode != null)
                childNode.collapse(true);
        }
    }
    public boolean getExpandedState(TreePath path) {
        FHTreeStateNode       node = getNodeForPath(path, true, false);
        return (node != null) ? (node.isVisible() && node.isExpanded()) :
                                 false;
    }
    public void treeNodesChanged(TreeModelEvent e) {
        if(e != null) {
            int                 changedIndexs[];
            FHTreeStateNode     changedParent = getNodeForPath
                                  (e.getTreePath(), false, false);
            int                 maxCounter;
            changedIndexs = e.getChildIndices();
            if (changedParent != null) {
                if (changedIndexs != null &&
                    (maxCounter = changedIndexs.length) > 0) {
                    Object       parentValue = changedParent.getUserObject();
                    for(int counter = 0; counter < maxCounter; counter++) {
                        FHTreeStateNode    child = changedParent.
                                 getChildAtModelIndex(changedIndexs[counter]);
                        if(child != null) {
                            child.setUserObject(treeModel.getChild(parentValue,
                                                     changedIndexs[counter]));
                        }
                    }
                    if(changedParent.isVisible() && changedParent.isExpanded())
                        visibleNodesChanged();
                }
                else if (changedParent == root && changedParent.isVisible() &&
                         changedParent.isExpanded()) {
                    visibleNodesChanged();
                }
            }
        }
    }
    public void treeNodesInserted(TreeModelEvent e) {
        if(e != null) {
            int                 changedIndexs[];
            FHTreeStateNode     changedParent = getNodeForPath
                                  (e.getTreePath(), false, false);
            int                 maxCounter;
            changedIndexs = e.getChildIndices();
            if(changedParent != null && changedIndexs != null &&
               (maxCounter = changedIndexs.length) > 0) {
                boolean          isVisible =
                    (changedParent.isVisible() &&
                     changedParent.isExpanded());
                for(int counter = 0; counter < maxCounter; counter++) {
                    changedParent.childInsertedAtModelIndex
                        (changedIndexs[counter], isVisible);
                }
                if(isVisible && treeSelectionModel != null)
                    treeSelectionModel.resetRowSelection();
                if(changedParent.isVisible())
                    this.visibleNodesChanged();
            }
        }
    }
    public void treeNodesRemoved(TreeModelEvent e) {
        if(e != null) {
            int                  changedIndexs[];
            int                  maxCounter;
            TreePath             parentPath = e.getTreePath();
            FHTreeStateNode      changedParentNode = getNodeForPath
                                       (parentPath, false, false);
            changedIndexs = e.getChildIndices();
            if(changedParentNode != null && changedIndexs != null &&
               (maxCounter = changedIndexs.length) > 0) {
                Object[]           children = e.getChildren();
                boolean            isVisible =
                    (changedParentNode.isVisible() &&
                     changedParentNode.isExpanded());
                for(int counter = maxCounter - 1; counter >= 0; counter--) {
                    changedParentNode.removeChildAtModelIndex
                                     (changedIndexs[counter], isVisible);
                }
                if(isVisible) {
                    if(treeSelectionModel != null)
                        treeSelectionModel.resetRowSelection();
                    if (treeModel.getChildCount(changedParentNode.
                                                getUserObject()) == 0 &&
                                  changedParentNode.isLeaf()) {
                        changedParentNode.collapse(false);
                    }
                    visibleNodesChanged();
                }
                else if(changedParentNode.isVisible())
                    visibleNodesChanged();
            }
        }
    }
    public void treeStructureChanged(TreeModelEvent e) {
        if(e != null) {
            TreePath          changedPath = e.getTreePath();
            FHTreeStateNode   changedNode = getNodeForPath
                                                (changedPath, false, false);
            if (changedNode == root ||
                (changedNode == null &&
                 ((changedPath == null && treeModel != null &&
                   treeModel.getRoot() == null) ||
                  (changedPath != null && changedPath.getPathCount() <= 1)))) {
                rebuild(true);
            }
            else if(changedNode != null) {
                boolean             wasExpanded, wasVisible;
                FHTreeStateNode     parent = (FHTreeStateNode)
                                              changedNode.getParent();
                wasExpanded = changedNode.isExpanded();
                wasVisible = changedNode.isVisible();
                int index = parent.getIndex(changedNode);
                changedNode.collapse(false);
                parent.remove(index);
                if(wasVisible && wasExpanded) {
                    int row = changedNode.getRow();
                    parent.resetChildrenRowsFrom(row, index,
                                                 changedNode.getChildIndex());
                    changedNode = getNodeForPath(changedPath, false, true);
                    changedNode.expand();
                }
                if(treeSelectionModel != null && wasVisible && wasExpanded)
                    treeSelectionModel.resetRowSelection();
                if(wasVisible)
                    this.visibleNodesChanged();
            }
        }
    }
    private void visibleNodesChanged() {
    }
    private Rectangle getBounds(FHTreeStateNode parent, int childIndex,
                                  Rectangle placeIn) {
        boolean              expanded;
        int                  level;
        int                  row;
        Object               value;
        if(childIndex == -1) {
            row = parent.getRow();
            value = parent.getUserObject();
            expanded = parent.isExpanded();
            level = parent.getLevel();
        }
        else {
            row = parent.getRowToModelIndex(childIndex);
            value = treeModel.getChild(parent.getUserObject(), childIndex);
            expanded = false;
            level = parent.getLevel() + 1;
        }
        Rectangle      bounds = getNodeDimensions(value, row, level,
                                                  expanded, boundsBuffer);
        if(bounds == null)
            return null;
        if(placeIn == null)
            placeIn = new Rectangle();
        placeIn.x = bounds.x;
        placeIn.height = getRowHeight();
        placeIn.y = row * placeIn.height;
        placeIn.width = bounds.width;
        return placeIn;
    }
    private void adjustRowCountBy(int changeAmount) {
        rowCount += changeAmount;
    }
    private void addMapping(FHTreeStateNode node) {
        treePathMapping.put(node.getTreePath(), node);
    }
    private void removeMapping(FHTreeStateNode node) {
        treePathMapping.remove(node.getTreePath());
    }
    private FHTreeStateNode getMapping(TreePath path) {
        return treePathMapping.get(path);
    }
    private void rebuild(boolean clearSelection) {
        Object            rootUO;
        treePathMapping.clear();
        if(treeModel != null && (rootUO = treeModel.getRoot()) != null) {
            root = createNodeForValue(rootUO, 0);
            root.path = new TreePath(rootUO);
            addMapping(root);
            if(isRootVisible()) {
                rowCount = 1;
                root.row = 0;
            }
            else {
                rowCount = 0;
                root.row = -1;
            }
            root.expand();
        }
        else {
            root = null;
            rowCount = 0;
        }
        if(clearSelection && treeSelectionModel != null) {
            treeSelectionModel.clearSelection();
        }
        this.visibleNodesChanged();
    }
    private int getRowContainingYLocation(int location) {
        if(getRowCount() == 0)
            return -1;
        return Math.max(0, Math.min(getRowCount() - 1,
                                    location / getRowHeight()));
    }
    private boolean ensurePathIsExpanded(TreePath aPath,
                                           boolean expandLast) {
        if(aPath != null) {
            if(treeModel.isLeaf(aPath.getLastPathComponent())) {
                aPath = aPath.getParentPath();
                expandLast = true;
            }
            if(aPath != null) {
                FHTreeStateNode     lastNode = getNodeForPath(aPath, false,
                                                              true);
                if(lastNode != null) {
                    lastNode.makeVisible();
                    if(expandLast)
                        lastNode.expand();
                    return true;
                }
            }
        }
        return false;
    }
    private FHTreeStateNode createNodeForValue(Object value,int childIndex) {
        return new FHTreeStateNode(value, childIndex, -1);
    }
    private FHTreeStateNode getNodeForPath(TreePath path,
                                             boolean onlyIfVisible,
                                             boolean shouldCreate) {
        if(path != null) {
            FHTreeStateNode      node;
            node = getMapping(path);
            if(node != null) {
                if(onlyIfVisible && !node.isVisible())
                    return null;
                return node;
            }
            if(onlyIfVisible)
                return null;
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
                            node = node.createChildFor(path.
                                                       getLastPathComponent());
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
            return null;
        }
        return null;
    }
    private class FHTreeStateNode extends DefaultMutableTreeNode {
        protected boolean         isExpanded;
        protected int             childIndex;
        protected int             childCount;
        protected int             row;
        protected TreePath        path;
        public FHTreeStateNode(Object userObject, int childIndex, int row) {
            super(userObject);
            this.childIndex = childIndex;
            this.row = row;
        }
        public void setParent(MutableTreeNode parent) {
            super.setParent(parent);
            if(parent != null) {
                path = ((FHTreeStateNode)parent).getTreePath().
                            pathByAddingChild(getUserObject());
                addMapping(this);
            }
        }
        public void remove(int childIndex) {
            FHTreeStateNode     node = (FHTreeStateNode)getChildAt(childIndex);
            node.removeFromMapping();
            super.remove(childIndex);
        }
        public void setUserObject(Object o) {
            super.setUserObject(o);
            if(path != null) {
                FHTreeStateNode      parent = (FHTreeStateNode)getParent();
                if(parent != null)
                    resetChildrenPaths(parent.getTreePath());
                else
                    resetChildrenPaths(null);
            }
        }
        public int getChildIndex() {
            return childIndex;
        }
        public TreePath getTreePath() {
            return path;
        }
        public FHTreeStateNode getChildAtModelIndex(int index) {
            for(int counter = getChildCount() - 1; counter >= 0; counter--)
                if(((FHTreeStateNode)getChildAt(counter)).childIndex == index)
                    return (FHTreeStateNode)getChildAt(counter);
            return null;
        }
        public boolean isVisible() {
            FHTreeStateNode         parent = (FHTreeStateNode)getParent();
            if(parent == null)
                return true;
            return (parent.isExpanded() && parent.isVisible());
        }
        public int getRow() {
            return row;
        }
        public int getRowToModelIndex(int index) {
            FHTreeStateNode      child;
            int                  lastRow = getRow() + 1;
            int                  retValue = lastRow;
            for(int counter = 0, maxCounter = getChildCount();
                counter < maxCounter; counter++) {
                child = (FHTreeStateNode)getChildAt(counter);
                if(child.childIndex >= index) {
                    if(child.childIndex == index)
                        return child.row;
                    if(counter == 0)
                        return getRow() + 1 + index;
                    return child.row - (child.childIndex - index);
                }
            }
            return getRow() + 1 + getTotalChildCount() -
                             (childCount - index);
        }
        public int getTotalChildCount() {
            if(isExpanded()) {
                FHTreeStateNode      parent = (FHTreeStateNode)getParent();
                int                  pIndex;
                if(parent != null && (pIndex = parent.getIndex(this)) + 1 <
                   parent.getChildCount()) {
                    FHTreeStateNode  nextSibling = (FHTreeStateNode)parent.
                                           getChildAt(pIndex + 1);
                    return nextSibling.row - row -
                           (nextSibling.childIndex - childIndex);
                }
                else {
                    int retCount = childCount;
                    for(int counter = getChildCount() - 1; counter >= 0;
                        counter--) {
                        retCount += ((FHTreeStateNode)getChildAt(counter))
                                                  .getTotalChildCount();
                    }
                    return retCount;
                }
            }
            return 0;
        }
        public boolean isExpanded() {
            return isExpanded;
        }
        public int getVisibleLevel() {
            if (isRootVisible()) {
                return getLevel();
            } else {
                return getLevel() - 1;
            }
        }
        protected void resetChildrenPaths(TreePath parentPath) {
            removeMapping(this);
            if(parentPath == null)
                path = new TreePath(getUserObject());
            else
                path = parentPath.pathByAddingChild(getUserObject());
            addMapping(this);
            for(int counter = getChildCount() - 1; counter >= 0; counter--)
                ((FHTreeStateNode)getChildAt(counter)).
                               resetChildrenPaths(path);
        }
        protected void removeFromMapping() {
            if(path != null) {
                removeMapping(this);
                for(int counter = getChildCount() - 1; counter >= 0; counter--)
                    ((FHTreeStateNode)getChildAt(counter)).removeFromMapping();
            }
        }
        protected FHTreeStateNode createChildFor(Object userObject) {
            int      newChildIndex = treeModel.getIndexOfChild
                                     (getUserObject(), userObject);
            if(newChildIndex < 0)
                return null;
            FHTreeStateNode     aNode;
            FHTreeStateNode     child = createNodeForValue(userObject,
                                                           newChildIndex);
            int                 childRow;
            if(isVisible()) {
                childRow = getRowToModelIndex(newChildIndex);
            }
            else {
                childRow = -1;
            }
            child.row = childRow;
            for(int counter = 0, maxCounter = getChildCount();
                counter < maxCounter; counter++) {
                aNode = (FHTreeStateNode)getChildAt(counter);
                if(aNode.childIndex > newChildIndex) {
                    insert(child, counter);
                    return child;
                }
            }
            add(child);
            return child;
        }
        protected void adjustRowBy(int amount) {
            row += amount;
            if(isExpanded) {
                for(int counter = getChildCount() - 1; counter >= 0;
                    counter--)
                    ((FHTreeStateNode)getChildAt(counter)).adjustRowBy(amount);
            }
        }
        protected void adjustRowBy(int amount, int startIndex) {
            if(isExpanded) {
                for(int counter = getChildCount() - 1; counter >= startIndex;
                    counter--)
                    ((FHTreeStateNode)getChildAt(counter)).adjustRowBy(amount);
            }
            FHTreeStateNode        parent = (FHTreeStateNode)getParent();
            if(parent != null) {
                parent.adjustRowBy(amount, parent.getIndex(this) + 1);
            }
        }
        protected void didExpand() {
            int               nextRow = setRowAndChildren(row);
            FHTreeStateNode   parent = (FHTreeStateNode)getParent();
            int               childRowCount = nextRow - row - 1;
            if(parent != null) {
                parent.adjustRowBy(childRowCount, parent.getIndex(this) + 1);
            }
            adjustRowCountBy(childRowCount);
        }
        protected int setRowAndChildren(int nextRow) {
            row = nextRow;
            if(!isExpanded())
                return row + 1;
            int              lastRow = row + 1;
            int              lastModelIndex = 0;
            FHTreeStateNode  child;
            int              maxCounter = getChildCount();
            for(int counter = 0; counter < maxCounter; counter++) {
                child = (FHTreeStateNode)getChildAt(counter);
                lastRow += (child.childIndex - lastModelIndex);
                lastModelIndex = child.childIndex + 1;
                if(child.isExpanded) {
                    lastRow = child.setRowAndChildren(lastRow);
                }
                else {
                    child.row = lastRow++;
                }
            }
            return lastRow + childCount - lastModelIndex;
        }
        protected void resetChildrenRowsFrom(int newRow, int childIndex,
                                            int modelIndex) {
            int              lastRow = newRow;
            int              lastModelIndex = modelIndex;
            FHTreeStateNode  node;
            int              maxCounter = getChildCount();
            for(int counter = childIndex; counter < maxCounter; counter++) {
                node = (FHTreeStateNode)getChildAt(counter);
                lastRow += (node.childIndex - lastModelIndex);
                lastModelIndex = node.childIndex + 1;
                if(node.isExpanded) {
                    lastRow = node.setRowAndChildren(lastRow);
                }
                else {
                    node.row = lastRow++;
                }
            }
            lastRow += childCount - lastModelIndex;
            node = (FHTreeStateNode)getParent();
            if(node != null) {
                node.resetChildrenRowsFrom(lastRow, node.getIndex(this) + 1,
                                           this.childIndex + 1);
            }
            else { 
                rowCount = lastRow;
            }
        }
        protected void makeVisible() {
            FHTreeStateNode       parent = (FHTreeStateNode)getParent();
            if(parent != null)
                parent.expandParentAndReceiver();
        }
        protected void expandParentAndReceiver() {
            FHTreeStateNode       parent = (FHTreeStateNode)getParent();
            if(parent != null)
                parent.expandParentAndReceiver();
            expand();
        }
        protected void expand() {
            if(!isExpanded && !isLeaf()) {
                boolean            visible = isVisible();
                isExpanded = true;
                childCount = treeModel.getChildCount(getUserObject());
                if(visible) {
                    didExpand();
                }
                if(visible && treeSelectionModel != null) {
                    treeSelectionModel.resetRowSelection();
                }
            }
        }
        protected void collapse(boolean adjustRows) {
            if(isExpanded) {
                if(isVisible() && adjustRows) {
                    int              childCount = getTotalChildCount();
                    isExpanded = false;
                    adjustRowCountBy(-childCount);
                    adjustRowBy(-childCount, 0);
                }
                else
                    isExpanded = false;
                if(adjustRows && isVisible() && treeSelectionModel != null)
                    treeSelectionModel.resetRowSelection();
            }
        }
        public boolean isLeaf() {
            TreeModel model = getModel();
            return (model != null) ? model.isLeaf(this.getUserObject()) :
                   true;
        }
        protected void addNode(FHTreeStateNode newChild) {
            boolean         added = false;
            int             childIndex = newChild.getChildIndex();
            for(int counter = 0, maxCounter = getChildCount();
                counter < maxCounter; counter++) {
                if(((FHTreeStateNode)getChildAt(counter)).getChildIndex() >
                   childIndex) {
                    added = true;
                    insert(newChild, counter);
                    counter = maxCounter;
                }
            }
            if(!added)
                add(newChild);
        }
        protected void removeChildAtModelIndex(int modelIndex,
                                               boolean isChildVisible) {
            FHTreeStateNode     childNode = getChildAtModelIndex(modelIndex);
            if(childNode != null) {
                int          row = childNode.getRow();
                int          index = getIndex(childNode);
                childNode.collapse(false);
                remove(index);
                adjustChildIndexs(index, -1);
                childCount--;
                if(isChildVisible) {
                    resetChildrenRowsFrom(row, index, modelIndex);
                }
            }
            else {
                int                  maxCounter = getChildCount();
                FHTreeStateNode      aChild;
                for(int counter = 0; counter < maxCounter; counter++) {
                    aChild = (FHTreeStateNode)getChildAt(counter);
                    if(aChild.childIndex >= modelIndex) {
                        if(isChildVisible) {
                            adjustRowBy(-1, counter);
                            adjustRowCountBy(-1);
                        }
                        for(; counter < maxCounter; counter++)
                            ((FHTreeStateNode)getChildAt(counter)).
                                              childIndex--;
                        childCount--;
                        return;
                    }
                }
                if(isChildVisible) {
                    adjustRowBy(-1, maxCounter);
                    adjustRowCountBy(-1);
                }
                childCount--;
            }
        }
        protected void adjustChildIndexs(int index, int amount) {
            for(int counter = index, maxCounter = getChildCount();
                counter < maxCounter; counter++) {
                ((FHTreeStateNode)getChildAt(counter)).childIndex += amount;
            }
        }
        protected void childInsertedAtModelIndex(int index,
                                               boolean isExpandedAndVisible) {
            FHTreeStateNode                aChild;
            int                            maxCounter = getChildCount();
            for(int counter = 0; counter < maxCounter; counter++) {
                aChild = (FHTreeStateNode)getChildAt(counter);
                if(aChild.childIndex >= index) {
                    if(isExpandedAndVisible) {
                        adjustRowBy(1, counter);
                        adjustRowCountBy(1);
                    }
                    for(; counter < maxCounter; counter++)
                        ((FHTreeStateNode)getChildAt(counter)).childIndex++;
                    childCount++;
                    return;
                }
            }
            if(isExpandedAndVisible) {
                adjustRowBy(1, maxCounter);
                adjustRowCountBy(1);
            }
            childCount++;
        }
        protected boolean getPathForRow(int row, int nextRow,
                                        SearchInfo info) {
            if(this.row == row) {
                info.node = this;
                info.isNodeParentNode = false;
                info.childIndex = childIndex;
                return true;
            }
            FHTreeStateNode            child;
            FHTreeStateNode            lastChild = null;
            for(int counter = 0, maxCounter = getChildCount();
                counter < maxCounter; counter++) {
                child = (FHTreeStateNode)getChildAt(counter);
                if(child.row > row) {
                    if(counter == 0) {
                        info.node = this;
                        info.isNodeParentNode = true;
                        info.childIndex = row - this.row - 1;
                        return true;
                    }
                    else {
                        int          lastChildEndRow = 1 + child.row -
                                     (child.childIndex - lastChild.childIndex);
                        if(row < lastChildEndRow) {
                            return lastChild.getPathForRow(row,
                                                       lastChildEndRow, info);
                        }
                        info.node = this;
                        info.isNodeParentNode = true;
                        info.childIndex = row - lastChildEndRow +
                                                lastChild.childIndex + 1;
                        return true;
                    }
                }
                lastChild = child;
            }
            if(lastChild != null) {
                int        lastChildEndRow = nextRow -
                                  (childCount - lastChild.childIndex) + 1;
                if(row < lastChildEndRow) {
                    return lastChild.getPathForRow(row, lastChildEndRow, info);
                }
                info.node = this;
                info.isNodeParentNode = true;
                info.childIndex = row - lastChildEndRow +
                                             lastChild.childIndex + 1;
                return true;
            }
            else {
                int         retChildIndex = row - this.row - 1;
                if(retChildIndex >= childCount) {
                    return false;
                }
                info.node = this;
                info.isNodeParentNode = true;
                info.childIndex = retChildIndex;
                return true;
            }
        }
        protected int getCountTo(int stopIndex) {
            FHTreeStateNode    aChild;
            int                retCount = stopIndex + 1;
            for(int counter = 0, maxCounter = getChildCount();
                counter < maxCounter; counter++) {
                aChild = (FHTreeStateNode)getChildAt(counter);
                if(aChild.childIndex >= stopIndex)
                    counter = maxCounter;
                else
                    retCount += aChild.getTotalChildCount();
            }
            if(parent != null)
                return retCount + ((FHTreeStateNode)getParent())
                                   .getCountTo(childIndex);
            if(!isRootVisible())
                return (retCount - 1);
            return retCount;
        }
        protected int getNumExpandedChildrenTo(int stopIndex) {
            FHTreeStateNode    aChild;
            int                retCount = stopIndex;
            for(int counter = 0, maxCounter = getChildCount();
                counter < maxCounter; counter++) {
                aChild = (FHTreeStateNode)getChildAt(counter);
                if(aChild.childIndex >= stopIndex)
                    return retCount;
                else {
                    retCount += aChild.getTotalChildCount();
                }
            }
            return retCount;
        }
        protected void didAdjustTree() {
        }
    } 
    private class SearchInfo {
        protected FHTreeStateNode   node;
        protected boolean           isNodeParentNode;
        protected int               childIndex;
        protected TreePath getPath() {
            if(node == null)
                return null;
            if(isNodeParentNode)
                return node.getTreePath().pathByAddingChild(treeModel.getChild
                                            (node.getUserObject(),
                                             childIndex));
            return node.path;
        }
    } 
    private class VisibleFHTreeStateNodeEnumeration
        implements Enumeration<TreePath>
    {
        protected FHTreeStateNode     parent;
        protected int                 nextIndex;
        protected int                 childCount;
        protected VisibleFHTreeStateNodeEnumeration(FHTreeStateNode node) {
            this(node, -1);
        }
        protected VisibleFHTreeStateNodeEnumeration(FHTreeStateNode parent,
                                                    int startIndex) {
            this.parent = parent;
            this.nextIndex = startIndex;
            this.childCount = treeModel.getChildCount(this.parent.
                                                      getUserObject());
        }
        public boolean hasMoreElements() {
            return (parent != null);
        }
        public TreePath nextElement() {
            if(!hasMoreElements())
                throw new NoSuchElementException("No more visible paths");
            TreePath                retObject;
            if(nextIndex == -1)
                retObject = parent.getTreePath();
            else {
                FHTreeStateNode  node = parent.getChildAtModelIndex(nextIndex);
                if(node == null)
                    retObject = parent.getTreePath().pathByAddingChild
                                  (treeModel.getChild(parent.getUserObject(),
                                                      nextIndex));
                else
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
                FHTreeStateNode      newParent = (FHTreeStateNode)parent.
                                                  getParent();
                if(newParent != null) {
                    nextIndex = parent.childIndex;
                    parent = newParent;
                    childCount = treeModel.getChildCount
                                            (parent.getUserObject());
                    if(updateNextIndex())
                        return true;
                }
                else
                    parent = null;
            }
            return false;
        }
        protected boolean updateNextIndex() {
            if(nextIndex == -1 && !parent.isExpanded()) {
                return false;
            }
            if(childCount == 0) {
                return false;
            }
            else if(++nextIndex >= childCount) {
                return false;
            }
            FHTreeStateNode    child = parent.getChildAtModelIndex(nextIndex);
            if(child != null && child.isExpanded()) {
                parent = child;
                nextIndex = -1;
                childCount = treeModel.getChildCount(child.getUserObject());
            }
            return true;
        }
    } 
}
