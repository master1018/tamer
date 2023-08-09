public final class ProjectHelper {
    public final static int COMPILER_COMPLIANCE_OK = 0;
    public final static int COMPILER_COMPLIANCE_LEVEL = 1;
    public final static int COMPILER_COMPLIANCE_SOURCE = 2;
    public final static int COMPILER_COMPLIANCE_CODEGEN_TARGET = 3;
    public static IClasspathEntry[] addEntryToClasspath(
            IClasspathEntry[] entries, IClasspathEntry new_entry) {
        int n = entries.length;
        IClasspathEntry[] newEntries = new IClasspathEntry[n + 1];
        System.arraycopy(entries, 0, newEntries, 0, n);
        newEntries[n] = new_entry;
        return newEntries;
    }
    public static void addEntryToClasspath(
            IJavaProject javaProject, IClasspathEntry new_entry)
            throws JavaModelException {
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        entries = addEntryToClasspath(entries, new_entry);
        javaProject.setRawClasspath(entries, new NullProgressMonitor());
    }
    public static IClasspathEntry[] removeEntryFromClasspath(
            IClasspathEntry[] entries, int index) {
        int n = entries.length;
        IClasspathEntry[] newEntries = new IClasspathEntry[n-1];
        System.arraycopy(entries, 0, newEntries, 0, index);
        System.arraycopy(entries, index + 1, newEntries, index,
                entries.length - index - 1);
        return newEntries;
    }
    public static String getJavaDocPath(String javaDocOSLocation) {
        String javaDoc = javaDocOSLocation.replaceAll("\\\\", 
                AndroidConstants.WS_SEP);
        if (javaDoc.startsWith(AndroidConstants.WS_SEP)) {
            return "file:" + javaDoc; 
        }
        return "file:/" + javaDoc; 
    }
    public static int findClasspathEntryByPath(IClasspathEntry[] entries,
            String entryPath, int entryKind) {
        for (int i = 0 ; i < entries.length ; i++) {
            IClasspathEntry entry = entries[i];
            int kind = entry.getEntryKind();
            if (kind == entryKind || entryKind == 0) {
                IPath path = entry.getPath();
                String osPathString = path.toOSString();
                if (osPathString.equals(entryPath)) {
                    return i;
                }
            }
        }
        return -1;
    }
    public static int findClasspathEntryByName(IClasspathEntry[] entries,
            String entryName, int entryKind, int startIndex) {
        if (startIndex < 0) {
            startIndex = 0;
        }
        for (int i = startIndex ; i < entries.length ; i++) {
            IClasspathEntry entry = entries[i];
            int kind = entry.getEntryKind();
            if (kind == entryKind || entryKind == 0) {
                IPath path = entry.getPath();
                String name = path.segment(path.segmentCount()-1);
                if (name.equals(entryName)) {
                    return i;
                }
            }
        }
        return -1;
    }
    public static void fixProject(IProject project) throws JavaModelException {
        if (AdtPlugin.getOsSdkFolder().length() == 0) {
            AdtPlugin.printToConsole(project, "Unknown SDK Location, project not fixed.");
            return;
        }
        IJavaProject javaProject = JavaCore.create(project);
        fixProjectClasspathEntries(javaProject);
    }
    public static void fixProjectClasspathEntries(IJavaProject javaProject)
            throws JavaModelException {
        IClasspathEntry[] entries = javaProject.getRawClasspath();
        IClasspathEntry[] oldEntries = entries;
        int jreIndex = ProjectHelper.findClasspathEntryByPath(entries, JavaRuntime.JRE_CONTAINER,
                IClasspathEntry.CPE_CONTAINER);
        if (jreIndex != -1) {
            entries = ProjectHelper.removeEntryFromClasspath(entries, jreIndex);
        }
        IPath outputFolder = javaProject.getOutputLocation();
        boolean foundContainer = false;
        for (int i = 0 ; i < entries.length ;) {
            IClasspathEntry entry = entries[i];
            int kind = entry.getEntryKind();
            if (kind == IClasspathEntry.CPE_SOURCE) {
                IPath path = entry.getPath();
                if (path.equals(outputFolder)) {
                    entries = ProjectHelper.removeEntryFromClasspath(entries, i);
                    continue;
                }
            } else if (kind == IClasspathEntry.CPE_CONTAINER) {
                if (AndroidClasspathContainerInitializer.checkPath(entry.getPath())) {
                    foundContainer = true;
                }
            }
            i++;
        }
        if (foundContainer == false) {
            entries = ProjectHelper.addEntryToClasspath(entries,
                    AndroidClasspathContainerInitializer.getContainerEntry());
        }
        if (entries != oldEntries) {
            javaProject.setRawClasspath(entries, new NullProgressMonitor());
        }
        ProjectHelper.checkAndFixCompilerCompliance(javaProject);
    }
    public static final int checkCompilerCompliance(IJavaProject javaProject) {
        String compliance = javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
        if (checkCompliance(compliance) == false) {
            return COMPILER_COMPLIANCE_LEVEL;
        }
        String source = javaProject.getOption(JavaCore.COMPILER_SOURCE, true);
        if (checkCompliance(source) == false) {
            return COMPILER_COMPLIANCE_SOURCE;
        }
        String codeGen = javaProject.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true);
        if (checkCompliance(codeGen) == false) {
            return COMPILER_COMPLIANCE_CODEGEN_TARGET;
        }
        return COMPILER_COMPLIANCE_OK;
    }
    public static final int checkCompilerCompliance(IProject project) {
        IJavaProject javaProject = JavaCore.create(project);
        return checkCompilerCompliance(javaProject);
    }
    public static final void checkAndFixCompilerCompliance(IProject project) {
        IJavaProject javaProject = JavaCore.create(project);
        checkAndFixCompilerCompliance(javaProject);
    }
    public static final void checkAndFixCompilerCompliance(IJavaProject javaProject) {
        if (checkCompilerCompliance(javaProject) != COMPILER_COMPLIANCE_OK) {
            javaProject.setOption(JavaCore.COMPILER_COMPLIANCE,
                    AndroidConstants.COMPILER_COMPLIANCE_PREFERRED);
            javaProject.setOption(JavaCore.COMPILER_SOURCE,
                    AndroidConstants.COMPILER_COMPLIANCE_PREFERRED);
            javaProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
                    AndroidConstants.COMPILER_COMPLIANCE_PREFERRED);
            try {
                javaProject.getProject().build(IncrementalProjectBuilder.CLEAN_BUILD,
                        new NullProgressMonitor());
            } catch (CoreException e) {
                AdtPlugin.printErrorToConsole(javaProject.getProject(),
                        "Project compiler settings changed. Clean your project.");
            }
        }
    }
    public static IProject findAndroidProjectByAppName(String applicationName) {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject[] projects = workspace.getRoot().getProjects();
        for (IProject p : projects) {
            if (p.isOpen()) {
                try {
                    if (p.hasNature(AndroidConstants.NATURE) == false) {
                        continue;
                    }
                } catch (CoreException e) {
                    continue;
                }
                IFile manifestFile = AndroidManifestParser.getManifest(p);
                if (manifestFile == null) {
                    continue;
                }
                AndroidManifestParser parser = null;
                try {
                    parser = AndroidManifestParser.parseForData(manifestFile);
                } catch (CoreException e) {
                }
                if (parser == null) {
                    continue;
                }
                String manifestPackage = parser.getPackage();
                if (manifestPackage != null && manifestPackage.equals(applicationName)) {
                    return p;
                } else {
                    String[] processes = parser.getProcesses();
                    for (String process : processes) {
                        if (process.equals(applicationName)) {
                            return p;
                        }
                    }
                }
            }
        }
        return null;
    }
    public static void fixProjectNatureOrder(IProject project) throws CoreException {
        IProjectDescription description = project.getDescription();
        String[] natures = description.getNatureIds();
        if (AndroidConstants.NATURE.equals(natures[0]) == false) {
            for (int i = 0 ; i < natures.length ; i++) {
                if (AndroidConstants.NATURE.equals(natures[i])) {
                    removeNature(project, AndroidConstants.NATURE);
                    description = project.getDescription();
                    natures = description.getNatureIds();
                    String[] newNatures = new String[natures.length + 1];
                    newNatures[0] = AndroidConstants.NATURE;
                    System.arraycopy(natures, 0, newNatures, 1, natures.length);
                    description.setNatureIds(newNatures);
                    project.setDescription(description, null);
                    break;
                }
            }
        }
    }
    public static void removeNature(IProject project, String nature) throws CoreException {
        IProjectDescription description = project.getDescription();
        String[] natures = description.getNatureIds();
        for (int i = 0; i < natures.length; ++i) {
            if (nature.equals(natures[i])) {
                String[] newNatures = new String[natures.length - 1];
                if (i > 0) {
                    System.arraycopy(natures, 0, newNatures, 0, i);
                }
                System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
                description.setNatureIds(newNatures);
                project.setDescription(description, null);
                return;
            }
        }
    }
    public static boolean hasError(IProject project, boolean includeReferencedProjects)
    throws CoreException {
        IMarker[] markers = project.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
        if (markers != null && markers.length > 0) {
            for (IMarker m : markers) {
                int s = m.getAttribute(IMarker.SEVERITY, -1);
                if (s == IMarker.SEVERITY_ERROR) {
                    return true;
                }
            }
        }
        if (includeReferencedProjects) {
            IProject[] projects = getReferencedProjects(project);
            for (IProject p : projects) {
                if (hasError(p, false)) {
                    return true;
                }
            }
        }
        return false;
    }
    public static boolean saveStringProperty(IResource resource, String propertyName,
            String value) {
        QualifiedName qname = new QualifiedName(AdtPlugin.PLUGIN_ID, propertyName);
        try {
            resource.setPersistentProperty(qname, value);
        } catch (CoreException e) {
            return false;
        }
        return true;
    }
    public static String loadStringProperty(IResource resource, String propertyName) {
        QualifiedName qname = new QualifiedName(AdtPlugin.PLUGIN_ID, propertyName);
        try {
            String value = resource.getPersistentProperty(qname);
            return value;
        } catch (CoreException e) {
            return null;
        }
    }
    public static boolean saveBooleanProperty(IResource resource, String propertyName,
            boolean value) {
        return saveStringProperty(resource, propertyName, Boolean.toString(value));
    }
    public static boolean loadBooleanProperty(IResource resource, String propertyName,
            boolean defaultValue) {
        String value = loadStringProperty(resource, propertyName);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
    public static boolean saveResourceProperty(IResource resource, String propertyName,
            IResource value) {
        if (value != null) {
            IPath iPath = value.getProjectRelativePath();
            return saveStringProperty(resource, propertyName, iPath.toString());
        }
        return saveStringProperty(resource, propertyName, ""); 
    }
    public static IResource loadResourceProperty(IResource resource, String propertyName) {
        String value = loadStringProperty(resource, propertyName);
        if (value != null && value.length() > 0) {
            return resource.getProject().findMember(value);
        }
        return null;
    }
    public static IProject[] getReferencedProjects(IProject project) throws CoreException {
        IProject[] projects = project.getReferencedProjects();
        ArrayList<IProject> list = new ArrayList<IProject>();
        for (IProject p : projects) {
            if (p.isOpen() && p.hasNature(JavaCore.NATURE_ID)) {
                list.add(p);
            }
        }
        return list.toArray(new IProject[list.size()]);
    }
    private static boolean checkCompliance(String optionValue) {
        for (String s : AndroidConstants.COMPILER_COMPLIANCE) {
            if (s != null && s.equals(optionValue)) {
                return true;
            }
        }
        return false;
    }
    public static String getApkFilename(IProject project, String config) {
        if (config != null) {
            return project.getName() + "-" + config + AndroidConstants.DOT_ANDROID_PACKAGE; 
        }
        return project.getName() + AndroidConstants.DOT_ANDROID_PACKAGE;
    }
    public static List<IJavaProject> getAndroidProjectDependencies(IJavaProject javaProject) 
        throws JavaModelException {
        String[] requiredProjectNames = javaProject.getRequiredProjectNames();
        IJavaModel javaModel = javaProject.getJavaModel();
        List<IJavaProject> projectList = new ArrayList<IJavaProject>(requiredProjectNames.length);
        for (String javaProjectName : requiredProjectNames) {
            IJavaProject androidJavaProject = javaModel.getJavaProject(javaProjectName);
            try {
                if (!androidJavaProject.getProject().hasNature(AndroidConstants.NATURE)) {
                    continue;
                }
            } catch (CoreException e) {
                continue;
            }
            projectList.add(androidJavaProject);
        }
        return projectList;
    }
    public static IFile getApplicationPackage(IProject project) {
        IFolder outputLocation = BaseProjectHelper.getOutputFolder(project);
        if (outputLocation == null) {
            AdtPlugin.printErrorToConsole(project,
                    "Failed to get the output location of the project. Check build path properties"
                    );
            return null;
        }
        String packageName = project.getName() + AndroidConstants.DOT_ANDROID_PACKAGE;
        IResource r = outputLocation.findMember(packageName);
        if (r instanceof IFile && r.exists()) {
            return (IFile)r;
        }
        String msg = String.format("Could not find %1$s!", packageName);
        AdtPlugin.printErrorToConsole(project, msg);
        return null;
    }
}
