public class ExtractStringWizard extends RefactoringWizard {
    private final IProject mProject;
    public ExtractStringWizard(ExtractStringRefactoring ref, IProject project) {
        super(ref, DIALOG_BASED_USER_INTERFACE | PREVIEW_EXPAND_FIRST_NODE);
        mProject = project;
        setDefaultPageTitle(ref.getName());
    }
    @Override
    protected void addUserInputPages() {
        addPage(new ExtractStringInputPage(mProject));
    }
}
