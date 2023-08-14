public class UiContentOutlinePage extends ContentOutlinePage {
    private GraphicalLayoutEditor mEditor;
    private Action mAddAction;
    private Action mDeleteAction;
    private Action mUpAction;
    private Action mDownAction;
    private UiOutlineActions mUiActions = new UiOutlineActions();
    public UiContentOutlinePage(GraphicalLayoutEditor editor, final EditPartViewer viewer) {
        super(viewer);
        mEditor = editor;
        IconFactory factory = IconFactory.getInstance();
        mAddAction = new Action("Add...") {
            @Override
            public void run() {
                List<UiElementNode> nodes = getModelSelections();
                UiElementNode node = nodes != null && nodes.size() > 0 ? nodes.get(0) : null;
                mUiActions.doAdd(node, viewer.getControl().getShell());
            }
        };
        mAddAction.setToolTipText("Adds a new element.");
        mAddAction.setImageDescriptor(factory.getImageDescriptor("add")); 
        mDeleteAction = new Action("Remove...") {
            @Override
            public void run() {
                List<UiElementNode> nodes = getModelSelections();
                mUiActions.doRemove(nodes, viewer.getControl().getShell());
            }
        };
        mDeleteAction.setToolTipText("Removes an existing selected element.");
        mDeleteAction.setImageDescriptor(factory.getImageDescriptor("delete")); 
        mUpAction = new Action("Up") {
            @Override
            public void run() {
                List<UiElementNode> nodes = getModelSelections();
                mUiActions.doUp(nodes);
            }
        };
        mUpAction.setToolTipText("Moves the selected element up");
        mUpAction.setImageDescriptor(factory.getImageDescriptor("up")); 
        mDownAction = new Action("Down") {
            @Override
            public void run() {
                List<UiElementNode> nodes = getModelSelections();
                mUiActions.doDown(nodes);
            }
        };
        mDownAction.setToolTipText("Moves the selected element down");
        mDownAction.setImageDescriptor(factory.getImageDescriptor("down")); 
        mAddAction.setEnabled(false);
        mDeleteAction.setEnabled(false);
        mUpAction.setEnabled(false);
        mDownAction.setEnabled(false);
        addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                if (selection instanceof StructuredSelection) {
                    StructuredSelection structSel = (StructuredSelection)selection;
                    if (structSel.size() == 1 &&
                            structSel.getFirstElement() instanceof UiDocumentTreeEditPart) {
                        mDeleteAction.setEnabled(false);
                        mUpAction.setEnabled(false);
                        mDownAction.setEnabled(false);
                    } else {
                        mDeleteAction.setEnabled(true);
                        mUpAction.setEnabled(true);
                        mDownAction.setEnabled(true);
                    }
                    mAddAction.setEnabled(true);
                }
            }
        });
    }
    @Override
    public void createControl(Composite parent) {
        getViewer().createControl(parent);
        getViewer().setEditPartFactory(new UiElementTreeEditPartFactory());
        setupOutline();
        setupContextMenu();
        setupTooltip();
        setupDoubleClick();
    }
    @Override
    public void setActionBars(IActionBars actionBars) {
        IToolBarManager toolBarManager = actionBars.getToolBarManager();
        toolBarManager.add(mAddAction);
        toolBarManager.add(mDeleteAction);
        toolBarManager.add(new Separator());
        toolBarManager.add(mUpAction);
        toolBarManager.add(mDownAction);
        IMenuManager menuManager = actionBars.getMenuManager();
        menuManager.add(mAddAction);
        menuManager.add(mDeleteAction);
        menuManager.add(new Separator());
        menuManager.add(mUpAction);
        menuManager.add(mDownAction);
    }
    @Override
    public void dispose() {
        breakConnectionWithEditor();
        super.dispose();
    }
    @Override
    public Control getControl() {
        return getViewer().getControl();
    }
    void setNewEditor(GraphicalLayoutEditor editor) {
        mEditor = editor;
        setupOutline();
    }
    void breakConnectionWithEditor() {
        mEditor.getSelectionSynchronizer().removeViewer(getViewer());
    }
    private void setupOutline() {
        getViewer().setEditDomain(mEditor.getEditDomain());
        mEditor.getSelectionSynchronizer().addViewer(getViewer());
        getViewer().setContents(mEditor.getModel());
    }
    private void setupContextMenu() {
        MenuManager menuManager = new MenuManager();
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(new IMenuListener() {
           public void menuAboutToShow(IMenuManager manager) {
               List<UiElementNode> selected = getModelSelections();
               if (selected != null) {
                   doCreateMenuAction(manager, selected);
                   return;
               }
               doCreateMenuAction(manager, null );
            }
        });
        Control control = getControl();
        Menu contextMenu = menuManager.createContextMenu(control);
        control.setMenu(contextMenu);
    }
    private void doCreateMenuAction(IMenuManager manager, List<UiElementNode> selected) {
        if (selected != null) {
            boolean hasXml = false;
            for (UiElementNode uiNode : selected) {
                if (uiNode.getXmlNode() != null) {
                    hasXml = true;
                    break;
                }
            }
            if (hasXml) {
                manager.add(new CopyCutAction(mEditor.getLayoutEditor(), mEditor.getClipboard(),
                        null, selected, true ));
                manager.add(new CopyCutAction(mEditor.getLayoutEditor(), mEditor.getClipboard(),
                        null, selected, false ));
                if (selected.size() <= 1) {
                    UiElementNode ui_root = selected.get(0).getUiRoot();
                    if (ui_root.getDescriptor().hasChildren() ||
                            !(ui_root.getUiParent() instanceof UiDocumentNode)) {
                        manager.add(new PasteAction(mEditor.getLayoutEditor(),
                                mEditor.getClipboard(),
                                selected.get(0)));
                    }
                }
                manager.add(new Separator());
            }
        }
        if (selected == null || selected.size() <= 1) {
            manager.add(mAddAction);
        }
        if (selected != null) {
            manager.add(mDeleteAction);
            manager.add(new Separator());
            manager.add(mUpAction);
            manager.add(mDownAction);
        }
        if (selected != null && selected.size() == 1) {
            manager.add(new Separator());
            Action propertiesAction = new Action("Properties") {
                @Override
                public void run() {
                    EclipseUiHelper.showView(EclipseUiHelper.PROPERTY_SHEET_VIEW_ID,
                            true );
                }
            };
            propertiesAction.setToolTipText("Displays properties of the selected element.");
            manager.add(propertiesAction);
        }
    }
    public void reloadModel() {
        List<UiElementNode> uiNodes = null;
        try {
            uiNodes = getModelSelections();
            getViewer().setContents(mEditor.getModel());
        } finally {
            if (uiNodes != null) {
                setModelSelection(uiNodes.get(0));
            }
        }
    }
    @SuppressWarnings("unchecked")
    private List<UiElementTreeEditPart> getViewerSelections() {
        ISelection selection = getSelection();
        if (selection instanceof StructuredSelection) {
            StructuredSelection structuredSelection = (StructuredSelection)selection;
            if (structuredSelection.size() > 0) {
                ArrayList<UiElementTreeEditPart> selected = new ArrayList<UiElementTreeEditPart>();
                for (Iterator it = structuredSelection.iterator(); it.hasNext(); ) {
                    Object selectedObj = it.next();
                    if (selectedObj instanceof UiElementTreeEditPart) {
                        selected.add((UiElementTreeEditPart) selectedObj);
                    }
                }
                return selected.size() > 0 ? selected : null;
            }
        }
        return null;
    }
    private List<UiElementNode> getModelSelections() {
        List<UiElementTreeEditPart> parts = getViewerSelections();
        if (parts != null) {
            ArrayList<UiElementNode> selected = new ArrayList<UiElementNode>();
            for (UiElementTreeEditPart part : parts) {
                if (part instanceof UiViewTreeEditPart || part instanceof UiLayoutTreeEditPart) {
                    selected.add((UiElementNode) part.getModel());
                }
            }
            return selected.size() > 0 ? selected : null;
        }
        return null;
    }
    private void setViewerSelection(UiElementTreeEditPart selectedPart) {
        if (selectedPart != null && !(selectedPart instanceof UiDocumentTreeEditPart)) {
            LinkedList<UiElementTreeEditPart> segments = new LinkedList<UiElementTreeEditPart>();
            for (UiElementTreeEditPart part = selectedPart;
                    !(part instanceof UiDocumentTreeEditPart);
                    part = (UiElementTreeEditPart) part.getParent()) {
                segments.add(0, part);
            }
            setSelection(new TreeSelection(new TreePath(segments.toArray())));
        }
    }
    private void setModelSelection(UiElementNode uiNodeToSelect) {
        if (uiNodeToSelect != null) {
            UiElementTreeEditPart part = findPartForModel(
                    (UiElementTreeEditPart) getViewer().getContents(),
                    uiNodeToSelect);
            if (part != null) {
                setViewerSelection(part);
                getViewer().reveal(part);
            }
        }
    }
    private UiElementTreeEditPart findPartForModel(UiElementTreeEditPart rootPart,
            UiElementNode uiNode) {
        if (rootPart.getModel() == uiNode) {
            return rootPart;
        }
        for (Object part : rootPart.getChildren()) {
            if (part instanceof UiElementTreeEditPart) {
                UiElementTreeEditPart found = findPartForModel(
                        (UiElementTreeEditPart) part, uiNode);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
    private void setupTooltip() {
        final Tree tree = (Tree) getControl();
        final Listener listener = new Listener() {
            Shell tip = null;
            Label label  = null;
            public void handleEvent(Event event) {
                switch(event.type) {
                case SWT.Dispose:
                case SWT.KeyDown:
                case SWT.MouseExit:
                case SWT.MouseDown:
                case SWT.MouseMove:
                    if (tip != null) {
                        tip.dispose();
                        tip = null;
                        label = null;
                    }
                    break;
                case SWT.MouseHover:
                    if (tip != null) {
                        tip.dispose();
                        tip = null;
                        label = null;
                    }
                    String tooltip = null;
                    TreeItem item = tree.getItem(new Point(event.x, event.y));
                    if (item != null) {
                        Object data = item.getData();
                        if (data instanceof UiElementTreeEditPart) {
                            Object model = ((UiElementTreeEditPart) data).getModel();
                            if (model instanceof UiElementNode) {
                                tooltip = ((UiElementNode) model).getDescriptor().getTooltip();
                            }
                        }
                        if (tooltip == null) {
                            tooltip = item.getText();
                        } else {
                            tooltip = item.getText() + ":\r" + tooltip;
                        }
                        if (tooltip != null) {
                            Shell shell = tree.getShell();
                            Display display = tree.getDisplay();
                            tip = new Shell(shell, SWT.ON_TOP | SWT.NO_FOCUS | SWT.TOOL);
                            tip.setBackground(display .getSystemColor(SWT.COLOR_INFO_BACKGROUND));
                            FillLayout layout = new FillLayout();
                            layout.marginWidth = 2;
                            tip.setLayout(layout);
                            label = new Label(tip, SWT.NONE);
                            label.setForeground(display.getSystemColor(SWT.COLOR_INFO_FOREGROUND));
                            label.setBackground(display.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
                            label.setData("_TABLEITEM", item);
                            label.setText(tooltip);
                            label.addListener(SWT.MouseExit, this);
                            label.addListener(SWT.MouseDown, this);
                            Point size = tip.computeSize(SWT.DEFAULT, SWT.DEFAULT);
                            Rectangle rect = item.getBounds(0);
                            Point pt = tree.toDisplay(rect.x, rect.y);
                            tip.setBounds(pt.x, pt.y, size.x, size.y);
                            tip.setVisible(true);
                        }
                    }
                }
            }
        };
        tree.addListener(SWT.Dispose, listener);
        tree.addListener(SWT.KeyDown, listener);
        tree.addListener(SWT.MouseMove, listener);
        tree.addListener(SWT.MouseHover, listener);
    }
    private void setupDoubleClick() {
        final Tree tree = (Tree) getControl();
        tree.addListener(SWT.DefaultSelection, new Listener() {
            public void handleEvent(Event event) {
                EclipseUiHelper.showView(EclipseUiHelper.PROPERTY_SHEET_VIEW_ID,
                        true );
            }
        });
    }
    private class UiOutlineActions extends UiActions {
        @Override
        protected UiDocumentNode getRootNode() {
            return mEditor.getModel(); 
        }
        @Override
        protected void selectUiNode(UiElementNode uiNodeToSelect) {
            setModelSelection(uiNodeToSelect);
        }
        @Override
        public void commitPendingXmlChanges() {
        }
    }
}
