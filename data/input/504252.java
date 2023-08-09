public class LayoutEditor extends AndroidEditor implements IShowEditorInput, IPartListener {
    public static final String ID = AndroidConstants.EDITORS_NAMESPACE + ".layout.LayoutEditor"; 
    private UiDocumentNode mUiRootNode;
    private IGraphicalLayoutEditor mGraphicalEditor;
    private int mGraphicalEditorIndex;
    private UiContentOutlinePage mOutline;
    private UiPropertySheetPage mPropertyPage;
    private UiEditorActions mUiEditorActions;
    private final HashMap<String, ElementDescriptor> mUnknownDescriptorMap =
        new HashMap<String, ElementDescriptor>();
    private boolean mNewFileOnConfigChange = false;
    public LayoutEditor() {
        super(false );
    }
    @Override
    public UiDocumentNode getUiRootNode() {
        return mUiRootNode;
    }
    @Override
    public void dispose() {
        getSite().getPage().removePartListener(this);
        super.dispose();
    }
    @Override
    public void doSave(IProgressMonitor monitor) {
        super.doSave(monitor);
        if (mGraphicalEditor != null) {
            mGraphicalEditor.doSave(monitor);
        }
    }
    @Override
    public boolean isSaveAsAllowed() {
        return true;
    }
    @Override
    protected void createFormPages() {
        try {
            if (System.getenv("ANDROID_DISABLE_LAYOUT") == null) {      
                IFile editedFile = null;
                IEditorInput input = getEditorInput();
                if (input instanceof FileEditorInput) {
                    FileEditorInput fileInput = (FileEditorInput)input;
                    editedFile = fileInput.getFile();
                } else {
                    AdtPlugin.log(IStatus.ERROR,
                            "Input is not of type FileEditorInput: %1$s",  
                            input.toString());
                }
                if (mGraphicalEditor == null) {
                    String useGle2 = System.getenv("USE_GLE2");     
                    if (useGle2 != null && !useGle2.equals("0")) {  
                        mGraphicalEditor = new GraphicalEditorPart(this);
                    } else {
                        mGraphicalEditor = new GraphicalLayoutEditor(this);
                    }
                    mGraphicalEditorIndex = addPage(mGraphicalEditor, getEditorInput());
                    setPageText(mGraphicalEditorIndex, mGraphicalEditor.getTitle());
                    mGraphicalEditor.openFile(editedFile);
                } else {
                    if (mNewFileOnConfigChange) {
                        mGraphicalEditor.changeFileOnNewConfig(editedFile);
                        mNewFileOnConfigChange = false;
                    } else {
                        mGraphicalEditor.replaceFile(editedFile);
                    }
                }
                getSite().getPage().addPartListener(this);
            }
        } catch (PartInitException e) {
            AdtPlugin.log(e, "Error creating nested page"); 
        }
     }
    @Override
    protected void postCreatePages() {
        super.postCreatePages();
        if (mGraphicalEditor instanceof GraphicalLayoutEditor) {
            setActivePage(getPageCount() - 1);
        }
    }
    @Override
    protected void setInput(IEditorInput input) {
        super.setInput(input);
        handleNewInput(input);
    }
    @Override
    protected void setInputWithNotify(IEditorInput input) {
        super.setInputWithNotify(input);
        handleNewInput(input);
    }
    public void showEditorInput(IEditorInput editorInput) {
        doSave(new NullProgressMonitor());
        int currentPage = getActivePage();
        int count = getPageCount();
        for (int i = count - 1 ; i > mGraphicalEditorIndex ; i--) {
            removePage(i);
        }
        for (int i = mGraphicalEditorIndex - 1 ; i >= 0 ; i--) {
            removePage(i);
        }
        setInputWithNotify(editorInput);
        createAndroidPages();
        selectDefaultPage(Integer.toString(currentPage));
        if (mOutline != null && mGraphicalEditor != null) {
            mOutline.reloadModel();
        }
    }
    @Override
    protected void xmlModelChanged(Document xml_doc) {
        initUiRootNode(false );
        mUiRootNode.loadFromXmlNode(xml_doc);
        super.xmlModelChanged(xml_doc);
        if (mGraphicalEditor != null) {
            mGraphicalEditor.onXmlModelChanged();
        }
        if (mOutline != null) {
            mOutline.reloadModel();
        }
    }
    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class adapter) {
        if (IContentOutlinePage.class == adapter && mGraphicalEditor != null) {
            if (mOutline == null && mGraphicalEditor instanceof GraphicalLayoutEditor) {
                mOutline = new UiContentOutlinePage(
                        (GraphicalLayoutEditor) mGraphicalEditor,
                        new TreeViewer());
            }
            return mOutline;
        }
        if (IPropertySheetPage.class == adapter && mGraphicalEditor != null) {
            if (mPropertyPage == null) {
                mPropertyPage = new UiPropertySheetPage();
            }
            return mPropertyPage;
        }
        return super.getAdapter(adapter);
    }
    @Override
    protected void pageChange(int newPageIndex) {
        super.pageChange(newPageIndex);
        if (mGraphicalEditor != null) {
            if (newPageIndex == mGraphicalEditorIndex) {
                mGraphicalEditor.activated();
            } else {
                mGraphicalEditor.deactivated();
            }
        }
    }
    public void partActivated(IWorkbenchPart part) {
        if (part == this) {
            if (mGraphicalEditor != null) {
                if (getActivePage() == mGraphicalEditorIndex) {
                    mGraphicalEditor.activated();
                } else {
                    mGraphicalEditor.deactivated();
                }
            }
        }
    }
    public void partBroughtToTop(IWorkbenchPart part) {
        partActivated(part);
    }
    public void partClosed(IWorkbenchPart part) {
    }
    public void partDeactivated(IWorkbenchPart part) {
        if (part == this) {
            if (mGraphicalEditor != null && getActivePage() == mGraphicalEditorIndex) {
                mGraphicalEditor.deactivated();
            }
        }
    }
    public void partOpened(IWorkbenchPart part) {
    }
    public class UiEditorActions extends UiActions {
        @Override
        protected UiDocumentNode getRootNode() {
            return mUiRootNode;
        }
        @Override
        protected void selectUiNode(UiElementNode uiNodeToSelect) {
            mGraphicalEditor.selectModel(uiNodeToSelect);
        }
        @Override
        public void commitPendingXmlChanges() {
        }
    }
    public UiEditorActions getUiEditorActions() {
        if (mUiEditorActions == null) {
            mUiEditorActions = new UiEditorActions();
        }
        return mUiEditorActions;
    }
    public boolean isGraphicalEditorActive() {
        IWorkbenchPartSite workbenchSite = getSite();
        IWorkbenchPage workbenchPage = workbenchSite.getPage();
        if (workbenchPage.isPartVisible(this) && workbenchPage.getActiveEditor() == this) {
            return mGraphicalEditorIndex == getActivePage();
        }
        return false;
    }
    @Override
    public void initUiRootNode(boolean force) {
        if (mUiRootNode == null || force) {
            AndroidTargetData data = getTargetData();
            Document doc = null;
            if (mUiRootNode != null) {
                doc = mUiRootNode.getXmlDocument();
            }
            DocumentDescriptor desc;
            if (data == null) {
                desc = new DocumentDescriptor("temp", null );
            } else {
                desc = data.getLayoutDescriptors().getDescriptor();
            }
            mUiRootNode = (UiDocumentNode) desc.createUiNode();
            mUiRootNode.setEditor(this);
            mUiRootNode.setUnknownDescriptorProvider(new IUnknownDescriptorProvider() {
                public ElementDescriptor getDescriptor(String xmlLocalName) {
                    ElementDescriptor desc = mUnknownDescriptorMap.get(xmlLocalName);
                    if (desc == null) {
                        desc = createUnknownDescriptor(xmlLocalName);
                        mUnknownDescriptorMap.put(xmlLocalName, desc);
                    }
                    return desc;
                }
            });
            onDescriptorsChanged(doc);
        }
    }
    private ElementDescriptor createUnknownDescriptor(String xmlLocalName) {
        IEditorInput editorInput = getEditorInput();
        if (editorInput instanceof IFileEditorInput) {
            IFileEditorInput fileInput = (IFileEditorInput)editorInput;
            IProject project = fileInput.getFile().getProject();
            ElementDescriptor desc = CustomViewDescriptorService.getInstance().getDescriptor(
                    project, xmlLocalName);
            if (desc != null) {
                return desc;
            }
            Sdk currentSdk = Sdk.getCurrent();
            if (currentSdk != null) {
                IAndroidTarget target = currentSdk.getTarget(project);
                if (target != null) {
                    AndroidTargetData data = currentSdk.getTargetData(target);
                    desc = data.getLayoutDescriptors().getBaseViewDescriptor();
                }
            }
            if (desc != null) {
                return desc;
            }
        }
        return new ViewElementDescriptor(xmlLocalName, xmlLocalName);
    }
    private void onDescriptorsChanged(Document document) {
        mUnknownDescriptorMap.clear();
        if (document != null) {
            mUiRootNode.loadFromXmlNode(document);
        } else {
            mUiRootNode.reloadFromXmlNode(mUiRootNode.getXmlDocument());
        }
        if (mOutline != null) {
            mOutline.reloadModel();
        }
        if (mGraphicalEditor != null) {
            mGraphicalEditor.onTargetChange();
            mGraphicalEditor.reloadPalette();
        }
    }
    private void handleNewInput(IEditorInput input) {
        if (input instanceof FileEditorInput) {
            FileEditorInput fileInput = (FileEditorInput) input;
            IFile file = fileInput.getFile();
            setPartName(String.format("%1$s",
                    file.getName()));
        }
    }
    public void setNewFileOnConfigChange(boolean state) {
        mNewFileOnConfigChange = state;
    }
}
