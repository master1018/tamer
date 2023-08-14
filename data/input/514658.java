public final class SetupTask extends ImportTask {
    private final static int ANT_RULES_MAX_VERSION = 2;
    private final static String RULES_LEGACY_MAIN = "android_rules.xml";
    private final static String RULES_LEGACY_TEST = "android_test_rules.xml";
    private final static String RULES_MAIN = "ant_rules_r%1$d.xml";
    private final static String RULES_TEST = "ant_test_rules_r%1$d.xml";
    private final static String RULES_LIBRARY = "ant_lib_rules_r%1$d.xml";
    private final static String PROPERTY_ANDROID_JAR = "android.jar";
    private final static String PROPERTY_ANDROID_JAR_LEGACY = "android-jar";
    private final static String PROPERTY_ANDROID_AIDL = "android.aidl";
    private final static String PROPERTY_ANDROID_AIDL_LEGACY = "android-aidl";
    private final static String PROPERTY_AAPT = "aapt";
    private final static String PROPERTY_AIDL = "aidl";
    private final static String PROPERTY_DX = "dx";
    private final static String REF_CLASSPATH = "android.target.classpath";
    private boolean mDoImport = true;
    @Override
    public void execute() throws BuildException {
        Project antProject = getProject();
        String sdkLocation = antProject.getProperty(ProjectProperties.PROPERTY_SDK);
        if (sdkLocation == null || sdkLocation.length() == 0) {
            sdkLocation = antProject.getProperty(ProjectProperties.PROPERTY_SDK_LEGACY);
            if (sdkLocation == null || sdkLocation.length() == 0) {
                throw new BuildException("SDK Location is not set.");
            }
        }
        File sdk = new File(sdkLocation);
        if (sdk.isDirectory() == false) {
            throw new BuildException(String.format("SDK Location '%s' is not valid.", sdkLocation));
        }
        int toolsRevison = getToolsRevision(sdk);
        if (toolsRevison != -1) {
            System.out.println("Android SDK Tools Revision " + toolsRevison);
        }
        String targetHashString = antProject.getProperty(ProjectProperties.PROPERTY_TARGET);
        boolean isTestProject = false;
        if (antProject.getProperty("tested.project.dir") != null) {
            isTestProject = true;
        }
        if (targetHashString == null) {
            throw new BuildException("Android Target is not set.");
        }
        final ArrayList<String> messages = new ArrayList<String>();
        SdkManager manager = SdkManager.createManager(sdkLocation, new ISdkLog() {
            public void error(Throwable t, String errorFormat, Object... args) {
                if (errorFormat != null) {
                    messages.add(String.format("Error: " + errorFormat, args));
                }
                if (t != null) {
                    messages.add("Error: " + t.getMessage());
                }
            }
            public void printf(String msgFormat, Object... args) {
                messages.add(String.format(msgFormat, args));
            }
            public void warning(String warningFormat, Object... args) {
                messages.add(String.format("Warning: " + warningFormat, args));
            }
        });
        if (manager == null) {
            for (String msg : messages) {
                System.out.println(msg);
            }
            throw new BuildException("Failed to parse SDK content.");
        }
        IAndroidTarget androidTarget = manager.getTargetFromHashString(targetHashString);
        if (androidTarget == null) {
            throw new BuildException(String.format(
                    "Unable to resolve target '%s'", targetHashString));
        }
        int antBuildVersion = androidTarget.getProperty(SdkConstants.PROP_SDK_ANT_BUILD_REVISION,
                1);
        if (antBuildVersion > ANT_RULES_MAX_VERSION) {
            throw new BuildException(String.format(
                    "The project target (%1$s) requires a more recent version of the tools. Please update.",
                    androidTarget.getName()));
        }
        boolean isLibrary = false;
        String libraryProp = antProject.getProperty(ProjectProperties.PROPERTY_LIBRARY);
        if (libraryProp != null) {
            isLibrary = Boolean.valueOf(libraryProp).booleanValue();
        }
        processReferencedLibraries(antProject, androidTarget);
        System.out.println("Project Target: " + androidTarget.getName());
        if (isLibrary) {
            System.out.println("Type: Android Library");
        }
        if (androidTarget.isPlatform() == false) {
            System.out.println("Vendor: " + androidTarget.getVendor());
            System.out.println("Platform Version: " + androidTarget.getVersionName());
        }
        System.out.println("API level: " + androidTarget.getVersion().getApiString());
        if (isLibrary &&
                androidTarget.getProperty(SdkConstants.PROP_SDK_SUPPORT_LIBRARY, false) == false) {
            throw new BuildException(String.format(
                    "Project target '%1$s' does not support building libraries.",
                    androidTarget.getFullName()));
        }
        checkManifest(antProject, androidTarget.getVersion());
        String androidJar = androidTarget.getPath(IAndroidTarget.ANDROID_JAR);
        antProject.setProperty(PROPERTY_ANDROID_JAR, androidJar);
        String androidAidl = androidTarget.getPath(IAndroidTarget.ANDROID_AIDL);
        antProject.setProperty(PROPERTY_ANDROID_AIDL, androidAidl);
        antProject.setProperty(PROPERTY_AAPT, androidTarget.getPath(IAndroidTarget.AAPT));
        antProject.setProperty(PROPERTY_AIDL, androidTarget.getPath(IAndroidTarget.AIDL));
        antProject.setProperty(PROPERTY_DX, androidTarget.getPath(IAndroidTarget.DX));
        Path bootclasspath = new Path(antProject);
        PathElement element = bootclasspath.createPathElement();
        element.setPath(androidJar);
        IOptionalLibrary[] libraries = androidTarget.getOptionalLibraries();
        if (libraries != null) {
            HashSet<String> visitedJars = new HashSet<String>();
            for (IOptionalLibrary library : libraries) {
                String jarPath = library.getJarPath();
                if (visitedJars.contains(jarPath) == false) {
                    visitedJars.add(jarPath);
                    element = bootclasspath.createPathElement();
                    element.setPath(library.getJarPath());
                }
            }
        }
        antProject.addReference(REF_CLASSPATH, bootclasspath);
        if (androidTarget.getVersion().getApiLevel() <= 4) { 
            antProject.setProperty(PROPERTY_ANDROID_JAR_LEGACY, androidJar);
            antProject.setProperty(PROPERTY_ANDROID_AIDL_LEGACY, androidAidl);
            antProject.setProperty(ProjectProperties.PROPERTY_SDK_LEGACY, sdkLocation);
            String appPackage = antProject.getProperty(ProjectProperties.PROPERTY_APP_PACKAGE);
            if (appPackage != null && appPackage.length() > 0) {
                antProject.setProperty(ProjectProperties.PROPERTY_APP_PACKAGE_LEGACY, appPackage);
            }
        }
        if (mDoImport) {
            int folderID = antBuildVersion == 1 ? IAndroidTarget.TEMPLATES : IAndroidTarget.ANT;
            String rulesOSPath = androidTarget.getPath(folderID);
            File rulesFolder = new File(rulesOSPath);
            if (rulesFolder.isDirectory() == false) {
                throw new BuildException(String.format("Rules directory '%s' is missing.",
                        rulesOSPath));
            }
            String importedRulesFileName;
            if (antBuildVersion == 1) {
                importedRulesFileName = isTestProject ? RULES_LEGACY_TEST : RULES_LEGACY_MAIN;
            } else {
                importedRulesFileName = String.format(
                        isLibrary ? RULES_LIBRARY : isTestProject ? RULES_TEST : RULES_MAIN,
                        antBuildVersion);;
            }
            File rules = new File(rulesFolder, importedRulesFileName);
            if (rules.isFile() == false) {
                throw new BuildException(String.format("Build rules file '%s' is missing.",
                        rules));
            }
            String rulesLocation = rules.getAbsolutePath();
            if (rulesLocation.startsWith(sdkLocation)) {
                rulesLocation = rulesLocation.substring(sdkLocation.length());
                if (rulesLocation.startsWith(File.separator)) {
                    rulesLocation = rulesLocation.substring(1);
                }
            }
            System.out.println("Importing rules file: " + rulesLocation);
            setFile(rules.getAbsolutePath());
            super.execute();
        }
    }
    public void setImport(boolean value) {
        mDoImport = value;
    }
    private void checkManifest(Project antProject, AndroidVersion androidVersion) {
        try {
            File manifest = new File(antProject.getBaseDir(), SdkConstants.FN_ANDROID_MANIFEST_XML);
            XPath xPath = AndroidXPathFactory.newXPath();
            String value = xPath.evaluate(
                    "/"  + AndroidManifest.NODE_MANIFEST +
                    "/@" + AndroidManifest.ATTRIBUTE_PACKAGE,
                    new InputSource(new FileInputStream(manifest)));
            if (value != null) { 
                if (value.indexOf('.') == -1) {
                    throw new BuildException(String.format(
                            "Application package '%1$s' must have a minimum of 2 segments.",
                            value));
                }
            }
            value = xPath.evaluate(
                    "/"  + AndroidManifest.NODE_MANIFEST +
                    "/"  + AndroidManifest.NODE_USES_SDK +
                    "/@" + AndroidXPathFactory.DEFAULT_NS_PREFIX + ":" +
                            AndroidManifest.ATTRIBUTE_MIN_SDK_VERSION,
                    new InputSource(new FileInputStream(manifest)));
            if (androidVersion.isPreview()) {
                String codeName = androidVersion.getCodename();
                if (codeName.equals(value) == false) {
                    throw new BuildException(String.format(
                            "For '%1$s' SDK Preview, attribute minSdkVersion in AndroidManifest.xml must be '%1$s'",
                            codeName));
                }
            } else if (value.length() > 0) {
                int minSdkValue = -1;
                try {
                    minSdkValue = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new BuildException(String.format(
                            "Attribute %1$s in AndroidManifest.xml must be an Integer!",
                            AndroidManifest.ATTRIBUTE_MIN_SDK_VERSION));
                }
                int projectApiLevel = androidVersion.getApiLevel();
                if (minSdkValue < projectApiLevel) {
                    System.out.println(String.format(
                            "WARNING: Attribute %1$s in AndroidManifest.xml (%2$d) is lower than the project target API level (%3$d)",
                            AndroidManifest.ATTRIBUTE_MIN_SDK_VERSION,
                            minSdkValue, projectApiLevel));
                } else if (minSdkValue > androidVersion.getApiLevel()) {
                    System.out.println(String.format(
                            "WARNING: Attribute %1$s in AndroidManifest.xml (%2$d) is higher than the project target API level (%3$d)",
                            AndroidManifest.ATTRIBUTE_MIN_SDK_VERSION,
                            minSdkValue, projectApiLevel));
                }
            } else {
                System.out.println(
                        "WARNING: No minSdkVersion value set. Application will install on all Android versions.");
            }
        } catch (XPathExpressionException e) {
            throw new BuildException(e);
        } catch (FileNotFoundException e) {
            throw new BuildException(e);
        }
    }
    private void processReferencedLibraries(Project antProject, IAndroidTarget androidTarget) {
        Path sourcePath = new Path(antProject);
        Path resPath = new Path(antProject);
        Path libsPath = new Path(antProject);
        Path jarsPath = new Path(antProject);
        StringBuilder sb = new StringBuilder();
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jar");
            }
        };
        boolean supportLibrary = androidTarget.getProperty(SdkConstants.PROP_SDK_SUPPORT_LIBRARY,
                false);
        int index = 1;
        while (true) {
            String propName = ProjectProperties.PROPERTY_LIB_REF + Integer.toString(index++);
            String rootPath = antProject.getProperty(propName);
            if (rootPath == null) {
                break;
            }
            if (supportLibrary == false) {
                throw new BuildException(String.format(
                        "The build system for this project target (%1$s) does not support libraries",
                        androidTarget.getFullName()));
            }
            PathElement element = sourcePath.createPathElement();
            ProjectProperties prop = ProjectProperties.load(rootPath, PropertyType.BUILD);
            String sourceDir = SdkConstants.FD_SOURCES;
            if (prop != null) {
                String value = prop.getProperty(ProjectProperties.PROPERTY_BUILD_SOURCE_DIR);
                if (value != null) {
                    sourceDir = value;
                }
            }
            element.setPath(rootPath + "/" + sourceDir);
            element = resPath.createPathElement();
            element.setPath(rootPath + "/" + SdkConstants.FD_RESOURCES);
            element = libsPath.createPathElement();
            element.setPath(rootPath + "/" + SdkConstants.FD_NATIVE_LIBS);
            File libsFolder = new File(rootPath, SdkConstants.FD_NATIVE_LIBS);
            File[] jarFiles = libsFolder.listFiles(filter);
            for (File jarFile : jarFiles) {
                element = jarsPath.createPathElement();
                element.setPath(jarFile.getAbsolutePath());
            }
            FileWrapper manifest = new FileWrapper(rootPath, SdkConstants.FN_ANDROID_MANIFEST_XML);
            try {
                String value = AndroidManifest.getPackage(manifest);
                if (value != null) { 
                    sb.append(';');
                    sb.append(value);
                }
            } catch (Exception e) {
                throw new BuildException(e);
            }
        }
        antProject.addReference("android.libraries.src", sourcePath);
        antProject.addReference("android.libraries.jars", jarsPath);
        antProject.addReference("android.libraries.libs", libsPath);
        if (sourcePath.list().length > 0) {
            antProject.addReference("android.libraries.res", resPath);
            antProject.setProperty("android.libraries.package", sb.toString());
        }
    }
    private int getToolsRevision(File sdkFile) {
        Properties p = new Properties();
        try{
            File toolsFolder= new File(sdkFile, SdkConstants.FD_TOOLS);
            File sourceProp = new File(toolsFolder, SdkConstants.FN_SOURCE_PROP);
            p.load(new FileInputStream(sourceProp));
            String value = p.getProperty("Pkg.Revision"); 
            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return -1;
    }
}
