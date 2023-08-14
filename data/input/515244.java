public final class UiTreeBlock extends MasterDetailsBlock implements ICommitXml {
    private static final int TREE_HEIGHT_HINT = 50;
    AndroidEditor mEditor;
    private UiElementNode mUiRootNode;
    private ElementDescriptor[] mDescriptorFilters;
    private String mTitle;
    private String mDescription;
    private ManifestSectionPart mMasterPart;
    private TreeViewer mTreeViewer;
    private Button mAddButton;
    private Button mRemoveButton;
    private Button mUpButton;
    private Button mDownButton;
    private IManagedForm mManagedForm;
    private DetailsPart mDetailsPart;
    private Clipboard mClipboard;
    private IUiUpdateListener mUiRefreshListener;
    private IUiUpdateListener mUiEnableListener;
    private UiTreeActions mUiTreeActions;
    private final boolean mAutoCreateRoot;
    public UiTreeBlock(AndroidEditor editor,
            UiElementNode uiRootNode,
            boolean autoCreateRoot,
            ElementDescriptor[] descriptorFilters,
            String title,
            String description) {
        mEditor = editor;
        mUiRootNode = uiRootNode;
        mAutoCreateRoot = autoCreateRoot;
        mDescriptorFilters = descriptorFilters;
        mTitle = title;
        mDescription = description;
    }
    AndroidEditor getEditor() {
        return mEditor;
    }
    Clipboard getClipboard() {
        return mClipboard;
    }
    ManifestSectionPart getMasterPart() {
        return mMasterPart;
    }
    public UiElementNode getRootNode() {
        return mUiRootNode;
    }
    @Override
    protected void createMasterPart(final IManagedForm managedForm, Composite parent) {
        FormToolkit toolkit = managedForm.getToolkit();
        mManagedForm = managedForm;
        mMasterPart = new ManifestSectionPart(parent, toolkit);
        Section section = mMasterPart.getSection();
        section.setText(mTitle);
        section.setDescription(mDescription);
        section.setLayout(new GridLayout());
        section.setLayoutData(new GridData(GridData.FILL_BOTH));
        Composite grid = SectionHelper.createGridLayout(section, toolkit, 2);
        Tree tree = createTreeViewer(toolkit, grid, managedForm);
        createButtons(toolkit, grid);
        createTreeContextMenu(tree);
        createSectionActions(section, toolkit);
    }
    private void createSectionActions(Section section, FormToolkit toolkit) {
        ToolBarManager manager = new ToolBarManager(SWT.FLAT);
        manager.removeAll();
        ToolBar toolbar = manager.createControl(section);
        section.setTextClient(toolbar);
        ElementDescriptor[] descs = mDescriptorFilters;
        if (descs == null && mUiRootNode != null) {
            descs = mUiRootNode.getDescriptor().getChildren();
        }
        if (descs != null && descs.length > 1) {
            for (ElementDescriptor desc : descs) {
                manager.add(new DescriptorFilterAction(desc));
            }
        }
        manager.add(new TreeSortAction());
        manager.update(true );
    }
    private Tree createTreeViewer(FormToolkit toolkit, Composite grid,
            final IManagedForm managedForm) {
        final Tree tree = toolkit.createTree(grid, SWT.MULTI);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = AndroidEditor.TEXT_WIDTH_HINT;
        gd.heightHint = TREE_HEIGHT_HINT;
        tree.setLayoutData(gd);
        mTreeViewer = new TreeViewer(tree);
        mTreeViewer.setContentProvider(new UiModelTreeContentProvider(mUiRootNode, mDescriptorFilters));
        mTreeViewer.setLabelProvider(new UiModelTreeLabelProvider());
        mTreeViewer.setInput("unused"); 
        mTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                managedForm.fireSelectionChanged(mMasterPart, event.getSelection());
                adjustTreeButtons(event.getSelection());
            }
        });
        mUiRefreshListener = new IUiUpdateListener() {
            public void uiElementNodeUpdated(UiElementNode ui_node, UiUpdateState state) {
                mTreeViewer.refresh();
            }
        };
        mUiEnableListener = new IUiUpdateListener() {
            public void uiElementNodeUpdated(UiElementNode ui_node, UiUpdateState state) {
                boolean exists = mAutoCreateRoot || (ui_node.getXmlNode() != null);
                if (mMasterPart != null) {
                    Section section = mMasterPart.getSection();
                    if (section.getEnabled() != exists) {
                        section.setEnabled(exists);
                        for (Control c : section.getChildren()) {
                            c.setEnabled(exists);
                        }
                    }
                }
            }
        };
        final ITargetChangeListener targetListener = new TargetChangeListener() {
            @Override
            public IProject getProject() {
                if (mEditor != null) {
                    return mEditor.getProject();
                }
                return null;
            }
            @Override
            public void reload() {
                if (mDetailsPart != null) {
                    int limit = mDetailsPart.getPageLimit();
                    mDetailsPart.setPageLimit(0);
                    mDetailsPart.setPageLimit(limit);
                }
                mTreeViewer.refresh();
            }
        };
        changeRootAndDescriptors(mUiRootNode, mDescriptorFilters, false );
        AdtPlugin.getDefault().addTargetListener(targetListener);
        tree.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                UiElementNode node = mUiRootNode.getUiParent() != null ?
                                        mUiRootNode.getUiParent() :
                                        mUiRootNode;
                node.removeUpdateListener(mUiRefreshListener);
                mUiRootNode.removeUpdateListener(mUiEnableListener);
                AdtPlugin.getDefault().removeTargetListener(targetListener);
                if (mClipboard != null) {
                    mClipboard.dispose();
                    mClipboard = null;
                }
            }
        });
        mClipboard = new Clipboard(tree.getDisplay());
        return tree;
    }
    public void changeRootAndDescriptors(UiElementNode uiRootNode,
            ElementDescriptor[] descriptorFilters, boolean forceRefresh) {
        UiElementNode node;
        if (mUiRootNode != null) {
            node = mUiRootNode.getUiParent() != null ? mUiRootNode.getUiParent() : mUiRootNode;
            node.removeUpdateListener(mUiRefreshListener);
            mUiRootNode.removeUpdateListener(mUiEnableListener);
        }
        mUiRootNode = uiRootNode;
        mDescriptorFilters = descriptorFilters;
        mTreeViewer.setContentProvider(new UiModelTreeContentProvider(mUiRootNode, mDescriptorFilters));
        node = mUiRootNode.getUiParent() != null ? mUiRootNode.getUiParent() : mUiRootNode;
        node.addUpdateListener(mUiRefreshListener);
        mUiRootNode.addUpdateListener(mUiEnableListener);
        mUiEnableListener.uiElementNodeUpdated(mUiRootNode, null );
        if (forceRefresh) {
            mTreeViewer.refresh();
        }
        createSectionActions(mMasterPart.getSection(), mManagedForm.getToolkit());
    }
    private void createButtons(FormToolkit toolkit, Composite grid) {
        mUiTreeActions = new UiTreeActions();
        Composite button_grid = SectionHelper.createGridLayout(grid, toolkit, 1);
        button_grid.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        mAddButton = toolkit.createButton(button_grid, "Add...", SWT.PUSH);
        SectionHelper.addControlTooltip(mAddButton, "Adds a new element.");
        mAddButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL |
                GridData.VERTICAL_ALIGN_BEGINNING));
        mAddButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                doTreeAdd();
            }
        });
        mRemoveButton = toolkit.createButton(button_grid, "Remove...", SWT.PUSH);
        SectionHelper.addControlTooltip(mRemoveButton, "Removes an existing selected element.");
        mRemoveButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mRemoveButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                doTreeRemove();
            }
        });
        mUpButton = toolkit.createButton(button_grid, "Up", SWT.PUSH);
        SectionHelper.addControlTooltip(mRemoveButton, "Moves the selected element up.");
        mUpButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mUpButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                doTreeUp();
            }
        });
        mDownButton = toolkit.createButton(button_grid, "Down", SWT.PUSH);
        SectionHelper.addControlTooltip(mRemoveButton, "Moves the selected element down.");
        mDownButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mDownButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                super.widgetSelected(e);
                doTreeDown();
            }
        });
        adjustTreeButtons(TreeSelection.EMPTY);
    }
    private void createTreeContextMenu(Tree tree) {
        MenuManager menuManager = new MenuManager();
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(new IMenuListener() {
           public void menuAboutToShow(IMenuManager manager) {
               ISelection selection = mTreeViewer.getSelection();
               if (!selection.isEmpty() && selection instanceof ITreeSelection) {
                   ArrayList<UiElementNode> selected = filterSelection((ITreeSelection) selection);
                   doCreateMenuAction(manager, selected);
                   return;
               }
               doCreateMenuAction(manager, null );
            }
        });
        Menu contextMenu = menuManager.createContextMenu(tree);
        tree.setMenu(contextMenu);
    }
    private void doCreateMenuAction(IMenuManager manager, ArrayList<UiElementNode> selected) {
        if (selected != null) {
            boolean hasXml = false;
            for (UiElementNode uiNode : selected) {
                if (uiNode.getXmlNode() != null) {
                    hasXml = true;
                    break;
                }
            }
            if (hasXml) {
                manager.add(new CopyCutAction(getEditor(), getClipboard(),
                        null, selected, true ));
                manager.add(new CopyCutAction(getEditor(), getClipboard(),
                        null, selected, false ));
                if (selected.size() <= 1) {
                    UiElementNode ui_root = selected.get(0).getUiRoot();
                    if (ui_root.getDescriptor().hasChildren() ||
                            !(ui_root.getUiParent() instanceof UiDocumentNode)) {
                        manager.add(new PasteAction(getEditor(), getClipboard(), selected.get(0)));
                    }
                }
                manager.add(new Separator());
            }
        }
        IconFactory factory = IconFactory.getInstance();
        if (selected == null || selected.size() <= 1) {
            manager.add(new Action("Add...", factory.getImageDescriptor("add")) { 
                @Override
                public void run() {
                    super.run();
                    doTreeAdd();
                }
            });
        }
        if (selected != null) {
            if (selected != null) {
                manager.add(new Action("Remove", factory.getImageDescriptor("delete")) { 
                    @Override
                    public void run() {
                        super.run();
                        doTreeRemove();
                    }
                });
            }
            manager.add(new Separator());
            manager.add(new Action("Up", factory.getImageDescriptor("up")) { 
                @Override
                public void run() {
                    super.run();
                    doTreeUp();
                }
            });
            manager.add(new Action("Down", factory.getImageDescriptor("down")) { 
                @Override
                public void run() {
                    super.run();
                    doTreeDown();
                }
            });
        }
    }
    private void adjustTreeButtons(ISelection selection) {
        mRemoveButton.setEnabled(!selection.isEmpty() && selection instanceof ITreeSelection);
        mUpButton.setEnabled(!selection.isEmpty() && selection instanceof ITreeSelection);
        mDownButton.setEnabled(!selection.isEmpty() && selection instanceof ITreeSelection);
    }
    private class UiTreeActions extends UiActions {
        @Override
        protected UiElementNode getRootNode() {
            return mUiRootNode;
        }
        @Override
        protected void selectUiNode(UiElementNode uiNodeToSelect) {
            if (uiNodeToSelect != null) {
                LinkedList<UiElementNode> segments = new LinkedList<UiElementNode>();
                for (UiElementNode ui_node = uiNodeToSelect; ui_node != mUiRootNode;
                        ui_node = ui_node.getUiParent()) {
                    segments.add(0, ui_node);
                }
                if (segments.size() > 0) {
                    mTreeViewer.setSelection(new TreeSelection(new TreePath(segments.toArray())));
                } else {
                    mTreeViewer.setSelection(null);
                }
            }
        }
        @Override
        public void commitPendingXmlChanges() {
            commitManagedForm();
        }
    }
    @SuppressWarnings("unchecked")
    private ArrayList<UiElementNode> filterSelection(ITreeSelection selection) {
        ArrayList<UiElementNode> selected = new ArrayList<UiElementNode>();
        for (Iterator it = selection.iterator(); it.hasNext(); ) {
            Object selectedObj = it.next();
            if (selectedObj instanceof UiElementNode) {
                selected.add((UiElementNode) selectedObj);
            }
        }
        return selected.size() > 0 ? selected : null;
    }
    private void doTreeAdd() {
        UiElementNode ui_node = mUiRootNode;
        ISelection selection = mTreeViewer.getSelection();
        if (!selection.isEmpty() && selection instanceof ITreeSelection) {
            ITreeSelection tree_selection = (ITreeSelection) selection;
            Object first = tree_selection.getFirstElement();
            if (first != null && first instanceof UiElementNode) {
                ui_node = (UiElementNode) first;
            }
        }
        mUiTreeActions.doAdd(
                ui_node,
                mDescriptorFilters,
                mTreeViewer.getControl().getShell(),
                (ILabelProvider) mTreeViewer.getLabelProvider());
    }
    protected void doTreeRemove() {
        ISelection selection = mTreeViewer.getSelection();
        if (!selection.isEmpty() && selection instanceof ITreeSelection) {
            ArrayList<UiElementNode> selected = filterSelection((ITreeSelection) selection);
            mUiTreeActions.doRemove(selected, mTreeViewer.getControl().getShell());
        }
    }
    protected void doTreeUp() {
        ISelection selection = mTreeViewer.getSelection();
        if (!selection.isEmpty() && selection instanceof ITreeSelection) {
            ArrayList<UiElementNode> selected = filterSelection((ITreeSelection) selection);
            mUiTreeActions.doUp(selected);
        }
    }
    protected void doTreeDown() {
        ISelection selection = mTreeViewer.getSelection();
        if (!selection.isEmpty() && selection instanceof ITreeSelection) {
            ArrayList<UiElementNode> selected = filterSelection((ITreeSelection) selection);
            mUiTreeActions.doDown(selected);
        }
    }
    void commitManagedForm() {
        if (mManagedForm != null) {
            mManagedForm.commit(false );
        }
    }
    public void commitPendingXmlChanges() {
        commitManagedForm();
    }
    @Override
    protected void createToolBarActions(IManagedForm managedForm) {
    }
    @Override
    protected void registerPages(DetailsPart detailsPart) {
        mDetailsPart = detailsPart;
        detailsPart.setPageLimit(5);
        final UiTreeBlock tree = this;
        detailsPart.setPageProvider(new IDetailsPageProvider() {
            public IDetailsPage getPage(Object key) {
                if (key instanceof UiElementNode) {
                    return new UiElementDetail(tree);
                }
                return null;
            }
            public Object getPageKey(Object object) {
                return object;  
            }
        });
    }
    private class TreeSortAction extends Action {
        private ViewerComparator mComparator;
        public TreeSortAction() {
            super("Sorts elements alphabetically.", AS_CHECK_BOX);
            setImageDescriptor(IconFactory.getInstance().getImageDescriptor("az_sort")); 
            if (mTreeViewer != null) {
                boolean is_sorted = mTreeViewer.getComparator() != null;
                setChecked(is_sorted);
            }
        }
        @Override
        public void run() {
            if (mTreeViewer == null) {
                notifyResult(false );
                return;
            }
            ViewerComparator comp = mTreeViewer.getComparator();
            if (comp != null) {
                mComparator = comp;
                mTreeViewer.setComparator(null);
            } else {
                if (mComparator == null) {
                    mComparator = new ViewerComparator();
                }
                mTreeViewer.setComparator(mComparator);
            }
            notifyResult(true );
        }
    }
    private class DescriptorFilterAction extends Action {
        private final ElementDescriptor mDescriptor;
        private ViewerFilter mFilter;
        public DescriptorFilterAction(ElementDescriptor descriptor) {
            super(String.format("Displays only %1$s elements.", descriptor.getUiName()),
                    AS_CHECK_BOX);
            mDescriptor = descriptor;
            setImageDescriptor(descriptor.getImageDescriptor());
        }
        @Override
        public void run() {
            super.run();
            if (isChecked()) {
                if (mFilter == null) {
                    mFilter = new DescriptorFilter(this);
                }
                mTreeViewer.addFilter(mFilter);
                for (ViewerFilter filter : mTreeViewer.getFilters()) {
                    if (filter instanceof DescriptorFilter && filter != mFilter) {
                        DescriptorFilterAction action = ((DescriptorFilter) filter).getAction();
                        action.setChecked(false);
                        mTreeViewer.removeFilter(filter);
                    }
                }
            } else if (mFilter != null){
                mTreeViewer.removeFilter(mFilter);
            }
        }
        private class DescriptorFilter extends ViewerFilter {
            private final DescriptorFilterAction mAction;
            public DescriptorFilter(DescriptorFilterAction action) {
                mAction = action;
            }
            public DescriptorFilterAction getAction() {
                return mAction;
            }
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                while (element instanceof UiElementNode) {
                    UiElementNode uiNode = (UiElementNode)element;
                    if (uiNode.getDescriptor() == mDescriptor) {
                        return true;
                    }
                    element = uiNode.getUiParent();
                }
                return false;
            }
        }
    }
}
