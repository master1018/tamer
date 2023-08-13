public class ExportWizardAction implements IObjectActionDelegate {
    private ISelection mSelection;
    private IWorkbench mWorkbench;
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        mWorkbench = targetPart.getSite().getWorkbenchWindow().getWorkbench();
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
                        MessageDialog.openError(mWorkbench.getDisplay().getActiveShell(),
                                "Android Export",
                                "Android library projects cannot be exported.");
                    } else {
                        ExportWizard wizard = new ExportWizard();
                        wizard.init(mWorkbench, selection);
                        WizardDialog dialog = new WizardDialog(
                                mWorkbench.getDisplay().getActiveShell(), wizard);
                        dialog.open();
                    }
                }
            }
        }
    }
    public void selectionChanged(IAction action, ISelection selection) {
        mSelection = selection;
    }
}
