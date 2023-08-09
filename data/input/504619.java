public class Archive implements IDescription {
    public static final int NUM_MONITOR_INC = 100;
    private static final String PROP_OS   = "Archive.Os";       
    private static final String PROP_ARCH = "Archive.Arch";     
    public enum ChecksumType {
        SHA1("SHA-1");  
        private final String mAlgorithmName;
        private ChecksumType(String algorithmName) {
            mAlgorithmName = algorithmName;
        }
        public MessageDigest getMessageDigest() throws NoSuchAlgorithmException {
            return MessageDigest.getInstance(mAlgorithmName);
        }
    }
    public enum Os {
        ANY("Any"),
        LINUX("Linux"),
        MACOSX("MacOS X"),
        WINDOWS("Windows");
        private final String mUiName;
        private Os(String uiName) {
            mUiName = uiName;
        }
        public String getUiName() {
            return mUiName;
        }
        public String getXmlName() {
            return toString().toLowerCase();
        }
        public static Os getCurrentOs() {
            String os = System.getProperty("os.name");          
            if (os.startsWith("Mac")) {                         
                return Os.MACOSX;
            } else if (os.startsWith("Windows")) {              
                return Os.WINDOWS;
            } else if (os.startsWith("Linux")) {                
                return Os.LINUX;
            }
            return null;
        }
        public boolean isCompatible() {
            if (this == ANY) {
                return true;
            }
            Os os = getCurrentOs();
            return this == os;
        }
    }
    public enum Arch {
        ANY("Any"),
        PPC("PowerPC"),
        X86("x86"),
        X86_64("x86_64");
        private final String mUiName;
        private Arch(String uiName) {
            mUiName = uiName;
        }
        public String getUiName() {
            return mUiName;
        }
        public String getXmlName() {
            return toString().toLowerCase();
        }
        public static Arch getCurrentArch() {
            String arch = System.getProperty("os.arch");
            if (arch.equalsIgnoreCase("x86_64") || arch.equalsIgnoreCase("amd64")) {
                return Arch.X86_64;
            } else if (arch.equalsIgnoreCase("x86")
                    || arch.equalsIgnoreCase("i386")
                    || arch.equalsIgnoreCase("i686")) {
                return Arch.X86;
            } else if (arch.equalsIgnoreCase("ppc") || arch.equalsIgnoreCase("PowerPC")) {
                return Arch.PPC;
            }
            return null;
        }
        public boolean isCompatible() {
            if (this == ANY) {
                return true;
            }
            Arch arch = getCurrentArch();
            return this == arch;
        }
    }
    private final Os     mOs;
    private final Arch   mArch;
    private final String mUrl;
    private final long   mSize;
    private final String mChecksum;
    private final ChecksumType mChecksumType = ChecksumType.SHA1;
    private final Package mPackage;
    private final String mLocalOsPath;
    private final boolean mIsLocal;
    Archive(Package pkg, Os os, Arch arch, String url, long size, String checksum) {
        mPackage = pkg;
        mOs = os;
        mArch = arch;
        mUrl = url;
        mLocalOsPath = null;
        mSize = size;
        mChecksum = checksum;
        mIsLocal = false;
    }
    Archive(Package pkg, Properties props, Os os, Arch arch, String localOsPath) {
        mPackage = pkg;
        mOs   = props == null ? os   : Os.valueOf(  props.getProperty(PROP_OS,   os.toString()));
        mArch = props == null ? arch : Arch.valueOf(props.getProperty(PROP_ARCH, arch.toString()));
        mUrl = null;
        mLocalOsPath = localOsPath;
        mSize = 0;
        mChecksum = "";
        mIsLocal = true;
    }
    void saveProperties(Properties props) {
        props.setProperty(PROP_OS,   mOs.toString());
        props.setProperty(PROP_ARCH, mArch.toString());
    }
    public boolean isLocal() {
        return mIsLocal;
    }
    public Package getParentPackage() {
        return mPackage;
    }
    public long getSize() {
        return mSize;
    }
    public String getChecksum() {
        return mChecksum;
    }
    public ChecksumType getChecksumType() {
        return mChecksumType;
    }
    public String getUrl() {
        return mUrl;
    }
    public String getLocalOsPath() {
        return mLocalOsPath;
    }
    public Os getOs() {
        return mOs;
    }
    public Arch getArch() {
        return mArch;
    }
    public String getOsDescription() {
        String os;
        if (mOs == null) {
            os = "unknown OS";
        } else if (mOs == Os.ANY) {
            os = "any OS";
        } else {
            os = mOs.getUiName();
        }
        String arch = "";                               
        if (mArch != null && mArch != Arch.ANY) {
            arch = mArch.getUiName();
        }
        return String.format("%1$s%2$s%3$s",
                os,
                arch.length() > 0 ? " " : "",           
                arch);
    }
    public String getShortDescription() {
        return String.format("Archive for %1$s", getOsDescription());
    }
    public String getLongDescription() {
        return String.format("%1$s\nSize: %2$d MiB\nSHA1: %3$s",
                getShortDescription(),
                Math.round(getSize() / (1024*1024)),
                getChecksum());
    }
    public boolean isCompatible() {
        return getOs().isCompatible() && getArch().isCompatible();
    }
    public void deleteLocal() {
        if (isLocal()) {
            deleteFileOrFolder(new File(getLocalOsPath()));
        }
    }
    public boolean install(String osSdkRoot,
            boolean forceHttp,
            SdkManager sdkManager,
            ITaskMonitor monitor) {
        Package pkg = getParentPackage();
        File archiveFile = null;
        String name = pkg.getShortDescription();
        if (pkg instanceof ExtraPackage && !((ExtraPackage) pkg).isPathValid()) {
            monitor.setResult("Skipping %1$s: %2$s is not a valid install path.",
                    name,
                    ((ExtraPackage) pkg).getPath());
            return false;
        }
        if (isLocal()) {
            monitor.setResult("Skipping already installed archive: %1$s for %2$s",
                    name,
                    getOsDescription());
            return false;
        }
        if (!isCompatible()) {
            monitor.setResult("Skipping incompatible archive: %1$s for %2$s",
                    name,
                    getOsDescription());
            return false;
        }
        archiveFile = downloadFile(osSdkRoot, monitor, forceHttp);
        if (archiveFile != null) {
            if (unarchive(osSdkRoot, archiveFile, sdkManager, monitor)) {
                monitor.setResult("Installed %1$s", name);
                deleteFileOrFolder(archiveFile);
                return true;
            }
        }
        return false;
    }
    private File downloadFile(String osSdkRoot, ITaskMonitor monitor, boolean forceHttp) {
        String name = getParentPackage().getShortDescription();
        String desc = String.format("Downloading %1$s", name);
        monitor.setDescription(desc);
        monitor.setResult(desc);
        String link = getUrl();
        if (!link.startsWith("http:
                && !link.startsWith("https:
                && !link.startsWith("ftp:
            Package pkg = getParentPackage();
            RepoSource src = pkg.getParentSource();
            if (src == null) {
                monitor.setResult("Internal error: no source for archive %1$s", name);
                return null;
            }
            String repoXml = src.getUrl();
            int pos = repoXml.lastIndexOf('/');
            String base = repoXml.substring(0, pos + 1);
            link = base + link;
        }
        if (forceHttp) {
            link = link.replaceAll("https:
        }
        int pos = link.lastIndexOf('/');
        String base = link.substring(pos + 1);
        File tmpFolder = getTempFolder(osSdkRoot);
        if (!tmpFolder.isDirectory()) {
            if (tmpFolder.isFile()) {
                deleteFileOrFolder(tmpFolder);
            }
            if (!tmpFolder.mkdirs()) {
                monitor.setResult("Failed to create directory %1$s", tmpFolder.getPath());
                return null;
            }
        }
        File tmpFile = new File(tmpFolder, base);
        if (tmpFile.exists()) {
            if (tmpFile.length() == getSize() &&
                    fileChecksum(tmpFile, monitor).equalsIgnoreCase(getChecksum())) {
                return tmpFile;
            }
            deleteFileOrFolder(tmpFile);
        }
        if (fetchUrl(tmpFile, link, desc, monitor)) {
            return tmpFile;
        } else {
            deleteFileOrFolder(tmpFile);
            return null;
        }
    }
    private String fileChecksum(File tmpFile, ITaskMonitor monitor) {
        InputStream is = null;
        try {
            is = new FileInputStream(tmpFile);
            MessageDigest digester = getChecksumType().getMessageDigest();
            byte[] buf = new byte[65536];
            int n;
            while ((n = is.read(buf)) >= 0) {
                if (n > 0) {
                    digester.update(buf, 0, n);
                }
            }
            return getDigestChecksum(digester);
        } catch (FileNotFoundException e) {
            monitor.setResult("File not found: %1$s", e.getMessage());
        } catch (Exception e) {
            monitor.setResult(e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return "";  
    }
    private String getDigestChecksum(MessageDigest digester) {
        int n;
        byte[] digest = digester.digest();
        n = digest.length;
        String hex = "0123456789abcdef";                     
        char[] hexDigest = new char[n * 2];
        for (int i = 0; i < n; i++) {
            int b = digest[i] & 0x0FF;
            hexDigest[i*2 + 0] = hex.charAt(b >>> 4);
            hexDigest[i*2 + 1] = hex.charAt(b & 0x0f);
        }
        return new String(hexDigest);
    }
    private boolean fetchUrl(File tmpFile,
            String urlString,
            String description,
            ITaskMonitor monitor) {
        URL url;
        description += " (%1$d%%, %2$.0f KiB/s, %3$d %4$s left)";
        FileOutputStream os = null;
        InputStream is = null;
        try {
            url = new URL(urlString);
            is = url.openStream();
            os = new FileOutputStream(tmpFile);
            MessageDigest digester = getChecksumType().getMessageDigest();
            byte[] buf = new byte[65536];
            int n;
            long total = 0;
            long size = getSize();
            long inc = size / NUM_MONITOR_INC;
            long next_inc = inc;
            long startMs = System.currentTimeMillis();
            long nextMs = startMs + 2000;  
            while ((n = is.read(buf)) >= 0) {
                if (n > 0) {
                    os.write(buf, 0, n);
                    digester.update(buf, 0, n);
                }
                long timeMs = System.currentTimeMillis();
                total += n;
                if (total >= next_inc) {
                    monitor.incProgress(1);
                    next_inc += inc;
                }
                if (timeMs > nextMs) {
                    long delta = timeMs - startMs;
                    if (total > 0 && delta > 0) {
                        int percent = (int) (100 * total / size);
                        float speed = (float)total / (float)delta * (1000.f / 1024.f);
                        int timeLeft = (speed > 1e-3) ?
                                               (int)(((size - total) / 1024.0f) / speed) :
                                               0;
                        String timeUnit = "seconds";
                        if (timeLeft > 120) {
                            timeUnit = "minutes";
                            timeLeft /= 60;
                        }
                        monitor.setDescription(description, percent, speed, timeLeft, timeUnit);
                    }
                    nextMs = timeMs + 1000;  
                }
                if (monitor.isCancelRequested()) {
                    monitor.setResult("Download aborted by user at %1$d bytes.", total);
                    return false;
                }
            }
            if (total != size) {
                monitor.setResult("Download finished with wrong size. Expected %1$d bytes, got %2$d bytes.",
                        size, total);
                return false;
            }
            String actual   = getDigestChecksum(digester);
            String expected = getChecksum();
            if (!actual.equalsIgnoreCase(expected)) {
                monitor.setResult("Download finished with wrong checksum. Expected %1$s, got %2$s.",
                        expected, actual);
                return false;
            }
            return true;
        } catch (FileNotFoundException e) {
            monitor.setResult("File not found: %1$s", e.getMessage());
        } catch (Exception e) {
            monitor.setResult(e.getMessage());
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }
    private boolean unarchive(String osSdkRoot,
            File archiveFile,
            SdkManager sdkManager,
            ITaskMonitor monitor) {
        boolean success = false;
        Package pkg = getParentPackage();
        String pkgName = pkg.getShortDescription();
        String pkgDesc = String.format("Installing %1$s", pkgName);
        monitor.setDescription(pkgDesc);
        monitor.setResult(pkgDesc);
        String pkgKind = pkg.getClass().getSimpleName();
        File destFolder = null;
        File unzipDestFolder = null;
        File oldDestFolder = null;
        try {
            unzipDestFolder = createTempFolder(osSdkRoot, pkgKind, "new");  
            if (unzipDestFolder == null) {
                monitor.setResult("Failed to find a temp directory in %1$s.", osSdkRoot);
                return false;
            }
            if (!unzipDestFolder.mkdirs()) {
                monitor.setResult("Failed to create directory %1$s", unzipDestFolder.getPath());
                return false;
            }
            String[] zipRootFolder = new String[] { null };
            if (!unzipFolder(archiveFile, getSize(),
                    unzipDestFolder, pkgDesc,
                    zipRootFolder, monitor)) {
                return false;
            }
            if (!generateSourceProperties(unzipDestFolder)) {
                return false;
            }
            destFolder = pkg.getInstallFolder(osSdkRoot, zipRootFolder[0], sdkManager);
            if (destFolder == null) {
                monitor.setResult("Failed to compute installation directory for %1$s.", pkgName);
                return false;
            }
            if (!pkg.preInstallHook(this, monitor, osSdkRoot, destFolder)) {
                monitor.setResult("Skipping archive: %1$s", pkgName);
                return false;
            }
            boolean move1done = false;
            boolean move2done = false;
            while (!move1done || !move2done) {
                File renameFailedForDir = null;
                if (!move1done) {
                    if (destFolder.isDirectory()) {
                        if (oldDestFolder == null) {
                            oldDestFolder = createTempFolder(osSdkRoot, pkgKind, "old");  
                        }
                        if (oldDestFolder == null) {
                            monitor.setResult("Failed to find a temp directory in %1$s.", osSdkRoot);
                            return false;
                        }
                        if (!destFolder.renameTo(oldDestFolder)) {
                            monitor.setResult("Failed to rename directory %1$s to %2$s.",
                                    destFolder.getPath(), oldDestFolder.getPath());
                            renameFailedForDir = destFolder;
                        }
                    }
                    move1done = (renameFailedForDir == null);
                }
                if (move1done && !move2done) {
                    if (renameFailedForDir == null && !unzipDestFolder.renameTo(destFolder)) {
                        monitor.setResult("Failed to rename directory %1$s to %2$s",
                                unzipDestFolder.getPath(), destFolder.getPath());
                        renameFailedForDir = unzipDestFolder;
                    }
                    move2done = (renameFailedForDir == null);
                }
                if (renameFailedForDir != null) {
                    if (SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_WINDOWS) {
                        String msg = String.format(
                                "-= Warning ! =-\n" +
                                "A folder failed to be renamed or moved. On Windows this " +
                                "typically means that a program is using that folder (for example " +
                                "Windows Explorer or your anti-virus software.)\n" +
                                "Please momentarily deactivate your anti-virus software.\n" +
                                "Please also close any running programs that may be accessing " +
                                "the directory '%1$s'.\n" +
                                "When ready, press YES to try again.",
                                renameFailedForDir.getPath());
                        if (monitor.displayPrompt("SDK Manager: failed to install", msg)) {
                            continue;
                        }
                    }
                    return false;
                }
                break;
            }
            unzipDestFolder = null;
            success = true;
            pkg.postInstallHook(this, monitor, destFolder);
            return true;
        } finally {
            deleteFileOrFolder(oldDestFolder);
            deleteFileOrFolder(unzipDestFolder);
            if (!success) {
                pkg.postInstallHook(this, monitor, null );
            }
        }
    }
    @SuppressWarnings("unchecked")
    private boolean unzipFolder(File archiveFile,
            long compressedSize,
            File unzipDestFolder,
            String description,
            String[] outZipRootFolder,
            ITaskMonitor monitor) {
        description += " (%1$d%%)";
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(archiveFile);
            boolean usingUnixPerm = SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_DARWIN ||
                    SdkConstants.CURRENT_PLATFORM == SdkConstants.PLATFORM_LINUX;
            long incStep = compressedSize / NUM_MONITOR_INC;
            long incTotal = 0;
            long incCurr = 0;
            int lastPercent = 0;
            byte[] buf = new byte[65536];
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                String name = entry.getName();
                name = name.replace('\\', '/');
                int pos = name.indexOf('/');
                if (pos < 0 || pos == name.length() - 1) {
                    continue;
                } else {
                    if (outZipRootFolder[0] == null && pos > 0) {
                        outZipRootFolder[0] = name.substring(0, pos);
                    }
                    name = name.substring(pos + 1);
                }
                File destFile = new File(unzipDestFolder, name);
                if (name.endsWith("/")) {  
                    if (!destFile.isDirectory() && !destFile.mkdirs()) {
                        monitor.setResult("Failed to create temp directory %1$s",
                                destFile.getPath());
                        return false;
                    }
                    continue;
                } else if (name.indexOf('/') != -1) {
                    File parentDir = destFile.getParentFile();
                    if (!parentDir.isDirectory()) {
                        if (!parentDir.mkdirs()) {
                            monitor.setResult("Failed to create temp directory %1$s",
                                    parentDir.getPath());
                            return false;
                        }
                    }
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(destFile);
                    int n;
                    InputStream entryContent = zipFile.getInputStream(entry);
                    while ((n = entryContent.read(buf)) != -1) {
                        if (n > 0) {
                            fos.write(buf, 0, n);
                        }
                    }
                } finally {
                    if (fos != null) {
                        fos.close();
                    }
                }
                if (usingUnixPerm && destFile.isFile()) {
                    int mode = entry.getUnixMode();
                    if ((mode & 0111) != 0) {
                        setExecutablePermission(destFile);
                    }
                }
                for(incTotal += entry.getCompressedSize(); incCurr < incTotal; incCurr += incStep) {
                    monitor.incProgress(1);
                }
                int percent = (int) (100 * incTotal / compressedSize);
                if (percent != lastPercent) {
                    monitor.setDescription(description, percent);
                    lastPercent = percent;
                }
                if (monitor.isCancelRequested()) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            monitor.setResult("Unzip failed: %1$s", e.getMessage());
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {
                }
            }
        }
        return false;
    }
    private File createTempFolder(String osBasePath, String prefix, String suffix) {
        File baseTempFolder = getTempFolder(osBasePath);
        if (!baseTempFolder.isDirectory()) {
            if (baseTempFolder.isFile()) {
                deleteFileOrFolder(baseTempFolder);
            }
            if (!baseTempFolder.mkdirs()) {
                return null;
            }
        }
        for (int i = 1; i < 100; i++) {
            File folder = new File(baseTempFolder,
                    String.format("%1$s.%2$s%3$02d", prefix, suffix, i));  
            if (!folder.exists()) {
                return folder;
            }
        }
        return null;
    }
    private File getTempFolder(String osBasePath) {
        File baseTempFolder = new File(osBasePath, "temp");     
        return baseTempFolder;
    }
    private void deleteFileOrFolder(File fileOrFolder) {
        if (fileOrFolder != null) {
            if (fileOrFolder.isDirectory()) {
                for (File item : fileOrFolder.listFiles()) {
                    deleteFileOrFolder(item);
                }
            }
            if (!fileOrFolder.delete()) {
                fileOrFolder.deleteOnExit();
            }
        }
    }
    private boolean generateSourceProperties(File unzipDestFolder) {
        Properties props = new Properties();
        saveProperties(props);
        mPackage.saveProperties(props);
        FileOutputStream fos = null;
        try {
            File f = new File(unzipDestFolder, SdkConstants.FN_SOURCE_PROP);
            fos = new FileOutputStream(f);
            props.store( fos, "## Android Tool: Source of this archive.");  
            return true;
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
        return false;
    }
    private void setExecutablePermission(File file) throws IOException {
        Runtime.getRuntime().exec(new String[] {
           "chmod", "777", file.getAbsolutePath()
        });
    }
}
