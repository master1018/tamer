public class XmlEditor extends AndroidEditor {
    public static final String ID = AndroidConstants.EDITORS_NAMESPACE + ".xml.XmlEditor"; 
    private UiDocumentNode mUiRootNode;
    public XmlEditor() {
        super();
    }
    @Override
    public UiDocumentNode getUiRootNode() {
        return mUiRootNode;
    }
    public static boolean canHandleFile(IFile file) {
        IProject project = file.getProject();
        IAndroidTarget target = Sdk.getCurrent().getTarget(project);
        if (target != null) {
            AndroidTargetData data = Sdk.getCurrent().getTargetData(target);
            FirstElementParser.Result result = FirstElementParser.parse(
                    file.getLocation().toOSString(),
                    SdkConstants.NS_RESOURCES);
            if (result != null && data != null) {
                String name = result.getElement();
                if (name != null && result.getXmlnsPrefix() != null) {
                    DocumentDescriptor desc = data.getXmlDescriptors().getDescriptor();
                    for (ElementDescriptor elem : desc.getChildren()) {
                        if (elem.getXmlName().equals(name)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    @Override
    public boolean isSaveAsAllowed() {
        return true;
    }
    @Override
    protected void createFormPages() {
        try {
            addPage(new XmlTreePage(this));
        } catch (PartInitException e) {
            AdtPlugin.log(e, "Error creating nested page"); 
        }
    }
    @Override
    protected void setInput(IEditorInput input) {
        super.setInput(input);
        if (input instanceof FileEditorInput) {
            FileEditorInput fileInput = (FileEditorInput) input;
            IFile file = fileInput.getFile();
            setPartName(String.format("%1$s", file.getName()));
        }
    }
    @Override
    protected void xmlModelChanged(Document xml_doc) {
        initUiRootNode(false );
        mUiRootNode.loadFromXmlNode(xml_doc);
        super.xmlModelChanged(xml_doc);
    }
    @Override
    protected void initUiRootNode(boolean force) {
        if (mUiRootNode == null || force) {
            Document doc = null;
            if (mUiRootNode != null) {
                doc = mUiRootNode.getXmlDocument();
            }
            AndroidTargetData data = getTargetData();
            DocumentDescriptor desc;
            if (data == null) {
                desc = new DocumentDescriptor("temp", null );
            } else {
                desc = data.getXmlDescriptors().getDescriptor();
            }
            mUiRootNode = (UiDocumentNode) desc.createUiNode();
            mUiRootNode.setEditor(this);
            onDescriptorsChanged(doc);
        }
    }
    private void onDescriptorsChanged(Document document) {
        if (document != null) {
            mUiRootNode.loadFromXmlNode(document);
        } else {
            mUiRootNode.reloadFromXmlNode(mUiRootNode.getXmlNode());
        }
    }
}
