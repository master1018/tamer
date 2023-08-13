public final class XmlTreePage extends FormPage {
    public final static String PAGE_ID = "xml_tree_page"; 
    XmlEditor mEditor;
    public XmlTreePage(XmlEditor editor) {
        super(editor, PAGE_ID, "Structure");  
        mEditor = editor;
    }
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        ScrolledForm form = managedForm.getForm();
        form.setText("Android Xml");
        form.setImage(AdtPlugin.getAndroidLogo());
        UiElementNode rootNode = mEditor.getUiRootNode();
        UiTreeBlock block = new UiTreeBlock(mEditor, rootNode,
                true ,
                null ,
                "Xml Elements",
                "List of all xml elements in this XML file.");
        block.createContent(managedForm);
    }
}
