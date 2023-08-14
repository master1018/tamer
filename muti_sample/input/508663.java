public class ResourcesEditor extends AndroidEditor {
    public static final String ID = AndroidConstants.EDITORS_NAMESPACE + ".resources.ResourcesEditor"; 
    private UiElementNode mUiResourcesNode;
    public ResourcesEditor() {
        super();
    }
    @Override
    public UiElementNode getUiRootNode() {
        return mUiResourcesNode;
    }
    @Override
    public boolean isSaveAsAllowed() {
        return true;
    }
    @Override
    protected void createFormPages() {
        try {
            addPage(new ResourcesTreePage(this));
        } catch (PartInitException e) {
            AdtPlugin.log(IStatus.ERROR, "Error creating nested page"); 
            AdtPlugin.getDefault().getLog().log(e.getStatus());
        }
     }
    @Override
    protected void setInput(IEditorInput input) {
        super.setInput(input);
        if (input instanceof FileEditorInput) {
            FileEditorInput fileInput = (FileEditorInput) input;
            IFile file = fileInput.getFile();
            setPartName(String.format("%1$s",
                    file.getName()));
        }
    }
    @Override
    protected void xmlModelChanged(Document xml_doc) {
        initUiRootNode(false );
        mUiResourcesNode.setXmlDocument(xml_doc);
        if (xml_doc != null) {
            ElementDescriptor resources_desc =
                    ResourcesDescriptors.getInstance().getElementDescriptor();
            try {
                XPath xpath = AndroidXPathFactory.newXPath();
                Node node = (Node) xpath.evaluate("/" + resources_desc.getXmlName(),  
                        xml_doc,
                        XPathConstants.NODE);
                assert node != null && node.getNodeName().equals(resources_desc.getXmlName());
                mUiResourcesNode.loadFromXmlNode(node);
            } catch (XPathExpressionException e) {
                AdtPlugin.log(e, "XPath error when trying to find '%s' element in XML.", 
                        resources_desc.getXmlName());
            }
        }
        super.xmlModelChanged(xml_doc);
    }
    @Override
    protected void initUiRootNode(boolean force) {
        if (mUiResourcesNode == null || force) {
            ElementDescriptor resources_desc =
                    ResourcesDescriptors.getInstance().getElementDescriptor();   
            mUiResourcesNode = resources_desc.createUiNode();
            mUiResourcesNode.setEditor(this);
            onDescriptorsChanged();
        }
    }
    private void onDescriptorsChanged() {
    }
}
