final class ApplicationToggle extends UiElementPart {
    private Button mCheckbox;
    private AppNodeUpdateListener mAppNodeUpdateListener;
    public boolean mInternalModification;
    private FormText mTooltipFormText;
    public ApplicationToggle(Composite body, FormToolkit toolkit, ManifestEditor editor,
            UiElementNode applicationUiNode) {
        super(body, toolkit, editor, applicationUiNode,
                "Application Toggle",
                null, 
                Section.TWISTIE | Section.EXPANDED);
    }
    @Override
    public void dispose() {
        super.dispose();
        if (getUiElementNode() != null && mAppNodeUpdateListener != null) {
            getUiElementNode().removeUpdateListener(mAppNodeUpdateListener);
            mAppNodeUpdateListener = null;
        }
    }
    @Override
    public void setUiElementNode(UiElementNode uiElementNode) {
        super.setUiElementNode(uiElementNode);
        updateTooltip();
        mAppNodeUpdateListener.uiElementNodeUpdated(getUiElementNode(),
                UiUpdateState.CHILDREN_CHANGED);
    }
    @Override
    protected void createFormControls(IManagedForm managedForm) {
        FormToolkit toolkit = managedForm.getToolkit();
        Composite table = createTableLayout(toolkit, 1 );
        mTooltipFormText = createFormText(table, toolkit, true, "<form></form>",
                false );
        updateTooltip();
        mCheckbox = toolkit.createButton(table,
                "Define an <application> tag in the AndroidManifest.xml",
                SWT.CHECK);
        mCheckbox.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
        mCheckbox.setSelection(false);
        mCheckbox.addSelectionListener(new CheckboxSelectionListener());
        mAppNodeUpdateListener = new AppNodeUpdateListener();
        getUiElementNode().addUpdateListener(mAppNodeUpdateListener);
        mAppNodeUpdateListener.uiElementNodeUpdated(getUiElementNode(),
                UiUpdateState.CHILDREN_CHANGED);
        layoutChanged();
    }
    private void updateTooltip() {
        boolean isVisible = false;
        String tooltip = getUiElementNode().getDescriptor().getTooltip();
        if (tooltip != null) {
            tooltip = DescriptorsUtils.formatFormText(tooltip,
                    getUiElementNode().getDescriptor(),
                    Sdk.getCurrent().getDocumentationBaseUrl());
            mTooltipFormText.setText(tooltip, true , true );
            mTooltipFormText.setImage(DescriptorsUtils.IMAGE_KEY, AdtPlugin.getAndroidLogo());
            mTooltipFormText.addHyperlinkListener(getEditor().createHyperlinkListener());
            isVisible = true;
        }
        mTooltipFormText.setVisible(isVisible);
    }
    private class CheckboxSelectionListener extends SelectionAdapter {
        private Node mUndoXmlNode;
        private Node mUndoXmlParent;
        private Node mUndoXmlNextNode;
        private Node mUndoXmlNextElement;
        private Document mUndoXmlDocument;
        @Override
        public void widgetSelected(SelectionEvent e) {
            super.widgetSelected(e);
            if (!mInternalModification && getUiElementNode() != null) {
                getUiElementNode().getEditor().wrapUndoRecording(
                        mCheckbox.getSelection()
                            ? "Create or restore Application node"
                            : "Remove Application node",
                        new Runnable() {
                            public void run() {
                                getUiElementNode().getEditor().editXmlModel(new Runnable() {
                                    public void run() {
                                        if (mCheckbox.getSelection()) {
                                            boolean create = true;
                                            if (mUndoXmlNode != null) {
                                                create = !restoreApplicationNode();
                                            }
                                            if (create) {
                                                getUiElementNode().createXmlNode();
                                            }
                                        } else {
                                            removeApplicationNode();
                                        }
                                    }
                                });
                            }
                });
            }
        }
        private boolean restoreApplicationNode() {
            if (mUndoXmlDocument == null || mUndoXmlNode == null) {
                return false;
            }
            mUndoXmlParent = validateNode(mUndoXmlDocument, mUndoXmlParent);
            mUndoXmlNextNode = validateNode(mUndoXmlDocument, mUndoXmlNextNode);
            mUndoXmlNextElement = validateNode(mUndoXmlDocument, mUndoXmlNextElement);
            if (mUndoXmlParent == null){
                mUndoXmlParent = getUiElementNode().getUiParent().prepareCommit();
                mUndoXmlNextNode = null;
                mUndoXmlNextElement = null;
            }
            boolean success = false;
            if (mUndoXmlParent != null) {
                Node next = mUndoXmlNextNode;
                if (next == null) {
                    next = mUndoXmlNextElement;
                }
                mUndoXmlParent.insertBefore(mUndoXmlNode, next);
                if (next == null) {
                    Text sep = mUndoXmlDocument.createTextNode("\n");  
                    mUndoXmlParent.insertBefore(sep, null);  
                }
                success = true;
            } 
            mUndoXmlParent = null;
            mUndoXmlNextNode = null;
            mUndoXmlNextElement = null;
            mUndoXmlNode = null;
            mUndoXmlDocument = null;
            return success;
        }
        private Node validateNode(Node root_node, Node xml_node) {
            if (root_node == xml_node) {
                return xml_node;
            } else {
                for (Node node = root_node.getFirstChild(); node != null;
                        node = node.getNextSibling()) {
                    if (root_node == xml_node || validateNode(node, xml_node) != null) {
                        return xml_node;
                    }
                }
            }
            return null;
        }
        private void removeApplicationNode() {
            Node xml_node = getUiElementNode().getXmlNode();
            if (xml_node == null) {
                return;
            }
            mUndoXmlDocument = xml_node.getOwnerDocument();
            mUndoXmlParent = xml_node.getParentNode();
            mUndoXmlNextNode = xml_node.getNextSibling();
            mUndoXmlNextElement = mUndoXmlNextNode;
            while (mUndoXmlNextElement != null &&
                    mUndoXmlNextElement.getNodeType() != Node.ELEMENT_NODE) {
                mUndoXmlNextElement = mUndoXmlNextElement.getNextSibling();
            }
            mUndoXmlNode = getUiElementNode().deleteXmlNode();
        }
    }
    private class AppNodeUpdateListener implements IUiUpdateListener {        
        public void uiElementNodeUpdated(UiElementNode ui_node, UiUpdateState state) {
            try {
                mInternalModification = true;
                boolean exists = ui_node.getXmlNode() != null;
                if (mCheckbox.getSelection() != exists) {
                    mCheckbox.setSelection(exists);
                }
            } finally {
                mInternalModification = false;
            }
        }
    }
}
