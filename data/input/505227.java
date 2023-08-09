final class OverviewLinksPart extends ManifestSectionPart {
    private final ManifestEditor mEditor;
    private FormText mFormText;
    public OverviewLinksPart(Composite body, FormToolkit toolkit, ManifestEditor editor) {
        super(body, toolkit, Section.TWISTIE | Section.EXPANDED, true );
        mEditor = editor;
        Section section = getSection();
        section.setText("Links");
        section.setDescription("The content of the Android Manifest is made up of three sections. You can also edit the XML directly.");
        Composite table = createTableLayout(toolkit, 2 );
        StringBuffer buf = new StringBuffer();
        buf.append(String.format("<form><li style=\"image\" value=\"app_img\"><a href=\"page:%1$s\">", 
                ApplicationPage.PAGE_ID));
        buf.append("Application");
        buf.append("</a>");  
        buf.append(": Activities, intent filters, providers, services and receivers.");
        buf.append("</li>"); 
        buf.append(String.format("<li style=\"image\" value=\"perm_img\"><a href=\"page:%1$s\">", 
                PermissionPage.PAGE_ID));
        buf.append("Permission");
        buf.append("</a>"); 
        buf.append(": Permissions defined and permissions used.");
        buf.append("</li>"); 
        buf.append(String.format("<li style=\"image\" value=\"inst_img\"><a href=\"page:%1$s\">", 
                InstrumentationPage.PAGE_ID));
        buf.append("Instrumentation");
        buf.append("</a>"); 
        buf.append(": Instrumentation defined.");
        buf.append("</li>"); 
        buf.append(String.format("<li style=\"image\" value=\"android_img\"><a href=\"page:%1$s\">", 
                ManifestEditor.TEXT_EDITOR_ID));
        buf.append("XML Source");
        buf.append("</a>"); 
        buf.append(": Directly edit the AndroidManifest.xml file.");
        buf.append("</li>"); 
        buf.append("<li style=\"image\" value=\"android_img\">"); 
        buf.append("<a href=\"http:
        buf.append("</li>"); 
        buf.append("</form>"); 
        mFormText = createFormText(table, toolkit, true, buf.toString(),
                false );
        AndroidManifestDescriptors manifestDescriptor = editor.getManifestDescriptors();
        Image androidLogo = AdtPlugin.getAndroidLogo();
        mFormText.setImage("android_img", androidLogo); 
        if (manifestDescriptor != null) {
            mFormText.setImage("app_img", getIcon(manifestDescriptor.getApplicationElement())); 
            mFormText.setImage("perm_img", getIcon(manifestDescriptor.getPermissionElement())); 
            mFormText.setImage("inst_img", getIcon(manifestDescriptor.getInstrumentationElement())); 
        } else {
            mFormText.setImage("app_img", androidLogo); 
            mFormText.setImage("perm_img", androidLogo); 
            mFormText.setImage("inst_img", androidLogo); 
        }
        mFormText.addHyperlinkListener(editor.createHyperlinkListener());
    }
    public void onSdkChanged() {
        AndroidManifestDescriptors manifestDescriptor = mEditor.getManifestDescriptors();
        if (manifestDescriptor != null) {
            mFormText.setImage("app_img", getIcon(manifestDescriptor.getApplicationElement())); 
            mFormText.setImage("perm_img", getIcon(manifestDescriptor.getPermissionElement())); 
            mFormText.setImage("inst_img", getIcon(manifestDescriptor.getInstrumentationElement())); 
        }
    }
    private Image getIcon(ElementDescriptor desc) {
        if (desc != null && desc.getIcon() != null) {
            return desc.getIcon();
        }
        return AdtPlugin.getAndroidLogo();
    }
}
