public class ApkDeltaVisitor extends BaseDeltaVisitor
        implements IResourceDeltaVisitor {
    private boolean mConvertToDex = false;
    private boolean mPackageResources = false;
    private boolean mMakeFinalPackage = false;
    private ArrayList<IPath> mSourceFolders;
    private IPath mOutputPath;
    private IPath mAssetPath;
    private IPath mResPath;
    private IPath mLibFolder;
    public ApkDeltaVisitor(BaseBuilder builder, ArrayList<IPath> sourceFolders,
            IFolder outputfolder) {
        super(builder);
        mSourceFolders = sourceFolders;
        if (outputfolder != null) {
            mOutputPath = outputfolder.getFullPath();
        }
        IResource assetFolder = builder.getProject().findMember(SdkConstants.FD_ASSETS);
        if (assetFolder != null) {
            mAssetPath = assetFolder.getFullPath();
        }
        IResource resFolder = builder.getProject().findMember(SdkConstants.FD_RESOURCES);
        if (resFolder != null) {
            mResPath = resFolder.getFullPath();
        }
        IResource libFolder = builder.getProject().findMember(SdkConstants.FD_NATIVE_LIBS);
        if (libFolder != null) {
            mLibFolder = libFolder.getFullPath();
        }
    }
    public boolean getConvertToDex() {
        return mConvertToDex;
    }
    public boolean getPackageResources() {
        return mPackageResources;
    }
    public boolean getMakeFinalPackage() {
        return mMakeFinalPackage;
    }
    public boolean visit(IResourceDelta delta) throws CoreException {
        if (mConvertToDex && mPackageResources && mMakeFinalPackage) {
            return false;
        }
        IResource resource = delta.getResource();
        IPath path = resource.getFullPath();
        String[] pathSegments = path.segments();
        int type = resource.getType();
        if (pathSegments.length == 1) {
            return true;
        }
        if (pathSegments.length == 2 &&
                AndroidConstants.FN_ANDROID_MANIFEST.equalsIgnoreCase(pathSegments[1])) {
            mPackageResources = true;
            mMakeFinalPackage = true;
            return false;
        }
        if (mOutputPath != null && mOutputPath.isPrefixOf(path)) {
            if (type == IResource.FILE) {
                String ext = resource.getFileExtension();
                if (AndroidConstants.EXT_CLASS.equalsIgnoreCase(ext)) {
                    mConvertToDex = true;
                    mMakeFinalPackage = true;
                    return false;
                }
                if (delta.getKind() == IResourceDelta.REMOVED) {
                    IPath parentPath = path.removeLastSegments(1);
                    if (mOutputPath.equals(parentPath)) {
                        String resourceName = resource.getName();
                        if (resourceName.equalsIgnoreCase(AndroidConstants.FN_CLASSES_DEX)) {
                            mConvertToDex = true;
                            mMakeFinalPackage = true;
                        } else if (resourceName.equalsIgnoreCase(
                                AndroidConstants.FN_RESOURCES_AP_) ||
                                AndroidConstants.PATTERN_RESOURCES_S_AP_.matcher(
                                        resourceName).matches()) {
                            mPackageResources = true;
                            mMakeFinalPackage = true;
                        }
                    }
                }
            }
            return mConvertToDex == false;
        } else if (mResPath != null && mResPath.isPrefixOf(path)) {
            if (type == IResource.FILE) {
                mPackageResources = true;
                mMakeFinalPackage = true;
                return false;
            }
            return mPackageResources == false;
        } else if (mAssetPath != null && mAssetPath.isPrefixOf(path)) {
            mPackageResources = true;
            mMakeFinalPackage = true;
            return false;
        } else if (mLibFolder != null && mLibFolder.isPrefixOf(path)) {
            if (type == IResource.FILE &&
                    (AndroidConstants.EXT_NATIVE_LIB.equalsIgnoreCase(path.getFileExtension())
                            || ApkBuilder.GDBSERVER_NAME.equals(resource.getName()))) {
                mMakeFinalPackage = true;
                return false; 
            }
            return mMakeFinalPackage == false;
        } else {
            if (mMakeFinalPackage == false) {
                for (IPath sourcePath : mSourceFolders) {
                    if (sourcePath.isPrefixOf(path)) {
                        if (type == IResource.FOLDER) {
                            return ApkBuilder.checkFolderForPackaging((IFolder)resource);
                        } else if (type == IResource.FILE) {
                            if (ApkBuilder.checkFileForPackaging((IFile)resource)) {
                                mMakeFinalPackage = true;
                            }
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
