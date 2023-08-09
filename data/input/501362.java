final class OverviewInfoPart extends UiElementPart {
    private IManagedForm mManagedForm;
    public OverviewInfoPart(Composite body, FormToolkit toolkit, ManifestEditor editor) {
        super(body, toolkit, editor,
                getManifestUiNode(editor),  
                "Manifest General Attributes", 
                "Defines general information about the AndroidManifest.xml", 
                Section.TWISTIE | Section.EXPANDED);
    }
    private static UiElementNode getManifestUiNode(ManifestEditor editor) {
        AndroidManifestDescriptors manifestDescriptors = editor.getManifestDescriptors();
        if (manifestDescriptors != null) {
            ElementDescriptor desc = manifestDescriptors.getManifestElement();
            if (editor.getUiRootNode().getDescriptor() == desc) {
                return editor.getUiRootNode();
            } else {
                return editor.getUiRootNode().findUiChildNode(desc.getXmlName());
            }
        }
        return editor.getUiRootNode();
    }
    @Override
    protected void createFormControls(final IManagedForm managedForm) {
        mManagedForm = managedForm; 
        super.createFormControls(managedForm);
    }
    public void onSdkChanged() {
        setUiElementNode(getManifestUiNode(getEditor()));
        createUiAttributes(mManagedForm);
    }
}
