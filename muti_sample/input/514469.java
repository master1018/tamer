public class ProfileView extends Composite implements Observer {
    private TreeViewer mTreeViewer;
    private Text mSearchBox;
    private SelectionController mSelectionController;
    private ProfileProvider mProfileProvider;
    private Color mColorNoMatch;
    private Color mColorMatch;
    private MethodData mCurrentHighlightedMethod;
    public ProfileView(Composite parent, TraceReader reader,
            SelectionController selectController) {
        super(parent, SWT.NONE);
        setLayout(new GridLayout(1, false));
        this.mSelectionController = selectController;
        mSelectionController.addObserver(this);
        mTreeViewer = new TreeViewer(this, SWT.MULTI | SWT.NONE);
        mTreeViewer.setUseHashlookup(true);
        mProfileProvider = reader.getProfileProvider();
        mProfileProvider.setTreeViewer(mTreeViewer);
        SelectionAdapter listener = mProfileProvider.getColumnListener();
        final Tree tree = mTreeViewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLayoutData(new GridData(GridData.FILL_BOTH));
        String[] columnNames = mProfileProvider.getColumnNames();
        int[] columnWidths = mProfileProvider.getColumnWidths();
        int[] columnAlignments = mProfileProvider.getColumnAlignments();
        for (int ii = 0; ii < columnWidths.length; ++ii) {
            TreeColumn column = new TreeColumn(tree, SWT.LEFT);
            column.setText(columnNames[ii]);
            column.setWidth(columnWidths[ii]);
            column.setMoveable(true);
            column.addSelectionListener(listener);
            column.setAlignment(columnAlignments[ii]);
        }
        tree.addListener(SWT.MeasureItem, new Listener() {
            public void handleEvent(Event event) {
                int fontHeight = event.gc.getFontMetrics().getHeight();
                event.height = fontHeight;
            }
        });
        mTreeViewer.setContentProvider(mProfileProvider);
        mTreeViewer.setLabelProvider(mProfileProvider.getLabelProvider());
        mTreeViewer.setInput(mProfileProvider.getRoot());
        Composite composite = new Composite(this, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label label = new Label(composite, SWT.NONE);
        label.setText("Find:");
        mSearchBox = new Text(composite, SWT.BORDER);
        mSearchBox.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Display display = getDisplay();
        mColorNoMatch = new Color(display, 255, 200, 200);
        mColorMatch = mSearchBox.getBackground();
        mSearchBox.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent ev) {
                String query = mSearchBox.getText();
                if (query.length() == 0)
                    return;
                findName(query);
            }
        });
        mSearchBox.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.keyCode == SWT.ESC) {
                    mSearchBox.setText("");
                } else if (event.keyCode == SWT.CR) {
                    String query = mSearchBox.getText();
                    if (query.length() == 0)
                        return;
                    findNextName(query);
                }
            }
        });
        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.keyCode == SWT.ESC) {
                    mSearchBox.setText("");
                } else if (event.keyCode == SWT.BS) {
                    String text = mSearchBox.getText();
                    int len = text.length();
                    String chopped;
                    if (len <= 1)
                        chopped = "";
                    else
                        chopped = text.substring(0, len - 1);
                    mSearchBox.setText(chopped);
                } else if (event.keyCode == SWT.CR) {
                    String query = mSearchBox.getText();
                    if (query.length() == 0)
                        return;
                    findNextName(query);
                } else {
                    String str = String.valueOf(event.character);
                    mSearchBox.append(str);
                }
                event.doit = false;
            }
        });
        mTreeViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent ev) {
                        ISelection sel = ev.getSelection();
                        if (sel.isEmpty())
                            return;
                        if (sel instanceof IStructuredSelection) {
                            IStructuredSelection selection = (IStructuredSelection) sel;
                            Object element = selection.getFirstElement();
                            if (element == null)
                                return;
                            if (element instanceof MethodData) {
                                MethodData md = (MethodData) element;
                                highlightMethod(md, true);
                            }
                            if (element instanceof ProfileData) {
                                MethodData md = ((ProfileData) element)
                                        .getMethodData();
                                highlightMethod(md, true);
                            }
                        }
                    }
                });
        mTreeViewer.addTreeListener(new ITreeViewerListener() {
            public void treeExpanded(TreeExpansionEvent event) {
                Object element = event.getElement();
                if (element instanceof MethodData) {
                    MethodData md = (MethodData) element;
                    expandNode(md);
                }
            }
            public void treeCollapsed(TreeExpansionEvent event) {
            }
        });
        tree.addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event event) {
                Point point = new Point(event.x, event.y);
                TreeItem treeItem = tree.getItem(point);
                MethodData md = mProfileProvider.findMatchingTreeItem(treeItem);
                if (md == null)
                    return;
                ArrayList<Selection> selections = new ArrayList<Selection>();
                selections.add(Selection.highlight("MethodData", md));
                mSelectionController.change(selections, "ProfileView");
            }
        });
    }
    private void findName(String query) {
        MethodData md = mProfileProvider.findMatchingName(query);
        selectMethod(md);
    }
    private void findNextName(String query) {
        MethodData md = mProfileProvider.findNextMatchingName(query);
        selectMethod(md);
    }
    private void selectMethod(MethodData md) {
        if (md == null) {
            mSearchBox.setBackground(mColorNoMatch);
            return;
        }
        mSearchBox.setBackground(mColorMatch);
        highlightMethod(md, false);
    }
    public void update(Observable objservable, Object arg) {
        if (arg == "ProfileView")
            return;
        ArrayList<Selection> selections;
        selections = mSelectionController.getSelections();
        for (Selection selection : selections) {
            Selection.Action action = selection.getAction();
            if (action != Selection.Action.Highlight)
                continue;
            String name = selection.getName();
            if (name == "MethodData") {
                MethodData md = (MethodData) selection.getValue();
                highlightMethod(md, true);
                return;
            }
            if (name == "Call") {
                Call call = (Call) selection.getValue();
                MethodData md = call.mMethodData;
                highlightMethod(md, true);
                return;
            }
        }
    }
    private void highlightMethod(MethodData md, boolean clearSearch) {
        if (md == null)
            return;
        if (md == mCurrentHighlightedMethod)
            return;
        if (clearSearch) {
            mSearchBox.setText("");
            mSearchBox.setBackground(mColorMatch);
        }
        mCurrentHighlightedMethod = md;
        mTreeViewer.collapseAll();
        expandNode(md);
        StructuredSelection sel = new StructuredSelection(md);
        mTreeViewer.setSelection(sel, true);
        Tree tree = mTreeViewer.getTree();
        TreeItem[] items = tree.getSelection();
        tree.setTopItem(items[0]);
        tree.showItem(items[0]);
    }
    private void expandNode(MethodData md) {
        ProfileNode[] nodes = md.getProfileNodes();
        mTreeViewer.setExpandedState(md, true);
        for (ProfileNode node : nodes) {
            if (node.isRecursive() == false)
                mTreeViewer.setExpandedState(node, true);
        }
    }
}
