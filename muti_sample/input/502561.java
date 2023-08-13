final class OverviewExportPart extends ManifestSectionPart {
    private final OverviewPage mOverviewPage;
    public OverviewExportPart(OverviewPage overviewPage, Composite body, FormToolkit toolkit, ManifestEditor editor) {
        super(body, toolkit, Section.TWISTIE | Section.EXPANDED, true );
        mOverviewPage = overviewPage;
        Section section = getSection();
        section.setText("Exporting");
        final IProject project = getProject();
        boolean isLibrary = false;
        if (project != null) {
            ProjectState state = Sdk.getProjectState(project);
            if (state != null) {
                isLibrary = state.isLibrary();
            }
        }
        if (isLibrary) {
            section.setDescription("Library project cannot be exported.");
            Composite table = createTableLayout(toolkit, 2 );
            createFormText(table, toolkit, true, "<form></form>", false );
        } else {
            section.setDescription("To export the application for distribution, you have the following options:");
            Composite table = createTableLayout(toolkit, 2 );
            StringBuffer buf = new StringBuffer();
            buf.append("<form><li><a href=\"wizard\">"); 
            buf.append("Use the Export Wizard");
            buf.append("</a>"); 
            buf.append(" to export and sign an APK");
            buf.append("</li>"); 
            buf.append("<li><a href=\"manual\">"); 
            buf.append("Export an unsigned APK");
            buf.append("</a>"); 
            buf.append(" and sign it manually");
            buf.append("</li></form>"); 
            FormText text = createFormText(table, toolkit, true, buf.toString(),
                    false );
            text.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    if (project != null) {
                        if ("manual".equals(e.data)) { 
                            ExportHelper.exportProject(project);
                        } else {
                            ExportHelper.startExportWizard(project);
                        }
                    }
                }
            });
        }
        layoutChanged();
    }
    private IProject getProject() {
        IEditorInput input = mOverviewPage.mEditor.getEditorInput();
        if (input instanceof FileEditorInput) {
            FileEditorInput fileInput = (FileEditorInput)input;
            IFile file = fileInput.getFile();
            return file.getProject();
        }
        return null;
    }
}
