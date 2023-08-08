public class Ocl4tstEditor extends MultiPageEditorPart implements IEditingDomainProvider, ISelectionProvider, IMenuListener, IViewerProvider, IGotoMarker {
    protected AdapterFactoryEditingDomain editingDomain;
    protected ComposedAdapterFactory adapterFactory;
    protected IContentOutlinePage contentOutlinePage;
    protected IStatusLineManager contentOutlineStatusLineManager;
    protected TreeViewer contentOutlineViewer;
    protected PropertySheetPage propertySheetPage;
    protected TreeViewer selectionViewer;
    protected TreeViewer parentViewer;
    protected TreeViewer treeViewer;
    protected ListViewer listViewer;
    protected TableViewer tableViewer;
    protected TreeViewer treeViewerWithColumns;
    protected ViewerPane currentViewerPane;
    protected Viewer currentViewer;
    protected ISelectionChangedListener selectionChangedListener;
    protected Collection<ISelectionChangedListener> selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
    protected ISelection editorSelection = StructuredSelection.EMPTY;
    protected MarkerHelper markerHelper = new EditUIMarkerHelper();
    protected IPartListener partListener = new IPartListener() {
        public void partActivated(IWorkbenchPart p) {
            if (p instanceof ContentOutline) {
                if (((ContentOutline) p).getCurrentPage() == contentOutlinePage) {
                    getActionBarContributor().setActiveEditor(Ocl4tstEditor.this);
                    setCurrentViewer(contentOutlineViewer);
                }
            } else if (p instanceof PropertySheet) {
                if (((PropertySheet) p).getCurrentPage() == propertySheetPage) {
                    getActionBarContributor().setActiveEditor(Ocl4tstEditor.this);
                    handleActivate();
                }
            } else if (p == Ocl4tstEditor.this) {
                handleActivate();
            }
        }
        public void partBroughtToTop(IWorkbenchPart p) {
        }
        public void partClosed(IWorkbenchPart p) {
        }
        public void partDeactivated(IWorkbenchPart p) {
        }
        public void partOpened(IWorkbenchPart p) {
        }
    };
    protected Collection<Resource> removedResources = new ArrayList<Resource>();
    protected Collection<Resource> changedResources = new ArrayList<Resource>();
    protected Collection<Resource> savedResources = new ArrayList<Resource>();
    protected Map<Resource, Diagnostic> resourceToDiagnosticMap = new LinkedHashMap<Resource, Diagnostic>();
    protected boolean updateProblemIndication = true;
    protected EContentAdapter problemIndicationAdapter = new EContentAdapter() {
        @Override
        public void notifyChanged(Notification notification) {
            if (notification.getNotifier() instanceof Resource) {
                switch(notification.getFeatureID(Resource.class)) {
                    case Resource.RESOURCE__IS_LOADED:
                    case Resource.RESOURCE__ERRORS:
                    case Resource.RESOURCE__WARNINGS:
                        {
                            Resource resource = (Resource) notification.getNotifier();
                            Diagnostic diagnostic = analyzeResourceProblems(resource, null);
                            if (diagnostic.getSeverity() != Diagnostic.OK) {
                                resourceToDiagnosticMap.put(resource, diagnostic);
                            } else {
                                resourceToDiagnosticMap.remove(resource);
                            }
                            if (updateProblemIndication) {
                                getSite().getShell().getDisplay().asyncExec(new Runnable() {
                                    public void run() {
                                        updateProblemIndication();
                                    }
                                });
                            }
                            break;
                        }
                }
            } else {
                super.notifyChanged(notification);
            }
        }
        @Override
        protected void setTarget(Resource target) {
            basicSetTarget(target);
        }
        @Override
        protected void unsetTarget(Resource target) {
            basicUnsetTarget(target);
        }
    };
    protected IResourceChangeListener resourceChangeListener = new IResourceChangeListener() {
        public void resourceChanged(IResourceChangeEvent event) {
            IResourceDelta delta = event.getDelta();
            try {
                class ResourceDeltaVisitor implements IResourceDeltaVisitor {
                    protected ResourceSet resourceSet = editingDomain.getResourceSet();
                    protected Collection<Resource> changedResources = new ArrayList<Resource>();
                    protected Collection<Resource> removedResources = new ArrayList<Resource>();
                    public boolean visit(IResourceDelta delta) {
                        if (delta.getResource().getType() == IResource.FILE) {
                            if (delta.getKind() == IResourceDelta.REMOVED || delta.getKind() == IResourceDelta.CHANGED && delta.getFlags() != IResourceDelta.MARKERS) {
                                Resource resource = resourceSet.getResource(URI.createPlatformResourceURI(delta.getFullPath().toString(), true), false);
                                if (resource != null) {
                                    if (delta.getKind() == IResourceDelta.REMOVED) {
                                        removedResources.add(resource);
                                    } else if (!savedResources.remove(resource)) {
                                        changedResources.add(resource);
                                    }
                                }
                            }
                        }
                        return true;
                    }
                    public Collection<Resource> getChangedResources() {
                        return changedResources;
                    }
                    public Collection<Resource> getRemovedResources() {
                        return removedResources;
                    }
                }
                final ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
                delta.accept(visitor);
                if (!visitor.getRemovedResources().isEmpty()) {
                    getSite().getShell().getDisplay().asyncExec(new Runnable() {
                        public void run() {
                            removedResources.addAll(visitor.getRemovedResources());
                            if (!isDirty()) {
                                getSite().getPage().closeEditor(Ocl4tstEditor.this, false);
                            }
                        }
                    });
                }
                if (!visitor.getChangedResources().isEmpty()) {
                    getSite().getShell().getDisplay().asyncExec(new Runnable() {
                        public void run() {
                            changedResources.addAll(visitor.getChangedResources());
                            if (getSite().getPage().getActiveEditor() == Ocl4tstEditor.this) {
                                handleActivate();
                            }
                        }
                    });
                }
            } catch (CoreException exception) {
                ocl4tstEditorPlugin.INSTANCE.log(exception);
            }
        }
    };
    protected void handleActivate() {
        if (editingDomain.getResourceToReadOnlyMap() != null) {
            editingDomain.getResourceToReadOnlyMap().clear();
            setSelection(getSelection());
        }
        if (!removedResources.isEmpty()) {
            if (handleDirtyConflict()) {
                getSite().getPage().closeEditor(Ocl4tstEditor.this, false);
            } else {
                removedResources.clear();
                changedResources.clear();
                savedResources.clear();
            }
        } else if (!changedResources.isEmpty()) {
            changedResources.removeAll(savedResources);
            handleChangedResources();
            changedResources.clear();
            savedResources.clear();
        }
    }
    protected void handleChangedResources() {
        if (!changedResources.isEmpty() && (!isDirty() || handleDirtyConflict())) {
            if (isDirty()) {
                changedResources.addAll(editingDomain.getResourceSet().getResources());
            }
            editingDomain.getCommandStack().flush();
            updateProblemIndication = false;
            for (Resource resource : changedResources) {
                if (resource.isLoaded()) {
                    resource.unload();
                    try {
                        resource.load(Collections.EMPTY_MAP);
                    } catch (IOException exception) {
                        if (!resourceToDiagnosticMap.containsKey(resource)) {
                            resourceToDiagnosticMap.put(resource, analyzeResourceProblems(resource, exception));
                        }
                    }
                }
            }
            if (AdapterFactoryEditingDomain.isStale(editorSelection)) {
                setSelection(StructuredSelection.EMPTY);
            }
            updateProblemIndication = true;
            updateProblemIndication();
        }
    }
    protected void updateProblemIndication() {
        if (updateProblemIndication) {
            BasicDiagnostic diagnostic = new BasicDiagnostic(Diagnostic.OK, "fr.inria.uml4tst.papyrus.ocl4tst.editor", 0, null, new Object[] { editingDomain.getResourceSet() });
            for (Diagnostic childDiagnostic : resourceToDiagnosticMap.values()) {
                if (childDiagnostic.getSeverity() != Diagnostic.OK) {
                    diagnostic.add(childDiagnostic);
                }
            }
            int lastEditorPage = getPageCount() - 1;
            if (lastEditorPage >= 0 && getEditor(lastEditorPage) instanceof ProblemEditorPart) {
                ((ProblemEditorPart) getEditor(lastEditorPage)).setDiagnostic(diagnostic);
                if (diagnostic.getSeverity() != Diagnostic.OK) {
                    setActivePage(lastEditorPage);
                }
            } else if (diagnostic.getSeverity() != Diagnostic.OK) {
                ProblemEditorPart problemEditorPart = new ProblemEditorPart();
                problemEditorPart.setDiagnostic(diagnostic);
                problemEditorPart.setMarkerHelper(markerHelper);
                try {
                    addPage(++lastEditorPage, problemEditorPart, getEditorInput());
                    setPageText(lastEditorPage, problemEditorPart.getPartName());
                    setActivePage(lastEditorPage);
                    showTabs();
                } catch (PartInitException exception) {
                    ocl4tstEditorPlugin.INSTANCE.log(exception);
                }
            }
            if (markerHelper.hasMarkers(editingDomain.getResourceSet())) {
                markerHelper.deleteMarkers(editingDomain.getResourceSet());
                if (diagnostic.getSeverity() != Diagnostic.OK) {
                    try {
                        markerHelper.createMarkers(diagnostic);
                    } catch (CoreException exception) {
                        ocl4tstEditorPlugin.INSTANCE.log(exception);
                    }
                }
            }
        }
    }
    protected boolean handleDirtyConflict() {
        return MessageDialog.openQuestion(getSite().getShell(), getString("_UI_FileConflict_label"), getString("_WARN_FileConflict"));
    }
    public Ocl4tstEditor() {
        super();
        initializeEditingDomain();
    }
    protected void initializeEditingDomain() {
        adapterFactory = new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
        adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
        adapterFactory.addAdapterFactory(new Ocl4tstItemProviderAdapterFactory());
        adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
        BasicCommandStack commandStack = new BasicCommandStack();
        commandStack.addCommandStackListener(new CommandStackListener() {
            public void commandStackChanged(final EventObject event) {
                getContainer().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        firePropertyChange(IEditorPart.PROP_DIRTY);
                        Command mostRecentCommand = ((CommandStack) event.getSource()).getMostRecentCommand();
                        if (mostRecentCommand != null) {
                            setSelectionToViewer(mostRecentCommand.getAffectedObjects());
                        }
                        if (propertySheetPage != null && !propertySheetPage.getControl().isDisposed()) {
                            propertySheetPage.refresh();
                        }
                    }
                });
            }
        });
        editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack, new HashMap<Resource, Boolean>());
    }
    @Override
    protected void firePropertyChange(int action) {
        super.firePropertyChange(action);
    }
    public void setSelectionToViewer(Collection<?> collection) {
        final Collection<?> theSelection = collection;
        if (theSelection != null && !theSelection.isEmpty()) {
            Runnable runnable = new Runnable() {
                public void run() {
                    if (currentViewer != null) {
                        currentViewer.setSelection(new StructuredSelection(theSelection.toArray()), true);
                    }
                }
            };
            getSite().getShell().getDisplay().asyncExec(runnable);
        }
    }
    public EditingDomain getEditingDomain() {
        return editingDomain;
    }
    public class ReverseAdapterFactoryContentProvider extends AdapterFactoryContentProvider {
        public ReverseAdapterFactoryContentProvider(AdapterFactory adapterFactory) {
            super(adapterFactory);
        }
        @Override
        public Object[] getElements(Object object) {
            Object parent = super.getParent(object);
            return (parent == null ? Collections.EMPTY_SET : Collections.singleton(parent)).toArray();
        }
        @Override
        public Object[] getChildren(Object object) {
            Object parent = super.getParent(object);
            return (parent == null ? Collections.EMPTY_SET : Collections.singleton(parent)).toArray();
        }
        @Override
        public boolean hasChildren(Object object) {
            Object parent = super.getParent(object);
            return parent != null;
        }
        @Override
        public Object getParent(Object object) {
            return null;
        }
    }
    public void setCurrentViewerPane(ViewerPane viewerPane) {
        if (currentViewerPane != viewerPane) {
            if (currentViewerPane != null) {
                currentViewerPane.showFocus(false);
            }
            currentViewerPane = viewerPane;
        }
        setCurrentViewer(currentViewerPane.getViewer());
    }
    public void setCurrentViewer(Viewer viewer) {
        if (currentViewer != viewer) {
            if (selectionChangedListener == null) {
                selectionChangedListener = new ISelectionChangedListener() {
                    public void selectionChanged(SelectionChangedEvent selectionChangedEvent) {
                        setSelection(selectionChangedEvent.getSelection());
                    }
                };
            }
            if (currentViewer != null) {
                currentViewer.removeSelectionChangedListener(selectionChangedListener);
            }
            if (viewer != null) {
                viewer.addSelectionChangedListener(selectionChangedListener);
            }
            currentViewer = viewer;
            setSelection(currentViewer == null ? StructuredSelection.EMPTY : currentViewer.getSelection());
        }
    }
    public Viewer getViewer() {
        return currentViewer;
    }
    protected void createContextMenuFor(StructuredViewer viewer) {
        MenuManager contextMenu = new MenuManager("#PopUp");
        contextMenu.add(new Separator("additions"));
        contextMenu.setRemoveAllWhenShown(true);
        contextMenu.addMenuListener(this);
        Menu menu = contextMenu.createContextMenu(viewer.getControl());
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu(contextMenu, new UnwrappingSelectionProvider(viewer));
        int dndOperations = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_LINK;
        Transfer[] transfers = new Transfer[] { LocalTransfer.getInstance() };
        viewer.addDragSupport(dndOperations, transfers, new ViewerDragAdapter(viewer));
        viewer.addDropSupport(dndOperations, transfers, new EditingDomainViewerDropAdapter(editingDomain, viewer));
    }
    public void createModel() {
        URI resourceURI = EditUIUtil.getURI(getEditorInput());
        Exception exception = null;
        Resource resource = null;
        try {
            resource = editingDomain.getResourceSet().getResource(resourceURI, true);
        } catch (Exception e) {
            exception = e;
            resource = editingDomain.getResourceSet().getResource(resourceURI, false);
        }
        Diagnostic diagnostic = analyzeResourceProblems(resource, exception);
        if (diagnostic.getSeverity() != Diagnostic.OK) {
            resourceToDiagnosticMap.put(resource, analyzeResourceProblems(resource, exception));
        }
        editingDomain.getResourceSet().eAdapters().add(problemIndicationAdapter);
    }
    public Diagnostic analyzeResourceProblems(Resource resource, Exception exception) {
        if (!resource.getErrors().isEmpty() || !resource.getWarnings().isEmpty()) {
            BasicDiagnostic basicDiagnostic = new BasicDiagnostic(Diagnostic.ERROR, "fr.inria.uml4tst.papyrus.ocl4tst.editor", 0, getString("_UI_CreateModelError_message", resource.getURI()), new Object[] { exception == null ? (Object) resource : exception });
            basicDiagnostic.merge(EcoreUtil.computeDiagnostic(resource, true));
            return basicDiagnostic;
        } else if (exception != null) {
            return new BasicDiagnostic(Diagnostic.ERROR, "fr.inria.uml4tst.papyrus.ocl4tst.editor", 0, getString("_UI_CreateModelError_message", resource.getURI()), new Object[] { exception });
        } else {
            return Diagnostic.OK_INSTANCE;
        }
    }
    @Override
    public void createPages() {
        createModel();
        if (!getEditingDomain().getResourceSet().getResources().isEmpty()) {
            {
                ViewerPane viewerPane = new ViewerPane(getSite().getPage(), Ocl4tstEditor.this) {
                    @Override
                    public Viewer createViewer(Composite composite) {
                        Tree tree = new Tree(composite, SWT.MULTI);
                        TreeViewer newTreeViewer = new TreeViewer(tree);
                        return newTreeViewer;
                    }
                    @Override
                    public void requestActivation() {
                        super.requestActivation();
                        setCurrentViewerPane(this);
                    }
                };
                viewerPane.createControl(getContainer());
                selectionViewer = (TreeViewer) viewerPane.getViewer();
                selectionViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
                selectionViewer.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
                selectionViewer.setInput(editingDomain.getResourceSet());
                selectionViewer.setSelection(new StructuredSelection(editingDomain.getResourceSet().getResources().get(0)), true);
                viewerPane.setTitle(editingDomain.getResourceSet());
                new AdapterFactoryTreeEditor(selectionViewer.getTree(), adapterFactory);
                createContextMenuFor(selectionViewer);
                int pageIndex = addPage(viewerPane.getControl());
                setPageText(pageIndex, getString("_UI_SelectionPage_label"));
            }
            {
                ViewerPane viewerPane = new ViewerPane(getSite().getPage(), Ocl4tstEditor.this) {
                    @Override
                    public Viewer createViewer(Composite composite) {
                        Tree tree = new Tree(composite, SWT.MULTI);
                        TreeViewer newTreeViewer = new TreeViewer(tree);
                        return newTreeViewer;
                    }
                    @Override
                    public void requestActivation() {
                        super.requestActivation();
                        setCurrentViewerPane(this);
                    }
                };
                viewerPane.createControl(getContainer());
                parentViewer = (TreeViewer) viewerPane.getViewer();
                parentViewer.setAutoExpandLevel(30);
                parentViewer.setContentProvider(new ReverseAdapterFactoryContentProvider(adapterFactory));
                parentViewer.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
                createContextMenuFor(parentViewer);
                int pageIndex = addPage(viewerPane.getControl());
                setPageText(pageIndex, getString("_UI_ParentPage_label"));
            }
            {
                ViewerPane viewerPane = new ViewerPane(getSite().getPage(), Ocl4tstEditor.this) {
                    @Override
                    public Viewer createViewer(Composite composite) {
                        return new ListViewer(composite);
                    }
                    @Override
                    public void requestActivation() {
                        super.requestActivation();
                        setCurrentViewerPane(this);
                    }
                };
                viewerPane.createControl(getContainer());
                listViewer = (ListViewer) viewerPane.getViewer();
                listViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
                listViewer.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
                createContextMenuFor(listViewer);
                int pageIndex = addPage(viewerPane.getControl());
                setPageText(pageIndex, getString("_UI_ListPage_label"));
            }
            {
                ViewerPane viewerPane = new ViewerPane(getSite().getPage(), Ocl4tstEditor.this) {
                    @Override
                    public Viewer createViewer(Composite composite) {
                        return new TreeViewer(composite);
                    }
                    @Override
                    public void requestActivation() {
                        super.requestActivation();
                        setCurrentViewerPane(this);
                    }
                };
                viewerPane.createControl(getContainer());
                treeViewer = (TreeViewer) viewerPane.getViewer();
                treeViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
                treeViewer.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
                new AdapterFactoryTreeEditor(treeViewer.getTree(), adapterFactory);
                createContextMenuFor(treeViewer);
                int pageIndex = addPage(viewerPane.getControl());
                setPageText(pageIndex, getString("_UI_TreePage_label"));
            }
            {
                ViewerPane viewerPane = new ViewerPane(getSite().getPage(), Ocl4tstEditor.this) {
                    @Override
                    public Viewer createViewer(Composite composite) {
                        return new TableViewer(composite);
                    }
                    @Override
                    public void requestActivation() {
                        super.requestActivation();
                        setCurrentViewerPane(this);
                    }
                };
                viewerPane.createControl(getContainer());
                tableViewer = (TableViewer) viewerPane.getViewer();
                Table table = tableViewer.getTable();
                TableLayout layout = new TableLayout();
                table.setLayout(layout);
                table.setHeaderVisible(true);
                table.setLinesVisible(true);
                TableColumn objectColumn = new TableColumn(table, SWT.NONE);
                layout.addColumnData(new ColumnWeightData(3, 100, true));
                objectColumn.setText(getString("_UI_ObjectColumn_label"));
                objectColumn.setResizable(true);
                TableColumn selfColumn = new TableColumn(table, SWT.NONE);
                layout.addColumnData(new ColumnWeightData(2, 100, true));
                selfColumn.setText(getString("_UI_SelfColumn_label"));
                selfColumn.setResizable(true);
                tableViewer.setColumnProperties(new String[] { "a", "b" });
                tableViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
                tableViewer.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
                createContextMenuFor(tableViewer);
                int pageIndex = addPage(viewerPane.getControl());
                setPageText(pageIndex, getString("_UI_TablePage_label"));
            }
            {
                ViewerPane viewerPane = new ViewerPane(getSite().getPage(), Ocl4tstEditor.this) {
                    @Override
                    public Viewer createViewer(Composite composite) {
                        return new TreeViewer(composite);
                    }
                    @Override
                    public void requestActivation() {
                        super.requestActivation();
                        setCurrentViewerPane(this);
                    }
                };
                viewerPane.createControl(getContainer());
                treeViewerWithColumns = (TreeViewer) viewerPane.getViewer();
                Tree tree = treeViewerWithColumns.getTree();
                tree.setLayoutData(new FillLayout());
                tree.setHeaderVisible(true);
                tree.setLinesVisible(true);
                TreeColumn objectColumn = new TreeColumn(tree, SWT.NONE);
                objectColumn.setText(getString("_UI_ObjectColumn_label"));
                objectColumn.setResizable(true);
                objectColumn.setWidth(250);
                TreeColumn selfColumn = new TreeColumn(tree, SWT.NONE);
                selfColumn.setText(getString("_UI_SelfColumn_label"));
                selfColumn.setResizable(true);
                selfColumn.setWidth(200);
                treeViewerWithColumns.setColumnProperties(new String[] { "a", "b" });
                treeViewerWithColumns.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
                treeViewerWithColumns.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
                createContextMenuFor(treeViewerWithColumns);
                int pageIndex = addPage(viewerPane.getControl());
                setPageText(pageIndex, getString("_UI_TreeWithColumnsPage_label"));
            }
            getSite().getShell().getDisplay().asyncExec(new Runnable() {
                public void run() {
                    setActivePage(0);
                }
            });
        }
        getContainer().addControlListener(new ControlAdapter() {
            boolean guard = false;
            @Override
            public void controlResized(ControlEvent event) {
                if (!guard) {
                    guard = true;
                    hideTabs();
                    guard = false;
                }
            }
        });
        getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                updateProblemIndication();
            }
        });
    }
    protected void hideTabs() {
        if (getPageCount() <= 1) {
            setPageText(0, "");
            if (getContainer() instanceof CTabFolder) {
                ((CTabFolder) getContainer()).setTabHeight(1);
                Point point = getContainer().getSize();
                getContainer().setSize(point.x, point.y + 6);
            }
        }
    }
    protected void showTabs() {
        if (getPageCount() > 1) {
            setPageText(0, getString("_UI_SelectionPage_label"));
            if (getContainer() instanceof CTabFolder) {
                ((CTabFolder) getContainer()).setTabHeight(SWT.DEFAULT);
                Point point = getContainer().getSize();
                getContainer().setSize(point.x, point.y - 6);
            }
        }
    }
    @Override
    protected void pageChange(int pageIndex) {
        super.pageChange(pageIndex);
        if (contentOutlinePage != null) {
            handleContentOutlineSelection(contentOutlinePage.getSelection());
        }
    }
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class key) {
        if (key.equals(IContentOutlinePage.class)) {
            return showOutlineView() ? getContentOutlinePage() : null;
        } else if (key.equals(IPropertySheetPage.class)) {
            return getPropertySheetPage();
        } else if (key.equals(IGotoMarker.class)) {
            return this;
        } else {
            return super.getAdapter(key);
        }
    }
    public IContentOutlinePage getContentOutlinePage() {
        if (contentOutlinePage == null) {
            class MyContentOutlinePage extends ContentOutlinePage {
                @Override
                public void createControl(Composite parent) {
                    super.createControl(parent);
                    contentOutlineViewer = getTreeViewer();
                    contentOutlineViewer.addSelectionChangedListener(this);
                    contentOutlineViewer.setContentProvider(new AdapterFactoryContentProvider(adapterFactory));
                    contentOutlineViewer.setLabelProvider(new AdapterFactoryLabelProvider(adapterFactory));
                    contentOutlineViewer.setInput(editingDomain.getResourceSet());
                    createContextMenuFor(contentOutlineViewer);
                    if (!editingDomain.getResourceSet().getResources().isEmpty()) {
                        contentOutlineViewer.setSelection(new StructuredSelection(editingDomain.getResourceSet().getResources().get(0)), true);
                    }
                }
                @Override
                public void makeContributions(IMenuManager menuManager, IToolBarManager toolBarManager, IStatusLineManager statusLineManager) {
                    super.makeContributions(menuManager, toolBarManager, statusLineManager);
                    contentOutlineStatusLineManager = statusLineManager;
                }
                @Override
                public void setActionBars(IActionBars actionBars) {
                    super.setActionBars(actionBars);
                    getActionBarContributor().shareGlobalActions(this, actionBars);
                }
            }
            contentOutlinePage = new MyContentOutlinePage();
            contentOutlinePage.addSelectionChangedListener(new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event) {
                    handleContentOutlineSelection(event.getSelection());
                }
            });
        }
        return contentOutlinePage;
    }
    public IPropertySheetPage getPropertySheetPage() {
        if (propertySheetPage == null) {
            propertySheetPage = new ExtendedPropertySheetPage(editingDomain) {
                @Override
                public void setSelectionToViewer(List<?> selection) {
                    Ocl4tstEditor.this.setSelectionToViewer(selection);
                    Ocl4tstEditor.this.setFocus();
                }
                @Override
                public void setActionBars(IActionBars actionBars) {
                    super.setActionBars(actionBars);
                    getActionBarContributor().shareGlobalActions(this, actionBars);
                }
            };
            propertySheetPage.setPropertySourceProvider(new AdapterFactoryContentProvider(adapterFactory));
        }
        return propertySheetPage;
    }
    public void handleContentOutlineSelection(ISelection selection) {
        if (currentViewerPane != null && !selection.isEmpty() && selection instanceof IStructuredSelection) {
            Iterator<?> selectedElements = ((IStructuredSelection) selection).iterator();
            if (selectedElements.hasNext()) {
                Object selectedElement = selectedElements.next();
                if (currentViewerPane.getViewer() == selectionViewer) {
                    ArrayList<Object> selectionList = new ArrayList<Object>();
                    selectionList.add(selectedElement);
                    while (selectedElements.hasNext()) {
                        selectionList.add(selectedElements.next());
                    }
                    selectionViewer.setSelection(new StructuredSelection(selectionList));
                } else {
                    if (currentViewerPane.getViewer().getInput() != selectedElement) {
                        currentViewerPane.getViewer().setInput(selectedElement);
                        currentViewerPane.setTitle(selectedElement);
                    }
                }
            }
        }
    }
    @Override
    public boolean isDirty() {
        return ((BasicCommandStack) editingDomain.getCommandStack()).isSaveNeeded();
    }
    @Override
    public void doSave(IProgressMonitor progressMonitor) {
        final Map<Object, Object> saveOptions = new HashMap<Object, Object>();
        saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED, Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
        WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
            @Override
            public void execute(IProgressMonitor monitor) {
                boolean first = true;
                for (Resource resource : editingDomain.getResourceSet().getResources()) {
                    if ((first || !resource.getContents().isEmpty() || isPersisted(resource)) && !editingDomain.isReadOnly(resource)) {
                        try {
                            long timeStamp = resource.getTimeStamp();
                            resource.save(saveOptions);
                            if (resource.getTimeStamp() != timeStamp) {
                                savedResources.add(resource);
                            }
                        } catch (Exception exception) {
                            resourceToDiagnosticMap.put(resource, analyzeResourceProblems(resource, exception));
                        }
                        first = false;
                    }
                }
            }
        };
        updateProblemIndication = false;
        try {
            new ProgressMonitorDialog(getSite().getShell()).run(true, false, operation);
            ((BasicCommandStack) editingDomain.getCommandStack()).saveIsDone();
            firePropertyChange(IEditorPart.PROP_DIRTY);
        } catch (Exception exception) {
            ocl4tstEditorPlugin.INSTANCE.log(exception);
        }
        updateProblemIndication = true;
        updateProblemIndication();
    }
    protected boolean isPersisted(Resource resource) {
        boolean result = false;
        try {
            InputStream stream = editingDomain.getResourceSet().getURIConverter().createInputStream(resource.getURI());
            if (stream != null) {
                result = true;
                stream.close();
            }
        } catch (IOException e) {
        }
        return result;
    }
    @Override
    public boolean isSaveAsAllowed() {
        return true;
    }
    @Override
    public void doSaveAs() {
        SaveAsDialog saveAsDialog = new SaveAsDialog(getSite().getShell());
        saveAsDialog.open();
        IPath path = saveAsDialog.getResult();
        if (path != null) {
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            if (file != null) {
                doSaveAs(URI.createPlatformResourceURI(file.getFullPath().toString(), true), new FileEditorInput(file));
            }
        }
    }
    protected void doSaveAs(URI uri, IEditorInput editorInput) {
        (editingDomain.getResourceSet().getResources().get(0)).setURI(uri);
        setInputWithNotify(editorInput);
        setPartName(editorInput.getName());
        IProgressMonitor progressMonitor = getActionBars().getStatusLineManager() != null ? getActionBars().getStatusLineManager().getProgressMonitor() : new NullProgressMonitor();
        doSave(progressMonitor);
    }
    public void gotoMarker(IMarker marker) {
        try {
            if (marker.getType().equals(EValidator.MARKER)) {
                String uriAttribute = marker.getAttribute(EValidator.URI_ATTRIBUTE, null);
                if (uriAttribute != null) {
                    URI uri = URI.createURI(uriAttribute);
                    EObject eObject = editingDomain.getResourceSet().getEObject(uri, true);
                    if (eObject != null) {
                        setSelectionToViewer(Collections.singleton(editingDomain.getWrapper(eObject)));
                    }
                }
            }
        } catch (CoreException exception) {
            ocl4tstEditorPlugin.INSTANCE.log(exception);
        }
    }
    @Override
    public void init(IEditorSite site, IEditorInput editorInput) {
        setSite(site);
        setInputWithNotify(editorInput);
        setPartName(editorInput.getName());
        site.setSelectionProvider(this);
        site.getPage().addPartListener(partListener);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
    }
    @Override
    public void setFocus() {
        if (currentViewerPane != null) {
            currentViewerPane.setFocus();
        } else {
            getControl(getActivePage()).setFocus();
        }
    }
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        selectionChangedListeners.add(listener);
    }
    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        selectionChangedListeners.remove(listener);
    }
    public ISelection getSelection() {
        return editorSelection;
    }
    public void setSelection(ISelection selection) {
        editorSelection = selection;
        for (ISelectionChangedListener listener : selectionChangedListeners) {
            listener.selectionChanged(new SelectionChangedEvent(this, selection));
        }
        setStatusLineManager(selection);
    }
    public void setStatusLineManager(ISelection selection) {
        IStatusLineManager statusLineManager = currentViewer != null && currentViewer == contentOutlineViewer ? contentOutlineStatusLineManager : getActionBars().getStatusLineManager();
        if (statusLineManager != null) {
            if (selection instanceof IStructuredSelection) {
                Collection<?> collection = ((IStructuredSelection) selection).toList();
                switch(collection.size()) {
                    case 0:
                        {
                            statusLineManager.setMessage(getString("_UI_NoObjectSelected"));
                            break;
                        }
                    case 1:
                        {
                            String text = new AdapterFactoryItemDelegator(adapterFactory).getText(collection.iterator().next());
                            statusLineManager.setMessage(getString("_UI_SingleObjectSelected", text));
                            break;
                        }
                    default:
                        {
                            statusLineManager.setMessage(getString("_UI_MultiObjectSelected", Integer.toString(collection.size())));
                            break;
                        }
                }
            } else {
                statusLineManager.setMessage("");
            }
        }
    }
    private static String getString(String key) {
        return ocl4tstEditorPlugin.INSTANCE.getString(key);
    }
    private static String getString(String key, Object s1) {
        return ocl4tstEditorPlugin.INSTANCE.getString(key, new Object[] { s1 });
    }
    public void menuAboutToShow(IMenuManager menuManager) {
        ((IMenuListener) getEditorSite().getActionBarContributor()).menuAboutToShow(menuManager);
    }
    public EditingDomainActionBarContributor getActionBarContributor() {
        return (EditingDomainActionBarContributor) getEditorSite().getActionBarContributor();
    }
    public IActionBars getActionBars() {
        return getActionBarContributor().getActionBars();
    }
    public AdapterFactory getAdapterFactory() {
        return adapterFactory;
    }
    @Override
    public void dispose() {
        updateProblemIndication = false;
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeListener);
        getSite().getPage().removePartListener(partListener);
        adapterFactory.dispose();
        if (getActionBarContributor().getActiveEditor() == this) {
            getActionBarContributor().setActiveEditor(null);
        }
        if (propertySheetPage != null) {
            propertySheetPage.dispose();
        }
        if (contentOutlinePage != null) {
            contentOutlinePage.dispose();
        }
        super.dispose();
    }
    protected boolean showOutlineView() {
        return true;
    }
}
