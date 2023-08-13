public class MenuEditor extends AndroidEditor {
    public static final String ID = AndroidConstants.EDITORS_NAMESPACE + ".menu.MenuEditor"; 
    private UiElementNode mUiRootNode;
    public MenuEditor() {
        super();
    }
    @Override
    public UiElementNode getUiRootNode() {
        return mUiRootNode;
    }
    @Override
    public boolean isSaveAsAllowed() {
        return true;
    }
    @Override
    protected void createFormPages() {
        try {
            addPage(new MenuTreePage(this));
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
        mUiRootNode.setXmlDocument(xml_doc);
        if (xml_doc != null) {
            ElementDescriptor root_desc = mUiRootNode.getDescriptor();
            try {
                XPath xpath = AndroidXPathFactory.newXPath();
                Node node = (Node) xpath.evaluate("/" + root_desc.getXmlName(),  
                        xml_doc,
                        XPathConstants.NODE);
                if (node == null && root_desc.isMandatory()) {
                    node = mUiRootNode.createXmlNode();
                }
                mUiRootNode.loadFromXmlNode(node);
            } catch (XPathExpressionException e) {
                AdtPlugin.log(e, "XPath error when trying to find '%s' element in XML.", 
                        root_desc.getXmlName());
            }
        }
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
            ElementDescriptor desc;
            if (data == null) {
                desc = new ElementDescriptor("temp", null );
            } else {
                desc = data.getMenuDescriptors().getDescriptor();
            }
            mUiRootNode = desc.createUiNode();
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
