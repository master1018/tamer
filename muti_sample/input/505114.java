public final class MenuTreePage extends FormPage {
    public final static String PAGE_ID = "layout_tree_page"; 
    MenuEditor mEditor;
    public MenuTreePage(MenuEditor editor) {
        super(editor, PAGE_ID, "Layout");  
        mEditor = editor;
    }
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        ScrolledForm form = managedForm.getForm();
        form.setText("Android Menu");
        form.setImage(AdtPlugin.getAndroidLogo());
        UiElementNode rootNode = mEditor.getUiRootNode();
        UiTreeBlock block = new UiTreeBlock(mEditor, rootNode,
                true ,
                null ,
                "Menu Elements",
                "List of all menu elements in this XML file.");
        block.createContent(managedForm);
    }
}
