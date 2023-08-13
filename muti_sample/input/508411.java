public class ExportAction implements IObjectActionDelegate {
    private ISelection mSelection;
    private Shell mShell;
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        mShell = targetPart.getSite().getShell();
    }
    public void run(IAction action) {
        if (mSelection instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection)mSelection;
            if (selection.size() == 1) {
                Object element = selection.getFirstElement();
                IProject project = null;
                if (element instanceof IProject) {
                    project = (IProject) element;
                } else if (element instanceof IAdaptable) {
                    project = (IProject) ((IAdaptable) element).getAdapter(IProject.class);
                }
                if (project != null) {
                    ProjectState state = Sdk.getProjectState(project);
                    if (state.isLibrary()) {
                        MessageDialog.openError(mShell, "Android Export",
                                "Android library projects cannot be exported.");
                    } else {
                        ExportHelper.exportProject(project);
                    }
                }
            }
        }
    }
    public void selectionChanged(IAction action, ISelection selection) {
        mSelection = selection;
    }
}
