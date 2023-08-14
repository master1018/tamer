public class ApkBuilderTask extends Task {
    private final static String REF_APK_PATH = "android.apks.path";
    private String mOutFolder;
    private String mBaseName;
    private boolean mVerbose = false;
    private boolean mSigned = true;
    private boolean mDebug = false;
    private final ArrayList<Path> mZipList = new ArrayList<Path>();
    private final ArrayList<Path> mFileList = new ArrayList<Path>();
    private final ArrayList<Path> mSourceList = new ArrayList<Path>();
    private final ArrayList<Path> mJarfolderList = new ArrayList<Path>();
    private final ArrayList<Path> mJarfileList = new ArrayList<Path>();
    private final ArrayList<Path> mNativeList = new ArrayList<Path>();
    private final ArrayList<FileInputStream> mZipArchives = new ArrayList<FileInputStream>();
    private final ArrayList<File> mArchiveFiles = new ArrayList<File>();
    private final ArrayList<ApkFile> mJavaResources = new ArrayList<ApkFile>();
    private final ArrayList<FileInputStream> mResourcesJars = new ArrayList<FileInputStream>();
    private final ArrayList<ApkFile> mNativeLibraries = new ArrayList<ApkFile>();
    public void setOutfolder(Path outFolder) {
        mOutFolder = TaskHelper.checkSinglePath("outfolder", outFolder);
    }
    public void setBasename(String baseName) {
        mBaseName = baseName;
    }
    public void setVerbose(boolean verbose) {
        mVerbose = verbose;
    }
    public void setSigned(boolean signed) {
        mSigned = signed;
    }
    public void setDebug(boolean debug) {
        mDebug = debug;
    }
    public Object createZip() {
        Path path = new Path(getProject());
        mZipList.add(path);
        return path;
    }
    public Object createFile() {
        Path path = new Path(getProject());
        mFileList.add(path);
        return path;
    }
    public Object createSourcefolder() {
        Path path = new Path(getProject());
        mSourceList.add(path);
        return path;
    }
    public Object createJarfolder() {
        Path path = new Path(getProject());
        mJarfolderList.add(path);
        return path;
    }
    public Object createJarfile() {
        Path path = new Path(getProject());
        mJarfileList.add(path);
        return path;
    }
    public Object createNativefolder() {
        Path path = new Path(getProject());
        mNativeList.add(path);
        return path;
    }
    @Override
    public void execute() throws BuildException {
        Project antProject = getProject();
        ApkBuilderImpl apkBuilder = new ApkBuilderImpl();
        apkBuilder.setVerbose(mVerbose);
        apkBuilder.setSignedPackage(mSigned);
        apkBuilder.setDebugMode(mDebug);
        try {
            for (Path pathList : mZipList) {
                for (String path : pathList.list()) {
                    FileInputStream input = new FileInputStream(path);
                    mZipArchives.add(input);
                }
            }
            for (Path pathList : mFileList) {
                for (String path : pathList.list()) {
                    mArchiveFiles.add(ApkBuilderImpl.getInputFile(path));
                }
            }
            for (Path pathList : mSourceList) {
                for (String path : pathList.list()) {
                    ApkBuilderImpl.processSourceFolderForResource(new File(path),
                            mJavaResources);
                }
            }
            for (Path pathList : mJarfolderList) {
                for (String path : pathList.list()) {
                    ApkBuilderImpl.processJar(new File(path), mResourcesJars);
                }
            }
            for (Path pathList : mJarfileList) {
                for (String path : pathList.list()) {
                    ApkBuilderImpl.processJar(new File(path), mResourcesJars);
                }
            }
            for (Path pathList : mNativeList) {
                for (String path : pathList.list()) {
                    ApkBuilderImpl.processNativeFolder(new File(path), mDebug,
                            mNativeLibraries);
                }
            }
            Path path = new Path(antProject);
            String debugPackageSuffix = "-debug-unaligned.apk";
            if (antProject.getProperty("out.debug.unaligned.package") == null
                    && antProject.getProperty("out-debug-unaligned-package") == null) {
                debugPackageSuffix = "-debug.apk";
            }
            createApk(apkBuilder, null , null , path,
                    debugPackageSuffix);
            File baseDir = antProject.getBaseDir();
            ProjectProperties properties = ProjectProperties.load(baseDir.getAbsolutePath(),
                    PropertyType.DEFAULT);
            ApkSettings apkSettings = new ApkSettings(properties);
            if (apkSettings != null) {
                Map<String, String> apkFilters = apkSettings.getResourceFilters();
                if (apkFilters.size() > 0) {
                    for (Entry<String, String> entry : apkFilters.entrySet()) {
                        createApk(apkBuilder, entry.getKey(), entry.getValue(), path,
                                debugPackageSuffix);
                    }
                }
            }
            antProject.addReference(REF_APK_PATH, path);
        } catch (FileNotFoundException e) {
            throw new BuildException(e);
        } catch (IllegalArgumentException e) {
            throw new BuildException(e);
        } catch (ApkCreationException e) {
            throw new BuildException(e);
        }
    }
    private void createApk(ApkBuilderImpl apkBuilder, String configName, String resourceFilter,
            Path path, String debugPackageSuffix)
            throws FileNotFoundException, ApkCreationException {
        String filename;
        if (configName != null && resourceFilter != null) {
            filename = mBaseName + "-" + configName + ".ap_";
        } else {
            filename = mBaseName + ".ap_";
        }
        FileInputStream resoucePackageZipFile = new FileInputStream(new File(mOutFolder, filename));
        mZipArchives.add(resoucePackageZipFile);
        if (configName != null && resourceFilter != null) {
            filename = mBaseName + "-" + configName;
        } else {
            filename = mBaseName;
        }
        if (mSigned) {
            filename = filename + debugPackageSuffix;
        } else {
            filename = filename + "-unsigned.apk";
        }
        if (configName == null || resourceFilter == null) {
            if (mSigned) {
                System.out.println(String.format(
                        "Creating %s and signing it with a debug key...", filename));
            } else {
                System.out.println(String.format(
                        "Creating %s for release...", filename));
            }
        } else {
            if (mSigned) {
                System.out.println(String.format(
                        "Creating %1$s (with %2$s) and signing it with a debug key...",
                        filename, resourceFilter));
            } else {
                System.out.println(String.format(
                        "Creating %1$s (with %2$s) for release...",
                        filename, resourceFilter));
            }
        }
        File f = new File(mOutFolder, filename);
        PathElement element = path.createPathElement();
        element.setLocation(f);
        apkBuilder.createPackage(f.getAbsoluteFile(), mZipArchives,
                mArchiveFiles, mJavaResources, mResourcesJars, mNativeLibraries);
        mZipArchives.remove(resoucePackageZipFile);
    }
}
