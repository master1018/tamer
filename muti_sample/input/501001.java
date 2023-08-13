public final class ResourcesTreePage extends FormPage {
    public final static String PAGE_ID = "res_tree_page"; 
    ResourcesEditor mEditor;
    public ResourcesTreePage(ResourcesEditor editor) {
        super(editor, PAGE_ID, "Resources");  
        mEditor = editor;
    }
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        ScrolledForm form = managedForm.getForm();
        String configText = null;
        IEditorInput input = mEditor.getEditorInput();
        if (input instanceof FileEditorInput) {
            FileEditorInput fileInput = (FileEditorInput)input;
            IFile iFile = fileInput.getFile();
            ResourceFolder resFolder = ResourceManager.getInstance().getResourceFolder(iFile);
            if (resFolder != null) {
                configText = resFolder.getConfiguration().toDisplayString();
            }
        }
        if (configText != null) {
            form.setText(String.format("Android Resources (%1$s)", configText));
        } else {
            form.setText("Android Resources");
        }
        form.setImage(AdtPlugin.getAndroidLogo());
        UiElementNode resources = mEditor.getUiRootNode();
        UiTreeBlock block = new UiTreeBlock(mEditor, resources,
                true ,
                null ,
                "Resources Elements",
                "List of all resources elements in this XML file.");
        block.createContent(managedForm);
    }
}
