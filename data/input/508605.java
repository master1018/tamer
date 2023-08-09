public final class ApplicationPage extends FormPage {
    public final static String PAGE_ID = "application_page"; 
    ManifestEditor mEditor;
    private ApplicationToggle mTooglePart;
    private ApplicationAttributesPart mAttrPart;
    private UiTreeBlock mTreeBlock;
    public ApplicationPage(ManifestEditor editor) {
        super(editor, PAGE_ID, "Application"); 
        mEditor = editor;
    }
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        ScrolledForm form = managedForm.getForm();
        form.setText("Android Manifest Application");
        form.setImage(AdtPlugin.getAndroidLogo());
        UiElementNode appUiNode = getUiApplicationNode();
        Composite body = form.getBody();
        FormToolkit toolkit = managedForm.getToolkit();
        mTooglePart = new ApplicationToggle(body, toolkit, mEditor, appUiNode);
        mTooglePart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        managedForm.addPart(mTooglePart);
        mAttrPart = new ApplicationAttributesPart(body, toolkit, mEditor, appUiNode);
        mAttrPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        managedForm.addPart(mAttrPart);
        mTreeBlock = new UiTreeBlock(mEditor, appUiNode,
                false ,
                null ,
                "Application Nodes",
                "List of all elements in the application");
        mTreeBlock.createContent(managedForm);
    }
    private UiElementNode getUiApplicationNode() {
        AndroidManifestDescriptors manifestDescriptor = mEditor.getManifestDescriptors();
        if (manifestDescriptor != null) {
            ElementDescriptor desc = manifestDescriptor.getApplicationElement();
            return mEditor.getUiRootNode().findUiChildNode(desc.getXmlName());
        } else {
            return mEditor.getUiRootNode();
        }
    }
    public void refreshUiApplicationNode() {
        UiElementNode appUiNode = getUiApplicationNode();
        if (mTooglePart != null) {
            mTooglePart.setUiElementNode(appUiNode);
        }
        if (mAttrPart != null) {
            mAttrPart.setUiElementNode(appUiNode);
        }
        if (mTreeBlock != null) {
            mTreeBlock.changeRootAndDescriptors(appUiNode,
                    null ,
                    true );
        }
    }
}
