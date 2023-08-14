public class ExtractStringAction implements IWorkbenchWindowActionDelegate {
    private IWorkbenchWindow mWindow;
    private ITextSelection mSelection;
    private IEditorPart mEditor;
    private IFile mFile;
    public void init(IWorkbenchWindow window) {
        mWindow = window;
    }
    public void dispose() {
    }
    public void selectionChanged(IAction action, ISelection selection) {
        mSelection = null;
        mFile = null;
        if (selection instanceof ITextSelection) {
            mSelection = (ITextSelection) selection;
            if (mSelection.getLength() > 0) {
                mEditor = getActiveEditor();
                mFile = getSelectedFile(mEditor);
            }
        }
        action.setEnabled(mSelection != null && mFile != null);
    }
    public void run(IAction action) {
        if (mSelection != null && mFile != null) {
            ExtractStringRefactoring ref = new ExtractStringRefactoring(mFile, mEditor, mSelection);
            RefactoringWizard wizard = new ExtractStringWizard(ref, mFile.getProject());
            RefactoringWizardOpenOperation op = new RefactoringWizardOpenOperation(wizard);
            try {
                op.run(mWindow.getShell(), wizard.getDefaultPageTitle());
            } catch (InterruptedException e) {
            }
        }
    }
    private IEditorPart getActiveEditor() {
        IWorkbenchWindow wwin = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (wwin != null) {
            IWorkbenchPage page = wwin.getActivePage();
            if (page != null) {
                return page.getActiveEditor();
            }
        }
        return null;
    }
    private IFile getSelectedFile(IEditorPart editor) {
        if (editor != null) {
            IEditorInput input = editor.getEditorInput();
            if (input instanceof FileEditorInput) {
                FileEditorInput fi = (FileEditorInput) input;
                IFile file = fi.getFile();
                if (file.exists()) {
                    IProject proj = file.getProject();
                    try {
                        if (proj != null && proj.hasNature(AndroidConstants.NATURE)) {
                            return file;
                        }
                    } catch (CoreException e) {
                    }
                }
            }
        }
        return null;
    }
}
