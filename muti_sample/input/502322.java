public final class AaptExecLoopTask extends Task {
    public final static class NoCompress {
        String mExtension;
        public void setExtension(String extention) {
            mExtension = extention;
        }
    }
    private String mExecutable;
    private String mCommand;
    private boolean mForce = true; 
    private boolean mVerbose = false;
    private String mManifest;
    private ArrayList<Path> mResources;
    private String mAssets;
    private String mAndroidJar;
    private String mApkFolder;
    private String mApkBaseName;
    private String mRFolder;
    private final ArrayList<NoCompress> mNoCompressList = new ArrayList<NoCompress>();
    public void setExecutable(Path executable) {
        mExecutable = TaskHelper.checkSinglePath("executable", executable);
    }
    public void setCommand(String command) {
        mCommand = command;
    }
    public void setForce(boolean force) {
        mForce = force;
    }
    public void setVerbose(boolean verbose) {
        mVerbose = verbose;
    }
    public void setManifest(Path manifest) {
        mManifest = TaskHelper.checkSinglePath("manifest", manifest);
    }
    @Deprecated
    public void setResources(Path resources) {
        if (mResources == null) {
            mResources = new ArrayList<Path>();
        }
        mResources.add(new Path(getProject(), resources.toString()));
    }
    public void setAssets(Path assets) {
        mAssets = TaskHelper.checkSinglePath("assets", assets);
    }
    public void setAndroidjar(Path androidJar) {
        mAndroidJar = TaskHelper.checkSinglePath("androidjar", androidJar);
    }
    @Deprecated
    public void setOutfolder(Path outFolder) {
        mApkFolder = TaskHelper.checkSinglePath("outfolder", outFolder);
    }
    public void setApkfolder(Path apkFolder) {
        mApkFolder = TaskHelper.checkSinglePath("apkfolder", apkFolder);
    }
    @Deprecated
    public void setBasename(String baseName) {
        mApkBaseName = baseName;
    }
    public void setApkbasename(String apkbaseName) {
        mApkBaseName = apkbaseName;
    }
    public void setRfolder(Path rFolder) {
        mRFolder = TaskHelper.checkSinglePath("rfolder", rFolder);
    }
    public Object createNocompress() {
        NoCompress nc = new NoCompress();
        mNoCompressList.add(nc);
        return nc;
    }
    public Object createRes() {
        if (mResources == null) {
            mResources = new ArrayList<Path>();
        }
        Path path = new Path(getProject());
        mResources.add(path);
        return path;
    }
    @Override
    public void execute() throws BuildException {
        Project taskProject = getProject();
        createPackage(null , null , null );
        if (mRFolder != null && new File(mRFolder).isDirectory()) {
            String libPkgProp = taskProject.getProperty("android.libraries.package");
            if (libPkgProp != null) {
                String mainPackage = taskProject.getProperty("manifest.package");
                String[] libPkgs = libPkgProp.split(";");
                for (String libPkg : libPkgs) {
                    if (libPkg.length() > 0 && mainPackage.equals(libPkg) == false) {
                        createPackage(null, null, libPkg);
                    }
                }
            }
        }
        File baseDir = taskProject.getBaseDir();
        ProjectProperties properties = ProjectProperties.load(baseDir.getAbsolutePath(),
                PropertyType.DEFAULT);
        ApkSettings apkSettings = new ApkSettings(properties);
        if (apkSettings != null) {
            Map<String, String> apkFilters = apkSettings.getResourceFilters();
            if (apkFilters.size() > 0) {
                for (Entry<String, String> entry : apkFilters.entrySet()) {
                    createPackage(entry.getKey(), entry.getValue(), null );
                }
            }
        }
    }
    private void createPackage(String configName, String resourceFilter, String customPackage) {
        Project taskProject = getProject();
        final boolean generateRClass = mRFolder != null && new File(mRFolder).isDirectory();
        if (generateRClass) {
        } else if (configName == null || resourceFilter == null) {
            System.out.println("Creating full resource package...");
        } else {
            System.out.println(String.format(
                    "Creating resource package for config '%1$s' (%2$s)...",
                    configName, resourceFilter));
        }
        ExecTask task = new ExecTask();
        task.setExecutable(mExecutable);
        task.setFailonerror(true);
        task.createArg().setValue(mCommand);
        if (mForce) {
            task.createArg().setValue("-f");
        }
        if (mVerbose) {
            task.createArg().setValue("-v");
        }
        if (generateRClass) {
            task.createArg().setValue("-m");
        }
        if (configName != null && resourceFilter != null) {
            task.createArg().setValue("-c");
            task.createArg().setValue(resourceFilter);
        }
        boolean compressNothing = false;
        for (NoCompress nc : mNoCompressList) {
            if (nc.mExtension == null) {
                task.createArg().setValue("-0");
                task.createArg().setValue("");
                compressNothing = true;
                break;
            }
        }
        if (compressNothing == false) {
            for (NoCompress nc : mNoCompressList) {
                task.createArg().setValue("-0");
                task.createArg().setValue(nc.mExtension);
            }
        }
        if (customPackage != null) {
            task.createArg().setValue("--custom-package");
            task.createArg().setValue(customPackage);
        }
        Object libSrc = taskProject.getReference("android.libraries.res");
        if (libSrc != null) {
            task.createArg().setValue("--auto-add-overlay");
        }
        if (mManifest != null) {
            task.createArg().setValue("-M");
            task.createArg().setValue(mManifest);
        }
        if (mResources.size() > 0) {
            for (Path pathList : mResources) {
                for (String path : pathList.list()) {
                    File res = new File(path);
                    if (res.isDirectory()) {
                        task.createArg().setValue("-S");
                        task.createArg().setValue(path);
                    }
                }
            }
        }
        Object libPath = taskProject.getReference("android.libraries.res");
        if (libPath instanceof Path) {
            for (String path : ((Path)libPath).list()) {
                File res = new File(path);
                if (res.isDirectory()) {
                    task.createArg().setValue("-S");
                    task.createArg().setValue(path);
                }
            }
        }
        if (mAssets != null && new File(mAssets).isDirectory()) {
            task.createArg().setValue("-A");
            task.createArg().setValue(mAssets);
        }
        if (mAndroidJar != null) {
            task.createArg().setValue("-I");
            task.createArg().setValue(mAndroidJar);
        }
        if (mApkBaseName != null && mApkBaseName != null) {
            String filename;
            if (configName != null && resourceFilter != null) {
                filename = mApkBaseName + "-" + configName + ".ap_";
            } else {
                filename = mApkBaseName + ".ap_";
            }
            File file = new File(mApkFolder, filename);
            task.createArg().setValue("-F");
            task.createArg().setValue(file.getAbsolutePath());
        }
        if (generateRClass) {
            task.createArg().setValue("-J");
            task.createArg().setValue(mRFolder);
        }
        task.setProject(taskProject);
        task.setOwningTarget(getOwningTarget());
        task.execute();
    }
}
