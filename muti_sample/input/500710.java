public class LaunchShortcut implements ILaunchShortcut {
    public void launch(ISelection selection, String mode) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structSelect = (IStructuredSelection)selection;
            Object o = structSelect.getFirstElement();
            if (o instanceof IAdaptable) {
                IResource r = (IResource)((IAdaptable)o).getAdapter(IResource.class);
                if (r != null) {
                    IProject project = r.getProject();
                    if (project != null)  {
                        ProjectState state = Sdk.getProjectState(project);
                        if (state != null && state.isLibrary()) {
                            MessageDialog.openError(
                                    PlatformUI.getWorkbench().getDisplay().getActiveShell(),
                                    "Android Launch",
                                    "Android library projects cannot be launched.");
                        } else{
                            launch(project, mode);
                        }
                    }
                }
            }
        }
    }
    public void launch(IEditorPart editor, String mode) {
    }
    private void launch(IProject project, String mode) {
        ILaunchConfiguration config = AndroidLaunchController.getLaunchConfig(project);
        if (config != null) {
            DebugUITools.launch(config, mode);
        }
    }
}
