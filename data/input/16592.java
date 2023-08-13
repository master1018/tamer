public class BasicTreeUI extends TreeUI
{
    private static final StringBuilder BASELINE_COMPONENT_KEY =
        new StringBuilder("Tree.baselineComponent");
    static private final Actions SHARED_ACTION = new Actions();
    transient protected Icon        collapsedIcon;
    transient protected Icon        expandedIcon;
    private Color hashColor;
    protected int               leftChildIndent;
    protected int               rightChildIndent;
    protected int               totalChildIndent;
    protected Dimension         preferredMinSize;
    protected int               lastSelectedRow;
    protected JTree             tree;
    transient protected TreeCellRenderer   currentCellRenderer;
    protected boolean           createdRenderer;
    transient protected TreeCellEditor     cellEditor;
    protected boolean           createdCellEditor;
    protected boolean           stopEditingInCompleteEditing;
    protected CellRendererPane  rendererPane;
    protected Dimension         preferredSize;
    protected boolean           validCachedPreferredSize;
    protected AbstractLayoutCache  treeState;
    protected Hashtable<TreePath,Boolean> drawingCache;
    protected boolean           largeModel;
    protected AbstractLayoutCache.NodeDimensions     nodeDimensions;
    protected TreeModel         treeModel;
    protected TreeSelectionModel treeSelectionModel;
    protected int               depthOffset;
    protected Component         editingComponent;
    protected TreePath          editingPath;
    protected int               editingRow;
    protected boolean           editorHasDifferentSize;
    private int                 leadRow;
    private boolean             ignoreLAChange;
    private boolean             leftToRight;
    private PropertyChangeListener propertyChangeListener;
    private PropertyChangeListener selectionModelPropertyChangeListener;
    private MouseListener mouseListener;
    private FocusListener focusListener;
    private KeyListener keyListener;
    private ComponentListener   componentListener;
    private CellEditorListener  cellEditorListener;
    private TreeSelectionListener treeSelectionListener;
    private TreeModelListener treeModelListener;
    private TreeExpansionListener treeExpansionListener;
    private boolean paintLines = true;
    private boolean lineTypeDashed;
    private long timeFactor = 1000L;
    private Handler handler;
    private MouseEvent releaseEvent;
    public static ComponentUI createUI(JComponent x) {
        return new BasicTreeUI();
    }
    static void loadActionMap(LazyActionMap map) {
        map.put(new Actions(Actions.SELECT_PREVIOUS));
        map.put(new Actions(Actions.SELECT_PREVIOUS_CHANGE_LEAD));
        map.put(new Actions(Actions.SELECT_PREVIOUS_EXTEND_SELECTION));
        map.put(new Actions(Actions.SELECT_NEXT));
        map.put(new Actions(Actions.SELECT_NEXT_CHANGE_LEAD));
        map.put(new Actions(Actions.SELECT_NEXT_EXTEND_SELECTION));
        map.put(new Actions(Actions.SELECT_CHILD));
        map.put(new Actions(Actions.SELECT_CHILD_CHANGE_LEAD));
        map.put(new Actions(Actions.SELECT_PARENT));
        map.put(new Actions(Actions.SELECT_PARENT_CHANGE_LEAD));
        map.put(new Actions(Actions.SCROLL_UP_CHANGE_SELECTION));
        map.put(new Actions(Actions.SCROLL_UP_CHANGE_LEAD));
        map.put(new Actions(Actions.SCROLL_UP_EXTEND_SELECTION));
        map.put(new Actions(Actions.SCROLL_DOWN_CHANGE_SELECTION));
        map.put(new Actions(Actions.SCROLL_DOWN_EXTEND_SELECTION));
        map.put(new Actions(Actions.SCROLL_DOWN_CHANGE_LEAD));
        map.put(new Actions(Actions.SELECT_FIRST));
        map.put(new Actions(Actions.SELECT_FIRST_CHANGE_LEAD));
        map.put(new Actions(Actions.SELECT_FIRST_EXTEND_SELECTION));
        map.put(new Actions(Actions.SELECT_LAST));
        map.put(new Actions(Actions.SELECT_LAST_CHANGE_LEAD));
        map.put(new Actions(Actions.SELECT_LAST_EXTEND_SELECTION));
        map.put(new Actions(Actions.TOGGLE));
        map.put(new Actions(Actions.CANCEL_EDITING));
        map.put(new Actions(Actions.START_EDITING));
        map.put(new Actions(Actions.SELECT_ALL));
        map.put(new Actions(Actions.CLEAR_SELECTION));
        map.put(new Actions(Actions.SCROLL_LEFT));
        map.put(new Actions(Actions.SCROLL_RIGHT));
        map.put(new Actions(Actions.SCROLL_LEFT_EXTEND_SELECTION));
        map.put(new Actions(Actions.SCROLL_RIGHT_EXTEND_SELECTION));
        map.put(new Actions(Actions.SCROLL_RIGHT_CHANGE_LEAD));
        map.put(new Actions(Actions.SCROLL_LEFT_CHANGE_LEAD));
        map.put(new Actions(Actions.EXPAND));
        map.put(new Actions(Actions.COLLAPSE));
        map.put(new Actions(Actions.MOVE_SELECTION_TO_PARENT));
        map.put(new Actions(Actions.ADD_TO_SELECTION));
        map.put(new Actions(Actions.TOGGLE_AND_ANCHOR));
        map.put(new Actions(Actions.EXTEND_TO));
        map.put(new Actions(Actions.MOVE_SELECTION_TO));
        map.put(TransferHandler.getCutAction());
        map.put(TransferHandler.getCopyAction());
        map.put(TransferHandler.getPasteAction());
    }
    public BasicTreeUI() {
        super();
    }
    protected Color getHashColor() {
        return hashColor;
    }
    protected void setHashColor(Color color) {
        hashColor = color;
    }
    public void setLeftChildIndent(int newAmount) {
        leftChildIndent = newAmount;
        totalChildIndent = leftChildIndent + rightChildIndent;
        if(treeState != null)
            treeState.invalidateSizes();
        updateSize();
    }
    public int getLeftChildIndent() {
        return leftChildIndent;
    }
    public void setRightChildIndent(int newAmount) {
        rightChildIndent = newAmount;
        totalChildIndent = leftChildIndent + rightChildIndent;
        if(treeState != null)
            treeState.invalidateSizes();
        updateSize();
    }
    public int getRightChildIndent() {
        return rightChildIndent;
    }
    public void setExpandedIcon(Icon newG) {
        expandedIcon = newG;
    }
    public Icon getExpandedIcon() {
        return expandedIcon;
    }
    public void setCollapsedIcon(Icon newG) {
        collapsedIcon = newG;
    }
    public Icon getCollapsedIcon() {
        return collapsedIcon;
    }
    protected void setLargeModel(boolean largeModel) {
        if(getRowHeight() < 1)
            largeModel = false;
        if(this.largeModel != largeModel) {
            completeEditing();
            this.largeModel = largeModel;
            treeState = createLayoutCache();
            configureLayoutCache();
            updateLayoutCacheExpandedNodesIfNecessary();
            updateSize();
        }
    }
    protected boolean isLargeModel() {
        return largeModel;
    }
    protected void setRowHeight(int rowHeight) {
        completeEditing();
        if(treeState != null) {
            setLargeModel(tree.isLargeModel());
            treeState.setRowHeight(rowHeight);
            updateSize();
        }
    }
    protected int getRowHeight() {
        return (tree == null) ? -1 : tree.getRowHeight();
    }
    protected void setCellRenderer(TreeCellRenderer tcr) {
        completeEditing();
        updateRenderer();
        if(treeState != null) {
            treeState.invalidateSizes();
            updateSize();
        }
    }
    protected TreeCellRenderer getCellRenderer() {
        return currentCellRenderer;
    }
    protected void setModel(TreeModel model) {
        completeEditing();
        if(treeModel != null && treeModelListener != null)
            treeModel.removeTreeModelListener(treeModelListener);
        treeModel = model;
        if(treeModel != null) {
            if(treeModelListener != null)
                treeModel.addTreeModelListener(treeModelListener);
        }
        if(treeState != null) {
            treeState.setModel(model);
            updateLayoutCacheExpandedNodesIfNecessary();
            updateSize();
        }
    }
    protected TreeModel getModel() {
        return treeModel;
    }
    protected void setRootVisible(boolean newValue) {
        completeEditing();
        updateDepthOffset();
        if(treeState != null) {
            treeState.setRootVisible(newValue);
            treeState.invalidateSizes();
            updateSize();
        }
    }
    protected boolean isRootVisible() {
        return (tree != null) ? tree.isRootVisible() : false;
    }
    protected void setShowsRootHandles(boolean newValue) {
        completeEditing();
        updateDepthOffset();
        if(treeState != null) {
            treeState.invalidateSizes();
            updateSize();
        }
    }
    protected boolean getShowsRootHandles() {
        return (tree != null) ? tree.getShowsRootHandles() : false;
    }
    protected void setCellEditor(TreeCellEditor editor) {
        updateCellEditor();
    }
    protected TreeCellEditor getCellEditor() {
        return (tree != null) ? tree.getCellEditor() : null;
    }
    protected void setEditable(boolean newValue) {
        updateCellEditor();
    }
    protected boolean isEditable() {
        return (tree != null) ? tree.isEditable() : false;
    }
    protected void setSelectionModel(TreeSelectionModel newLSM) {
        completeEditing();
        if(selectionModelPropertyChangeListener != null &&
           treeSelectionModel != null)
            treeSelectionModel.removePropertyChangeListener
                              (selectionModelPropertyChangeListener);
        if(treeSelectionListener != null && treeSelectionModel != null)
            treeSelectionModel.removeTreeSelectionListener
                               (treeSelectionListener);
        treeSelectionModel = newLSM;
        if(treeSelectionModel != null) {
            if(selectionModelPropertyChangeListener != null)
                treeSelectionModel.addPropertyChangeListener
                              (selectionModelPropertyChangeListener);
            if(treeSelectionListener != null)
                treeSelectionModel.addTreeSelectionListener
                                   (treeSelectionListener);
            if(treeState != null)
                treeState.setSelectionModel(treeSelectionModel);
        }
        else if(treeState != null)
            treeState.setSelectionModel(null);
        if(tree != null)
            tree.repaint();
    }
    protected TreeSelectionModel getSelectionModel() {
        return treeSelectionModel;
    }
    public Rectangle getPathBounds(JTree tree, TreePath path) {
        if(tree != null && treeState != null) {
            return getPathBounds(path, tree.getInsets(), new Rectangle());
        }
        return null;
    }
    private Rectangle getPathBounds(TreePath path, Insets insets,
                                    Rectangle bounds) {
        bounds = treeState.getBounds(path, bounds);
        if (bounds != null) {
            if (leftToRight) {
                bounds.x += insets.left;
            } else {
                bounds.x = tree.getWidth() - (bounds.x + bounds.width) -
                        insets.right;
            }
            bounds.y += insets.top;
        }
        return bounds;
    }
    public TreePath getPathForRow(JTree tree, int row) {
        return (treeState != null) ? treeState.getPathForRow(row) : null;
    }
    public int getRowForPath(JTree tree, TreePath path) {
        return (treeState != null) ? treeState.getRowForPath(path) : -1;
    }
    public int getRowCount(JTree tree) {
        return (treeState != null) ? treeState.getRowCount() : 0;
    }
    public TreePath getClosestPathForLocation(JTree tree, int x, int y) {
        if(tree != null && treeState != null) {
            y -= tree.getInsets().top;
            return treeState.getPathClosestTo(x, y);
        }
        return null;
    }
    public boolean isEditing(JTree tree) {
        return (editingComponent != null);
    }
    public boolean stopEditing(JTree tree) {
        if(editingComponent != null && cellEditor.stopCellEditing()) {
            completeEditing(false, false, true);
            return true;
        }
        return false;
    }
    public void cancelEditing(JTree tree) {
        if(editingComponent != null) {
            completeEditing(false, true, false);
        }
    }
    public void startEditingAtPath(JTree tree, TreePath path) {
        tree.scrollPathToVisible(path);
        if(path != null && tree.isVisible(path))
            startEditing(path, null);
    }
    public TreePath getEditingPath(JTree tree) {
        return editingPath;
    }
    public void installUI(JComponent c) {
        if ( c == null ) {
            throw new NullPointerException( "null component passed to BasicTreeUI.installUI()" );
        }
        tree = (JTree)c;
        prepareForUIInstall();
        installDefaults();
        installKeyboardActions();
        installComponents();
        installListeners();
        completeUIInstall();
    }
    protected void prepareForUIInstall() {
        drawingCache = new Hashtable<TreePath,Boolean>(7);
        leftToRight = BasicGraphicsUtils.isLeftToRight(tree);
        stopEditingInCompleteEditing = true;
        lastSelectedRow = -1;
        leadRow = -1;
        preferredSize = new Dimension();
        largeModel = tree.isLargeModel();
        if(getRowHeight() <= 0)
            largeModel = false;
        setModel(tree.getModel());
    }
    protected void completeUIInstall() {
        this.setShowsRootHandles(tree.getShowsRootHandles());
        updateRenderer();
        updateDepthOffset();
        setSelectionModel(tree.getSelectionModel());
        treeState = createLayoutCache();
        configureLayoutCache();
        updateSize();
    }
    protected void installDefaults() {
        if(tree.getBackground() == null ||
           tree.getBackground() instanceof UIResource) {
            tree.setBackground(UIManager.getColor("Tree.background"));
        }
        if(getHashColor() == null || getHashColor() instanceof UIResource) {
            setHashColor(UIManager.getColor("Tree.hash"));
        }
        if (tree.getFont() == null || tree.getFont() instanceof UIResource)
            tree.setFont( UIManager.getFont("Tree.font") );
        setExpandedIcon( (Icon)UIManager.get( "Tree.expandedIcon" ) );
        setCollapsedIcon( (Icon)UIManager.get( "Tree.collapsedIcon" ) );
        setLeftChildIndent(((Integer)UIManager.get("Tree.leftChildIndent")).
                           intValue());
        setRightChildIndent(((Integer)UIManager.get("Tree.rightChildIndent")).
                           intValue());
        LookAndFeel.installProperty(tree, "rowHeight",
                                    UIManager.get("Tree.rowHeight"));
        largeModel = (tree.isLargeModel() && tree.getRowHeight() > 0);
        Object scrollsOnExpand = UIManager.get("Tree.scrollsOnExpand");
        if (scrollsOnExpand != null) {
            LookAndFeel.installProperty(tree, "scrollsOnExpand", scrollsOnExpand);
        }
        paintLines = UIManager.getBoolean("Tree.paintLines");
        lineTypeDashed = UIManager.getBoolean("Tree.lineTypeDashed");
        Long l = (Long)UIManager.get("Tree.timeFactor");
        timeFactor = (l!=null) ? l.longValue() : 1000L;
        Object showsRootHandles = UIManager.get("Tree.showsRootHandles");
        if (showsRootHandles != null) {
            LookAndFeel.installProperty(tree,
                    JTree.SHOWS_ROOT_HANDLES_PROPERTY, showsRootHandles);
        }
    }
    protected void installListeners() {
        if ( (propertyChangeListener = createPropertyChangeListener())
             != null ) {
            tree.addPropertyChangeListener(propertyChangeListener);
        }
        if ( (mouseListener = createMouseListener()) != null ) {
            tree.addMouseListener(mouseListener);
            if (mouseListener instanceof MouseMotionListener) {
                tree.addMouseMotionListener((MouseMotionListener)mouseListener);
            }
        }
        if ((focusListener = createFocusListener()) != null ) {
            tree.addFocusListener(focusListener);
        }
        if ((keyListener = createKeyListener()) != null) {
            tree.addKeyListener(keyListener);
        }
        if((treeExpansionListener = createTreeExpansionListener()) != null) {
            tree.addTreeExpansionListener(treeExpansionListener);
        }
        if((treeModelListener = createTreeModelListener()) != null &&
           treeModel != null) {
            treeModel.addTreeModelListener(treeModelListener);
        }
        if((selectionModelPropertyChangeListener =
            createSelectionModelPropertyChangeListener()) != null &&
           treeSelectionModel != null) {
            treeSelectionModel.addPropertyChangeListener
                (selectionModelPropertyChangeListener);
        }
        if((treeSelectionListener = createTreeSelectionListener()) != null &&
           treeSelectionModel != null) {
            treeSelectionModel.addTreeSelectionListener(treeSelectionListener);
        }
        TransferHandler th = tree.getTransferHandler();
        if (th == null || th instanceof UIResource) {
            tree.setTransferHandler(defaultTransferHandler);
            if (tree.getDropTarget() instanceof UIResource) {
                tree.setDropTarget(null);
            }
        }
        LookAndFeel.installProperty(tree, "opaque", Boolean.TRUE);
    }
    protected void installKeyboardActions() {
        InputMap km = getInputMap(JComponent.
                                  WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        SwingUtilities.replaceUIInputMap(tree, JComponent.
                                         WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                                         km);
        km = getInputMap(JComponent.WHEN_FOCUSED);
        SwingUtilities.replaceUIInputMap(tree, JComponent.WHEN_FOCUSED, km);
        LazyActionMap.installLazyActionMap(tree, BasicTreeUI.class,
                                           "Tree.actionMap");
    }
    InputMap getInputMap(int condition) {
        if (condition == JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT) {
            return (InputMap)DefaultLookup.get(tree, this,
                                               "Tree.ancestorInputMap");
        }
        else if (condition == JComponent.WHEN_FOCUSED) {
            InputMap keyMap = (InputMap)DefaultLookup.get(tree, this,
                                                      "Tree.focusInputMap");
            InputMap rtlKeyMap;
            if (tree.getComponentOrientation().isLeftToRight() ||
                  ((rtlKeyMap = (InputMap)DefaultLookup.get(tree, this,
                  "Tree.focusInputMap.RightToLeft")) == null)) {
                return keyMap;
            } else {
                rtlKeyMap.setParent(keyMap);
                return rtlKeyMap;
            }
        }
        return null;
    }
    protected void installComponents() {
        if ((rendererPane = createCellRendererPane()) != null) {
            tree.add( rendererPane );
        }
    }
    protected AbstractLayoutCache.NodeDimensions createNodeDimensions() {
        return new NodeDimensionsHandler();
    }
    protected PropertyChangeListener createPropertyChangeListener() {
        return getHandler();
    }
    private Handler getHandler() {
        if (handler == null) {
            handler = new Handler();
        }
        return handler;
    }
    protected MouseListener createMouseListener() {
        return getHandler();
    }
    protected FocusListener createFocusListener() {
        return getHandler();
    }
    protected KeyListener createKeyListener() {
        return getHandler();
    }
    protected PropertyChangeListener createSelectionModelPropertyChangeListener() {
        return getHandler();
    }
    protected TreeSelectionListener createTreeSelectionListener() {
        return getHandler();
    }
    protected CellEditorListener createCellEditorListener() {
        return getHandler();
    }
    protected ComponentListener createComponentListener() {
        return new ComponentHandler();
    }
    protected TreeExpansionListener createTreeExpansionListener() {
        return getHandler();
    }
    protected AbstractLayoutCache createLayoutCache() {
        if(isLargeModel() && getRowHeight() > 0) {
            return new FixedHeightLayoutCache();
        }
        return new VariableHeightLayoutCache();
    }
    protected CellRendererPane createCellRendererPane() {
        return new CellRendererPane();
    }
    protected TreeCellEditor createDefaultCellEditor() {
        if(currentCellRenderer != null &&
           (currentCellRenderer instanceof DefaultTreeCellRenderer)) {
            DefaultTreeCellEditor editor = new DefaultTreeCellEditor
                        (tree, (DefaultTreeCellRenderer)currentCellRenderer);
            return editor;
        }
        return new DefaultTreeCellEditor(tree, null);
    }
    protected TreeCellRenderer createDefaultCellRenderer() {
        return new DefaultTreeCellRenderer();
    }
    protected TreeModelListener createTreeModelListener() {
        return getHandler();
    }
    public void uninstallUI(JComponent c) {
        completeEditing();
        prepareForUIUninstall();
        uninstallDefaults();
        uninstallListeners();
        uninstallKeyboardActions();
        uninstallComponents();
        completeUIUninstall();
    }
    protected void prepareForUIUninstall() {
    }
    protected void completeUIUninstall() {
        if(createdRenderer) {
            tree.setCellRenderer(null);
        }
        if(createdCellEditor) {
            tree.setCellEditor(null);
        }
        cellEditor = null;
        currentCellRenderer = null;
        rendererPane = null;
        componentListener = null;
        propertyChangeListener = null;
        mouseListener = null;
        focusListener = null;
        keyListener = null;
        setSelectionModel(null);
        treeState = null;
        drawingCache = null;
        selectionModelPropertyChangeListener = null;
        tree = null;
        treeModel = null;
        treeSelectionModel = null;
        treeSelectionListener = null;
        treeExpansionListener = null;
    }
    protected void uninstallDefaults() {
        if (tree.getTransferHandler() instanceof UIResource) {
            tree.setTransferHandler(null);
        }
    }
    protected void uninstallListeners() {
        if(componentListener != null) {
            tree.removeComponentListener(componentListener);
        }
        if (propertyChangeListener != null) {
            tree.removePropertyChangeListener(propertyChangeListener);
        }
        if (mouseListener != null) {
            tree.removeMouseListener(mouseListener);
            if (mouseListener instanceof MouseMotionListener) {
                tree.removeMouseMotionListener((MouseMotionListener)mouseListener);
            }
        }
        if (focusListener != null) {
            tree.removeFocusListener(focusListener);
        }
        if (keyListener != null) {
            tree.removeKeyListener(keyListener);
        }
        if(treeExpansionListener != null) {
            tree.removeTreeExpansionListener(treeExpansionListener);
        }
        if(treeModel != null && treeModelListener != null) {
            treeModel.removeTreeModelListener(treeModelListener);
        }
        if(selectionModelPropertyChangeListener != null &&
           treeSelectionModel != null) {
            treeSelectionModel.removePropertyChangeListener
                (selectionModelPropertyChangeListener);
        }
        if(treeSelectionListener != null && treeSelectionModel != null) {
            treeSelectionModel.removeTreeSelectionListener
                               (treeSelectionListener);
        }
        handler = null;
    }
    protected void uninstallKeyboardActions() {
        SwingUtilities.replaceUIActionMap(tree, null);
        SwingUtilities.replaceUIInputMap(tree, JComponent.
                                         WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                                         null);
        SwingUtilities.replaceUIInputMap(tree, JComponent.WHEN_FOCUSED, null);
    }
    protected void uninstallComponents() {
        if(rendererPane != null) {
            tree.remove(rendererPane);
        }
    }
    private void redoTheLayout() {
        if (treeState != null) {
            treeState.invalidateSizes();
        }
    }
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        UIDefaults lafDefaults = UIManager.getLookAndFeelDefaults();
        Component renderer = (Component)lafDefaults.get(
                BASELINE_COMPONENT_KEY);
        if (renderer == null) {
            TreeCellRenderer tcr = createDefaultCellRenderer();
            renderer = tcr.getTreeCellRendererComponent(
                    tree, "a", false, false, false, -1, false);
            lafDefaults.put(BASELINE_COMPONENT_KEY, renderer);
        }
        int rowHeight = tree.getRowHeight();
        int baseline;
        if (rowHeight > 0) {
            baseline = renderer.getBaseline(Integer.MAX_VALUE, rowHeight);
        }
        else {
            Dimension pref = renderer.getPreferredSize();
            baseline = renderer.getBaseline(pref.width, pref.height);
        }
        return baseline + tree.getInsets().top;
    }
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(
            JComponent c) {
        super.getBaselineResizeBehavior(c);
        return Component.BaselineResizeBehavior.CONSTANT_ASCENT;
    }
    public void paint(Graphics g, JComponent c) {
        if (tree != c) {
            throw new InternalError("incorrect component");
        }
        if(treeState == null) {
            return;
        }
        Rectangle        paintBounds = g.getClipBounds();
        Insets           insets = tree.getInsets();
        TreePath         initialPath = getClosestPathForLocation
                                       (tree, 0, paintBounds.y);
        Enumeration      paintingEnumerator = treeState.getVisiblePathsFrom
                                              (initialPath);
        int              row = treeState.getRowForPath(initialPath);
        int              endY = paintBounds.y + paintBounds.height;
        drawingCache.clear();
        if(initialPath != null && paintingEnumerator != null) {
            TreePath   parentPath = initialPath;
            parentPath = parentPath.getParentPath();
            while(parentPath != null) {
                paintVerticalPartOfLeg(g, paintBounds, insets, parentPath);
                drawingCache.put(parentPath, Boolean.TRUE);
                parentPath = parentPath.getParentPath();
            }
            boolean         done = false;
            boolean         isExpanded;
            boolean         hasBeenExpanded;
            boolean         isLeaf;
            Rectangle       boundsBuffer = new Rectangle();
            Rectangle       bounds;
            TreePath        path;
            boolean         rootVisible = isRootVisible();
            while(!done && paintingEnumerator.hasMoreElements()) {
                path = (TreePath)paintingEnumerator.nextElement();
                if(path != null) {
                    isLeaf = treeModel.isLeaf(path.getLastPathComponent());
                    if(isLeaf)
                        isExpanded = hasBeenExpanded = false;
                    else {
                        isExpanded = treeState.getExpandedState(path);
                        hasBeenExpanded = tree.hasBeenExpanded(path);
                    }
                    bounds = getPathBounds(path, insets, boundsBuffer);
                    if(bounds == null)
                        return;
                    parentPath = path.getParentPath();
                    if(parentPath != null) {
                        if(drawingCache.get(parentPath) == null) {
                            paintVerticalPartOfLeg(g, paintBounds,
                                                   insets, parentPath);
                            drawingCache.put(parentPath, Boolean.TRUE);
                        }
                        paintHorizontalPartOfLeg(g, paintBounds, insets,
                                                 bounds, path, row,
                                                 isExpanded,
                                                 hasBeenExpanded, isLeaf);
                    }
                    else if(rootVisible && row == 0) {
                        paintHorizontalPartOfLeg(g, paintBounds, insets,
                                                 bounds, path, row,
                                                 isExpanded,
                                                 hasBeenExpanded, isLeaf);
                    }
                    if(shouldPaintExpandControl(path, row, isExpanded,
                                                hasBeenExpanded, isLeaf)) {
                        paintExpandControl(g, paintBounds, insets, bounds,
                                           path, row, isExpanded,
                                           hasBeenExpanded, isLeaf);
                    }
                    paintRow(g, paintBounds, insets, bounds, path,
                                 row, isExpanded, hasBeenExpanded, isLeaf);
                    if((bounds.y + bounds.height) >= endY)
                        done = true;
                }
                else {
                    done = true;
                }
                row++;
            }
        }
        paintDropLine(g);
        rendererPane.removeAll();
        drawingCache.clear();
    }
    protected boolean isDropLine(JTree.DropLocation loc) {
        return loc != null && loc.getPath() != null && loc.getChildIndex() != -1;
    }
    protected void paintDropLine(Graphics g) {
        JTree.DropLocation loc = tree.getDropLocation();
        if (!isDropLine(loc)) {
            return;
        }
        Color c = UIManager.getColor("Tree.dropLineColor");
        if (c != null) {
            g.setColor(c);
            Rectangle rect = getDropLineRect(loc);
            g.fillRect(rect.x, rect.y, rect.width, rect.height);
        }
    }
    protected Rectangle getDropLineRect(JTree.DropLocation loc) {
        Rectangle rect;
        TreePath path = loc.getPath();
        int index = loc.getChildIndex();
        boolean ltr = leftToRight;
        Insets insets = tree.getInsets();
        if (tree.getRowCount() == 0) {
            rect = new Rectangle(insets.left,
                                 insets.top,
                                 tree.getWidth() - insets.left - insets.right,
                                 0);
        } else {
            TreeModel model = getModel();
            Object root = model.getRoot();
            if (path.getLastPathComponent() == root
                    && index >= model.getChildCount(root)) {
                rect = tree.getRowBounds(tree.getRowCount() - 1);
                rect.y = rect.y + rect.height;
                Rectangle xRect;
                if (!tree.isRootVisible()) {
                    xRect = tree.getRowBounds(0);
                } else if (model.getChildCount(root) == 0){
                    xRect = tree.getRowBounds(0);
                    xRect.x += totalChildIndent;
                    xRect.width -= totalChildIndent + totalChildIndent;
                } else {
                    TreePath lastChildPath = path.pathByAddingChild(
                        model.getChild(root, model.getChildCount(root) - 1));
                    xRect = tree.getPathBounds(lastChildPath);
                }
                rect.x = xRect.x;
                rect.width = xRect.width;
            } else {
                rect = tree.getPathBounds(path.pathByAddingChild(
                    model.getChild(path.getLastPathComponent(), index)));
            }
        }
        if (rect.y != 0) {
            rect.y--;
        }
        if (!ltr) {
            rect.x = rect.x + rect.width - 100;
        }
        rect.width = 100;
        rect.height = 2;
        return rect;
    }
    protected void paintHorizontalPartOfLeg(Graphics g, Rectangle clipBounds,
                                            Insets insets, Rectangle bounds,
                                            TreePath path, int row,
                                            boolean isExpanded,
                                            boolean hasBeenExpanded, boolean
                                            isLeaf) {
        if (!paintLines) {
            return;
        }
        int depth = path.getPathCount() - 1;
        if((depth == 0 || (depth == 1 && !isRootVisible())) &&
           !getShowsRootHandles()) {
            return;
        }
        int clipLeft = clipBounds.x;
        int clipRight = clipBounds.x + clipBounds.width;
        int clipTop = clipBounds.y;
        int clipBottom = clipBounds.y + clipBounds.height;
        int lineY = bounds.y + bounds.height / 2;
        if (leftToRight) {
            int leftX = bounds.x - getRightChildIndent();
            int nodeX = bounds.x - getHorizontalLegBuffer();
            if(lineY >= clipTop
                    && lineY < clipBottom
                    && nodeX >= clipLeft
                    && leftX < clipRight
                    && leftX < nodeX) {
                g.setColor(getHashColor());
                paintHorizontalLine(g, tree, lineY, leftX, nodeX - 1);
            }
        } else {
            int nodeX = bounds.x + bounds.width + getHorizontalLegBuffer();
            int rightX = bounds.x + bounds.width + getRightChildIndent();
            if(lineY >= clipTop
                    && lineY < clipBottom
                    && rightX >= clipLeft
                    && nodeX < clipRight
                    && nodeX < rightX) {
                g.setColor(getHashColor());
                paintHorizontalLine(g, tree, lineY, nodeX, rightX - 1);
            }
        }
    }
    protected void paintVerticalPartOfLeg(Graphics g, Rectangle clipBounds,
                                          Insets insets, TreePath path) {
        if (!paintLines) {
            return;
        }
        int depth = path.getPathCount() - 1;
        if (depth == 0 && !getShowsRootHandles() && !isRootVisible()) {
            return;
        }
        int lineX = getRowX(-1, depth + 1);
        if (leftToRight) {
            lineX = lineX - getRightChildIndent() + insets.left;
        }
        else {
            lineX = tree.getWidth() - lineX - insets.right +
                    getRightChildIndent() - 1;
        }
        int clipLeft = clipBounds.x;
        int clipRight = clipBounds.x + (clipBounds.width - 1);
        if (lineX >= clipLeft && lineX <= clipRight) {
            int clipTop = clipBounds.y;
            int clipBottom = clipBounds.y + clipBounds.height;
            Rectangle parentBounds = getPathBounds(tree, path);
            Rectangle lastChildBounds = getPathBounds(tree,
                                                     getLastChildPath(path));
            if(lastChildBounds == null)
                return;
            int       top;
            if(parentBounds == null) {
                top = Math.max(insets.top + getVerticalLegBuffer(),
                               clipTop);
            }
            else
                top = Math.max(parentBounds.y + parentBounds.height +
                               getVerticalLegBuffer(), clipTop);
            if(depth == 0 && !isRootVisible()) {
                TreeModel      model = getModel();
                if(model != null) {
                    Object        root = model.getRoot();
                    if(model.getChildCount(root) > 0) {
                        parentBounds = getPathBounds(tree, path.
                                  pathByAddingChild(model.getChild(root, 0)));
                        if(parentBounds != null)
                            top = Math.max(insets.top + getVerticalLegBuffer(),
                                           parentBounds.y +
                                           parentBounds.height / 2);
                    }
                }
            }
            int bottom = Math.min(lastChildBounds.y +
                                  (lastChildBounds.height / 2), clipBottom);
            if (top <= bottom) {
                g.setColor(getHashColor());
                paintVerticalLine(g, tree, lineX, top, bottom);
            }
        }
    }
    protected void paintExpandControl(Graphics g,
                                      Rectangle clipBounds, Insets insets,
                                      Rectangle bounds, TreePath path,
                                      int row, boolean isExpanded,
                                      boolean hasBeenExpanded,
                                      boolean isLeaf) {
        Object       value = path.getLastPathComponent();
        if (!isLeaf && (!hasBeenExpanded ||
                        treeModel.getChildCount(value) > 0)) {
            int middleXOfKnob;
            if (leftToRight) {
                middleXOfKnob = bounds.x - getRightChildIndent() + 1;
            } else {
                middleXOfKnob = bounds.x + bounds.width + getRightChildIndent() - 1;
            }
            int middleYOfKnob = bounds.y + (bounds.height / 2);
            if (isExpanded) {
                Icon expandedIcon = getExpandedIcon();
                if(expandedIcon != null)
                  drawCentered(tree, g, expandedIcon, middleXOfKnob,
                               middleYOfKnob );
            }
            else {
                Icon collapsedIcon = getCollapsedIcon();
                if(collapsedIcon != null)
                  drawCentered(tree, g, collapsedIcon, middleXOfKnob,
                               middleYOfKnob);
            }
        }
    }
    protected void paintRow(Graphics g, Rectangle clipBounds,
                            Insets insets, Rectangle bounds, TreePath path,
                            int row, boolean isExpanded,
                            boolean hasBeenExpanded, boolean isLeaf) {
        if(editingComponent != null && editingRow == row)
            return;
        int leadIndex;
        if(tree.hasFocus()) {
            leadIndex = getLeadSelectionRow();
        }
        else
            leadIndex = -1;
        Component component;
        component = currentCellRenderer.getTreeCellRendererComponent
                      (tree, path.getLastPathComponent(),
                       tree.isRowSelected(row), isExpanded, isLeaf, row,
                       (leadIndex == row));
        rendererPane.paintComponent(g, component, tree, bounds.x, bounds.y,
                                    bounds.width, bounds.height, true);
    }
    protected boolean shouldPaintExpandControl(TreePath path, int row,
                                               boolean isExpanded,
                                               boolean hasBeenExpanded,
                                               boolean isLeaf) {
        if(isLeaf)
            return false;
        int              depth = path.getPathCount() - 1;
        if((depth == 0 || (depth == 1 && !isRootVisible())) &&
           !getShowsRootHandles())
            return false;
        return true;
    }
    protected void paintVerticalLine(Graphics g, JComponent c, int x, int top,
                                    int bottom) {
        if (lineTypeDashed) {
            drawDashedVerticalLine(g, x, top, bottom);
        } else {
            g.drawLine(x, top, x, bottom);
        }
    }
    protected void paintHorizontalLine(Graphics g, JComponent c, int y,
                                      int left, int right) {
        if (lineTypeDashed) {
            drawDashedHorizontalLine(g, y, left, right);
        } else {
            g.drawLine(left, y, right, y);
        }
    }
    protected int getVerticalLegBuffer() {
        return 0;
    }
    protected int getHorizontalLegBuffer() {
        return 0;
    }
    private int findCenteredX(int x, int iconWidth) {
        return leftToRight
               ? x - (int)Math.ceil(iconWidth / 2.0)
               : x - (int)Math.floor(iconWidth / 2.0);
    }
    protected void drawCentered(Component c, Graphics graphics, Icon icon,
                                int x, int y) {
        icon.paintIcon(c, graphics,
                      findCenteredX(x, icon.getIconWidth()),
                      y - icon.getIconHeight() / 2);
    }
    protected void drawDashedHorizontalLine(Graphics g, int y, int x1, int x2){
        x1 += (x1 % 2);
        for (int x = x1; x <= x2; x+=2) {
            g.drawLine(x, y, x, y);
        }
    }
    protected void drawDashedVerticalLine(Graphics g, int x, int y1, int y2) {
        y1 += (y1 % 2);
        for (int y = y1; y <= y2; y+=2) {
            g.drawLine(x, y, x, y);
        }
    }
    protected int getRowX(int row, int depth) {
        return totalChildIndent * (depth + depthOffset);
    }
    protected void updateLayoutCacheExpandedNodes() {
        if(treeModel != null && treeModel.getRoot() != null)
            updateExpandedDescendants(new TreePath(treeModel.getRoot()));
    }
    private void updateLayoutCacheExpandedNodesIfNecessary() {
        if (treeModel != null && treeModel.getRoot() != null) {
            TreePath rootPath = new TreePath(treeModel.getRoot());
            if (tree.isExpanded(rootPath)) {
                updateLayoutCacheExpandedNodes();
            } else {
                treeState.setExpandedState(rootPath, false);
            }
        }
    }
    protected void updateExpandedDescendants(TreePath path) {
        completeEditing();
        if(treeState != null) {
            treeState.setExpandedState(path, true);
            Enumeration   descendants = tree.getExpandedDescendants(path);
            if(descendants != null) {
                while(descendants.hasMoreElements()) {
                    path = (TreePath)descendants.nextElement();
                    treeState.setExpandedState(path, true);
                }
            }
            updateLeadSelectionRow();
            updateSize();
        }
    }
    protected TreePath getLastChildPath(TreePath parent) {
        if(treeModel != null) {
            int         childCount = treeModel.getChildCount
                (parent.getLastPathComponent());
            if(childCount > 0)
                return parent.pathByAddingChild(treeModel.getChild
                           (parent.getLastPathComponent(), childCount - 1));
        }
        return null;
    }
    protected void updateDepthOffset() {
        if(isRootVisible()) {
            if(getShowsRootHandles())
                depthOffset = 1;
            else
                depthOffset = 0;
        }
        else if(!getShowsRootHandles())
            depthOffset = -1;
        else
            depthOffset = 0;
    }
    protected void updateCellEditor() {
        TreeCellEditor        newEditor;
        completeEditing();
        if(tree == null)
            newEditor = null;
        else {
            if(tree.isEditable()) {
                newEditor = tree.getCellEditor();
                if(newEditor == null) {
                    newEditor = createDefaultCellEditor();
                    if(newEditor != null) {
                        tree.setCellEditor(newEditor);
                        createdCellEditor = true;
                    }
                }
            }
            else
                newEditor = null;
        }
        if(newEditor != cellEditor) {
            if(cellEditor != null && cellEditorListener != null)
                cellEditor.removeCellEditorListener(cellEditorListener);
            cellEditor = newEditor;
            if(cellEditorListener == null)
                cellEditorListener = createCellEditorListener();
            if(newEditor != null && cellEditorListener != null)
                newEditor.addCellEditorListener(cellEditorListener);
            createdCellEditor = false;
        }
    }
    protected void updateRenderer() {
        if(tree != null) {
            TreeCellRenderer      newCellRenderer;
            newCellRenderer = tree.getCellRenderer();
            if(newCellRenderer == null) {
                tree.setCellRenderer(createDefaultCellRenderer());
                createdRenderer = true;
            }
            else {
                createdRenderer = false;
                currentCellRenderer = newCellRenderer;
                if(createdCellEditor) {
                    tree.setCellEditor(null);
                }
            }
        }
        else {
            createdRenderer = false;
            currentCellRenderer = null;
        }
        updateCellEditor();
    }
    protected void configureLayoutCache() {
        if(treeState != null && tree != null) {
            if(nodeDimensions == null)
                nodeDimensions = createNodeDimensions();
            treeState.setNodeDimensions(nodeDimensions);
            treeState.setRootVisible(tree.isRootVisible());
            treeState.setRowHeight(tree.getRowHeight());
            treeState.setSelectionModel(getSelectionModel());
            if(treeState.getModel() != tree.getModel())
                treeState.setModel(tree.getModel());
            updateLayoutCacheExpandedNodesIfNecessary();
            if(isLargeModel()) {
                if(componentListener == null) {
                    componentListener = createComponentListener();
                    if(componentListener != null)
                        tree.addComponentListener(componentListener);
                }
            }
            else if(componentListener != null) {
                tree.removeComponentListener(componentListener);
                componentListener = null;
            }
        }
        else if(componentListener != null) {
            tree.removeComponentListener(componentListener);
            componentListener = null;
        }
    }
    protected void updateSize() {
        validCachedPreferredSize = false;
        tree.treeDidChange();
    }
    private void updateSize0() {
        validCachedPreferredSize = false;
        tree.revalidate();
    }
    protected void updateCachedPreferredSize() {
        if(treeState != null) {
            Insets               i = tree.getInsets();
            if(isLargeModel()) {
                Rectangle            visRect = tree.getVisibleRect();
                if (visRect.x == 0 && visRect.y == 0 &&
                        visRect.width == 0 && visRect.height == 0 &&
                        tree.getVisibleRowCount() > 0) {
                    visRect.width = 1;
                    visRect.height = tree.getRowHeight() *
                            tree.getVisibleRowCount();
                } else {
                    visRect.x -= i.left;
                    visRect.y -= i.top;
                }
                preferredSize.width = treeState.getPreferredWidth(visRect);
            }
            else {
                preferredSize.width = treeState.getPreferredWidth(null);
            }
            preferredSize.height = treeState.getPreferredHeight();
            preferredSize.width += i.left + i.right;
            preferredSize.height += i.top + i.bottom;
        }
        validCachedPreferredSize = true;
    }
    protected void pathWasExpanded(TreePath path) {
        if(tree != null) {
            tree.fireTreeExpanded(path);
        }
    }
    protected void pathWasCollapsed(TreePath path) {
        if(tree != null) {
            tree.fireTreeCollapsed(path);
        }
    }
    protected void ensureRowsAreVisible(int beginRow, int endRow) {
        if(tree != null && beginRow >= 0 && endRow < getRowCount(tree)) {
            boolean scrollVert = DefaultLookup.getBoolean(tree, this,
                              "Tree.scrollsHorizontallyAndVertically", false);
            if(beginRow == endRow) {
                Rectangle     scrollBounds = getPathBounds(tree, getPathForRow
                                                           (tree, beginRow));
                if(scrollBounds != null) {
                    if (!scrollVert) {
                        scrollBounds.x = tree.getVisibleRect().x;
                        scrollBounds.width = 1;
                    }
                    tree.scrollRectToVisible(scrollBounds);
                }
            }
            else {
                Rectangle   beginRect = getPathBounds(tree, getPathForRow
                                                      (tree, beginRow));
                Rectangle   visRect = tree.getVisibleRect();
                Rectangle   testRect = beginRect;
                int         beginY = beginRect.y;
                int         maxY = beginY + visRect.height;
                for(int counter = beginRow + 1; counter <= endRow; counter++) {
                    testRect = getPathBounds(tree,
                                             getPathForRow(tree, counter));
                    if((testRect.y + testRect.height) > maxY)
                        counter = endRow;
                }
                tree.scrollRectToVisible(new Rectangle(visRect.x, beginY, 1,
                                                  testRect.y + testRect.height-
                                                  beginY));
            }
        }
    }
    public void setPreferredMinSize(Dimension newSize) {
        preferredMinSize = newSize;
    }
    public Dimension getPreferredMinSize() {
        if(preferredMinSize == null)
            return null;
        return new Dimension(preferredMinSize);
    }
    public Dimension getPreferredSize(JComponent c) {
        return getPreferredSize(c, true);
    }
    public Dimension getPreferredSize(JComponent c,
                                      boolean checkConsistency) {
        Dimension       pSize = this.getPreferredMinSize();
        if(!validCachedPreferredSize)
            updateCachedPreferredSize();
        if(tree != null) {
            if(pSize != null)
                return new Dimension(Math.max(pSize.width,
                                              preferredSize.width),
                              Math.max(pSize.height, preferredSize.height));
            return new Dimension(preferredSize.width, preferredSize.height);
        }
        else if(pSize != null)
            return pSize;
        else
            return new Dimension(0, 0);
    }
    public Dimension getMinimumSize(JComponent c) {
        if(this.getPreferredMinSize() != null)
            return this.getPreferredMinSize();
        return new Dimension(0, 0);
    }
    public Dimension getMaximumSize(JComponent c) {
        if(tree != null)
            return getPreferredSize(tree);
        if(this.getPreferredMinSize() != null)
            return this.getPreferredMinSize();
        return new Dimension(0, 0);
    }
    protected void completeEditing() {
        if(tree.getInvokesStopCellEditing() &&
           stopEditingInCompleteEditing && editingComponent != null) {
            cellEditor.stopCellEditing();
        }
        completeEditing(false, true, false);
    }
    protected void completeEditing(boolean messageStop,
                                   boolean messageCancel,
                                   boolean messageTree) {
        if(stopEditingInCompleteEditing && editingComponent != null) {
            Component             oldComponent = editingComponent;
            TreePath              oldPath = editingPath;
            TreeCellEditor        oldEditor = cellEditor;
            Object                newValue = oldEditor.getCellEditorValue();
            Rectangle             editingBounds = getPathBounds(tree,
                                                                editingPath);
            boolean               requestFocus = (tree != null &&
                                   (tree.hasFocus() || SwingUtilities.
                                    findFocusOwner(editingComponent) != null));
            editingComponent = null;
            editingPath = null;
            if(messageStop)
                oldEditor.stopCellEditing();
            else if(messageCancel)
                oldEditor.cancelCellEditing();
            tree.remove(oldComponent);
            if(editorHasDifferentSize) {
                treeState.invalidatePathBounds(oldPath);
                updateSize();
            }
            else {
                editingBounds.x = 0;
                editingBounds.width = tree.getSize().width;
                tree.repaint(editingBounds);
            }
            if(requestFocus)
                tree.requestFocus();
            if(messageTree)
                treeModel.valueForPathChanged(oldPath, newValue);
        }
    }
    private boolean startEditingOnRelease(TreePath path,
                                          MouseEvent event,
                                          MouseEvent releaseEvent) {
        this.releaseEvent = releaseEvent;
        try {
            return startEditing(path, event);
        } finally {
            this.releaseEvent = null;
        }
    }
    protected boolean startEditing(TreePath path, MouseEvent event) {
        if (isEditing(tree) && tree.getInvokesStopCellEditing() &&
                               !stopEditing(tree)) {
            return false;
        }
        completeEditing();
        if(cellEditor != null && tree.isPathEditable(path)) {
            int           row = getRowForPath(tree, path);
            if(cellEditor.isCellEditable(event)) {
                editingComponent = cellEditor.getTreeCellEditorComponent
                      (tree, path.getLastPathComponent(),
                       tree.isPathSelected(path), tree.isExpanded(path),
                       treeModel.isLeaf(path.getLastPathComponent()), row);
                Rectangle           nodeBounds = getPathBounds(tree, path);
                editingRow = row;
                Dimension editorSize = editingComponent.getPreferredSize();
                if(editorSize.height != nodeBounds.height &&
                   getRowHeight() > 0)
                    editorSize.height = getRowHeight();
                if(editorSize.width != nodeBounds.width ||
                   editorSize.height != nodeBounds.height) {
                    editorHasDifferentSize = true;
                    treeState.invalidatePathBounds(path);
                    updateSize();
                    nodeBounds = getPathBounds(tree, path);
                }
                else
                    editorHasDifferentSize = false;
                tree.add(editingComponent);
                editingComponent.setBounds(nodeBounds.x, nodeBounds.y,
                                           nodeBounds.width,
                                           nodeBounds.height);
                editingPath = path;
                if (editingComponent instanceof JComponent) {
                    ((JComponent)editingComponent).revalidate();
                } else {
                    editingComponent.validate();
                }
                editingComponent.repaint();
                if(cellEditor.shouldSelectCell(event)) {
                    stopEditingInCompleteEditing = false;
                    tree.setSelectionRow(row);
                    stopEditingInCompleteEditing = true;
                }
                Component focusedComponent = SwingUtilities2.
                                 compositeRequestFocus(editingComponent);
                boolean selectAll = true;
                if(event != null) {
                    Point          componentPoint = SwingUtilities.convertPoint
                        (tree, new Point(event.getX(), event.getY()),
                         editingComponent);
                    Component activeComponent = SwingUtilities.
                                    getDeepestComponentAt(editingComponent,
                                       componentPoint.x, componentPoint.y);
                    if (activeComponent != null) {
                        MouseInputHandler handler =
                            new MouseInputHandler(tree, activeComponent,
                                                  event, focusedComponent);
                        if (releaseEvent != null) {
                            handler.mouseReleased(releaseEvent);
                        }
                        selectAll = false;
                    }
                }
                if (selectAll && focusedComponent instanceof JTextField) {
                    ((JTextField)focusedComponent).selectAll();
                }
                return true;
            }
            else
                editingComponent = null;
        }
        return false;
    }
    protected void checkForClickInExpandControl(TreePath path,
                                                int mouseX, int mouseY) {
      if (isLocationInExpandControl(path, mouseX, mouseY)) {
          handleExpandControlClick(path, mouseX, mouseY);
        }
    }
    protected boolean isLocationInExpandControl(TreePath path,
                                                int mouseX, int mouseY) {
        if(path != null && !treeModel.isLeaf(path.getLastPathComponent())){
            int                     boxWidth;
            Insets                  i = tree.getInsets();
            if(getExpandedIcon() != null)
                boxWidth = getExpandedIcon().getIconWidth();
            else
                boxWidth = 8;
            int boxLeftX = getRowX(tree.getRowForPath(path),
                                   path.getPathCount() - 1);
            if (leftToRight) {
                boxLeftX = boxLeftX + i.left - getRightChildIndent() + 1;
            } else {
                boxLeftX = tree.getWidth() - boxLeftX - i.right + getRightChildIndent() - 1;
            }
            boxLeftX = findCenteredX(boxLeftX, boxWidth);
            return (mouseX >= boxLeftX && mouseX < (boxLeftX + boxWidth));
        }
        return false;
    }
    protected void handleExpandControlClick(TreePath path, int mouseX,
                                            int mouseY) {
        toggleExpandState(path);
    }
    protected void toggleExpandState(TreePath path) {
        if(!tree.isExpanded(path)) {
            int       row = getRowForPath(tree, path);
            tree.expandPath(path);
            updateSize();
            if(row != -1) {
                if(tree.getScrollsOnExpand())
                    ensureRowsAreVisible(row, row + treeState.
                                         getVisibleChildCount(path));
                else
                    ensureRowsAreVisible(row, row);
            }
        }
        else {
            tree.collapsePath(path);
            updateSize();
        }
    }
    protected boolean isToggleSelectionEvent(MouseEvent event) {
        return (SwingUtilities.isLeftMouseButton(event) &&
                BasicGraphicsUtils.isMenuShortcutKeyDown(event));
    }
    protected boolean isMultiSelectEvent(MouseEvent event) {
        return (SwingUtilities.isLeftMouseButton(event) &&
                event.isShiftDown());
    }
    protected boolean isToggleEvent(MouseEvent event) {
        if(!SwingUtilities.isLeftMouseButton(event)) {
            return false;
        }
        int           clickCount = tree.getToggleClickCount();
        if(clickCount <= 0) {
            return false;
        }
        return ((event.getClickCount() % clickCount) == 0);
    }
    protected void selectPathForEvent(TreePath path, MouseEvent event) {
        if(isMultiSelectEvent(event)) {
            TreePath    anchor = getAnchorSelectionPath();
            int         anchorRow = (anchor == null) ? -1 :
                                    getRowForPath(tree, anchor);
            if(anchorRow == -1 || tree.getSelectionModel().
                      getSelectionMode() == TreeSelectionModel.
                      SINGLE_TREE_SELECTION) {
                tree.setSelectionPath(path);
            }
            else {
                int          row = getRowForPath(tree, path);
                TreePath     lastAnchorPath = anchor;
                if (isToggleSelectionEvent(event)) {
                    if (tree.isRowSelected(anchorRow)) {
                        tree.addSelectionInterval(anchorRow, row);
                    } else {
                        tree.removeSelectionInterval(anchorRow, row);
                        tree.addSelectionInterval(row, row);
                    }
                } else if(row < anchorRow) {
                    tree.setSelectionInterval(row, anchorRow);
                } else {
                    tree.setSelectionInterval(anchorRow, row);
                }
                lastSelectedRow = row;
                setAnchorSelectionPath(lastAnchorPath);
                setLeadSelectionPath(path);
            }
        }
        else if(isToggleSelectionEvent(event)) {
            if(tree.isPathSelected(path))
                tree.removeSelectionPath(path);
            else
                tree.addSelectionPath(path);
            lastSelectedRow = getRowForPath(tree, path);
            setAnchorSelectionPath(path);
            setLeadSelectionPath(path);
        }
        else if(SwingUtilities.isLeftMouseButton(event)) {
            tree.setSelectionPath(path);
            if(isToggleEvent(event)) {
                toggleExpandState(path);
            }
        }
    }
    protected boolean isLeaf(int row) {
        TreePath          path = getPathForRow(tree, row);
        if(path != null)
            return treeModel.isLeaf(path.getLastPathComponent());
        return true;
    }
    private void setAnchorSelectionPath(TreePath newPath) {
        ignoreLAChange = true;
        try {
            tree.setAnchorSelectionPath(newPath);
        } finally{
            ignoreLAChange = false;
        }
    }
    private TreePath getAnchorSelectionPath() {
        return tree.getAnchorSelectionPath();
    }
    private void setLeadSelectionPath(TreePath newPath) {
        setLeadSelectionPath(newPath, false);
    }
    private void setLeadSelectionPath(TreePath newPath, boolean repaint) {
        Rectangle       bounds = repaint ?
                            getPathBounds(tree, getLeadSelectionPath()) : null;
        ignoreLAChange = true;
        try {
            tree.setLeadSelectionPath(newPath);
        } finally {
            ignoreLAChange = false;
        }
        leadRow = getRowForPath(tree, newPath);
        if (repaint) {
            if (bounds != null) {
                tree.repaint(getRepaintPathBounds(bounds));
            }
            bounds = getPathBounds(tree, newPath);
            if (bounds != null) {
                tree.repaint(getRepaintPathBounds(bounds));
            }
        }
    }
    private Rectangle getRepaintPathBounds(Rectangle bounds) {
        if (UIManager.getBoolean("Tree.repaintWholeRow")) {
           bounds.x = 0;
           bounds.width = tree.getWidth();
        }
        return bounds;
    }
    private TreePath getLeadSelectionPath() {
        return tree.getLeadSelectionPath();
    }
    protected void updateLeadSelectionRow() {
        leadRow = getRowForPath(tree, getLeadSelectionPath());
    }
    protected int getLeadSelectionRow() {
        return leadRow;
    }
    private void extendSelection(TreePath newLead) {
        TreePath           aPath = getAnchorSelectionPath();
        int                aRow = (aPath == null) ? -1 :
                                  getRowForPath(tree, aPath);
        int                newIndex = getRowForPath(tree, newLead);
        if(aRow == -1) {
            tree.setSelectionRow(newIndex);
        }
        else {
            if(aRow < newIndex) {
                tree.setSelectionInterval(aRow, newIndex);
            }
            else {
                tree.setSelectionInterval(newIndex, aRow);
            }
            setAnchorSelectionPath(aPath);
            setLeadSelectionPath(newLead);
        }
    }
    private void repaintPath(TreePath path) {
        if (path != null) {
            Rectangle bounds = getPathBounds(tree, path);
            if (bounds != null) {
                tree.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }
    }
    public class TreeExpansionHandler implements TreeExpansionListener {
        public void treeExpanded(TreeExpansionEvent event) {
            getHandler().treeExpanded(event);
        }
        public void treeCollapsed(TreeExpansionEvent event) {
            getHandler().treeCollapsed(event);
        }
    } 
    public class ComponentHandler extends ComponentAdapter implements
                 ActionListener {
        protected Timer                timer;
        protected JScrollBar           scrollBar;
        public void componentMoved(ComponentEvent e) {
            if(timer == null) {
                JScrollPane   scrollPane = getScrollPane();
                if(scrollPane == null)
                    updateSize();
                else {
                    scrollBar = scrollPane.getVerticalScrollBar();
                    if(scrollBar == null ||
                        !scrollBar.getValueIsAdjusting()) {
                        if((scrollBar = scrollPane.getHorizontalScrollBar())
                            != null && scrollBar.getValueIsAdjusting())
                            startTimer();
                        else
                            updateSize();
                    }
                    else
                        startTimer();
                }
            }
        }
        protected void startTimer() {
            if(timer == null) {
                timer = new Timer(200, this);
                timer.setRepeats(true);
            }
            timer.start();
        }
        protected JScrollPane getScrollPane() {
            Component       c = tree.getParent();
            while(c != null && !(c instanceof JScrollPane))
                c = c.getParent();
            if(c instanceof JScrollPane)
                return (JScrollPane)c;
            return null;
        }
        public void actionPerformed(ActionEvent ae) {
            if(scrollBar == null || !scrollBar.getValueIsAdjusting()) {
                if(timer != null)
                    timer.stop();
                updateSize();
                timer = null;
                scrollBar = null;
            }
        }
    } 
    public class TreeModelHandler implements TreeModelListener {
        public void treeNodesChanged(TreeModelEvent e) {
            getHandler().treeNodesChanged(e);
        }
        public void treeNodesInserted(TreeModelEvent e) {
            getHandler().treeNodesInserted(e);
        }
        public void treeNodesRemoved(TreeModelEvent e) {
            getHandler().treeNodesRemoved(e);
        }
        public void treeStructureChanged(TreeModelEvent e) {
            getHandler().treeStructureChanged(e);
        }
    } 
    public class TreeSelectionHandler implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent event) {
            getHandler().valueChanged(event);
        }
    }
    public class CellEditorHandler implements CellEditorListener {
        public void editingStopped(ChangeEvent e) {
            getHandler().editingStopped(e);
        }
        public void editingCanceled(ChangeEvent e) {
            getHandler().editingCanceled(e);
        }
    } 
    public class KeyHandler extends KeyAdapter {
        protected Action              repeatKeyAction;
        protected boolean            isKeyDown;
        public void keyTyped(KeyEvent e) {
            getHandler().keyTyped(e);
        }
        public void keyPressed(KeyEvent e) {
            getHandler().keyPressed(e);
        }
        public void keyReleased(KeyEvent e) {
            getHandler().keyReleased(e);
        }
    } 
    public class FocusHandler implements FocusListener {
        public void focusGained(FocusEvent e) {
            getHandler().focusGained(e);
        }
        public void focusLost(FocusEvent e) {
            getHandler().focusLost(e);
        }
    } 
    public class NodeDimensionsHandler extends
                 AbstractLayoutCache.NodeDimensions {
        public Rectangle getNodeDimensions(Object value, int row,
                                           int depth, boolean expanded,
                                           Rectangle size) {
            if(editingComponent != null && editingRow == row) {
                Dimension        prefSize = editingComponent.
                                              getPreferredSize();
                int              rh = getRowHeight();
                if(rh > 0 && rh != prefSize.height)
                    prefSize.height = rh;
                if(size != null) {
                    size.x = getRowX(row, depth);
                    size.width = prefSize.width;
                    size.height = prefSize.height;
                }
                else {
                    size = new Rectangle(getRowX(row, depth), 0,
                                         prefSize.width, prefSize.height);
                }
                return size;
            }
            if(currentCellRenderer != null) {
                Component          aComponent;
                aComponent = currentCellRenderer.getTreeCellRendererComponent
                    (tree, value, tree.isRowSelected(row),
                     expanded, treeModel.isLeaf(value), row,
                     false);
                if(tree != null) {
                    rendererPane.add(aComponent);
                    aComponent.validate();
                }
                Dimension        prefSize = aComponent.getPreferredSize();
                if(size != null) {
                    size.x = getRowX(row, depth);
                    size.width = prefSize.width;
                    size.height = prefSize.height;
                }
                else {
                    size = new Rectangle(getRowX(row, depth), 0,
                                         prefSize.width, prefSize.height);
                }
                return size;
            }
            return null;
        }
        protected int getRowX(int row, int depth) {
            return BasicTreeUI.this.getRowX(row, depth);
        }
    } 
    public class MouseHandler extends MouseAdapter implements MouseMotionListener
 {
        public void mousePressed(MouseEvent e) {
            getHandler().mousePressed(e);
        }
        public void mouseDragged(MouseEvent e) {
            getHandler().mouseDragged(e);
        }
        public void mouseMoved(MouseEvent e) {
            getHandler().mouseMoved(e);
        }
        public void mouseReleased(MouseEvent e) {
            getHandler().mouseReleased(e);
        }
    } 
    public class PropertyChangeHandler implements
                       PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            getHandler().propertyChange(event);
        }
    } 
    public class SelectionModelPropertyChangeHandler implements
                      PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            getHandler().propertyChange(event);
        }
    } 
    public class TreeTraverseAction extends AbstractAction {
        protected int direction;
        private boolean changeSelection;
        public TreeTraverseAction(int direction, String name) {
            this(direction, name, true);
        }
        private TreeTraverseAction(int direction, String name,
                                   boolean changeSelection) {
            this.direction = direction;
            this.changeSelection = changeSelection;
        }
        public void actionPerformed(ActionEvent e) {
            if (tree != null) {
                SHARED_ACTION.traverse(tree, BasicTreeUI.this, direction,
                                       changeSelection);
            }
        }
        public boolean isEnabled() { return (tree != null &&
                                             tree.isEnabled()); }
    } 
    public class TreePageAction extends AbstractAction {
        protected int         direction;
        private boolean       addToSelection;
        private boolean       changeSelection;
        public TreePageAction(int direction, String name) {
            this(direction, name, false, true);
        }
        private TreePageAction(int direction, String name,
                               boolean addToSelection,
                               boolean changeSelection) {
            this.direction = direction;
            this.addToSelection = addToSelection;
            this.changeSelection = changeSelection;
        }
        public void actionPerformed(ActionEvent e) {
            if (tree != null) {
                SHARED_ACTION.page(tree, BasicTreeUI.this, direction,
                                   addToSelection, changeSelection);
            }
        }
        public boolean isEnabled() { return (tree != null &&
                                             tree.isEnabled()); }
    } 
    public class TreeIncrementAction extends AbstractAction  {
        protected int         direction;
        private boolean       addToSelection;
        private boolean       changeSelection;
        public TreeIncrementAction(int direction, String name) {
            this(direction, name, false, true);
        }
        private TreeIncrementAction(int direction, String name,
                                   boolean addToSelection,
                                    boolean changeSelection) {
            this.direction = direction;
            this.addToSelection = addToSelection;
            this.changeSelection = changeSelection;
        }
        public void actionPerformed(ActionEvent e) {
            if (tree != null) {
                SHARED_ACTION.increment(tree, BasicTreeUI.this, direction,
                                        addToSelection, changeSelection);
            }
        }
        public boolean isEnabled() { return (tree != null &&
                                             tree.isEnabled()); }
    } 
    public class TreeHomeAction extends AbstractAction {
        protected int            direction;
        private boolean          addToSelection;
        private boolean          changeSelection;
        public TreeHomeAction(int direction, String name) {
            this(direction, name, false, true);
        }
        private TreeHomeAction(int direction, String name,
                               boolean addToSelection,
                               boolean changeSelection) {
            this.direction = direction;
            this.changeSelection = changeSelection;
            this.addToSelection = addToSelection;
        }
        public void actionPerformed(ActionEvent e) {
            if (tree != null) {
                SHARED_ACTION.home(tree, BasicTreeUI.this, direction,
                                   addToSelection, changeSelection);
            }
        }
        public boolean isEnabled() { return (tree != null &&
                                             tree.isEnabled()); }
    } 
    public class TreeToggleAction extends AbstractAction {
        public TreeToggleAction(String name) {
        }
        public void actionPerformed(ActionEvent e) {
            if(tree != null) {
                SHARED_ACTION.toggle(tree, BasicTreeUI.this);
            }
        }
        public boolean isEnabled() { return (tree != null &&
                                             tree.isEnabled()); }
    } 
    public class TreeCancelEditingAction extends AbstractAction {
        public TreeCancelEditingAction(String name) {
        }
        public void actionPerformed(ActionEvent e) {
            if(tree != null) {
                SHARED_ACTION.cancelEditing(tree, BasicTreeUI.this);
            }
        }
        public boolean isEnabled() { return (tree != null &&
                                             tree.isEnabled() &&
                                             isEditing(tree)); }
    } 
    public class MouseInputHandler extends Object implements
                     MouseInputListener
    {
        protected Component        source;
        protected Component        destination;
        private Component          focusComponent;
        private boolean            dispatchedEvent;
        public MouseInputHandler(Component source, Component destination,
                                      MouseEvent event){
            this(source, destination, event, null);
        }
        MouseInputHandler(Component source, Component destination,
                          MouseEvent event, Component focusComponent) {
            this.source = source;
            this.destination = destination;
            this.source.addMouseListener(this);
            this.source.addMouseMotionListener(this);
            SwingUtilities2.setSkipClickCount(destination,
                                              event.getClickCount() - 1);
            destination.dispatchEvent(SwingUtilities.convertMouseEvent
                                          (source, event, destination));
            this.focusComponent = focusComponent;
        }
        public void mouseClicked(MouseEvent e) {
            if(destination != null) {
                dispatchedEvent = true;
                destination.dispatchEvent(SwingUtilities.convertMouseEvent
                                          (source, e, destination));
            }
        }
        public void mousePressed(MouseEvent e) {
        }
        public void mouseReleased(MouseEvent e) {
            if(destination != null)
                destination.dispatchEvent(SwingUtilities.convertMouseEvent
                                          (source, e, destination));
            removeFromSource();
        }
        public void mouseEntered(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) {
                removeFromSource();
            }
        }
        public void mouseExited(MouseEvent e) {
            if (!SwingUtilities.isLeftMouseButton(e)) {
                removeFromSource();
            }
        }
        public void mouseDragged(MouseEvent e) {
            if(destination != null) {
                dispatchedEvent = true;
                destination.dispatchEvent(SwingUtilities.convertMouseEvent
                                          (source, e, destination));
            }
        }
        public void mouseMoved(MouseEvent e) {
            removeFromSource();
        }
        protected void removeFromSource() {
            if(source != null) {
                source.removeMouseListener(this);
                source.removeMouseMotionListener(this);
                if (focusComponent != null &&
                      focusComponent == destination && !dispatchedEvent &&
                      (focusComponent instanceof JTextField)) {
                    ((JTextField)focusComponent).selectAll();
                }
            }
            source = destination = null;
        }
    } 
    private static final TransferHandler defaultTransferHandler = new TreeTransferHandler();
    static class TreeTransferHandler extends TransferHandler implements UIResource, Comparator<TreePath> {
        private JTree tree;
        protected Transferable createTransferable(JComponent c) {
            if (c instanceof JTree) {
                tree = (JTree) c;
                TreePath[] paths = tree.getSelectionPaths();
                if (paths == null || paths.length == 0) {
                    return null;
                }
                StringBuffer plainBuf = new StringBuffer();
                StringBuffer htmlBuf = new StringBuffer();
                htmlBuf.append("<html>\n<body>\n<ul>\n");
                TreeModel model = tree.getModel();
                TreePath lastPath = null;
                TreePath[] displayPaths = getDisplayOrderPaths(paths);
                for (TreePath path : displayPaths) {
                    Object node = path.getLastPathComponent();
                    boolean leaf = model.isLeaf(node);
                    String label = getDisplayString(path, true, leaf);
                    plainBuf.append(label + "\n");
                    htmlBuf.append("  <li>" + label + "\n");
                }
                plainBuf.deleteCharAt(plainBuf.length() - 1);
                htmlBuf.append("</ul>\n</body>\n</html>");
                tree = null;
                return new BasicTransferable(plainBuf.toString(), htmlBuf.toString());
            }
            return null;
        }
        public int compare(TreePath o1, TreePath o2) {
            int row1 = tree.getRowForPath(o1);
            int row2 = tree.getRowForPath(o2);
            return row1 - row2;
        }
        String getDisplayString(TreePath path, boolean selected, boolean leaf) {
            int row = tree.getRowForPath(path);
            boolean hasFocus = tree.getLeadSelectionRow() == row;
            Object node = path.getLastPathComponent();
            return tree.convertValueToText(node, selected, tree.isExpanded(row),
                                           leaf, row, hasFocus);
        }
        TreePath[] getDisplayOrderPaths(TreePath[] paths) {
            ArrayList<TreePath> selOrder = new ArrayList<TreePath>();
            for (TreePath path : paths) {
                selOrder.add(path);
            }
            Collections.sort(selOrder, this);
            int n = selOrder.size();
            TreePath[] displayPaths = new TreePath[n];
            for (int i = 0; i < n; i++) {
                displayPaths[i] = selOrder.get(i);
            }
            return displayPaths;
        }
        public int getSourceActions(JComponent c) {
            return COPY;
        }
    }
    private class Handler implements CellEditorListener, FocusListener,
                  KeyListener, MouseListener, MouseMotionListener,
                  PropertyChangeListener, TreeExpansionListener,
                  TreeModelListener, TreeSelectionListener,
                  BeforeDrag {
        private String prefix = "";
        private String typedString = "";
        private long lastTime = 0L;
        public void keyTyped(KeyEvent e) {
            if(tree != null && tree.getRowCount()>0 && tree.hasFocus() &&
               tree.isEnabled()) {
                if (e.isAltDown() || BasicGraphicsUtils.isMenuShortcutKeyDown(e) ||
                    isNavigationKey(e)) {
                    return;
                }
                boolean startingFromSelection = true;
                char c = e.getKeyChar();
                long time = e.getWhen();
                int startingRow = tree.getLeadSelectionRow();
                if (time - lastTime < timeFactor) {
                    typedString += c;
                    if((prefix.length() == 1) && (c == prefix.charAt(0))) {
                        startingRow++;
                    } else {
                        prefix = typedString;
                    }
                } else {
                    startingRow++;
                    typedString = "" + c;
                    prefix = typedString;
                }
                lastTime = time;
                if (startingRow < 0 || startingRow >= tree.getRowCount()) {
                    startingFromSelection = false;
                    startingRow = 0;
                }
                TreePath path = tree.getNextMatch(prefix, startingRow,
                                                  Position.Bias.Forward);
                if (path != null) {
                    tree.setSelectionPath(path);
                    int row = getRowForPath(tree, path);
                    ensureRowsAreVisible(row, row);
                } else if (startingFromSelection) {
                    path = tree.getNextMatch(prefix, 0,
                                             Position.Bias.Forward);
                    if (path != null) {
                        tree.setSelectionPath(path);
                        int row = getRowForPath(tree, path);
                        ensureRowsAreVisible(row, row);
                    }
                }
            }
        }
        public void keyPressed(KeyEvent e) {
            if (tree != null && isNavigationKey(e)) {
                prefix = "";
                typedString = "";
                lastTime = 0L;
            }
        }
        public void keyReleased(KeyEvent e) {
        }
        private boolean isNavigationKey(KeyEvent event) {
            InputMap inputMap = tree.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            KeyStroke key = KeyStroke.getKeyStrokeForEvent(event);
            return inputMap != null && inputMap.get(key) != null;
        }
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getSource() == treeSelectionModel) {
                treeSelectionModel.resetRowSelection();
            }
            else if(event.getSource() == tree) {
                String              changeName = event.getPropertyName();
                if (changeName == JTree.LEAD_SELECTION_PATH_PROPERTY) {
                    if (!ignoreLAChange) {
                        updateLeadSelectionRow();
                        repaintPath((TreePath)event.getOldValue());
                        repaintPath((TreePath)event.getNewValue());
                    }
                }
                else if (changeName == JTree.ANCHOR_SELECTION_PATH_PROPERTY) {
                    if (!ignoreLAChange) {
                        repaintPath((TreePath)event.getOldValue());
                        repaintPath((TreePath)event.getNewValue());
                    }
                }
                if(changeName == JTree.CELL_RENDERER_PROPERTY) {
                    setCellRenderer((TreeCellRenderer)event.getNewValue());
                    redoTheLayout();
                }
                else if(changeName == JTree.TREE_MODEL_PROPERTY) {
                    setModel((TreeModel)event.getNewValue());
                }
                else if(changeName == JTree.ROOT_VISIBLE_PROPERTY) {
                    setRootVisible(((Boolean)event.getNewValue()).
                                   booleanValue());
                }
                else if(changeName == JTree.SHOWS_ROOT_HANDLES_PROPERTY) {
                    setShowsRootHandles(((Boolean)event.getNewValue()).
                                        booleanValue());
                }
                else if(changeName == JTree.ROW_HEIGHT_PROPERTY) {
                    setRowHeight(((Integer)event.getNewValue()).
                                 intValue());
                }
                else if(changeName == JTree.CELL_EDITOR_PROPERTY) {
                    setCellEditor((TreeCellEditor)event.getNewValue());
                }
                else if(changeName == JTree.EDITABLE_PROPERTY) {
                    setEditable(((Boolean)event.getNewValue()).booleanValue());
                }
                else if(changeName == JTree.LARGE_MODEL_PROPERTY) {
                    setLargeModel(tree.isLargeModel());
                }
                else if(changeName == JTree.SELECTION_MODEL_PROPERTY) {
                    setSelectionModel(tree.getSelectionModel());
                }
                else if(changeName == "font") {
                    completeEditing();
                    if(treeState != null)
                        treeState.invalidateSizes();
                    updateSize();
                }
                else if (changeName == "componentOrientation") {
                    if (tree != null) {
                        leftToRight = BasicGraphicsUtils.isLeftToRight(tree);
                        redoTheLayout();
                        tree.treeDidChange();
                        InputMap km = getInputMap(JComponent.WHEN_FOCUSED);
                        SwingUtilities.replaceUIInputMap(tree,
                                                JComponent.WHEN_FOCUSED, km);
                    }
                } else if ("dropLocation" == changeName) {
                    JTree.DropLocation oldValue = (JTree.DropLocation)event.getOldValue();
                    repaintDropLocation(oldValue);
                    repaintDropLocation(tree.getDropLocation());
                }
            }
        }
        private void repaintDropLocation(JTree.DropLocation loc) {
            if (loc == null) {
                return;
            }
            Rectangle r;
            if (isDropLine(loc)) {
                r = getDropLineRect(loc);
            } else {
                r = tree.getPathBounds(loc.getPath());
            }
            if (r != null) {
                tree.repaint(r);
            }
        }
        private boolean dragPressDidSelection;
        private boolean dragStarted;
        private TreePath pressedPath;
        private MouseEvent pressedEvent;
        private boolean valueChangedOnPress;
        private boolean isActualPath(TreePath path, int x, int y) {
            if (path == null) {
                return false;
            }
            Rectangle bounds = getPathBounds(tree, path);
            if (y > (bounds.y + bounds.height)) {
                return false;
            }
            return (x >= bounds.x) && (x <= (bounds.x + bounds.width));
        }
        public void mouseClicked(MouseEvent e) {
        }
        public void mouseEntered(MouseEvent e) {
        }
        public void mouseExited(MouseEvent e) {
        }
        public void mousePressed(MouseEvent e) {
            if (SwingUtilities2.shouldIgnore(e, tree)) {
                return;
            }
            if (isEditing(tree) && tree.getInvokesStopCellEditing()
                                && !stopEditing(tree)) {
                return;
            }
            completeEditing();
            pressedPath = getClosestPathForLocation(tree, e.getX(), e.getY());
            if (tree.getDragEnabled()) {
                mousePressedDND(e);
            } else {
                SwingUtilities2.adjustFocus(tree);
                handleSelection(e);
            }
        }
        private void mousePressedDND(MouseEvent e) {
            pressedEvent = e;
            boolean grabFocus = true;
            dragStarted = false;
            valueChangedOnPress = false;
            if (isActualPath(pressedPath, e.getX(), e.getY()) &&
                    DragRecognitionSupport.mousePressed(e)) {
                dragPressDidSelection = false;
                if (BasicGraphicsUtils.isMenuShortcutKeyDown(e)) {
                    return;
                } else if (!e.isShiftDown() && tree.isPathSelected(pressedPath)) {
                    setAnchorSelectionPath(pressedPath);
                    setLeadSelectionPath(pressedPath, true);
                    return;
                }
                dragPressDidSelection = true;
                grabFocus = false;
            }
            if (grabFocus) {
                SwingUtilities2.adjustFocus(tree);
            }
            handleSelection(e);
        }
        void handleSelection(MouseEvent e) {
            if(pressedPath != null) {
                Rectangle bounds = getPathBounds(tree, pressedPath);
                if(e.getY() >= (bounds.y + bounds.height)) {
                    return;
                }
                if(SwingUtilities.isLeftMouseButton(e)) {
                    checkForClickInExpandControl(pressedPath, e.getX(), e.getY());
                }
                int x = e.getX();
                if (x >= bounds.x && x < (bounds.x + bounds.width)) {
                    if (tree.getDragEnabled() || !startEditing(pressedPath, e)) {
                        selectPathForEvent(pressedPath, e);
                    }
                }
            }
        }
        public void dragStarting(MouseEvent me) {
            dragStarted = true;
            if (BasicGraphicsUtils.isMenuShortcutKeyDown(me)) {
                tree.addSelectionPath(pressedPath);
                setAnchorSelectionPath(pressedPath);
                setLeadSelectionPath(pressedPath, true);
            }
            pressedEvent = null;
            pressedPath = null;
        }
        public void mouseDragged(MouseEvent e) {
            if (SwingUtilities2.shouldIgnore(e, tree)) {
                return;
            }
            if (tree.getDragEnabled()) {
                DragRecognitionSupport.mouseDragged(e, this);
            }
        }
        public void mouseMoved(MouseEvent e) {
        }
        public void mouseReleased(MouseEvent e) {
            if (SwingUtilities2.shouldIgnore(e, tree)) {
                return;
            }
            if (tree.getDragEnabled()) {
                mouseReleasedDND(e);
            }
            pressedEvent = null;
            pressedPath = null;
        }
        private void mouseReleasedDND(MouseEvent e) {
            MouseEvent me = DragRecognitionSupport.mouseReleased(e);
            if (me != null) {
                SwingUtilities2.adjustFocus(tree);
                if (!dragPressDidSelection) {
                    handleSelection(me);
                }
            }
            if (!dragStarted) {
                if (pressedPath != null && !valueChangedOnPress &&
                        isActualPath(pressedPath, pressedEvent.getX(), pressedEvent.getY())) {
                    startEditingOnRelease(pressedPath, pressedEvent, e);
                }
            }
        }
        public void focusGained(FocusEvent e) {
            if(tree != null) {
                Rectangle                 pBounds;
                pBounds = getPathBounds(tree, tree.getLeadSelectionPath());
                if(pBounds != null)
                    tree.repaint(getRepaintPathBounds(pBounds));
                pBounds = getPathBounds(tree, getLeadSelectionPath());
                if(pBounds != null)
                    tree.repaint(getRepaintPathBounds(pBounds));
            }
        }
        public void focusLost(FocusEvent e) {
            focusGained(e);
        }
        public void editingStopped(ChangeEvent e) {
            completeEditing(false, false, true);
        }
        public void editingCanceled(ChangeEvent e) {
            completeEditing(false, false, false);
        }
        public void valueChanged(TreeSelectionEvent event) {
            valueChangedOnPress = true;
            completeEditing();
            if(tree.getExpandsSelectedPaths() && treeSelectionModel != null) {
                TreePath[]           paths = treeSelectionModel
                                         .getSelectionPaths();
                if(paths != null) {
                    for(int counter = paths.length - 1; counter >= 0;
                        counter--) {
                        TreePath path = paths[counter].getParentPath();
                        boolean expand = true;
                        while (path != null) {
                            if (treeModel.isLeaf(path.getLastPathComponent())){
                                expand = false;
                                path = null;
                            }
                            else {
                                path = path.getParentPath();
                            }
                        }
                        if (expand) {
                            tree.makeVisible(paths[counter]);
                        }
                    }
                }
            }
            TreePath oldLead = getLeadSelectionPath();
            lastSelectedRow = tree.getMinSelectionRow();
            TreePath lead = tree.getSelectionModel().getLeadSelectionPath();
            setAnchorSelectionPath(lead);
            setLeadSelectionPath(lead);
            TreePath[]       changedPaths = event.getPaths();
            Rectangle        nodeBounds;
            Rectangle        visRect = tree.getVisibleRect();
            boolean          paintPaths = true;
            int              nWidth = tree.getWidth();
            if(changedPaths != null) {
                int              counter, maxCounter = changedPaths.length;
                if(maxCounter > 4) {
                    tree.repaint();
                    paintPaths = false;
                }
                else {
                    for (counter = 0; counter < maxCounter; counter++) {
                        nodeBounds = getPathBounds(tree,
                                                   changedPaths[counter]);
                        if(nodeBounds != null &&
                           visRect.intersects(nodeBounds))
                            tree.repaint(0, nodeBounds.y, nWidth,
                                         nodeBounds.height);
                    }
                }
            }
            if(paintPaths) {
                nodeBounds = getPathBounds(tree, oldLead);
                if(nodeBounds != null && visRect.intersects(nodeBounds))
                    tree.repaint(0, nodeBounds.y, nWidth, nodeBounds.height);
                nodeBounds = getPathBounds(tree, lead);
                if(nodeBounds != null && visRect.intersects(nodeBounds))
                    tree.repaint(0, nodeBounds.y, nWidth, nodeBounds.height);
            }
        }
        public void treeExpanded(TreeExpansionEvent event) {
            if(event != null && tree != null) {
                TreePath      path = event.getPath();
                updateExpandedDescendants(path);
            }
        }
        public void treeCollapsed(TreeExpansionEvent event) {
            if(event != null && tree != null) {
                TreePath        path = event.getPath();
                completeEditing();
                if(path != null && tree.isVisible(path)) {
                    treeState.setExpandedState(path, false);
                    updateLeadSelectionRow();
                    updateSize();
                }
            }
        }
        public void treeNodesChanged(TreeModelEvent e) {
            if(treeState != null && e != null) {
                TreePath parentPath = e.getTreePath();
                int[] indices = e.getChildIndices();
                if (indices == null || indices.length == 0) {
                    treeState.treeNodesChanged(e);
                    updateSize();
                }
                else if (treeState.isExpanded(parentPath)) {
                    int minIndex = indices[0];
                    for (int i = indices.length - 1; i > 0; i--) {
                        minIndex = Math.min(indices[i], minIndex);
                    }
                    Object minChild = treeModel.getChild(
                            parentPath.getLastPathComponent(), minIndex);
                    TreePath minPath = parentPath.pathByAddingChild(minChild);
                    Rectangle minBounds = getPathBounds(tree, minPath);
                    treeState.treeNodesChanged(e);
                    updateSize0();
                    Rectangle newMinBounds = getPathBounds(tree, minPath);
                    if (indices.length == 1 &&
                            newMinBounds.height == minBounds.height) {
                        tree.repaint(0, minBounds.y, tree.getWidth(),
                                     minBounds.height);
                    }
                    else {
                        tree.repaint(0, minBounds.y, tree.getWidth(),
                                     tree.getHeight() - minBounds.y);
                    }
                }
                else {
                    treeState.treeNodesChanged(e);
                }
            }
        }
        public void treeNodesInserted(TreeModelEvent e) {
            if(treeState != null && e != null) {
                treeState.treeNodesInserted(e);
                updateLeadSelectionRow();
                TreePath       path = e.getTreePath();
                if(treeState.isExpanded(path)) {
                    updateSize();
                }
                else {
                    int[]      indices = e.getChildIndices();
                    int        childCount = treeModel.getChildCount
                                            (path.getLastPathComponent());
                    if(indices != null && (childCount - indices.length) == 0)
                        updateSize();
                }
            }
        }
        public void treeNodesRemoved(TreeModelEvent e) {
            if(treeState != null && e != null) {
                treeState.treeNodesRemoved(e);
                updateLeadSelectionRow();
                TreePath       path = e.getTreePath();
                if(treeState.isExpanded(path) ||
                   treeModel.getChildCount(path.getLastPathComponent()) == 0)
                    updateSize();
            }
        }
        public void treeStructureChanged(TreeModelEvent e) {
            if(treeState != null && e != null) {
                treeState.treeStructureChanged(e);
                updateLeadSelectionRow();
                TreePath       pPath = e.getTreePath();
                if (pPath != null) {
                    pPath = pPath.getParentPath();
                }
                if(pPath == null || treeState.isExpanded(pPath))
                    updateSize();
            }
        }
    }
    private static class Actions extends UIAction {
        private static final String SELECT_PREVIOUS = "selectPrevious";
        private static final String SELECT_PREVIOUS_CHANGE_LEAD =
                             "selectPreviousChangeLead";
        private static final String SELECT_PREVIOUS_EXTEND_SELECTION =
                             "selectPreviousExtendSelection";
        private static final String SELECT_NEXT = "selectNext";
        private static final String SELECT_NEXT_CHANGE_LEAD =
                                    "selectNextChangeLead";
        private static final String SELECT_NEXT_EXTEND_SELECTION =
                                    "selectNextExtendSelection";
        private static final String SELECT_CHILD = "selectChild";
        private static final String SELECT_CHILD_CHANGE_LEAD =
                                    "selectChildChangeLead";
        private static final String SELECT_PARENT = "selectParent";
        private static final String SELECT_PARENT_CHANGE_LEAD =
                                    "selectParentChangeLead";
        private static final String SCROLL_UP_CHANGE_SELECTION =
                                    "scrollUpChangeSelection";
        private static final String SCROLL_UP_CHANGE_LEAD =
                                    "scrollUpChangeLead";
        private static final String SCROLL_UP_EXTEND_SELECTION =
                                    "scrollUpExtendSelection";
        private static final String SCROLL_DOWN_CHANGE_SELECTION =
                                    "scrollDownChangeSelection";
        private static final String SCROLL_DOWN_EXTEND_SELECTION =
                                    "scrollDownExtendSelection";
        private static final String SCROLL_DOWN_CHANGE_LEAD =
                                    "scrollDownChangeLead";
        private static final String SELECT_FIRST = "selectFirst";
        private static final String SELECT_FIRST_CHANGE_LEAD =
                                    "selectFirstChangeLead";
        private static final String SELECT_FIRST_EXTEND_SELECTION =
                                    "selectFirstExtendSelection";
        private static final String SELECT_LAST = "selectLast";
        private static final String SELECT_LAST_CHANGE_LEAD =
                                    "selectLastChangeLead";
        private static final String SELECT_LAST_EXTEND_SELECTION =
                                    "selectLastExtendSelection";
        private static final String TOGGLE = "toggle";
        private static final String CANCEL_EDITING = "cancel";
        private static final String START_EDITING = "startEditing";
        private static final String SELECT_ALL = "selectAll";
        private static final String CLEAR_SELECTION = "clearSelection";
        private static final String SCROLL_LEFT = "scrollLeft";
        private static final String SCROLL_RIGHT = "scrollRight";
        private static final String SCROLL_LEFT_EXTEND_SELECTION =
                                    "scrollLeftExtendSelection";
        private static final String SCROLL_RIGHT_EXTEND_SELECTION =
                                    "scrollRightExtendSelection";
        private static final String SCROLL_RIGHT_CHANGE_LEAD =
                                    "scrollRightChangeLead";
        private static final String SCROLL_LEFT_CHANGE_LEAD =
                                    "scrollLeftChangeLead";
        private static final String EXPAND = "expand";
        private static final String COLLAPSE = "collapse";
        private static final String MOVE_SELECTION_TO_PARENT =
                                    "moveSelectionToParent";
        private static final String ADD_TO_SELECTION = "addToSelection";
        private static final String TOGGLE_AND_ANCHOR = "toggleAndAnchor";
        private static final String EXTEND_TO = "extendTo";
        private static final String MOVE_SELECTION_TO = "moveSelectionTo";
        Actions() {
            super(null);
        }
        Actions(String key) {
            super(key);
        }
        public boolean isEnabled(Object o) {
            if (o instanceof JTree) {
                if (getName() == CANCEL_EDITING) {
                    return ((JTree)o).isEditing();
                }
            }
            return true;
        }
        public void actionPerformed(ActionEvent e) {
            JTree tree = (JTree)e.getSource();
            BasicTreeUI ui = (BasicTreeUI)BasicLookAndFeel.getUIOfType(
                             tree.getUI(), BasicTreeUI.class);
            if (ui == null) {
                return;
            }
            String key = getName();
            if (key == SELECT_PREVIOUS) {
                increment(tree, ui, -1, false, true);
            }
            else if (key == SELECT_PREVIOUS_CHANGE_LEAD) {
                increment(tree, ui, -1, false, false);
            }
            else if (key == SELECT_PREVIOUS_EXTEND_SELECTION) {
                increment(tree, ui, -1, true, true);
            }
            else if (key == SELECT_NEXT) {
                increment(tree, ui, 1, false, true);
            }
            else if (key == SELECT_NEXT_CHANGE_LEAD) {
                increment(tree, ui, 1, false, false);
            }
            else if (key == SELECT_NEXT_EXTEND_SELECTION) {
                increment(tree, ui, 1, true, true);
            }
            else if (key == SELECT_CHILD) {
                traverse(tree, ui, 1, true);
            }
            else if (key == SELECT_CHILD_CHANGE_LEAD) {
                traverse(tree, ui, 1, false);
            }
            else if (key == SELECT_PARENT) {
                traverse(tree, ui, -1, true);
            }
            else if (key == SELECT_PARENT_CHANGE_LEAD) {
                traverse(tree, ui, -1, false);
            }
            else if (key == SCROLL_UP_CHANGE_SELECTION) {
                page(tree, ui, -1, false, true);
            }
            else if (key == SCROLL_UP_CHANGE_LEAD) {
                page(tree, ui, -1, false, false);
            }
            else if (key == SCROLL_UP_EXTEND_SELECTION) {
                page(tree, ui, -1, true, true);
            }
            else if (key == SCROLL_DOWN_CHANGE_SELECTION) {
                page(tree, ui, 1, false, true);
            }
            else if (key == SCROLL_DOWN_EXTEND_SELECTION) {
                page(tree, ui, 1, true, true);
            }
            else if (key == SCROLL_DOWN_CHANGE_LEAD) {
                page(tree, ui, 1, false, false);
            }
            else if (key == SELECT_FIRST) {
                home(tree, ui, -1, false, true);
            }
            else if (key == SELECT_FIRST_CHANGE_LEAD) {
                home(tree, ui, -1, false, false);
            }
            else if (key == SELECT_FIRST_EXTEND_SELECTION) {
                home(tree, ui, -1, true, true);
            }
            else if (key == SELECT_LAST) {
                home(tree, ui, 1, false, true);
            }
            else if (key == SELECT_LAST_CHANGE_LEAD) {
                home(tree, ui, 1, false, false);
            }
            else if (key == SELECT_LAST_EXTEND_SELECTION) {
                home(tree, ui, 1, true, true);
            }
            else if (key == TOGGLE) {
                toggle(tree, ui);
            }
            else if (key == CANCEL_EDITING) {
                cancelEditing(tree, ui);
            }
            else if (key == START_EDITING) {
                startEditing(tree, ui);
            }
            else if (key == SELECT_ALL) {
                selectAll(tree, ui, true);
            }
            else if (key == CLEAR_SELECTION) {
                selectAll(tree, ui, false);
            }
            else if (key == ADD_TO_SELECTION) {
                if (ui.getRowCount(tree) > 0) {
                    int lead = ui.getLeadSelectionRow();
                    if (!tree.isRowSelected(lead)) {
                        TreePath aPath = ui.getAnchorSelectionPath();
                        tree.addSelectionRow(lead);
                        ui.setAnchorSelectionPath(aPath);
                    }
                }
            }
            else if (key == TOGGLE_AND_ANCHOR) {
                if (ui.getRowCount(tree) > 0) {
                    int lead = ui.getLeadSelectionRow();
                    TreePath lPath = ui.getLeadSelectionPath();
                    if (!tree.isRowSelected(lead)) {
                        tree.addSelectionRow(lead);
                    } else {
                        tree.removeSelectionRow(lead);
                        ui.setLeadSelectionPath(lPath);
                    }
                    ui.setAnchorSelectionPath(lPath);
                }
            }
            else if (key == EXTEND_TO) {
                extendSelection(tree, ui);
            }
            else if (key == MOVE_SELECTION_TO) {
                if (ui.getRowCount(tree) > 0) {
                    int lead = ui.getLeadSelectionRow();
                    tree.setSelectionInterval(lead, lead);
                }
            }
            else if (key == SCROLL_LEFT) {
                scroll(tree, ui, SwingConstants.HORIZONTAL, -10);
            }
            else if (key == SCROLL_RIGHT) {
                scroll(tree, ui, SwingConstants.HORIZONTAL, 10);
            }
            else if (key == SCROLL_LEFT_EXTEND_SELECTION) {
                scrollChangeSelection(tree, ui, -1, true, true);
            }
            else if (key == SCROLL_RIGHT_EXTEND_SELECTION) {
                scrollChangeSelection(tree, ui, 1, true, true);
            }
            else if (key == SCROLL_RIGHT_CHANGE_LEAD) {
                scrollChangeSelection(tree, ui, 1, false, false);
            }
            else if (key == SCROLL_LEFT_CHANGE_LEAD) {
                scrollChangeSelection(tree, ui, -1, false, false);
            }
            else if (key == EXPAND) {
                expand(tree, ui);
            }
            else if (key == COLLAPSE) {
                collapse(tree, ui);
            }
            else if (key == MOVE_SELECTION_TO_PARENT) {
                moveSelectionToParent(tree, ui);
            }
        }
        private void scrollChangeSelection(JTree tree, BasicTreeUI ui,
                           int direction, boolean addToSelection,
                           boolean changeSelection) {
            int           rowCount;
            if((rowCount = ui.getRowCount(tree)) > 0 &&
                ui.treeSelectionModel != null) {
                TreePath          newPath;
                Rectangle         visRect = tree.getVisibleRect();
                if (direction == -1) {
                    newPath = ui.getClosestPathForLocation(tree, visRect.x,
                                                        visRect.y);
                    visRect.x = Math.max(0, visRect.x - visRect.width);
                }
                else {
                    visRect.x = Math.min(Math.max(0, tree.getWidth() -
                                   visRect.width), visRect.x + visRect.width);
                    newPath = ui.getClosestPathForLocation(tree, visRect.x,
                                                 visRect.y + visRect.height);
                }
                tree.scrollRectToVisible(visRect);
                if (addToSelection) {
                    ui.extendSelection(newPath);
                }
                else if(changeSelection) {
                    tree.setSelectionPath(newPath);
                }
                else {
                    ui.setLeadSelectionPath(newPath, true);
                }
            }
        }
        private void scroll(JTree component, BasicTreeUI ui, int direction,
                            int amount) {
            Rectangle visRect = component.getVisibleRect();
            Dimension size = component.getSize();
            if (direction == SwingConstants.HORIZONTAL) {
                visRect.x += amount;
                visRect.x = Math.max(0, visRect.x);
                visRect.x = Math.min(Math.max(0, size.width - visRect.width),
                                     visRect.x);
            }
            else {
                visRect.y += amount;
                visRect.y = Math.max(0, visRect.y);
                visRect.y = Math.min(Math.max(0, size.width - visRect.height),
                                     visRect.y);
            }
            component.scrollRectToVisible(visRect);
        }
        private void extendSelection(JTree tree, BasicTreeUI ui) {
            if (ui.getRowCount(tree) > 0) {
                int       lead = ui.getLeadSelectionRow();
                if (lead != -1) {
                    TreePath      leadP = ui.getLeadSelectionPath();
                    TreePath      aPath = ui.getAnchorSelectionPath();
                    int           aRow = ui.getRowForPath(tree, aPath);
                    if(aRow == -1)
                        aRow = 0;
                    tree.setSelectionInterval(aRow, lead);
                    ui.setLeadSelectionPath(leadP);
                    ui.setAnchorSelectionPath(aPath);
                }
            }
        }
        private void selectAll(JTree tree, BasicTreeUI ui, boolean selectAll) {
            int                   rowCount = ui.getRowCount(tree);
            if(rowCount > 0) {
                if(selectAll) {
                    if (tree.getSelectionModel().getSelectionMode() ==
                            TreeSelectionModel.SINGLE_TREE_SELECTION) {
                        int lead = ui.getLeadSelectionRow();
                        if (lead != -1) {
                            tree.setSelectionRow(lead);
                        } else if (tree.getMinSelectionRow() == -1) {
                            tree.setSelectionRow(0);
                            ui.ensureRowsAreVisible(0, 0);
                        }
                        return;
                    }
                    TreePath      lastPath = ui.getLeadSelectionPath();
                    TreePath      aPath = ui.getAnchorSelectionPath();
                    if(lastPath != null && !tree.isVisible(lastPath)) {
                        lastPath = null;
                    }
                    tree.setSelectionInterval(0, rowCount - 1);
                    if(lastPath != null) {
                        ui.setLeadSelectionPath(lastPath);
                    }
                    if(aPath != null && tree.isVisible(aPath)) {
                        ui.setAnchorSelectionPath(aPath);
                    }
                }
                else {
                    TreePath      lastPath = ui.getLeadSelectionPath();
                    TreePath      aPath = ui.getAnchorSelectionPath();
                    tree.clearSelection();
                    ui.setAnchorSelectionPath(aPath);
                    ui.setLeadSelectionPath(lastPath);
                }
            }
        }
        private void startEditing(JTree tree, BasicTreeUI ui) {
            TreePath   lead = ui.getLeadSelectionPath();
            int        editRow = (lead != null) ?
                                     ui.getRowForPath(tree, lead) : -1;
            if(editRow != -1) {
                tree.startEditingAtPath(lead);
            }
        }
        private void cancelEditing(JTree tree, BasicTreeUI ui) {
            tree.cancelEditing();
        }
        private void toggle(JTree tree, BasicTreeUI ui) {
            int            selRow = ui.getLeadSelectionRow();
            if(selRow != -1 && !ui.isLeaf(selRow)) {
                TreePath aPath = ui.getAnchorSelectionPath();
                TreePath lPath = ui.getLeadSelectionPath();
                ui.toggleExpandState(ui.getPathForRow(tree, selRow));
                ui.setAnchorSelectionPath(aPath);
                ui.setLeadSelectionPath(lPath);
            }
        }
        private void expand(JTree tree, BasicTreeUI ui) {
            int selRow = ui.getLeadSelectionRow();
            tree.expandRow(selRow);
        }
        private void collapse(JTree tree, BasicTreeUI ui) {
            int selRow = ui.getLeadSelectionRow();
            tree.collapseRow(selRow);
        }
        private void increment(JTree tree, BasicTreeUI ui, int direction,
                               boolean addToSelection,
                               boolean changeSelection) {
            if (!addToSelection && !changeSelection &&
                    tree.getSelectionModel().getSelectionMode() !=
                        TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION) {
                changeSelection = true;
            }
            int              rowCount;
            if(ui.treeSelectionModel != null &&
                  (rowCount = tree.getRowCount()) > 0) {
                int                  selIndex = ui.getLeadSelectionRow();
                int                  newIndex;
                if(selIndex == -1) {
                    if(direction == 1)
                        newIndex = 0;
                    else
                        newIndex = rowCount - 1;
                }
                else
                    newIndex = Math.min(rowCount - 1, Math.max
                                        (0, (selIndex + direction)));
                if(addToSelection && ui.treeSelectionModel.
                        getSelectionMode() != TreeSelectionModel.
                        SINGLE_TREE_SELECTION) {
                    ui.extendSelection(tree.getPathForRow(newIndex));
                }
                else if(changeSelection) {
                    tree.setSelectionInterval(newIndex, newIndex);
                }
                else {
                    ui.setLeadSelectionPath(tree.getPathForRow(newIndex),true);
                }
                ui.ensureRowsAreVisible(newIndex, newIndex);
                ui.lastSelectedRow = newIndex;
            }
        }
        private void traverse(JTree tree, BasicTreeUI ui, int direction,
                              boolean changeSelection) {
            if (!changeSelection &&
                    tree.getSelectionModel().getSelectionMode() !=
                        TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION) {
                changeSelection = true;
            }
            int                rowCount;
            if((rowCount = tree.getRowCount()) > 0) {
                int               minSelIndex = ui.getLeadSelectionRow();
                int               newIndex;
                if(minSelIndex == -1)
                    newIndex = 0;
                else {
                    if(direction == 1) {
                        TreePath minSelPath = ui.getPathForRow(tree, minSelIndex);
                        int childCount = tree.getModel().
                            getChildCount(minSelPath.getLastPathComponent());
                        newIndex = -1;
                        if (!ui.isLeaf(minSelIndex)) {
                            if (!tree.isExpanded(minSelIndex)) {
                                ui.toggleExpandState(minSelPath);
                            }
                            else if (childCount > 0) {
                                newIndex = Math.min(minSelIndex + 1, rowCount - 1);
                            }
                        }
                    }
                    else {
                        if(!ui.isLeaf(minSelIndex) &&
                           tree.isExpanded(minSelIndex)) {
                            ui.toggleExpandState(ui.getPathForRow
                                              (tree, minSelIndex));
                            newIndex = -1;
                        }
                        else {
                            TreePath         path = ui.getPathForRow(tree,
                                                                  minSelIndex);
                            if(path != null && path.getPathCount() > 1) {
                                newIndex = ui.getRowForPath(tree, path.
                                                         getParentPath());
                            }
                            else
                                newIndex = -1;
                        }
                    }
                }
                if(newIndex != -1) {
                    if(changeSelection) {
                        tree.setSelectionInterval(newIndex, newIndex);
                    }
                    else {
                        ui.setLeadSelectionPath(ui.getPathForRow(
                                                    tree, newIndex), true);
                    }
                    ui.ensureRowsAreVisible(newIndex, newIndex);
                }
            }
        }
        private void moveSelectionToParent(JTree tree, BasicTreeUI ui) {
            int selRow = ui.getLeadSelectionRow();
            TreePath path = ui.getPathForRow(tree, selRow);
            if (path != null && path.getPathCount() > 1) {
                int  newIndex = ui.getRowForPath(tree, path.getParentPath());
                if (newIndex != -1) {
                    tree.setSelectionInterval(newIndex, newIndex);
                    ui.ensureRowsAreVisible(newIndex, newIndex);
                }
            }
        }
        private void page(JTree tree, BasicTreeUI ui, int direction,
                          boolean addToSelection, boolean changeSelection) {
            if (!addToSelection && !changeSelection &&
                    tree.getSelectionModel().getSelectionMode() !=
                        TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION) {
                changeSelection = true;
            }
            int           rowCount;
            if((rowCount = ui.getRowCount(tree)) > 0 &&
                           ui.treeSelectionModel != null) {
                Dimension         maxSize = tree.getSize();
                TreePath          lead = ui.getLeadSelectionPath();
                TreePath          newPath;
                Rectangle         visRect = tree.getVisibleRect();
                if(direction == -1) {
                    newPath = ui.getClosestPathForLocation(tree, visRect.x,
                                                         visRect.y);
                    if(newPath.equals(lead)) {
                        visRect.y = Math.max(0, visRect.y - visRect.height);
                        newPath = tree.getClosestPathForLocation(visRect.x,
                                                                 visRect.y);
                    }
                }
                else {
                    visRect.y = Math.min(maxSize.height, visRect.y +
                                         visRect.height - 1);
                    newPath = tree.getClosestPathForLocation(visRect.x,
                                                             visRect.y);
                    if(newPath.equals(lead)) {
                        visRect.y = Math.min(maxSize.height, visRect.y +
                                             visRect.height - 1);
                        newPath = tree.getClosestPathForLocation(visRect.x,
                                                                 visRect.y);
                    }
                }
                Rectangle            newRect = ui.getPathBounds(tree, newPath);
                newRect.x = visRect.x;
                newRect.width = visRect.width;
                if(direction == -1) {
                    newRect.height = visRect.height;
                }
                else {
                    newRect.y -= (visRect.height - newRect.height);
                    newRect.height = visRect.height;
                }
                if(addToSelection) {
                    ui.extendSelection(newPath);
                }
                else if(changeSelection) {
                    tree.setSelectionPath(newPath);
                }
                else {
                    ui.setLeadSelectionPath(newPath, true);
                }
                tree.scrollRectToVisible(newRect);
            }
        }
        private void home(JTree tree, BasicTreeUI ui, int direction,
                          boolean addToSelection, boolean changeSelection) {
            if (!addToSelection && !changeSelection &&
                    tree.getSelectionModel().getSelectionMode() !=
                        TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION) {
                changeSelection = true;
            }
            int rowCount = ui.getRowCount(tree);
            if (rowCount > 0) {
                if(direction == -1) {
                    ui.ensureRowsAreVisible(0, 0);
                    if (addToSelection) {
                        TreePath        aPath = ui.getAnchorSelectionPath();
                        int             aRow = (aPath == null) ? -1 :
                                        ui.getRowForPath(tree, aPath);
                        if (aRow == -1) {
                            tree.setSelectionInterval(0, 0);
                        }
                        else {
                            tree.setSelectionInterval(0, aRow);
                            ui.setAnchorSelectionPath(aPath);
                            ui.setLeadSelectionPath(ui.getPathForRow(tree, 0));
                        }
                    }
                    else if(changeSelection) {
                        tree.setSelectionInterval(0, 0);
                    }
                    else {
                        ui.setLeadSelectionPath(ui.getPathForRow(tree, 0),
                                                true);
                    }
                }
                else {
                    ui.ensureRowsAreVisible(rowCount - 1, rowCount - 1);
                    if (addToSelection) {
                        TreePath        aPath = ui.getAnchorSelectionPath();
                        int             aRow = (aPath == null) ? -1 :
                                        ui.getRowForPath(tree, aPath);
                        if (aRow == -1) {
                            tree.setSelectionInterval(rowCount - 1,
                                                      rowCount -1);
                        }
                        else {
                            tree.setSelectionInterval(aRow, rowCount - 1);
                            ui.setAnchorSelectionPath(aPath);
                            ui.setLeadSelectionPath(ui.getPathForRow(tree,
                                                               rowCount -1));
                        }
                    }
                    else if(changeSelection) {
                        tree.setSelectionInterval(rowCount - 1, rowCount - 1);
                    }
                    else {
                        ui.setLeadSelectionPath(ui.getPathForRow(tree,
                                                          rowCount - 1), true);
                    }
                }
            }
        }
    }
} 
