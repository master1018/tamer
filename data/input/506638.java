public final class InstrumentationPage extends FormPage {
    public final static String PAGE_ID = "instrumentation_page"; 
    ManifestEditor mEditor;
    private UiTreeBlock mTreeBlock;
    public InstrumentationPage(ManifestEditor editor) {
        super(editor, PAGE_ID, "Instrumentation");  
        mEditor = editor;
    }
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        ScrolledForm form = managedForm.getForm();
        form.setText("Android Manifest Instrumentation");
        form.setImage(AdtPlugin.getAndroidLogo());
        UiElementNode manifest = mEditor.getUiRootNode();
        AndroidManifestDescriptors manifestDescriptor = mEditor.getManifestDescriptors();
        ElementDescriptor[] descriptorFilters = null;
        if (manifestDescriptor != null) {
            descriptorFilters = new ElementDescriptor[] {
                    manifestDescriptor.getInstrumentationElement(),
            };
        }
        mTreeBlock = new UiTreeBlock(mEditor, manifest,
                true ,
                descriptorFilters,
                "Instrumentation",
                "List of instrumentations defined in the manifest");
        mTreeBlock.createContent(managedForm);
    }
    public void refreshUiNode() {
        if (mTreeBlock != null) {
            UiElementNode manifest = mEditor.getUiRootNode();
            AndroidManifestDescriptors manifestDescriptor = mEditor.getManifestDescriptors();
            mTreeBlock.changeRootAndDescriptors(manifest,
                    new ElementDescriptor[] {
                        manifestDescriptor.getInstrumentationElement()
                    },
                    true );
        }
    }
}
