public class PreCompilerBuilder extends BaseBuilder {
    public static final String ID = "com.android.ide.eclipse.adt.PreCompilerBuilder"; 
    private static final String PROPERTY_PACKAGE = "manifestPackage"; 
    private static final String PROPERTY_COMPILE_RESOURCES = "compileResources"; 
    private static final String PROPERTY_COMPILE_AIDL = "compileAidl"; 
    private static Pattern sAidlPattern1 = Pattern.compile("^(.+?):(\\d+):?\\s(.+)$"); 
    static class AidlData {
        IFile aidlFile;
        IFolder sourceFolder;
        AidlData(IFolder sourceFolder, IFile aidlFile) {
            this.sourceFolder = sourceFolder;
            this.aidlFile = aidlFile;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof AidlData) {
                AidlData file = (AidlData)obj;
                return aidlFile.equals(file.aidlFile) && sourceFolder.equals(file.sourceFolder);
            }
            return false;
        }
    }
    private boolean mMustCompileResources = false;
    private final ArrayList<AidlData> mAidlToCompile = new ArrayList<AidlData>();
    private final ArrayList<AidlData> mAidlToRemove = new ArrayList<AidlData>();
    private String mManifestPackage;
    private IFolder mGenFolder;
    private DerivedProgressMonitor mDerivedProgressMonitor;
    private static class DerivedProgressMonitor implements IProgressMonitor {
        private boolean mCancelled = false;
        private final ArrayList<IFile> mFileList = new ArrayList<IFile>();
        private boolean mDone = false;
        public DerivedProgressMonitor() {
        }
        void addFile(IFile file) {
            mFileList.add(file);
        }
        void reset() {
            mFileList.clear();
            mDone = false;
        }
        public void beginTask(String name, int totalWork) {
        }
        public void done() {
            if (mDone == false) {
                mDone = true;
                for (IFile file : mFileList) {
                    if (file.exists()) {
                        try {
                            file.setDerived(true);
                        } catch (CoreException e) {
                        }
                    }
                }
            }
        }
        public void internalWorked(double work) {
        }
        public boolean isCanceled() {
            return mCancelled;
        }
        public void setCanceled(boolean value) {
            mCancelled = value;
        }
        public void setTaskName(String name) {
        }
        public void subTask(String name) {
        }
        public void worked(int work) {
        }
    }
    public PreCompilerBuilder() {
        super();
    }
    @SuppressWarnings("unchecked")
    @Override
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
            throws CoreException {
        IProject project = getProject();
        IProject[] libProjects = null;
        try {
            mDerivedProgressMonitor.reset();
            ProjectState projectState = Sdk.getProjectState(project);
            if (projectState == null) {
                return null;
            }
            IAndroidTarget projectTarget = projectState.getTarget();
            libProjects = projectState.getLibraryProjects();
            IJavaProject javaProject = JavaCore.create(project);
            abortOnBadSetup(javaProject);
            ArrayList<IPath> sourceFolderPathList = BaseProjectHelper.getSourceClasspaths(
                    javaProject);
            PreCompilerDeltaVisitor dv = null;
            String javaPackage = null;
            String minSdkVersion = null;
            if (kind == FULL_BUILD) {
                AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, project,
                        Messages.Start_Full_Pre_Compiler);
                mMustCompileResources = true;
                buildAidlCompilationList(project, sourceFolderPathList);
            } else {
                AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, project,
                        Messages.Start_Inc_Pre_Compiler);
                IResourceDelta delta = getDelta(project);
                if (delta == null) {
                    mMustCompileResources = true;
                    buildAidlCompilationList(project, sourceFolderPathList);
                } else {
                    dv = new PreCompilerDeltaVisitor(this, sourceFolderPathList);
                    delta.accept(dv);
                    mMustCompileResources |= dv.getCompileResources();
                    if (dv.getForceAidlCompile()) {
                        buildAidlCompilationList(project, sourceFolderPathList);
                    } else {
                        mergeAidlFileModifications(dv.getAidlToCompile(),
                                dv.getAidlToRemove());
                    }
                    javaPackage = dv.getManifestPackage();
                    minSdkVersion = dv.getMinSdkVersion();
                    if (mMustCompileResources == false && libProjects != null &&
                            libProjects.length > 0) {
                        for (IProject libProject : libProjects) {
                            delta = getDelta(libProject);
                            if (delta != null) {
                                LibraryDeltaVisitor visitor = new LibraryDeltaVisitor();
                                delta.accept(visitor);
                                mMustCompileResources = visitor.getResChange();
                                if (mMustCompileResources) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            saveProjectBooleanProperty(PROPERTY_COMPILE_RESOURCES , mMustCompileResources);
            if (dv != null && dv.mXmlError) {
                AdtPlugin.printErrorToConsole(project, Messages.Xml_Error);
                stopBuild(Messages.Xml_Error);
            }
            IFile manifest = AndroidManifestParser.getManifest(project);
            if (manifest == null) {
                String msg = String.format(Messages.s_File_Missing,
                        AndroidConstants.FN_ANDROID_MANIFEST);
                AdtPlugin.printErrorToConsole(project, msg);
                markProject(AndroidConstants.MARKER_ADT, msg, IMarker.SEVERITY_ERROR);
                stopBuild(msg);
            }
            if (dv == null || dv.getCheckedManifestXml() == false) {
                BasicXmlErrorListener errorListener = new BasicXmlErrorListener();
                AndroidManifestParser parser = BaseProjectHelper.parseManifestForError(manifest,
                        errorListener);
                if (errorListener.mHasXmlError == true) {
                    String msg = String.format(Messages.s_Contains_Xml_Error,
                            AndroidConstants.FN_ANDROID_MANIFEST);
                    AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, project, msg);
                    stopBuild(msg);
                }
                javaPackage = parser.getPackage();
                minSdkVersion = parser.getApiLevelRequirement();
            }
            if (minSdkVersion != null) {
                int minSdkValue = -1;
                try {
                    minSdkValue = Integer.parseInt(minSdkVersion);
                } catch (NumberFormatException e) {
                }
                AndroidVersion projectVersion = projectTarget.getVersion();
                removeMarkersFromFile(manifest, AndroidConstants.MARKER_ADT);
                if (minSdkValue != -1) {
                    String codename = projectVersion.getCodename();
                    if (codename != null) {
                        String msg = String.format(
                                "Platform %1$s is a preview and requires appication manifest to set %2$s to '%1$s'",
                                codename, AndroidManifest.ATTRIBUTE_MIN_SDK_VERSION);
                        AdtPlugin.printErrorToConsole(project, msg);
                        BaseProjectHelper.markResource(manifest, AndroidConstants.MARKER_ADT, msg,
                                IMarker.SEVERITY_ERROR);
                        stopBuild(msg);
                    } else if (minSdkValue < projectVersion.getApiLevel()) {
                        String msg = String.format(
                                "Attribute %1$s (%2$d) is lower than the project target API level (%3$d)",
                                AndroidManifest.ATTRIBUTE_MIN_SDK_VERSION,
                                minSdkValue, projectVersion.getApiLevel());
                        AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, project, msg);
                        BaseProjectHelper.markResource(manifest, AndroidConstants.MARKER_ADT, msg,
                                IMarker.SEVERITY_WARNING);
                    } else if (minSdkValue > projectVersion.getApiLevel()) {
                        String msg = String.format(
                                "Attribute %1$s (%2$d) is higher than the project target API level (%3$d)",
                                AndroidManifest.ATTRIBUTE_MIN_SDK_VERSION,
                                minSdkValue, projectVersion.getApiLevel());
                        AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, project, msg);
                        BaseProjectHelper.markResource(manifest, AndroidConstants.MARKER_ADT, msg,
                                IMarker.SEVERITY_WARNING);
                    }
                } else {
                    String codename = projectVersion.getCodename();
                    if (codename == null) {
                        String msg = String.format(
                                "Manifest attribute '%1$s' is set to '%2$s'. Integer is expected.",
                                AndroidManifest.ATTRIBUTE_MIN_SDK_VERSION, minSdkVersion);
                        AdtPlugin.printErrorToConsole(project, msg);
                        BaseProjectHelper.markResource(manifest, AndroidConstants.MARKER_ADT, msg,
                                IMarker.SEVERITY_ERROR);
                        stopBuild(msg);
                    } else if (codename.equals(minSdkVersion) == false) {
                        String msg = String.format(
                                "Value of manifest attribute '%1$s' does not match platform codename '%2$s'",
                                AndroidManifest.ATTRIBUTE_MIN_SDK_VERSION, codename);
                        AdtPlugin.printErrorToConsole(project, msg);
                        BaseProjectHelper.markResource(manifest, AndroidConstants.MARKER_ADT, msg,
                                IMarker.SEVERITY_ERROR);
                        stopBuild(msg);
                    }
                }
            } else if (projectTarget.getVersion().isPreview()) {
                String codename = projectTarget.getVersion().getCodename();
                String msg = String.format(
                        "Platform %1$s is a preview and requires appication manifests to set %2$s to '%1$s'",
                        codename, AndroidManifest.ATTRIBUTE_MIN_SDK_VERSION);
                AdtPlugin.printErrorToConsole(project, msg);
                BaseProjectHelper.markResource(manifest, AndroidConstants.MARKER_ADT, msg,
                        IMarker.SEVERITY_ERROR);
                stopBuild(msg);
            }
            if (javaPackage == null || javaPackage.length() == 0) {
                String msg = String.format(Messages.s_Doesnt_Declare_Package_Error,
                        AndroidConstants.FN_ANDROID_MANIFEST);
                AdtPlugin.printErrorToConsole(project, msg);
                BaseProjectHelper.markResource(manifest, AndroidConstants.MARKER_ADT,
                        msg, IMarker.SEVERITY_ERROR);
                stopBuild(msg);
            } else if (javaPackage.indexOf('.') == -1) {
                String msg = String.format(
                        "Application package '%1$s' must have a minimum of 2 segments.",
                        AndroidConstants.FN_ANDROID_MANIFEST);
                AdtPlugin.printErrorToConsole(project, msg);
                BaseProjectHelper.markResource(manifest, AndroidConstants.MARKER_ADT,
                        msg, IMarker.SEVERITY_ERROR);
                stopBuild(msg);
            }
            if (javaPackage.equals(mManifestPackage) == false) {
                if (mManifestPackage != null) {
                    AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, project,
                            Messages.Checking_Package_Change);
                    FixLaunchConfig flc = new FixLaunchConfig(project, mManifestPackage,
                            javaPackage);
                    flc.start();
                }
                deleteObsoleteGeneratedClass(AndroidConstants.FN_RESOURCE_CLASS,
                        mManifestPackage);
                deleteObsoleteGeneratedClass(AndroidConstants.FN_MANIFEST_CLASS,
                        mManifestPackage);
                mManifestPackage = javaPackage;
                saveProjectStringProperty(PROPERTY_PACKAGE, mManifestPackage);
            }
            if (mMustCompileResources) {
                handleResources(project, javaPackage, projectTarget, manifest, libProjects);
            }
            boolean aidlStatus = handleAidl(projectTarget, sourceFolderPathList, monitor);
            if (aidlStatus == false && mMustCompileResources == false) {
                AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, project,
                        Messages.Nothing_To_Compile);
            }
        } finally {
            mGenFolder.refreshLocal(IResource.DEPTH_INFINITE, mDerivedProgressMonitor);
        }
        return libProjects;
    }
    @Override
    protected void clean(IProgressMonitor monitor) throws CoreException {
        super.clean(monitor);
        IProject project = getProject();
        AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, project,
                Messages.Removing_Generated_Classes);
        removeDerivedResources(mGenFolder, monitor);
        removeMarkersFromProject(project, AndroidConstants.MARKER_AAPT_COMPILE);
        removeMarkersFromProject(project, AndroidConstants.MARKER_XML);
        removeMarkersFromProject(project, AndroidConstants.MARKER_AIDL);
    }
    @Override
    protected void startupOnInitialize() {
        super.startupOnInitialize();
        mDerivedProgressMonitor = new DerivedProgressMonitor();
        IProject project = getProject();
        mManifestPackage = loadProjectStringProperty(PROPERTY_PACKAGE);
        mGenFolder = project.getFolder(SdkConstants.FD_GEN_SOURCES);
        mMustCompileResources = loadProjectBooleanProperty(PROPERTY_COMPILE_RESOURCES, true);
        boolean mustCompileAidl = loadProjectBooleanProperty(PROPERTY_COMPILE_AIDL, true);
        if (mustCompileAidl) {
            IJavaProject javaProject = JavaCore.create(project);
            ArrayList<IPath> sourceFolderPathList = BaseProjectHelper.getSourceClasspaths(
                    javaProject);
            buildAidlCompilationList(project, sourceFolderPathList);
        }
    }
    private void handleResources(IProject project, String javaPackage, IAndroidTarget projectTarget,
            IFile manifest, IProject[] libProjects) throws CoreException {
        IFolder resFolder = project.getFolder(AndroidConstants.WS_RESOURCES);
        IPath outputLocation = mGenFolder.getLocation();
        IPath resLocation = resFolder.getLocation();
        IPath manifestLocation = manifest == null ? null : manifest.getLocation();
        if (outputLocation != null && resLocation != null
                && manifestLocation != null) {
            String osOutputPath = outputLocation.toOSString();
            String osResPath = resLocation.toOSString();
            String osManifestPath = manifestLocation.toOSString();
            removeMarkersFromFile(manifest, AndroidConstants.MARKER_AAPT_COMPILE);
            removeMarkersFromContainer(resFolder, AndroidConstants.MARKER_AAPT_COMPILE);
            AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, project,
                    Messages.Preparing_Generated_Files);
            IFolder mainPackageFolder = getGenManifestPackageFolder();
            ArrayList<IFolder> libResFolders = new ArrayList<IFolder>();
            ArrayList<IFolder> libOutputFolders = new ArrayList<IFolder>();
            ArrayList<String> libJavaPackages = new ArrayList<String>();
            if (libProjects != null) {
                for (IProject lib : libProjects) {
                    IFolder libResFolder = lib.getFolder(SdkConstants.FD_RES);
                    if (libResFolder.exists()) {
                        libResFolders.add(libResFolder);
                    }
                    try {
                        String libJavaPackage = AndroidManifest.getPackage(new IFolderWrapper(lib));
                        if (libJavaPackage.equals(javaPackage) == false) {
                            libJavaPackages.add(libJavaPackage);
                            libOutputFolders.add(getGenManifestPackageFolder(libJavaPackage));
                        }
                    } catch (Exception e) {
                    }
                }
            }
            execAapt(project, projectTarget, osOutputPath, osResPath, osManifestPath,
                    mainPackageFolder, libResFolders, null );
            final int count = libOutputFolders.size();
            if (count > 0) {
                for (int i = 0 ; i < count ; i++) {
                    IFolder libFolder = libOutputFolders.get(i);
                    String libJavaPackage = libJavaPackages.get(i);
                    execAapt(project, projectTarget, osOutputPath, osResPath, osManifestPath,
                            libFolder, libResFolders, libJavaPackage);
                }
            }
        }
    }
    private void execAapt(IProject project, IAndroidTarget projectTarget, String osOutputPath,
            String osResPath, String osManifestPath, IFolder packageFolder,
            ArrayList<IFolder> libResFolders, String customJavaPackage) throws CoreException {
        IFile rJavaFile = packageFolder.getFile(AndroidConstants.FN_RESOURCE_CLASS);
        IFile manifestJavaFile = packageFolder.getFile(AndroidConstants.FN_MANIFEST_CLASS);
        manifestJavaFile.delete(true, null);
        ArrayList<String> array = new ArrayList<String>();
        array.add(projectTarget.getPath(IAndroidTarget.AAPT));
        array.add("package"); 
        array.add("-m"); 
        if (AdtPrefs.getPrefs().getBuildVerbosity() == BuildVerbosity.VERBOSE) {
            array.add("-v"); 
        }
        if (libResFolders.size() > 0) {
            array.add("--auto-add-overlay"); 
        }
        if (customJavaPackage != null) {
            array.add("--custom-package"); 
            array.add(customJavaPackage);
        }
        array.add("-J"); 
        array.add(osOutputPath);
        array.add("-M"); 
        array.add(osManifestPath);
        array.add("-S"); 
        array.add(osResPath);
        for (IFolder libResFolder : libResFolders) {
            array.add("-S"); 
            array.add(libResFolder.getLocation().toOSString());
        }
        array.add("-I"); 
        array.add(projectTarget.getPath(IAndroidTarget.ANDROID_JAR));
        if (AdtPrefs.getPrefs().getBuildVerbosity() == BuildVerbosity.VERBOSE) {
            StringBuilder sb = new StringBuilder();
            for (String c : array) {
                sb.append(c);
                sb.append(' ');
            }
            String cmd_line = sb.toString();
            AdtPlugin.printToConsole(project, cmd_line);
        }
        int execError = 1;
        try {
            Process process = Runtime.getRuntime().exec(
                    array.toArray(new String[array.size()]));
            ArrayList<String> results = new ArrayList<String>();
            execError = grabProcessOutput(process, results);
            boolean parsingError = parseAaptOutput(results, project);
            if (parsingError) {
                if (execError != 0) {
                    AdtPlugin.printErrorToConsole(project, results.toArray());
                } else {
                    AdtPlugin.printBuildToConsole(BuildVerbosity.NORMAL,
                            project, results.toArray());
                }
            }
            if (execError != 0) {
                if (parsingError) {
                    markProject(AndroidConstants.MARKER_ADT,
                            Messages.Unparsed_AAPT_Errors, IMarker.SEVERITY_ERROR);
                }
                AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE, project,
                        Messages.AAPT_Error);
                stopBuild(Messages.AAPT_Error);
            }
        } catch (IOException e1) {
            String msg = String.format(Messages.AAPT_Exec_Error, array.get(0));
            markProject(AndroidConstants.MARKER_ADT, msg, IMarker.SEVERITY_ERROR);
            stopBuild(msg);
        } catch (InterruptedException e) {
            String msg = String.format(Messages.AAPT_Exec_Error, array.get(0));
            markProject(AndroidConstants.MARKER_ADT, msg, IMarker.SEVERITY_ERROR);
            stopBuild(msg);
        }
        if (execError == 0) {
            mDerivedProgressMonitor.addFile(rJavaFile);
            mDerivedProgressMonitor.addFile(manifestJavaFile);
            mMustCompileResources = false;
            saveProjectBooleanProperty(PROPERTY_COMPILE_RESOURCES,
                    mMustCompileResources);
        }
    }
    private void deleteObsoleteGeneratedClass(String filename, String javaPackage) {
        if (javaPackage == null) {
            return;
        }
        IPath packagePath = getJavaPackagePath(javaPackage);
        IPath iPath = packagePath.append(filename);
        IResource javaFile = mGenFolder.findMember(iPath);
        if (javaFile != null && javaFile.exists() && javaFile.getType() == IResource.FILE) {
            try {
                javaFile.delete(true, null);
                javaFile.getParent().refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
            } catch (CoreException e) {
                String message = String.format(Messages.Delete_Obsolete_Error,
                        javaFile.getFullPath());
                IProject project = getProject();
                AdtPlugin.printErrorToConsole(project, message);
                AdtPlugin.printErrorToConsole(project, e.getMessage());
            }
        }
    }
    private IPath getJavaPackagePath(String javaPackageName) {
        String[] segments = javaPackageName.split(AndroidConstants.RE_DOT);
        StringBuilder path = new StringBuilder();
        for (String s : segments) {
           path.append(AndroidConstants.WS_SEP_CHAR);
           path.append(s);
        }
        return new Path(path.toString());
    }
    private IFolder getGenManifestPackageFolder() throws CoreException {
        IPath packagePath = getJavaPackagePath(mManifestPackage);
        return mGenFolder.getFolder(packagePath);
    }
    private IFolder getGenManifestPackageFolder(String javaPackage) throws CoreException {
        IPath packagePath = getJavaPackagePath(javaPackage);
        return mGenFolder.getFolder(packagePath);
    }
    private boolean handleAidl(IAndroidTarget projectTarget, ArrayList<IPath> sourceFolders,
            IProgressMonitor monitor) throws CoreException {
        if (mAidlToCompile.size() == 0 && mAidlToRemove.size() == 0) {
            return false;
        }
        String[] command = new String[4 + sourceFolders.size()];
        int index = 0;
        command[index++] = projectTarget.getPath(IAndroidTarget.AIDL);
        command[index++] = "-p" + Sdk.getCurrent().getTarget(getProject()).getPath( 
                IAndroidTarget.ANDROID_AIDL);
        IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
        for (IPath p : sourceFolders) {
            IFolder f = wsRoot.getFolder(p);
            command[index++] = "-I" + f.getLocation().toOSString(); 
        }
        ArrayList<AidlData> stillNeedCompilation = new ArrayList<AidlData>();
        for (AidlData aidlFile : mAidlToRemove) {
            int pos = mAidlToCompile.indexOf(aidlFile);
            if (pos != -1) {
                mAidlToCompile.remove(pos);
            }
        }
        for (AidlData aidlData : mAidlToCompile) {
            removeMarkersFromFile(aidlData.aidlFile, AndroidConstants.MARKER_AIDL);
            IPath sourcePath = aidlData.aidlFile.getLocation();
            String osSourcePath = sourcePath.toOSString();
            IFile javaFile = getGenDestinationFile(aidlData, true , monitor);
            command[index] = osSourcePath;
            command[index + 1] = javaFile.getLocation().toOSString();
            if (execAidl(command, aidlData.aidlFile) == false) {
                stillNeedCompilation.add(aidlData);
                continue;
            } else {
                mDerivedProgressMonitor.addFile(javaFile);
            }
        }
        mAidlToCompile.clear();
        mAidlToCompile.addAll(stillNeedCompilation);
        for (AidlData aidlData : mAidlToRemove) {
            IFile javaFile = getGenDestinationFile(aidlData, false , monitor);
            if (javaFile.exists()) {
                javaFile.delete(true, null);
                javaFile.getParent().refreshLocal(IResource.DEPTH_ONE, monitor);
            }
        }
        mAidlToRemove.clear();
        saveProjectBooleanProperty(PROPERTY_COMPILE_AIDL , mAidlToCompile.size() > 0);
        return true;
    }
    private IFile getGenDestinationFile(AidlData aidlData, boolean createFolders,
            IProgressMonitor monitor) throws CoreException {
        int segmentToSourceFolderCount = aidlData.sourceFolder.getFullPath().segmentCount();
        IPath packagePath = aidlData.aidlFile.getFullPath().removeFirstSegments(
                segmentToSourceFolderCount).removeLastSegments(1);
        Path destinationPath = new Path(packagePath.toString());
        IFolder destinationFolder = mGenFolder.getFolder(destinationPath);
        if (destinationFolder.exists() == false && createFolders) {
            createFolder(destinationFolder, monitor);
        }
        String javaName = aidlData.aidlFile.getName().replaceAll(AndroidConstants.RE_AIDL_EXT,
                AndroidConstants.DOT_JAVA);
        IFile javaFile = destinationFolder.getFile(javaName);
        return javaFile;
    }
    private void createFolder(IFolder destinationFolder, IProgressMonitor monitor)
            throws CoreException {
        IContainer parent = destinationFolder.getParent();
        if (parent.getType() == IResource.FOLDER && parent.exists() == false) {
            createFolder((IFolder)parent, monitor);
        }
        destinationFolder.create(true , true ,
                new SubProgressMonitor(monitor, 10));
    }
    private boolean execAidl(String[] command, IFile file) {
        try {
            Process p = Runtime.getRuntime().exec(command);
            ArrayList<String> results = new ArrayList<String>();
            int result = grabProcessOutput(p, results);
            boolean error = parseAidlOutput(results, file);
            if (result != 0 && error == true) {
                AdtPlugin.printErrorToConsole(getProject(), results.toArray());
                markProject(AndroidConstants.MARKER_ADT, Messages.Unparsed_AIDL_Errors,
                        IMarker.SEVERITY_ERROR);
                return false;
            }
        } catch (IOException e) {
            String msg = String.format(Messages.AIDL_Exec_Error, command[0]);
            markProject(AndroidConstants.MARKER_ADT, msg, IMarker.SEVERITY_ERROR);
            return false;
        } catch (InterruptedException e) {
            String msg = String.format(Messages.AIDL_Exec_Error, command[0]);
            markProject(AndroidConstants.MARKER_ADT, msg, IMarker.SEVERITY_ERROR);
            return false;
        }
        return true;
    }
    private void buildAidlCompilationList(IProject project,
            ArrayList<IPath> sourceFolderPathList) {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        for (IPath sourceFolderPath : sourceFolderPathList) {
            IFolder sourceFolder = root.getFolder(sourceFolderPath);
            if (sourceFolder.exists() && sourceFolder.equals(mGenFolder) == false) {
                scanFolderForAidl(sourceFolder, sourceFolder);
            }
        }
    }
    private void scanFolderForAidl(IFolder sourceFolder, IFolder folder) {
        try {
            IResource[] members = folder.members();
            for (IResource r : members) {
               switch (r.getType()) {
                   case IResource.FILE:
                       if (r.exists() &&
                               AndroidConstants.EXT_AIDL.equalsIgnoreCase(r.getFileExtension())) {
                           mAidlToCompile.add(new AidlData(sourceFolder, (IFile)r));
                       }
                       break;
                   case IResource.FOLDER:
                       scanFolderForAidl(sourceFolder, (IFolder)r);
                       break;
                   default:
                       break;
               }
            }
        } catch (CoreException e) {
        }
    }
    private boolean parseAidlOutput(ArrayList<String> lines, IFile file) {
        if (lines.size() == 0) {
            return false;
        }
        Matcher m;
        for (int i = 0; i < lines.size(); i++) {
            String p = lines.get(i);
            m = sAidlPattern1.matcher(p);
            if (m.matches()) {
                String lineStr = m.group(2);
                String msg = m.group(3);
                int line = 0;
                try {
                    line = Integer.parseInt(lineStr);
                } catch (NumberFormatException e) {
                    return true;
                }
                BaseProjectHelper.markResource(file, AndroidConstants.MARKER_AIDL, msg, line,
                        IMarker.SEVERITY_ERROR);
                continue;
            }
            return true;
        }
        return false;
    }
    private void mergeAidlFileModifications(ArrayList<AidlData> toCompile,
            ArrayList<AidlData> toRemove) {
        for (AidlData r : toRemove) {
            if (mAidlToRemove.indexOf(r) == -1) {
                mAidlToRemove.add(r);
            }
            int index = mAidlToCompile.indexOf(r);
            if (index != -1) {
                mAidlToCompile.remove(index);
            }
        }
        for (AidlData r : toCompile) {
            if (mAidlToCompile.indexOf(r) == -1) {
                mAidlToCompile.add(r);
            }
            int index = mAidlToRemove.indexOf(r);
            if (index != -1) {
                mAidlToRemove.remove(index);
            }
        }
    }
}
