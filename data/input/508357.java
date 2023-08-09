public final class BaseProjectHelper {
    public static final String TEST_CLASS_OK = null;
    public static interface IProjectFilter {
        boolean accept(IProject project);
    }
    public static ArrayList<IPath> getSourceClasspaths(IJavaProject javaProject) {
        ArrayList<IPath> sourceList = new ArrayList<IPath>();
        IClasspathEntry[] classpaths = javaProject.readRawClasspath();
        if (classpaths != null) {
            for (IClasspathEntry e : classpaths) {
                if (e.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                    sourceList.add(e.getPath());
                }
            }
        }
        return sourceList;
    }
    public final static IMarker markResource(IResource resource, String markerId,
            String message, int lineNumber, int severity) {
        try {
            IMarker marker = resource.createMarker(markerId);
            marker.setAttribute(IMarker.MESSAGE, message);
            marker.setAttribute(IMarker.SEVERITY, severity);
            if (lineNumber < 1 && marker.isSubtypeOf(IMarker.TEXT)) {
                lineNumber = 1;
            }
            if (lineNumber >= 1) {
                marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
            }
            resource.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
            return marker;
        } catch (CoreException e) {
            AdtPlugin.log(e, "Failed to add marker '%1$s' to '%2$s'", 
                    markerId, resource.getFullPath());
        }
        return null;
    }
    public final static IMarker markResource(IResource resource, String markerId,
            String message, int severity) {
        return markResource(resource, markerId, message, -1, severity);
    }
    public final static IMarker markProject(IProject project, String markerId,
            String message, int severity, int priority) throws CoreException {
        IMarker marker = project.createMarker(markerId);
        marker.setAttribute(IMarker.MESSAGE, message);
        marker.setAttribute(IMarker.SEVERITY, severity);
        marker.setAttribute(IMarker.PRIORITY, priority);
        project.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
        return marker;
    }
    public final static String testClassForManifest(IJavaProject javaProject, String className,
            String superClassName, boolean testVisibility) {
        try {
            String javaClassName = className.replaceAll("\\$", "\\."); 
            IType type = javaProject.findType(javaClassName);
            if (type != null && type.exists()) {
                int flags = type.getFlags();
                if (Flags.isAbstract(flags)) {
                    return String.format("%1$s is abstract", className);
                }
                if (testVisibility && Flags.isPublic(flags) == false) {
                    IMethod basicConstructor = type.getMethod(type.getElementName(), new String[0]);
                    if (basicConstructor != null && basicConstructor.exists()) {
                        int constructFlags = basicConstructor.getFlags();
                        if (Flags.isPublic(constructFlags) == false) {
                            return String.format(
                                    "%1$s or its default constructor must be public for the system to be able to instantiate it",
                                    className);
                        }
                    } else {
                        return String.format(
                                "%1$s must be public, or the system will not be able to instantiate it.",
                                className);
                    }
                }
                IType declaringType = type;
                do {
                    IType tmpType = declaringType.getDeclaringType();
                    if (tmpType != null) {
                        if (tmpType.exists()) {
                            flags = declaringType.getFlags();
                            if (Flags.isStatic(flags) == false) {
                                return String.format("%1$s is enclosed, but not static",
                                        declaringType.getFullyQualifiedName());
                            }
                            flags = tmpType.getFlags();
                            if (testVisibility && Flags.isPublic(flags) == false) {
                                return String.format("%1$s is not public",
                                        tmpType.getFullyQualifiedName());
                            }
                        } else {
                            tmpType = null;
                        }
                    }
                    declaringType = tmpType;
                } while (declaringType != null);
                ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
                IType superType = type;
                boolean foundProperSuperClass = false;
                while ((superType = hierarchy.getSuperclass(superType)) != null &&
                        superType.exists()) {
                    if (superClassName.equals(superType.getFullyQualifiedName())) {
                        foundProperSuperClass = true;
                    }
                }
                if (foundProperSuperClass == false) {
                    return String.format("%1$s does not extend %2$s", className, superClassName);
                }
                return TEST_CLASS_OK;
            } else {
                return String.format("Class %1$s does not exist", className);
            }
        } catch (JavaModelException e) {
            return String.format("%1$s: %2$s", className, e.getMessage());
        }
    }
    public static AndroidManifestParser parseManifestForError(IFile manifestFile,
            XmlErrorListener errorListener) throws CoreException {
        if (manifestFile.exists()) {
            manifestFile.deleteMarkers(AndroidConstants.MARKER_XML, true, IResource.DEPTH_ZERO);
            manifestFile.deleteMarkers(AndroidConstants.MARKER_ANDROID, true, IResource.DEPTH_ZERO);
        }
        return AndroidManifestParser.parseForError(
                BaseProjectHelper.getJavaProject(manifestFile.getProject()),
                manifestFile, errorListener);
    }
    public static IJavaProject getJavaProject(IProject project) throws CoreException {
        if (project != null && project.hasNature(JavaCore.NATURE_ID)) {
            return JavaCore.create(project);
        }
        return null;
    }
    public static void revealSource(IProject project, String className, int line) {
        className = className.replaceAll("\\$", "\\."); 
        IJavaProject javaProject = JavaCore.create(project);
        try {
            IType result = javaProject.findType(className);
            if (result != null && result.exists()) {
                IWorkbench workbench = PlatformUI.getWorkbench();
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                IWorkbenchPage page = window.getActivePage();
                if (page.isEditorAreaVisible() == false) {
                    new OpenJavaPerspectiveAction().run();
                }
                IEditorPart editor = JavaUI.openInEditor(result);
                if (editor instanceof ITextEditor) {
                    ITextEditor textEditor = (ITextEditor)editor;
                    IEditorInput input = textEditor.getEditorInput();
                    IDocumentProvider documentProvider = textEditor.getDocumentProvider();
                    IDocument document = documentProvider.getDocument(input);
                    IRegion lineInfo = document.getLineInformation(line - 1);
                    textEditor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
                }
            }
        } catch (JavaModelException e) {
        } catch (PartInitException e) {
        } catch (BadLocationException e) {
        }
    }
    public static IJavaProject[] getAndroidProjects(IProjectFilter filter) {
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IJavaModel javaModel = JavaCore.create(workspaceRoot);
        return getAndroidProjects(javaModel, filter);
    }
    public static IJavaProject[] getAndroidProjects(IJavaModel javaModel, IProjectFilter filter) {
        IJavaProject[] javaProjectList = null;
        try {
            javaProjectList  = javaModel.getJavaProjects();
        }
        catch (JavaModelException jme) {
            return new IJavaProject[0];
        }
        ArrayList<IJavaProject> androidProjectList = new ArrayList<IJavaProject>();
        for (IJavaProject javaProject : javaProjectList) {
            IProject project = javaProject.getProject();
            try {
                if (project.hasNature(AndroidConstants.NATURE)) {
                    if (filter == null || filter.accept(project)) {
                        androidProjectList.add(javaProject);
                    }
                }
            } catch (CoreException e) {
            }
        }
        return androidProjectList.toArray(new IJavaProject[androidProjectList.size()]);
    }
    public final static IFolder getOutputFolder(IProject project) {
        try {
            if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
                IJavaProject javaProject = JavaCore.create(project);
                IPath path = javaProject.getOutputLocation();
                IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
                IResource outputResource = wsRoot.findMember(path);
                if (outputResource != null && outputResource.getType() == IResource.FOLDER) {
                    return (IFolder)outputResource;
                }
            }
        } catch (JavaModelException e) {
        } catch (CoreException e) {
        }
        return null;
    }
}
