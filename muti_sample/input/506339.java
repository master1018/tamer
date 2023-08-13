final class ApplicationAttributesPart extends UiElementPart {
    private AppNodeUpdateListener mAppNodeUpdateListener;
    private IManagedForm mManagedForm;
    public ApplicationAttributesPart(Composite body, FormToolkit toolkit, ManifestEditor editor,
            UiElementNode applicationUiNode) {
        super(body, toolkit, editor, applicationUiNode,
                "Application Attributes", 
                "Defines the attributes specific to the application.", 
                Section.TWISTIE | Section.EXPANDED);
    }
    @Override
    public void setUiElementNode(UiElementNode uiElementNode) {
        super.setUiElementNode(uiElementNode);
        createUiAttributes(mManagedForm);
    }
    @Override
    protected void createFormControls(final IManagedForm managedForm) {
        mManagedForm = managedForm; 
        setTable(createTableLayout(managedForm.getToolkit(), 4 ));
        mAppNodeUpdateListener = new AppNodeUpdateListener();
        getUiElementNode().addUpdateListener(mAppNodeUpdateListener);
        createUiAttributes(mManagedForm);
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
    protected void createUiAttributes(IManagedForm managedForm) {
        Composite table = getTable();
        if (table == null || managedForm == null) {
            return;
        }
        for (Control c : table.getChildren()) {
            c.dispose();
        }
        UiElementNode uiElementNode = getUiElementNode(); 
        AttributeDescriptor[] attr_desc_list = uiElementNode.getAttributeDescriptors();
        int n = attr_desc_list.length;
        int n2 = (int) Math.ceil(n / 2.0);
        for (int i = 0; i < n; i++) {
            AttributeDescriptor attr_desc = attr_desc_list[i / 2 + (i & 1) * n2];
            if (attr_desc instanceof XmlnsAttributeDescriptor) {
                continue;
            }
            UiAttributeNode ui_attr = uiElementNode.findUiAttribute(attr_desc);
            if (ui_attr != null) {
                ui_attr.createUiControl(table, managedForm);
            } else {
                AdtPlugin.log(IStatus.WARNING,
                        "Attribute %1$s not declared in node %2$s, ignored.", 
                        attr_desc.getXmlLocalName(),
                        uiElementNode.getDescriptor().getXmlName());
            }
        }
        if (n == 0) {
            createLabel(table, managedForm.getToolkit(),
                    "No attributes to display, waiting for SDK to finish loading...",
                    null  );
        }
        if (mAppNodeUpdateListener != null) {
            mAppNodeUpdateListener.uiElementNodeUpdated(uiElementNode, null );
        }
        layoutChanged();
    }
    private class AppNodeUpdateListener implements IUiUpdateListener {        
        public void uiElementNodeUpdated(UiElementNode ui_node, UiUpdateState state) {
            Composite table = getTable();
            boolean exists = (ui_node.getXmlNode() != null);
            if (table != null && table.getEnabled() != exists) {
                table.setEnabled(exists);
                for (Control c : table.getChildren()) {
                    c.setEnabled(exists);
                }
            }
        }
    }
}
