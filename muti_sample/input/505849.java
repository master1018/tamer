public class ProjectChooserHelper {
    private final Shell mParentShell;
    private final IProjectChooserFilter mFilter;
    private IJavaProject[] mAndroidProjects;
    public interface IProjectChooserFilter extends IProjectFilter {
        boolean useCache();
    }
    public final static class NonLibraryProjectOnlyFilter implements IProjectChooserFilter {
        public boolean accept(IProject project) {
            ProjectState state = Sdk.getProjectState(project);
            return state.isLibrary() == false;
        }
        public boolean useCache() {
            return true;
        }
    }
    public final static class LibraryProjectOnlyFilter implements IProjectChooserFilter {
        public boolean accept(IProject project) {
            ProjectState state = Sdk.getProjectState(project);
            return state.isLibrary();
        }
        public boolean useCache() {
            return true;
        }
    }
    public ProjectChooserHelper(Shell parentShell, IProjectChooserFilter filter) {
        mParentShell = parentShell;
        mFilter = filter;
    }
    public IJavaProject chooseJavaProject(String projectName, String message) {
        ILabelProvider labelProvider = new JavaElementLabelProvider(
                JavaElementLabelProvider.SHOW_DEFAULT);
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                mParentShell, labelProvider);
        dialog.setTitle("Project Selection");
        if (message == null) {
            message = "Please select a project";
        }
        dialog.setMessage(message);
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IJavaModel javaModel = JavaCore.create(workspaceRoot);
        dialog.setElements(getAndroidProjects(javaModel));
        IJavaProject javaProject = null;
        if (projectName != null && projectName.length() > 0) {
            javaProject = javaModel.getJavaProject(projectName);
        }
        if (javaProject != null) {
            dialog.setInitialSelections(new Object[] { javaProject });
        }
        if (dialog.open() == Window.OK) {
            return (IJavaProject) dialog.getFirstResult();
        }
        return null;
    }
    public IJavaProject[] getAndroidProjects(IJavaModel javaModel) {
        if (mAndroidProjects == null || (mFilter != null && mFilter.useCache() == false)) {
            if (javaModel == null) {
                mAndroidProjects = BaseProjectHelper.getAndroidProjects(mFilter);
            } else {
                mAndroidProjects = BaseProjectHelper.getAndroidProjects(javaModel, mFilter);
            }
        }
        return mAndroidProjects;
    }
    public IProject getAndroidProject(String projectName) {
        IProject iproject = null;
        IJavaProject[] javaProjects = getAndroidProjects(null);
        if (javaProjects != null) {
            for (IJavaProject javaProject : javaProjects) {
                if (javaProject.getElementName().equals(projectName)) {
                    iproject = javaProject.getProject();
                    break;
                }
            }
        }
        return iproject;
    }
}
