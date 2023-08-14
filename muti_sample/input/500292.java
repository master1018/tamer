public class SamplePackage extends MinToolsPackage
    implements IPackageVersion, IMinApiLevelDependency, IMinToolsDependency {
    private static final String PROP_MIN_API_LEVEL = "Sample.MinApiLevel";  
    private final AndroidVersion mVersion;
    private final int mMinApiLevel;
    SamplePackage(RepoSource source, Node packageNode, Map<String,String> licenses) {
        super(source, packageNode, licenses);
        int apiLevel = XmlParserUtils.getXmlInt   (packageNode, SdkRepository.NODE_API_LEVEL, 0);
        String codeName = XmlParserUtils.getXmlString(packageNode, SdkRepository.NODE_CODENAME);
        if (codeName.length() == 0) {
            codeName = null;
        }
        mVersion = new AndroidVersion(apiLevel, codeName);
        mMinApiLevel = XmlParserUtils.getXmlInt(packageNode, SdkRepository.NODE_MIN_API_LEVEL,
                MIN_API_LEVEL_NOT_SPECIFIED);
    }
    SamplePackage(IAndroidTarget target, Properties props) {
        super(  null,                                   
                props,                                  
                0,                                      
                null,                                   
                null,                                   
                null,                                   
                Os.ANY,                                 
                Arch.ANY,                               
                target.getPath(IAndroidTarget.SAMPLES)  
                );
        mVersion = target.getVersion();
        mMinApiLevel = Integer.parseInt(
            getProperty(props, PROP_MIN_API_LEVEL, Integer.toString(MIN_API_LEVEL_NOT_SPECIFIED)));
    }
    SamplePackage(String archiveOsPath, Properties props) throws AndroidVersionException {
        super(null,                                   
              props,                                  
              0,                                      
              null,                                   
              null,                                   
              null,                                   
              Os.ANY,                                 
              Arch.ANY,                               
              archiveOsPath                           
              );
        mVersion = new AndroidVersion(props);
        mMinApiLevel = Integer.parseInt(
            getProperty(props, PROP_MIN_API_LEVEL, Integer.toString(MIN_API_LEVEL_NOT_SPECIFIED)));
    }
    @Override
    void saveProperties(Properties props) {
        super.saveProperties(props);
        mVersion.saveProperties(props);
        if (getMinApiLevel() != MIN_API_LEVEL_NOT_SPECIFIED) {
            props.setProperty(PROP_MIN_API_LEVEL, Integer.toString(getMinApiLevel()));
        }
    }
    public int getMinApiLevel() {
        return mMinApiLevel;
    }
    public AndroidVersion getVersion() {
        return mVersion;
    }
    @Override
    public String getShortDescription() {
        String s = String.format("Samples for SDK API %1$s%2$s, revision %3$d%4$s",
                mVersion.getApiString(),
                mVersion.isPreview() ? " Preview" : "",
                getRevision(),
                isObsolete() ? " (Obsolete)" : "");
        return s;
    }
    @Override
    public String getLongDescription() {
        String s = getDescription();
        if (s == null || s.length() == 0) {
            s = getShortDescription();
        }
        if (s.indexOf("revision") == -1) {
            s += String.format("\nRevision %1$d%2$s",
                    getRevision(),
                    isObsolete() ? " (Obsolete)" : "");
        }
        return s;
    }
    @Override
    public File getInstallFolder(String osSdkRoot, String suggestedDir, SdkManager sdkManager) {
        File samplesRoot = new File(osSdkRoot, SdkConstants.FD_SAMPLES);
        for (IAndroidTarget target : sdkManager.getTargets()) {
            if (target.isPlatform() &&
                    target.getVersion().equals(mVersion)) {
                String p = target.getPath(IAndroidTarget.SAMPLES);
                File f = new File(p);
                if (f.isDirectory()) {
                    if (f.getParentFile().equals(samplesRoot)) {
                        return f;
                    }
                }
            }
        }
        File folder = new File(samplesRoot,
                String.format("android-%s", getVersion().getApiString())); 
        for (int n = 1; folder.exists(); n++) {
            folder = new File(samplesRoot,
                    String.format("android-%s_%d", getVersion().getApiString(), n)); 
        }
        return folder;
    }
    @Override
    public boolean sameItemAs(Package pkg) {
        if (pkg instanceof SamplePackage) {
            SamplePackage newPkg = (SamplePackage)pkg;
            return newPkg.getVersion().equals(this.getVersion());
        }
        return false;
    }
    @Override
    public boolean preInstallHook(Archive archive,
            ITaskMonitor monitor,
            String osSdkRoot,
            File installFolder) {
        File samplesRoot = new File(osSdkRoot, SdkConstants.FD_SAMPLES);
        if (!samplesRoot.isDirectory()) {
            samplesRoot.mkdir();
        }
        if (installFolder != null && installFolder.isDirectory()) {
            String storedHash = readContentHash(installFolder);
            if (storedHash != null && storedHash.length() > 0) {
                String currentHash = computeContentHash(installFolder);
                if (!storedHash.equals(currentHash)) {
                    String pkgName = archive.getParentPackage().getShortDescription();
                    String msg = String.format(
                            "-= Warning ! =-\n" +
                            "You are about to replace the content of the folder:\n " +
                            "  %1$s\n" +
                            "by the new package:\n" +
                            "  %2$s.\n" +
                            "\n" +
                            "However it seems that the content of the existing samples " +
                            "has been modified since it was last installed. Are you sure " +
                            "you want to DELETE the existing samples? This cannot be undone.\n" +
                            "Please select YES to delete the existing sample and replace them " +
                            "by the new ones.\n" +
                            "Please select NO to skip this package. You can always install it later.",
                            installFolder.getAbsolutePath(),
                            pkgName);
                    return monitor.displayPrompt("SDK Manager: overwrite samples?", msg);
                }
            }
        }
        return super.preInstallHook(archive, monitor, osSdkRoot, installFolder);
    }
    @Override
    public void postInstallHook(Archive archive, ITaskMonitor monitor, File installFolder) {
        super.postInstallHook(archive, monitor, installFolder);
        if (installFolder == null) {
            return;
        }
        String h = computeContentHash(installFolder);
        saveContentHash(installFolder, h);
    }
    private String readContentHash(File folder) {
        Properties props = new Properties();
        FileInputStream fis = null;
        try {
            File f = new File(folder, SdkConstants.FN_CONTENT_HASH_PROP);
            if (f.isFile()) {
                fis = new FileInputStream(f);
                props.load(fis);
                return props.getProperty("content-hash", null);  
            }
        } catch (Exception e) {
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
    private void saveContentHash(File folder, String hash) {
        Properties props = new Properties();
        props.setProperty("content-hash", hash == null ? "" : hash);  
        FileOutputStream fos = null;
        try {
            File f = new File(folder, SdkConstants.FN_CONTENT_HASH_PROP);
            fos = new FileOutputStream(f);
            props.store( fos, "## Android - hash of this archive.");  
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }
    private String computeContentHash(File installFolder) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");    
        } catch (NoSuchAlgorithmException e) {
        }
        if (md != null) {
            hashDirectoryContent(installFolder, md);
            return getDigestHexString(md);
        }
        return null;
    }
    private void hashDirectoryContent(File folder, MessageDigest md) {
        if (folder == null || md == null || !folder.isDirectory()) {
            return;
        }
        for (File f : folder.listFiles()) {
            if (f.isDirectory()) {
                hashDirectoryContent(f, md);
            } else {
                String name = f.getName();
                if (name == null || SdkConstants.FN_CONTENT_HASH_PROP.equals(name)) {
                    continue;
                }
                try {
                    md.update(name.getBytes("UTF-8"));   
                } catch (UnsupportedEncodingException e) {
                }
                try {
                    long len = f.length();
                    md.update((byte) (len & 0x0FF));
                    md.update((byte) ((len >> 8) & 0x0FF));
                    md.update((byte) ((len >> 16) & 0x0FF));
                    md.update((byte) ((len >> 24) & 0x0FF));
                } catch (SecurityException e) {
                }
            }
        }
    }
    private String getDigestHexString(MessageDigest digester) {
        byte[] digest = digester.digest();
        int n = digest.length;
        String hex = "0123456789abcdef";                     
        char[] hexDigest = new char[n * 2];
        for (int i = 0; i < n; i++) {
            int b = digest[i] & 0x0FF;
            hexDigest[i*2 + 0] = hex.charAt(b >>> 4);
            hexDigest[i*2 + 1] = hex.charAt(b & 0x0f);
        }
        return new String(hexDigest);
    }
}
