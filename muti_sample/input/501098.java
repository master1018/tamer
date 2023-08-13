public class ResourceManagerBuilder extends BaseBuilder {
    public static final String ID = "com.android.ide.eclipse.adt.ResourceManagerBuilder"; 
    public ResourceManagerBuilder() {
        super();
    }
    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
        super.clean(monitor);
        IProject project = getProject();
        removeMarkersFromProject(project, AndroidConstants.MARKER_ADT);
    }
    @SuppressWarnings("unchecked")
    @Override
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
            throws CoreException {
        IProject project = getProject();
        IJavaProject javaProject = JavaCore.create(project);
        removeMarkersFromProject(project, AndroidConstants.MARKER_ADT);
        abortOnBadSetup(javaProject);
        int res = ProjectHelper.checkCompilerCompliance(project);
        String errorMessage = null;
        switch (res) {
            case ProjectHelper.COMPILER_COMPLIANCE_LEVEL:
                errorMessage = Messages.Requires_Compiler_Compliance_5;
            case ProjectHelper.COMPILER_COMPLIANCE_SOURCE:
                errorMessage = Messages.Requires_Source_Compatibility_5;
            case ProjectHelper.COMPILER_COMPLIANCE_CODEGEN_TARGET:
                errorMessage = Messages.Requires_Class_Compatibility_5;
        }
        if (errorMessage != null) {
            markProject(AndroidConstants.MARKER_ADT, errorMessage, IMarker.SEVERITY_ERROR);
            AdtPlugin.printErrorToConsole(project, errorMessage);
            stopBuild(errorMessage);
        }
        String osSdkFolder = AdtPlugin.getOsSdkFolder();
        if (osSdkFolder == null || osSdkFolder.length() == 0) {
            AdtPlugin.printErrorToConsole(project, Messages.No_SDK_Setup_Error);
            markProject(AndroidConstants.MARKER_ADT, Messages.No_SDK_Setup_Error,
                    IMarker.SEVERITY_ERROR);
            stopBuild(Messages.No_SDK_Setup_Error);
        }
        IAndroidTarget projectTarget = Sdk.getCurrent().getTarget(project);
        if (projectTarget == null) {
            stopBuild("Project has no target");
        }
        boolean hasGenSrcFolder = false; 
        IClasspathEntry[] classpaths = javaProject.readRawClasspath();
        if (classpaths != null) {
            for (IClasspathEntry e : classpaths) {
                if (e.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                    IPath path = e.getPath();
                    if (path.segmentCount() == 2 &&
                            path.segment(1).equals(SdkConstants.FD_GEN_SOURCES)) {
                        hasGenSrcFolder = true;
                        break;
                    }
                }
            }
        }
        boolean genFolderPresent = false; 
        IResource resource = project.findMember(SdkConstants.FD_GEN_SOURCES);
        genFolderPresent = resource != null && resource.exists();
        if (hasGenSrcFolder == false && genFolderPresent) {
            String message;
            if (resource.getType() == IResource.FOLDER) {
                message = String.format("%1$s already exists but is not a source folder. Convert to a source folder or rename it.",
                        resource.getFullPath().toString());
            } else {
                message = String.format(
                        "Resource %1$s is in the way. ADT needs a source folder called 'gen' to work. Rename or delete resource.",
                        resource.getFullPath().toString());
            }
            AdtPlugin.printErrorToConsole(project, message);
            markProject(AndroidConstants.MARKER_ADT, message, IMarker.SEVERITY_ERROR);
            stopBuild(message);
        } else if (hasGenSrcFolder == false || genFolderPresent == false) {
            ArrayList<IPath> sourceFolders = BaseProjectHelper.getSourceClasspaths(javaProject);
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            for (IPath path : sourceFolders) {
                IResource member = root.findMember(path);
                if (member != null) {
                    removeDerivedResources(member, monitor);
                }
            }
            IFolder genFolder = project.getFolder(SdkConstants.FD_GEN_SOURCES);
            if (genFolderPresent == false) {
                AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, project,
                        "Creating 'gen' source folder for generated Java files");
                genFolder.create(true , true ,
                        new SubProgressMonitor(monitor, 10));
                genFolder.setDerived(true);
            }
            if (hasGenSrcFolder == false) {
                IClasspathEntry[] entries = javaProject.getRawClasspath();
                entries = ProjectHelper.addEntryToClasspath(entries,
                        JavaCore.newSourceEntry(genFolder.getFullPath()));
                javaProject.setRawClasspath(entries, new SubProgressMonitor(monitor, 10));
            }
            genFolder.refreshLocal(IResource.DEPTH_ZERO, new SubProgressMonitor(monitor, 10));
            project.refreshLocal(IResource.DEPTH_INFINITE, new SubProgressMonitor(monitor, 10));
        }
        if (AdtPrefs.getPrefs().getBuildForceResResfresh()) {
            AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, project, Messages.Refreshing_Res);
            IFolder resFolder = project.getFolder(AndroidConstants.WS_RESOURCES);
            resFolder.refreshLocal(IResource.DEPTH_INFINITE, monitor);
            IFolder assetsFolder = project.getFolder(AndroidConstants.WS_ASSETS);
            assetsFolder.refreshLocal(IResource.DEPTH_INFINITE, monitor);
        }
        return null;
    }
}
