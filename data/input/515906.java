public class ConvertToAndroidAction implements IObjectActionDelegate {
    private ISelection mSelection;
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }
    public void run(IAction action) {
        if (mSelection instanceof IStructuredSelection) {
            for (Iterator<?> it = ((IStructuredSelection)mSelection).iterator(); it.hasNext();) {
                Object element = it.next();
                IProject project = null;
                if (element instanceof IProject) {
                    project = (IProject)element;
                } else if (element instanceof IAdaptable) {
                    project = (IProject)((IAdaptable)element).getAdapter(IProject.class);
                }
                if (project != null) {
                    convertProject(project);
                }
            }
        }
    }
    public void selectionChanged(IAction action, ISelection selection) {
        this.mSelection = selection;
    }
    private void convertProject(final IProject project) {
        new Job("Convert Project") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    if (monitor != null) {
                        monitor.beginTask(String.format(
                                "Convert %1$s to Android", project.getName()), 5);
                    }
                    IProjectDescription description = project.getDescription();
                    String[] natures = description.getNatureIds();
                    for (int i = 0; i < natures.length; ++i) {
                        if (AndroidConstants.NATURE.equals(natures[i])) {
                            return new Status(Status.WARNING, AdtPlugin.PLUGIN_ID,
                                    "Project is already an Android project");
                        }
                    }
                    if (monitor != null) {
                        monitor.worked(1);
                    }
                    String[] newNatures = new String[natures.length + 1];
                    System.arraycopy(natures, 0, newNatures, 1, natures.length);
                    newNatures[0] = AndroidConstants.NATURE;
                    description.setNatureIds(newNatures);
                    project.setDescription(description, null);
                    if (monitor != null) {
                        monitor.worked(1);
                    }
                    IJavaProject javaProject = JavaCore.create(project);
                    ProjectHelper.fixProjectClasspathEntries(javaProject);
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
}
