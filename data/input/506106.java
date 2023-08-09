public final class OverviewPage extends FormPage {
    final static String PAGE_ID = "overview_page"; 
    ManifestEditor mEditor;
    private OverviewInfoPart mOverviewPart;
    private OverviewLinksPart mOverviewLinkPart;
    private UiTreeBlock mTreeBlock;
    public OverviewPage(ManifestEditor editor) {
        super(editor, PAGE_ID, "Manifest");  
        mEditor = editor;
    }
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        ScrolledForm form = managedForm.getForm();
        form.setText("Android Manifest");
        form.setImage(AdtPlugin.getAndroidLogo());
        Composite body = form.getBody();
        FormToolkit toolkit = managedForm.getToolkit();
        mOverviewPart = new OverviewInfoPart(body, toolkit, mEditor);
        mOverviewPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        managedForm.addPart(mOverviewPart);
        newManifestExtrasPart(managedForm);
        OverviewExportPart exportPart = new OverviewExportPart(this, body, toolkit, mEditor);
        exportPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        managedForm.addPart(exportPart);
        mOverviewLinkPart = new OverviewLinksPart(body, toolkit, mEditor);
        mOverviewLinkPart.getSection().setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        managedForm.addPart(mOverviewLinkPart);
    }
    private void newManifestExtrasPart(IManagedForm managedForm) {
        UiElementNode manifest = mEditor.getUiRootNode();
        mTreeBlock = new UiTreeBlock(mEditor, manifest,
                true ,
                computeManifestExtraFilters(),
                "Manifest Extras",
                "Extra manifest elements");
        mTreeBlock.createContent(managedForm);
    }
    public void refreshUiApplicationNode() {
        if (mOverviewPart != null) {
            mOverviewPart.onSdkChanged();
        }
        if (mOverviewLinkPart != null) {
            mOverviewLinkPart.onSdkChanged();
        }
        if (mTreeBlock != null) {
            UiElementNode manifest = mEditor.getUiRootNode();
            mTreeBlock.changeRootAndDescriptors(manifest,
                    computeManifestExtraFilters(),
                    true );
        }
    }
    private ElementDescriptor[] computeManifestExtraFilters() {
        UiElementNode manifest = mEditor.getUiRootNode();
        AndroidManifestDescriptors manifestDescriptor = mEditor.getManifestDescriptors();
        if (manifestDescriptor == null) {
            return null;
        }
        HashSet<ElementDescriptor> excludes = new HashSet<ElementDescriptor>();
        excludes.add(manifestDescriptor.getApplicationElement());
        excludes.add(manifestDescriptor.getInstrumentationElement());
        excludes.add(manifestDescriptor.getPermissionElement());
        excludes.add(manifestDescriptor.getPermissionGroupElement());
        excludes.add(manifestDescriptor.getPermissionTreeElement());
        excludes.add(manifestDescriptor.getUsesPermissionElement());
        ArrayList<ElementDescriptor> descriptorFilters = new ArrayList<ElementDescriptor>();
        for (ElementDescriptor child : manifest.getDescriptor().getChildren()) {
            if (!excludes.contains(child)) {
                descriptorFilters.add(child);
            }
        }
        if (descriptorFilters.size() == 0) {
            return null;
        }
        return descriptorFilters.toArray(new ElementDescriptor[descriptorFilters.size()]);
    }
}
