public class UiElementPart extends ManifestSectionPart {
    private ManifestEditor mEditor;
    private UiElementNode mUiElementNode;
    private Composite mTable;
    public UiElementPart(Composite body, FormToolkit toolkit, ManifestEditor editor,
            UiElementNode uiElementNode, String sectionTitle, String sectionDescription,
            int extra_style) {
        super(body, toolkit, extra_style, sectionDescription != null);
        mEditor = editor;
        mUiElementNode = uiElementNode;
        setupSection(sectionTitle, sectionDescription);
        if (uiElementNode == null) {
            AdtPlugin.log(IStatus.ERROR, "Missing node to edit!"); 
            return;
        }
    }
    public ManifestEditor getEditor() {
        return mEditor;
    }
    public UiElementNode getUiElementNode() {
        return mUiElementNode;
    }
    public void setUiElementNode(UiElementNode uiElementNode) {
        mUiElementNode = uiElementNode;
    }
    @Override
    public void initialize(IManagedForm form) {
        super.initialize(form);
        createFormControls(form);
    }
    protected void setupSection(String sectionTitle, String sectionDescription) {
        Section section = getSection();
        section.setText(sectionTitle);
        section.setDescription(sectionDescription);
    }
    protected void createFormControls(IManagedForm managedForm) {
        setTable(createTableLayout(managedForm.getToolkit(), 2 ));
        createUiAttributes(managedForm);
    }
    protected void setTable(Composite table) {
        mTable = table;
    }
    protected Composite getTable() {
        return mTable;
    }
    protected void createUiAttributes(IManagedForm managedForm) {
        Composite table = getTable();
        if (table == null || managedForm == null) {
            return;
        }
        for (Control c : table.getChildren()) {
            c.dispose();
        }
        fillTable(table, managedForm);
        layoutChanged();
    }
    protected void fillTable(Composite table, IManagedForm managedForm) {
        int inserted = insertUiAttributes(mUiElementNode, table, managedForm);
        if (inserted == 0) {
            createLabel(table, managedForm.getToolkit(),
                    "No attributes to display, waiting for SDK to finish loading...",
                    null  );
        }
    }
    protected int insertUiAttributes(UiElementNode uiNode, Composite table, IManagedForm managedForm) {
        if (uiNode == null || table == null || managedForm == null) {
            return 0;
        }
        AttributeDescriptor[] attr_desc_list = uiNode.getAttributeDescriptors();
        for (AttributeDescriptor attr_desc : attr_desc_list) {
            if (attr_desc instanceof XmlnsAttributeDescriptor) {
                continue;
            }
            UiAttributeNode ui_attr = uiNode.findUiAttribute(attr_desc);
            if (ui_attr != null) {
                ui_attr.createUiControl(table, managedForm);
            } else {
                AdtPlugin.log(IStatus.WARNING,
                        "Attribute %1$s not declared in node %2$s, ignored.", 
                        attr_desc.getXmlLocalName(),
                        uiNode.getDescriptor().getXmlName());
            }
        }
        return attr_desc_list.length;
    }
    @Override
    public boolean isDirty() {
        if (mUiElementNode != null && !super.isDirty()) {
            for (UiAttributeNode ui_attr : mUiElementNode.getUiAttributes()) {
                if (ui_attr.isDirty()) {
                    markDirty();
                    break;
                }
            }
        }
        return super.isDirty();
    }
    @Override
    public void commit(boolean onSave) {
        if (mUiElementNode != null) {
            mEditor.editXmlModel(new Runnable() {
                public void run() {
                    for (UiAttributeNode ui_attr : mUiElementNode.getUiAttributes()) {
                        ui_attr.commit();
                    }
                }
            });
        }
        super.commit(onSave);
    }
}
