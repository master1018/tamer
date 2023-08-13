public final class PermissionPage extends FormPage {
    public final static String PAGE_ID = "permission_page"; 
    ManifestEditor mEditor;
    private UiTreeBlock mTreeBlock;
    public PermissionPage(ManifestEditor editor) {
        super(editor, PAGE_ID, "Permissions");  
        mEditor = editor;
    }
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        ScrolledForm form = managedForm.getForm();
        form.setText("Android Manifest Permissions");
        form.setImage(AdtPlugin.getAndroidLogo());
        UiElementNode manifest = mEditor.getUiRootNode();
        AndroidManifestDescriptors manifestDescriptor = mEditor.getManifestDescriptors();
        ElementDescriptor[] descriptorFilters = null;
        if (manifestDescriptor != null) {
            descriptorFilters = new ElementDescriptor[] {
                    manifestDescriptor.getPermissionElement(),
                    manifestDescriptor.getUsesPermissionElement(),
                    manifestDescriptor.getPermissionGroupElement(),
                    manifestDescriptor.getPermissionTreeElement()
            };
        }
        mTreeBlock = new UiTreeBlock(mEditor, manifest,
                true ,
                descriptorFilters,
                "Permissions",
                "List of permissions defined and used by the manifest");
        mTreeBlock.createContent(managedForm);
    }
    public void refreshUiNode() {
        if (mTreeBlock != null) {
            UiElementNode manifest = mEditor.getUiRootNode();
            AndroidManifestDescriptors manifestDescriptor = mEditor.getManifestDescriptors();
            mTreeBlock.changeRootAndDescriptors(manifest,
                    new ElementDescriptor[] {
                        manifestDescriptor.getPermissionElement(),
                        manifestDescriptor.getUsesPermissionElement(),
                        manifestDescriptor.getPermissionGroupElement(),
                        manifestDescriptor.getPermissionTreeElement()
                    },
                    true );
        }
    }
}
