public class FixProjectAction implements IObjectActionDelegate {
    private ISelection mSelection;
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }
    public void run(IAction action) {
        if (mSelection instanceof IStructuredSelection) {
            for (Iterator<?> it = ((IStructuredSelection) mSelection).iterator();
                    it.hasNext();) {
                Object element = it.next();
                IProject project = null;
                if (element instanceof IProject) {
                    project = (IProject) element;
                } else if (element instanceof IAdaptable) {
                    project = (IProject) ((IAdaptable) element)
                            .getAdapter(IProject.class);
                }
                if (project != null) {
                    fixProject(project);
                }
            }
        }
    }
    public void selectionChanged(IAction action, ISelection selection) {
        this.mSelection = selection;
    }
    private void fixProject(final IProject project) {
        new Job("Fix Project Properties") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    if (monitor != null) {
                        monitor.beginTask("Fix Project Properties", 6);
                    }
                    ProjectHelper.fixProject(project);
                    if (monitor != null) {
                        monitor.worked(1);
                    }
                    ProjectHelper.fixProjectNatureOrder(project);
                    if (monitor != null) {
                        monitor.worked(1);
                    }
                    AndroidNature.configureResourceManagerBuilder(project);
                    if (monitor != null) {
                        monitor.worked(1);
                    }
                    AndroidNature.configurePreBuilder(project);
                    if (monitor != null) {
                        monitor.worked(1);
                    }
                    AndroidNature.configureApkBuilder(project);
                    if (monitor != null) {
                        monitor.worked(1);
                    }
                    return Status.OK_STATUS;
                } catch (JavaModelException e) {
                    return e.getJavaModelStatus();
                } catch (CoreException e) {
                    return e.getStatus();
                } finally {
                    if (monitor != null) {
                        monitor.done();
                    }
                }
            }
        }.schedule();
    }
    public void init(IWorkbenchWindow window) {
    }
}
