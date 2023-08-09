public abstract class AndroidEditor extends FormEditor implements IResourceChangeListener {
    private static final String PREF_CURRENT_PAGE = "_current_page";
    private static String BROWSER_ID = "android"; 
    public final static String TEXT_EDITOR_ID = "editor_part"; 
    public static final int TEXT_WIDTH_HINT = 50;
    private int mTextPageIndex;
    private StructuredTextEditor mTextEditor;
    private XmlModelStateListener mXmlModelStateListener;
    private TargetChangeListener mTargetListener = null;
    private boolean mIsCreatingPage = false;
    private boolean mIsEditXmlModelPending;
    public AndroidEditor() {
        this(true);
    }
    public AndroidEditor(boolean addTargetListener) {
        super();
        if (addTargetListener) {
            ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
            mTargetListener = new TargetChangeListener() {
                @Override
                public IProject getProject() {
                    return AndroidEditor.this.getProject();
                }
                @Override
                public void reload() {
                    commitPages(false );
                    initUiRootNode(true );
                }
            };
            AdtPlugin.getDefault().addTargetListener(mTargetListener);
        }
    }
    abstract public UiElementNode getUiRootNode();
    abstract protected void createFormPages();
    protected void postCreatePages() {
    }
    abstract protected void initUiRootNode(boolean force);
    protected void xmlModelChanged(Document xml_doc) {
    }
    @Override
    protected void addPages() {
        createAndroidPages();
        selectDefaultPage(null );
    }
    protected void createAndroidPages() {
        mIsCreatingPage = true;
        createFormPages();
        createTextEditor();
        createUndoRedoActions();
        postCreatePages();
        mIsCreatingPage = false;
    }
    public boolean isCreatingPages() {
        return mIsCreatingPage;
    }
    private void createUndoRedoActions() {
        IActionBars bars = getEditorSite().getActionBars();
        if (bars != null) {
            IAction action = mTextEditor.getAction(ActionFactory.UNDO.getId());
            bars.setGlobalActionHandler(ActionFactory.UNDO.getId(), action);
            action = mTextEditor.getAction(ActionFactory.REDO.getId());
            bars.setGlobalActionHandler(ActionFactory.REDO.getId(), action);
            bars.updateActionBars();
        }
    }
    protected void selectDefaultPage(String defaultPageId) {
        if (defaultPageId == null) {
            if (getEditorInput() instanceof IFileEditorInput) {
                IFile file = ((IFileEditorInput) getEditorInput()).getFile();
                QualifiedName qname = new QualifiedName(AdtPlugin.PLUGIN_ID,
                        getClass().getSimpleName() + PREF_CURRENT_PAGE);
                String pageId;
                try {
                    pageId = file.getPersistentProperty(qname);
                    if (pageId != null) {
                        defaultPageId = pageId;
                    }
                } catch (CoreException e) {
                }
            }
        }
        if (defaultPageId != null) {
            try {
                setActivePage(Integer.parseInt(defaultPageId));
            } catch (Exception e) {
                AdtPlugin.log(e, "Selecting page '%s' in AndroidEditor failed", defaultPageId);
            }
        }
    }
    protected void removePages() {
        int count = getPageCount();
        for (int i = count - 1 ; i >= 0 ; i--) {
            removePage(i);
        }
    }
    @Override
    public IFormPage setActivePage(String pageId) {
        if (pageId.equals(TEXT_EDITOR_ID)) {
            super.setActivePage(mTextPageIndex);
            return null;
        } else {
            return super.setActivePage(pageId);
        }
    }
    @Override
    protected void pageChange(int newPageIndex) {
        super.pageChange(newPageIndex);
        if (mIsCreatingPage) {
            return;
        }
        if (getEditorInput() instanceof IFileEditorInput) {
            IFile file = ((IFileEditorInput) getEditorInput()).getFile();
            QualifiedName qname = new QualifiedName(AdtPlugin.PLUGIN_ID,
                    getClass().getSimpleName() + PREF_CURRENT_PAGE);
            try {
                file.setPersistentProperty(qname, Integer.toString(newPageIndex));
            } catch (CoreException e) {
            }
        }
    }
    public void resourceChanged(final IResourceChangeEvent event) {
        if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    IWorkbenchPage[] pages = getSite().getWorkbenchWindow()
                            .getPages();
                    for (int i = 0; i < pages.length; i++) {
                        if (((FileEditorInput)mTextEditor.getEditorInput())
                                .getFile().getProject().equals(
                                        event.getResource())) {
                            IEditorPart editorPart = pages[i].findEditor(mTextEditor
                                    .getEditorInput());
                            pages[i].closeEditor(editorPart, true);
                        }
                    }
                }
            });
        }
    }
    @Override
    public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
        if (!(editorInput instanceof IFileEditorInput))
            throw new PartInitException("Invalid Input: Must be IFileEditorInput");
        super.init(site, editorInput);
    }
    @Override
    public void dispose() {
        IStructuredModel xml_model = getModelForRead();
        if (xml_model != null) {
            try {
                if (mXmlModelStateListener != null) {
                    xml_model.removeModelStateListener(mXmlModelStateListener);
                }
            } finally {
                xml_model.releaseFromRead();
            }
        }
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        if (mTargetListener != null) {
            AdtPlugin.getDefault().removeTargetListener(mTargetListener);
            mTargetListener = null;
        }
        super.dispose();
    }
    @Override
    public void doSave(IProgressMonitor monitor) {
        commitPages(true );
        getEditor(mTextPageIndex).doSave(monitor);
    }
    @Override
    public void doSaveAs() {
        commitPages(true );
        IEditorPart editor = getEditor(mTextPageIndex);
        editor.doSaveAs();
        setPageText(mTextPageIndex, editor.getTitle());
        setInput(editor.getEditorInput());
    }
    @Override
    public void commitPages(boolean onSave) {
        if (pages != null) {
            for (int i = 0; i < pages.size(); i++) {
                Object page = pages.get(i);
                if (page != null && page instanceof IFormPage) {
                    IFormPage form_page = (IFormPage) page;
                    IManagedForm managed_form = form_page.getManagedForm();
                    if (managed_form != null && managed_form.isDirty()) {
                        managed_form.commit(onSave);
                    }
                }
            }
        }
    }
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }
    public final IHyperlinkListener createHyperlinkListener() {
        return new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent e) {
                super.linkActivated(e);
                String link = e.data.toString();
                if (link.startsWith("http") ||          
                        link.startsWith("file:/")) {    
                    openLinkInBrowser(link);
                } else if (link.startsWith("page:")) {  
                    setActivePage(link.substring(5 ));
                }
            }
        };
    }
    private void openLinkInBrowser(String link) {
        try {
            IWorkbenchBrowserSupport wbs = WorkbenchBrowserSupport.getInstance();
            wbs.createBrowser(BROWSER_ID).openURL(new URL(link));
        } catch (PartInitException e1) {
        } catch (MalformedURLException e1) {
        }
    }
    private void createTextEditor() {
        try {
            mTextEditor = new StructuredTextEditor();
            int index = addPage(mTextEditor, getEditorInput());
            mTextPageIndex = index;
            setPageText(index, mTextEditor.getTitle());
            if (!(mTextEditor.getTextViewer().getDocument() instanceof IStructuredDocument)) {
                Status status = new Status(IStatus.ERROR, AdtPlugin.PLUGIN_ID,
                        "Error opening the Android XML editor. Is the document an XML file?");
                throw new RuntimeException("Android XML Editor Error", new CoreException(status));
            }
            IStructuredModel xml_model = getModelForRead();
            if (xml_model != null) {
                try {
                    mXmlModelStateListener = new XmlModelStateListener();
                    xml_model.addModelStateListener(mXmlModelStateListener);
                    mXmlModelStateListener.modelChanged(xml_model);
                } catch (Exception e) {
                    AdtPlugin.log(e, "Error while loading editor"); 
                } finally {
                    xml_model.releaseFromRead();
                }
            }
        } catch (PartInitException e) {
            ErrorDialog.openError(getSite().getShell(),
                    "Android XML Editor Error", null, e.getStatus());
        }
    }
    public final ISourceViewer getStructuredSourceViewer() {
        if (mTextEditor != null) {
            return mTextEditor.getTextViewer();
        }
        return null;
    }
    public final IStructuredDocument getStructuredDocument() {
        if (mTextEditor != null && mTextEditor.getTextViewer() != null) {
            return (IStructuredDocument) mTextEditor.getTextViewer().getDocument();
        }
        return null;
    }
    public final IStructuredModel getModelForRead() {
        IStructuredDocument document = getStructuredDocument();
        if (document != null) {
            IModelManager mm = StructuredModelManager.getModelManager();
            if (mm != null) {
                return mm.getModelForRead(document);
            }
        }
        return null;
    }
    public final IStructuredModel getModelForEdit() {
        IStructuredDocument document = getStructuredDocument();
        if (document != null) {
            IModelManager mm = StructuredModelManager.getModelManager();
            if (mm != null) {
                return mm.getModelForEdit(document);
            }
        }
        return null;
    }
    public final void editXmlModel(Runnable edit_action) {
        IStructuredModel model = getModelForEdit();
        try {
            model.aboutToChangeModel();
            mIsEditXmlModelPending = true;
            edit_action.run();
        } finally {
            mIsEditXmlModelPending = false;
            model.changedModel();
            model.releaseFromEdit();
        }
    }
    public boolean isEditXmlModelPending() {
        return mIsEditXmlModelPending;
    }
    private final boolean beginUndoRecording(String label) {
        IStructuredDocument document = getStructuredDocument();
        if (document != null) {
            IModelManager mm = StructuredModelManager.getModelManager();
            if (mm != null) {
                IStructuredModel model = mm.getModelForEdit(document);
                if (model != null) {
                    model.beginRecording(this, label);
                    return true;
                }
            }
        }
        return false;
    }
    private final void endUndoRecording() {
        IStructuredDocument document = getStructuredDocument();
        if (document != null) {
            IModelManager mm = StructuredModelManager.getModelManager();
            if (mm != null) {
                IStructuredModel model = mm.getModelForEdit(document);
                if (model != null) {
                    model.endRecording(this);
                }
            }
        }
    }
    public void wrapUndoRecording(String label, Runnable undoableAction) {
        boolean recording = false;
        try {
            recording = beginUndoRecording(label);
            undoableAction.run();
        } finally {
            if (recording) {
                endUndoRecording();
            }
        }
    }
    protected final Document getXmlDocument(IStructuredModel model) {
        if (model == null) {
            AdtPlugin.log(IStatus.WARNING, "Android Editor: No XML model for root node."); 
            return null;
        }
        if (model instanceof IDOMModel) {
            IDOMModel dom_model = (IDOMModel) model;
            return dom_model.getDocument();
        }
        return null;
    }
    public IProject getProject() {
        if (mTextEditor != null) {
            IEditorInput input = mTextEditor.getEditorInput();
            if (input instanceof FileEditorInput) {
                FileEditorInput fileInput = (FileEditorInput)input;
                IFile inputFile = fileInput.getFile();
                if (inputFile != null) {
                    return inputFile.getProject();
                }
            }
        }
        return null;
    }
    public AndroidTargetData getTargetData() {
        IProject project = getProject();
        if (project != null) {
            Sdk currentSdk = Sdk.getCurrent();
            if (currentSdk != null) {
                IAndroidTarget target = currentSdk.getTarget(project);
                if (target != null) {
                    return currentSdk.getTargetData(target);
                }
            }
        }
        return null;
    }
    private class XmlModelStateListener implements IModelStateListener {
        public void modelAboutToBeChanged(IStructuredModel model) {
        }
        public void modelChanged(IStructuredModel model) {
            xmlModelChanged(getXmlDocument(model));
        }
        public void modelDirtyStateChanged(IStructuredModel model, boolean isDirty) {
        }
        public void modelResourceDeleted(IStructuredModel model) {
        }
        public void modelResourceMoved(IStructuredModel oldModel, IStructuredModel newModel) {
        }
        public void modelAboutToBeReinitialized(IStructuredModel structuredModel) {
        }
        public void modelReinitialized(IStructuredModel structuredModel) {
        }
    }
}
