public final class ApkBuilderImpl {
    private final static Pattern PATTERN_JAR_EXT = Pattern.compile("^.+\\.jar$",
            Pattern.CASE_INSENSITIVE);
    private final static Pattern PATTERN_NATIVELIB_EXT = Pattern.compile("^.+\\.so$",
            Pattern.CASE_INSENSITIVE);
    private final static String NATIVE_LIB_ROOT = "lib/";
    private final static String GDBSERVER_NAME = "gdbserver";
    public final static class ApkFile {
        String archivePath;
        File file;
        ApkFile(File file, String path) {
            this.file = file;
            this.archivePath = path;
        }
    }
    private JavaResourceFilter mResourceFilter = new JavaResourceFilter();
    private boolean mVerbose = false;
    private boolean mSignedPackage = true;
    private boolean mDebugMode = false;
    private String mStoreType = null;
    public void setVerbose(boolean verbose) {
        mVerbose = verbose;
    }
    public void setSignedPackage(boolean signedPackage) {
        mSignedPackage = signedPackage;
    }
    public void setDebugMode(boolean debugMode) {
        mDebugMode = debugMode;
    }
    public void run(String[] args) throws WrongOptionException, FileNotFoundException,
            ApkCreationException {
        if (args.length < 1) {
            throw new WrongOptionException("No options specified");
        }
        File outFile = getOutFile(args[0]);
        ArrayList<FileInputStream> zipArchives = new ArrayList<FileInputStream>();
        ArrayList<File> archiveFiles = new ArrayList<File>();
        ArrayList<ApkFile> javaResources = new ArrayList<ApkFile>();
        ArrayList<FileInputStream> resourcesJars = new ArrayList<FileInputStream>();
        ArrayList<ApkFile> nativeLibraries = new ArrayList<ApkFile>();
        int index = 1;
        do {
            String argument = args[index++];
            if ("-v".equals(argument)) {
                mVerbose = true;
            } else if ("-d".equals(argument)) {
                mDebugMode = true;
            } else if ("-u".equals(argument)) {
                mSignedPackage = false;
            } else if ("-z".equals(argument)) {
                if (index == args.length)  {
                    throw new WrongOptionException("Missing value for -z");
                }
                try {
                    FileInputStream input = new FileInputStream(args[index++]);
                    zipArchives.add(input);
                } catch (FileNotFoundException e) {
                    throw new ApkCreationException("-z file is not found");
                }
            } else if ("-f". equals(argument)) {
                if (index == args.length) {
                    throw new WrongOptionException("Missing value for -f");
                }
                archiveFiles.add(getInputFile(args[index++]));
            } else if ("-rf". equals(argument)) {
                if (index == args.length) {
                    throw new WrongOptionException("Missing value for -rf");
                }
                processSourceFolderForResource(new File(args[index++]), javaResources);
            } else if ("-rj". equals(argument)) {
                if (index == args.length) {
                    throw new WrongOptionException("Missing value for -rj");
                }
                processJar(new File(args[index++]), resourcesJars);
            } else if ("-nf".equals(argument)) {
                if (index == args.length) {
                    throw new WrongOptionException("Missing value for -nf");
                }
                processNativeFolder(new File(args[index++]), mDebugMode, nativeLibraries);
            } else if ("-storetype".equals(argument)) {
                if (index == args.length) {
                    throw new WrongOptionException("Missing value for -storetype");
                }
                mStoreType  = args[index++];
            } else {
                throw new WrongOptionException("Unknown argument: " + argument);
            }
        } while (index < args.length);
        createPackage(outFile, zipArchives, archiveFiles, javaResources, resourcesJars,
                nativeLibraries);
    }
    private File getOutFile(String filepath) throws ApkCreationException {
        File f = new File(filepath);
        if (f.isDirectory()) {
            throw new ApkCreationException(filepath + " is a directory!");
        }
        if (f.exists()) { 
            if (f.canWrite() == false) {
                throw new ApkCreationException("Cannot write " + filepath);
            }
        } else {
            try {
                if (f.createNewFile() == false) {
                    throw new ApkCreationException("Failed to create " + filepath);
                }
            } catch (IOException e) {
                throw new ApkCreationException(
                        "Failed to create '" + filepath + "' : " + e.getMessage());
            }
        }
        return f;
    }
    public static File getInputFile(String filepath) throws ApkCreationException {
        File f = new File(filepath);
        if (f.isDirectory()) {
            throw new ApkCreationException(filepath + " is a directory!");
        }
        if (f.exists()) {
            if (f.canRead() == false) {
                throw new ApkCreationException("Cannot read " + filepath);
            }
        } else {
            throw new ApkCreationException(filepath + " does not exists!");
        }
        return f;
    }
    public static void processSourceFolderForResource(File folder,
            ArrayList<ApkFile> javaResources) throws ApkCreationException {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            for (File file : files) {
                processFileForResource(file, null, javaResources);
            }
        } else {
            if (folder.exists()) {
                throw new ApkCreationException(folder.getAbsolutePath() + " is not a folder!");
            } else {
                throw new ApkCreationException(folder.getAbsolutePath() + " does not exist!");
            }
        }
    }
    public static void processJar(File file, Collection<FileInputStream> resourcesJars)
            throws FileNotFoundException {
        if (file.isDirectory()) {
            String[] filenames = file.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return PATTERN_JAR_EXT.matcher(name).matches();
                }
            });
            for (String filename : filenames) {
                File f = new File(file, filename);
                processJarFile(f, resourcesJars);
            }
        } else {
            processJarFile(file, resourcesJars);
        }
    }
    public static void processJarFile(File file, Collection<FileInputStream> resourcesJars)
            throws FileNotFoundException {
        FileInputStream input = new FileInputStream(file);
        resourcesJars.add(input);
    }
    private static void processFileForResource(File file, String path,
            Collection<ApkFile> javaResources) {
        if (file.isDirectory()) {
            if (JavaResourceFilter.checkFolderForPackaging(file.getName())) {
                if (path == null) {
                    path = file.getName();
                } else {
                    path = path + "/" + file.getName();
                }
                File[] files = file.listFiles();
                for (File contentFile : files) {
                    processFileForResource(contentFile, path, javaResources);
                }
            }
        } else {
            if (JavaResourceFilter.checkFileForPackaging(file.getName())) {
                if (path == null) {
                    path = file.getName();
                } else {
                    path = path + "/" + file.getName();
                }
                javaResources.add(new ApkFile(file, path));
            }
        }
    }
    public static void processNativeFolder(File root, boolean debugMode,
            Collection<ApkFile> nativeLibraries) throws ApkCreationException {
        if (root.isDirectory() == false) {
            throw new ApkCreationException(root.getAbsolutePath() + " is not a folder!");
        }
        File[] abiList = root.listFiles();
        if (abiList != null) {
            for (File abi : abiList) {
                if (abi.isDirectory()) { 
                    File[] libs = abi.listFiles();
                    if (libs != null) {
                        for (File lib : libs) {
                            if (lib.isFile() &&
                                    (PATTERN_NATIVELIB_EXT.matcher(lib.getName()).matches() ||
                                            (debugMode && GDBSERVER_NAME.equals(lib.getName())))) {
                                String path =
                                    NATIVE_LIB_ROOT + abi.getName() + "/" + lib.getName();
                                nativeLibraries.add(new ApkFile(lib, path));
                            }
                        }
                    }
                }
            }
        }
    }
    public void createPackage(File outFile, Iterable<? extends FileInputStream> zipArchives,
            Iterable<? extends File> files, Iterable<? extends ApkFile> javaResources,
            Iterable<? extends FileInputStream> resourcesJars,
            Iterable<? extends ApkFile> nativeLibraries) throws ApkCreationException {
        try {
            SignedJarBuilder builder;
            if (mSignedPackage) {
                System.err.println(String.format("Using keystore: %s",
                        DebugKeyProvider.getDefaultKeyStoreOsPath()));
                DebugKeyProvider keyProvider = new DebugKeyProvider(
                        null ,
                        mStoreType, null );
                PrivateKey key = keyProvider.getDebugKey();
                X509Certificate certificate = (X509Certificate)keyProvider.getCertificate();
                if (key == null) {
                    throw new ApkCreationException("Unable to get debug signature key");
                }
                if (certificate != null && certificate.getNotAfter().compareTo(new Date()) < 0) {
                    throw new ApkCreationException("Debug Certificate expired on " +
                            DateFormat.getInstance().format(certificate.getNotAfter()));
                }
                builder = new SignedJarBuilder(
                        new FileOutputStream(outFile.getAbsolutePath(), false ), key,
                        certificate);
            } else {
                builder = new SignedJarBuilder(
                        new FileOutputStream(outFile.getAbsolutePath(), false ),
                        null , null );
            }
            for (FileInputStream input : zipArchives) {
                builder.writeZip(input, null );
            }
            for (File input : files) {
                builder.writeFile(input, input.getName());
                if (mVerbose) {
                    System.err.println(String.format("%1$s => %2$s", input.getAbsolutePath(),
                            input.getName()));
                }
            }
            for (ApkFile resource : javaResources) {
                builder.writeFile(resource.file, resource.archivePath);
                if (mVerbose) {
                    System.err.println(String.format("%1$s => %2$s",
                            resource.file.getAbsolutePath(), resource.archivePath));
                }
            }
            for (FileInputStream input : resourcesJars) {
                builder.writeZip(input, mResourceFilter);
            }
            for (ApkFile file : nativeLibraries) {
                builder.writeFile(file.file, file.archivePath);
                if (mVerbose) {
                    System.err.println(String.format("%1$s => %2$s", file.file.getAbsolutePath(),
                            file.archivePath));
                }
            }
            builder.close();
        } catch (KeytoolException e) {
            if (e.getJavaHome() == null) {
                throw new ApkCreationException(e.getMessage() +
                        "\nJAVA_HOME seems undefined, setting it will help locating keytool automatically\n" +
                        "You can also manually execute the following command\n:" +
                        e.getCommandLine());
            } else {
                throw new ApkCreationException(e.getMessage() +
                        "\nJAVA_HOME is set to: " + e.getJavaHome() +
                        "\nUpdate it if necessary, or manually execute the following command:\n" +
                        e.getCommandLine());
            }
        } catch (AndroidLocationException e) {
            throw new ApkCreationException(e);
        } catch (Exception e) {
            throw new ApkCreationException(e);
        }
    }
}
