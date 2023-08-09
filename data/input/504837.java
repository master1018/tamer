class PreCompilerDeltaVisitor extends BaseDeltaVisitor implements
        IResourceDeltaVisitor {
    private enum AidlType {
        UNKNOWN, INTERFACE, PARCELABLE;
    }
    private boolean mCompileResources = false;
    private boolean mForceAidlCompile = false;
    private final ArrayList<AidlData> mAidlToCompile = new ArrayList<AidlData>();
    private final ArrayList<AidlData> mAidlToRemove = new ArrayList<AidlData>();
    private boolean mCheckedManifestXml = false;
    private String mJavaPackage = null;
    private String mMinSdkVersion = null;
    private boolean mInRes = false;
    private IFolder mSourceFolder = null;
    private ArrayList<IPath> mSourceFolders;
    private boolean mIsGenSourceFolder = false;
    private IWorkspaceRoot mRoot;
    public PreCompilerDeltaVisitor(BaseBuilder builder, ArrayList<IPath> sourceFolders) {
        super(builder);
        mSourceFolders = sourceFolders;
        mRoot = ResourcesPlugin.getWorkspace().getRoot();
    }
    public boolean getCompileResources() {
        return mCompileResources;
    }
    public boolean getForceAidlCompile() {
        return mForceAidlCompile;
    }
    public ArrayList<AidlData> getAidlToCompile() {
        return mAidlToCompile;
    }
    public ArrayList<AidlData> getAidlToRemove() {
        return mAidlToRemove;
    }
    public boolean getCheckedManifestXml() {
        return mCheckedManifestXml;
    }
    public String getManifestPackage() {
        return mJavaPackage;
    }
    public String getMinSdkVersion() {
        return mMinSdkVersion;
    }
    public boolean visit(IResourceDelta delta) throws CoreException {
        IResource resource = delta.getResource();
        IPath path = resource.getFullPath();
        String[] segments = path.segments();
        if (segments.length == 1) {
            return true;
        } else if (segments.length == 2) {
            mInRes = false;
            mSourceFolder = null;
            if (SdkConstants.FD_RESOURCES.equalsIgnoreCase(segments[1])) {
                mInRes = true;
                mSourceFolder = null;
                return true;
            } else if (AndroidConstants.FN_ANDROID_MANIFEST.equalsIgnoreCase(segments[1])) {
                if (delta.getKind() != IResourceDelta.REMOVED) {
                    AndroidManifestParser parser = BaseProjectHelper.parseManifestForError(
                            (IFile)resource, this);
                    if (parser != null) {
                        mJavaPackage = parser.getPackage();
                        mMinSdkVersion = parser.getApiLevelRequirement();
                    }
                    mCheckedManifestXml = true;
                }
                mCompileResources = true;
                return false;
            }
        }
        if (mSourceFolder != null) {
            if (resource.getType() == IResource.FOLDER) {
                return true;
            }
            if (resource.getType() != IResource.FILE) {
                return false;
            }
            IFile file = (IFile)resource;
            int kind = delta.getKind();
            if (mIsGenSourceFolder) {
                boolean outputWarning = false;
                String fileName = resource.getName();
                if (AndroidConstants.FN_RESOURCE_CLASS.equals(fileName) ||
                        AndroidConstants.FN_MANIFEST_CLASS.equals(fileName)) {
                    mCompileResources = true;
                    outputWarning = true;
                } else {
                    String aidlFileName = fileName.replaceAll(AndroidConstants.RE_JAVA_EXT,
                            AndroidConstants.DOT_AIDL);
                    for (IPath sourceFolderPath : mSourceFolders) {
                        if (sourceFolderPath.equals(mSourceFolder.getFullPath())) {
                            continue;
                        }
                        IFolder sourceFolder = getFolder(sourceFolderPath);
                        if (sourceFolder != null) {
                            IFile sourceFile = findFile(sourceFolder, segments, 2, aidlFileName);
                            if (sourceFile != null) {
                                mAidlToCompile.add(new AidlData(sourceFolder, sourceFile));
                                outputWarning = true;
                                break;
                            }
                        }
                    }
                }
                if (outputWarning) {
                    if (kind == IResourceDelta.REMOVED) {
                        String msg = String.format(Messages.s_Removed_Recreating_s, fileName);
                        AdtPlugin.printErrorToConsole(mBuilder.getProject(), msg);
                    } else if (kind == IResourceDelta.CHANGED) {
                        String msg = String.format(Messages.s_Modified_Manually_Recreating_s,
                                fileName);
                        AdtPlugin.printErrorToConsole(mBuilder.getProject(), msg);
                    }
                }
            } else {
                String ext = resource.getFileExtension();
                if (AndroidConstants.EXT_AIDL.equalsIgnoreCase(ext)) {
                    AidlType type = getAidlType(file);
                    if (type == AidlType.INTERFACE) {
                        if (kind == IResourceDelta.REMOVED) {
                            mAidlToRemove.add(new AidlData(mSourceFolder, file));
                        } else if (mForceAidlCompile == false) {
                            mAidlToCompile.add(new AidlData(mSourceFolder, file));
                        }
                    } else {
                        mForceAidlCompile = true;
                        mAidlToCompile.clear();
                    }
                }
            }
            return false;
        } else if (mInRes) {
            if (resource.getType() == IResource.FOLDER) {
                return true;
            }
            String ext = resource.getFileExtension();
            int kind = delta.getKind();
            String p = resource.getProjectRelativePath().toString();
            String message = null;
            switch (kind) {
                case IResourceDelta.CHANGED:
                    message = String.format(Messages.s_Modified_Recreating_s, p,
                            AndroidConstants.FN_RESOURCE_CLASS);
                    break;
                case IResourceDelta.ADDED:
                    message = String.format(Messages.Added_s_s_Needs_Updating, p,
                            AndroidConstants.FN_RESOURCE_CLASS);
                    break;
                case IResourceDelta.REMOVED:
                    message = String.format(Messages.s_Removed_s_Needs_Updating, p,
                            AndroidConstants.FN_RESOURCE_CLASS);
                    break;
            }
            if (message != null) {
                AdtPlugin.printBuildToConsole(BuildVerbosity.VERBOSE,
                        mBuilder.getProject(), message);
            }
            if (AndroidConstants.EXT_XML.equalsIgnoreCase(ext)) {
                if (kind != IResourceDelta.REMOVED) {
                    mBuilder.checkXML(resource, this);
                }
                mCompileResources = true;
                return false;
            } else {
                if (kind == IResourceDelta.ADDED
                        || kind == IResourceDelta.REMOVED) {
                    mCompileResources = true;
                    return false;
                }
            }
        } else if (resource instanceof IFolder) {
            for (IPath sourceFolderPath : mSourceFolders) {
                if (sourceFolderPath.equals(path)) {
                    mInRes = false;
                    mSourceFolder = getFolder(sourceFolderPath); 
                    mIsGenSourceFolder = path.segmentCount() == 2 &&
                            path.segment(1).equals(SdkConstants.FD_GEN_SOURCES);
                    return true;
                }
                int count = sourceFolderPath.matchingFirstSegments(path);
                if (count == path.segmentCount()) {
                    mInRes = false;
                    return true;
                }
            }
            return false;
        }
        return false;
    }
    private IFile findFile(IFolder folder, String[] segments, int index, String filename) {
        boolean lastSegment = index == segments.length - 1;
        IResource resource = folder.findMember(lastSegment ? filename : segments[index]);
        if (resource != null && resource.exists()) {
            if (lastSegment) {
                if (resource.getType() == IResource.FILE) {
                    return (IFile)resource;
                }
            } else {
                if (resource.getType() == IResource.FOLDER) {
                    return findFile((IFolder)resource, segments, index+1, filename);
                }
            }
        }
        return null;
    }
    private IFolder getFolder(IPath path) {
        IResource resource = mRoot.findMember(path);
        if (resource != null && resource.exists() && resource.getType() == IResource.FOLDER) {
            return (IFolder)resource;
        }
        return null;
    }
    private AidlType getAidlType(IFile file) throws CoreException {
        return AidlType.UNKNOWN;
    }
}
