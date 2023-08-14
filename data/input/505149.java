public final class AvdManager {
    private final static class InvalidTargetPathException extends Exception {
        private static final long serialVersionUID = 1L;
        InvalidTargetPathException(String message) {
            super(message);
        }
    }
    public static final String AVD_FOLDER_EXTENSION = ".avd";  
    public final static String AVD_INFO_PATH = "path";         
    public final static String AVD_INFO_TARGET = "target";     
    public final static String AVD_INI_SKIN_PATH = "skin.path"; 
    public final static String AVD_INI_SKIN_NAME = "skin.name"; 
    public final static String AVD_INI_SDCARD_PATH = "sdcard.path"; 
    public final static String AVD_INI_SDCARD_SIZE = "sdcard.size"; 
    public final static String AVD_INI_IMAGES_1 = "image.sysdir.1"; 
    public final static String AVD_INI_IMAGES_2 = "image.sysdir.2"; 
    public final static Pattern NUMERIC_SKIN_SIZE = Pattern.compile("([0-9]{2,})x([0-9]{2,})"); 
    private final static String USERDATA_IMG = "userdata.img"; 
    private final static String CONFIG_INI = "config.ini"; 
    private final static String SDCARD_IMG = "sdcard.img"; 
    private final static String INI_EXTENSION = ".ini"; 
    private final static Pattern INI_NAME_PATTERN = Pattern.compile("(.+)\\" + 
            INI_EXTENSION + "$",                                               
            Pattern.CASE_INSENSITIVE);
    private final static Pattern IMAGE_NAME_PATTERN = Pattern.compile("(.+)\\.img$", 
            Pattern.CASE_INSENSITIVE);
    public final static Pattern SDCARD_SIZE_PATTERN = Pattern.compile("(\\d+)([MK])"); 
    public final static Pattern RE_AVD_NAME = Pattern.compile("[a-zA-Z0-9._-]+"); 
    public final static String CHARS_AVD_NAME = "a-z A-Z 0-9 . _ -"; 
    public final static String HARDWARE_INI = "hardware.ini"; 
    public static final class AvdInfo implements Comparable<AvdInfo> {
        public static enum AvdStatus {
            OK,
            ERROR_PATH,
            ERROR_CONFIG,
            ERROR_TARGET_HASH,
            ERROR_TARGET,
            ERROR_PROPERTIES,
            ERROR_IMAGE_DIR;
        }
        private final String mName;
        private final String mPath;
        private final String mTargetHash;
        private final IAndroidTarget mTarget;
        private final Map<String, String> mProperties;
        private final AvdStatus mStatus;
        public AvdInfo(String name, String path, String targetHash, IAndroidTarget target,
                Map<String, String> properties) {
            this(name, path, targetHash, target, properties, AvdStatus.OK);
        }
        public AvdInfo(String name, String path, String targetHash, IAndroidTarget target,
                Map<String, String> properties, AvdStatus status) {
            mName = name;
            mPath = path;
            mTargetHash = targetHash;
            mTarget = target;
            mProperties = properties == null ? null : Collections.unmodifiableMap(properties);
            mStatus = status;
        }
        public String getName() {
            return mName;
        }
        public String getPath() {
            return mPath;
        }
        public String getTargetHash() {
            return mTargetHash;
        }
        public IAndroidTarget getTarget() {
            return mTarget;
        }
        public AvdStatus getStatus() {
            return mStatus;
        }
        public static File getIniFile(String name) throws AndroidLocationException {
            String avdRoot;
            avdRoot = getBaseAvdFolder();
            return new File(avdRoot, name + INI_EXTENSION);
        }
        public File getIniFile() throws AndroidLocationException {
            return getIniFile(mName);
        }
        public static File getConfigFile(String path) {
            return new File(path, CONFIG_INI);
        }
        public File getConfigFile() {
            return getConfigFile(mPath);
        }
        public Map<String, String> getProperties() {
            return mProperties;
        }
        public String getErrorMessage() {
            try {
                switch (mStatus) {
                    case ERROR_PATH:
                        return String.format("Missing AVD 'path' property in %1$s", getIniFile());
                    case ERROR_CONFIG:
                        return String.format("Missing config.ini file in %1$s", mPath);
                    case ERROR_TARGET_HASH:
                        return String.format("Missing 'target' property in %1$s", getIniFile());
                    case ERROR_TARGET:
                        return String.format("Unknown target '%1$s' in %2$s",
                                mTargetHash, getIniFile());
                    case ERROR_PROPERTIES:
                        return String.format("Failed to parse properties from %1$s",
                                getConfigFile());
                    case ERROR_IMAGE_DIR:
                        return String.format(
                                "Invalid value in image.sysdir. Run 'android update avd -n %1$s'",
                                mName);
                    case OK:
                        assert false;
                        return null;
                }
            } catch (AndroidLocationException e) {
                return "Unable to get HOME folder.";
            }
            return null;
        }
        public boolean isRunning() {
            File f = new File(mPath, "userdata-qemu.img.lock");
            return f.isFile();
        }
        public int compareTo(AvdInfo o) {
            if (mTarget == null) {
                return +1;
            } else if (o.mTarget == null) {
                return -1;
            }
            int targetDiff = mTarget.compareTo(o.mTarget);
            if (targetDiff == 0) {
                return mName.compareTo(o.mName);
            }
            return targetDiff;
        }
    }
    private final ArrayList<AvdInfo> mAllAvdList = new ArrayList<AvdInfo>();
    private AvdInfo[] mValidAvdList;
    private AvdInfo[] mBrokenAvdList;
    private final SdkManager mSdkManager;
    public static String getBaseAvdFolder() throws AndroidLocationException {
        return AndroidLocation.getFolder() + AndroidLocation.FOLDER_AVD;
    }
    public AvdManager(SdkManager sdkManager, ISdkLog log) throws AndroidLocationException {
        mSdkManager = sdkManager;
        buildAvdList(mAllAvdList, log);
    }
    public SdkManager getSdkManager() {
        return mSdkManager;
    }
    public AvdInfo[] getAllAvds() {
        synchronized (mAllAvdList) {
            return mAllAvdList.toArray(new AvdInfo[mAllAvdList.size()]);
        }
    }
    public AvdInfo[] getValidAvds() {
        synchronized (mAllAvdList) {
            if (mValidAvdList == null) {
                ArrayList<AvdInfo> list = new ArrayList<AvdInfo>();
                for (AvdInfo avd : mAllAvdList) {
                    if (avd.getStatus() == AvdStatus.OK) {
                        list.add(avd);
                    }
                }
                mValidAvdList = list.toArray(new AvdInfo[list.size()]);
            }
            return mValidAvdList;
        }
    }
    public AvdInfo[] getBrokenAvds() {
        synchronized (mAllAvdList) {
            if (mBrokenAvdList == null) {
                ArrayList<AvdInfo> list = new ArrayList<AvdInfo>();
                for (AvdInfo avd : mAllAvdList) {
                    if (avd.getStatus() != AvdStatus.OK) {
                        list.add(avd);
                    }
                }
                mBrokenAvdList = list.toArray(new AvdInfo[list.size()]);
            }
            return mBrokenAvdList;
        }
    }
    public AvdInfo getAvd(String name, boolean validAvdOnly) {
        boolean ignoreCase = SdkConstants.currentPlatform() == SdkConstants.PLATFORM_WINDOWS;
        if (validAvdOnly) {
            for (AvdInfo info : getValidAvds()) {
                String name2 = info.getName();
                if (name2.equals(name) || (ignoreCase && name2.equalsIgnoreCase(name))) {
                    return info;
                }
            }
        } else {
            synchronized (mAllAvdList) {
                for (AvdInfo info : mAllAvdList) {
                    String name2 = info.getName();
                    if (name2.equals(name) || (ignoreCase && name2.equalsIgnoreCase(name))) {
                        return info;
                    }
                }
            }
        }
        return null;
    }
    public void reloadAvds(ISdkLog log) throws AndroidLocationException {
        ArrayList<AvdInfo> allList = new ArrayList<AvdInfo>();
        buildAvdList(allList, log);
        synchronized (mAllAvdList) {
            mAllAvdList.clear();
            mAllAvdList.addAll(allList);
            mValidAvdList = mBrokenAvdList = null;
        }
    }
    public AvdInfo createAvd(File avdFolder, String name, IAndroidTarget target,
            String skinName, String sdcard, Map<String,String> hardwareConfig,
            boolean removePrevious, ISdkLog log) {
        if (log == null) {
            throw new IllegalArgumentException("log cannot be null");
        }
        File iniFile = null;
        boolean needCleanup = false;
        try {
            if (avdFolder.exists()) {
                if (removePrevious) {
                    try {
                        deleteContentOf(avdFolder);
                    } catch (SecurityException e) {
                        log.error(e, "Failed to delete %1$s", avdFolder.getAbsolutePath());
                    }
                } else {
                    log.error(null,
                            "Folder %1$s is in the way. Use --force if you want to overwrite.",
                            avdFolder.getAbsolutePath());
                    return null;
                }
            } else {
                avdFolder.mkdir();
            }
            iniFile = createAvdIniFile(name, avdFolder, target);
            String imagePath = target.getPath(IAndroidTarget.IMAGES);
            File userdataSrc = new File(imagePath, USERDATA_IMG);
            if (userdataSrc.exists() == false && target.isPlatform() == false) {
                imagePath = target.getParent().getPath(IAndroidTarget.IMAGES);
                userdataSrc = new File(imagePath, USERDATA_IMG);
            }
            if (userdataSrc.exists() == false) {
                log.error(null, "Unable to find a '%1$s' file to copy into the AVD folder.",
                        USERDATA_IMG);
                needCleanup = true;
                return null;
            }
            FileInputStream fis = new FileInputStream(userdataSrc);
            File userdataDest = new File(avdFolder, USERDATA_IMG);
            FileOutputStream fos = new FileOutputStream(userdataDest);
            byte[] buffer = new byte[4096];
            int count;
            while ((count = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, count);
            }
            fos.close();
            fis.close();
            HashMap<String, String> values = new HashMap<String, String>();
            if (setImagePathProperties(target, values, log) == false) {
                needCleanup = true;
                return null;
            }
            if (skinName == null || skinName.length() == 0) {
                skinName = target.getDefaultSkin();
            }
            if (NUMERIC_SKIN_SIZE.matcher(skinName).matches()) {
                values.put(AVD_INI_SKIN_NAME, skinName);
                values.put(AVD_INI_SKIN_PATH, skinName);
            } else {
                String skinPath = getSkinRelativePath(skinName, target, log);
                if (skinPath == null) {
                    needCleanup = true;
                    return null;
                }
                values.put(AVD_INI_SKIN_PATH, skinPath);
                values.put(AVD_INI_SKIN_NAME, skinName);
            }
            if (sdcard != null && sdcard.length() > 0) {
                File sdcardFile = new File(sdcard);
                if (sdcardFile.isFile()) {
                    values.put(AVD_INI_SDCARD_PATH, sdcard);
                } else {
                    Matcher m = SDCARD_SIZE_PATTERN.matcher(sdcard);
                    if (m.matches()) {
                        int sdcardSize = Integer.parseInt(m.group(1)); 
                        String sdcardSizeModifier = m.group(2);
                        if ("K".equals(sdcardSizeModifier)) {
                            sdcardSize *= 1024;
                        } else { 
                            sdcardSize *= 1024 * 1024;
                        }
                        if (sdcardSize < 9 * 1024 * 1024) {
                            log.error(null, "SD Card size must be at least 9MB");
                            needCleanup = true;
                            return null;
                        }
                        sdcardFile = new File(avdFolder, SDCARD_IMG);
                        String path = sdcardFile.getAbsolutePath();
                        File toolsFolder = new File(mSdkManager.getLocation(),
                                SdkConstants.FD_TOOLS);
                        File mkSdCard = new File(toolsFolder, SdkConstants.mkSdCardCmdName());
                        if (mkSdCard.isFile() == false) {
                            log.error(null, "'%1$s' is missing from the SDK tools folder.",
                                    mkSdCard.getName());
                            needCleanup = true;
                            return null;
                        }
                        if (createSdCard(mkSdCard.getAbsolutePath(), sdcard, path, log) == false) {
                            needCleanup = true;
                            return null; 
                        }
                        values.put(AVD_INI_SDCARD_SIZE, sdcard);
                    } else {
                        log.error(null, "'%1$s' is not recognized as a valid sdcard value.\n"
                                + "Value should be:\n" + "1. path to an sdcard.\n"
                                + "2. size of the sdcard to create: <size>[K|M]", sdcard);
                        needCleanup = true;
                        return null;
                    }
                }
            }
            HashMap<String, String> finalHardwareValues = new HashMap<String, String>();
            File targetHardwareFile = new File(target.getLocation(), AvdManager.HARDWARE_INI);
            if (targetHardwareFile.isFile()) {
                Map<String, String> targetHardwareConfig = SdkManager.parsePropertyFile(
                        targetHardwareFile, log);
                if (targetHardwareConfig != null) {
                    finalHardwareValues.putAll(targetHardwareConfig);
                    values.putAll(targetHardwareConfig);
                }
            }
            File skinFolder = getSkinPath(skinName, target);
            File skinHardwareFile = new File(skinFolder, AvdManager.HARDWARE_INI);
            if (skinHardwareFile.isFile()) {
                Map<String, String> skinHardwareConfig = SdkManager.parsePropertyFile(
                        skinHardwareFile, log);
                if (skinHardwareConfig != null) {
                    finalHardwareValues.putAll(skinHardwareConfig);
                    values.putAll(skinHardwareConfig);
                }
            }
            if (hardwareConfig != null) {
                finalHardwareValues.putAll(hardwareConfig);
                values.putAll(hardwareConfig);
            }
            File configIniFile = new File(avdFolder, CONFIG_INI);
            writeIniFile(configIniFile, values);
            StringBuilder report = new StringBuilder();
            if (target.isPlatform()) {
                report.append(String.format("Created AVD '%1$s' based on %2$s",
                        name, target.getName()));
            } else {
                report.append(String.format("Created AVD '%1$s' based on %2$s (%3$s)", name,
                        target.getName(), target.getVendor()));
            }
            if (finalHardwareValues.size() > 0) {
                report.append(",\nwith the following hardware config:\n");
                for (Entry<String, String> entry : finalHardwareValues.entrySet()) {
                    report.append(String.format("%s=%s\n",entry.getKey(), entry.getValue()));
                }
            } else {
                report.append("\n");
            }
            log.printf(report.toString());
            AvdInfo newAvdInfo = new AvdInfo(name,
                    avdFolder.getAbsolutePath(),
                    target.hashString(),
                    target, values);
            AvdInfo oldAvdInfo = getAvd(name, false );
            synchronized (mAllAvdList) {
                if (oldAvdInfo != null && removePrevious) {
                    mAllAvdList.remove(oldAvdInfo);
                }
                mAllAvdList.add(newAvdInfo);
                mValidAvdList = mBrokenAvdList = null;
            }
            if (removePrevious &&
                    newAvdInfo != null &&
                    oldAvdInfo != null &&
                    !oldAvdInfo.getPath().equals(newAvdInfo.getPath())) {
                log.warning("Removing previous AVD directory at %s", oldAvdInfo.getPath());
                File dir = new File(oldAvdInfo.getPath());
                try {
                    deleteContentOf(dir);
                    dir.delete();
                } catch (SecurityException e) {
                    log.error(e, "Failed to delete %1$s", dir.getAbsolutePath());
                }
            }
            return newAvdInfo;
        } catch (AndroidLocationException e) {
            log.error(e, null);
        } catch (IOException e) {
            log.error(e, null);
        } catch (SecurityException e) {
            log.error(e, null);
        } finally {
            if (needCleanup) {
                if (iniFile != null && iniFile.exists()) {
                    iniFile.delete();
                }
                try {
                    deleteContentOf(avdFolder);
                    avdFolder.delete();
                } catch (SecurityException e) {
                    log.error(e, "Failed to delete %1$s", avdFolder.getAbsolutePath());
                }
            }
        }
        return null;
    }
    private String getImageRelativePath(IAndroidTarget target)
            throws InvalidTargetPathException {
        String imageFullPath = target.getPath(IAndroidTarget.IMAGES);
        String sdkLocation = mSdkManager.getLocation();
        if (imageFullPath.startsWith(sdkLocation) == false) {
            assert false;
            throw new InvalidTargetPathException("Target location is not inside the SDK.");
        }
        File folder = new File(imageFullPath);
        if (folder.isDirectory()) {
            String[] list = folder.list(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return IMAGE_NAME_PATTERN.matcher(name).matches();
                }
            });
            if (list.length > 0) {
                imageFullPath = imageFullPath.substring(sdkLocation.length());
                if (imageFullPath.charAt(0) == File.separatorChar) {
                    imageFullPath = imageFullPath.substring(1);
                }
                return imageFullPath;
            }
        }
        return null;
    }
    public String getSkinRelativePath(String skinName, IAndroidTarget target, ISdkLog log) {
        if (log == null) {
            throw new IllegalArgumentException("log cannot be null");
        }
        File skin = getSkinPath(skinName, target);
        if (skin.exists() == false) {
            log.error(null, "Skin '%1$s' does not exist.", skinName);
            return null;
        }
        String path = skin.getAbsolutePath();
        String sdkLocation = mSdkManager.getLocation();
        if (path.startsWith(sdkLocation) == false) {
            log.error(null, "Target location is not inside the SDK.");
            assert false;
            return null;
        }
        path = path.substring(sdkLocation.length());
        if (path.charAt(0) == File.separatorChar) {
            path = path.substring(1);
        }
        return path;
    }
    public File getSkinPath(String skinName, IAndroidTarget target) {
        String path = target.getPath(IAndroidTarget.SKINS);
        File skin = new File(path, skinName);
        if (skin.exists() == false && target.isPlatform() == false) {
            target = target.getParent();
            path = target.getPath(IAndroidTarget.SKINS);
            skin = new File(path, skinName);
        }
        return skin;
    }
    private File createAvdIniFile(String name, File avdFolder, IAndroidTarget target)
            throws AndroidLocationException, IOException {
        HashMap<String, String> values = new HashMap<String, String>();
        File iniFile = AvdInfo.getIniFile(name);
        values.put(AVD_INFO_PATH, avdFolder.getAbsolutePath());
        values.put(AVD_INFO_TARGET, target.hashString());
        writeIniFile(iniFile, values);
        return iniFile;
    }
    private File createAvdIniFile(AvdInfo info) throws AndroidLocationException, IOException {
        return createAvdIniFile(info.getName(), new File(info.getPath()), info.getTarget());
    }
    public boolean deleteAvd(AvdInfo avdInfo, ISdkLog log) {
        try {
            boolean error = false;
            File f = avdInfo.getIniFile();
            if (f != null && f.exists()) {
                log.printf("Deleting file %1$s\n", f.getCanonicalPath());
                if (!f.delete()) {
                    log.error(null, "Failed to delete %1$s\n", f.getCanonicalPath());
                    error = true;
                }
            }
            String path = avdInfo.getPath();
            if (path != null) {
                f = new File(path);
                if (f.exists()) {
                    log.printf("Deleting folder %1$s\n", f.getCanonicalPath());
                    if (deleteContentOf(f) == false || f.delete() == false) {
                        log.error(null, "Failed to delete %1$s\n", f.getCanonicalPath());
                        error = true;
                    }
                }
            }
            removeAvd(avdInfo);
            if (error) {
                log.printf("\nAVD '%1$s' deleted with errors. See errors above.\n",
                        avdInfo.getName());
            } else {
                log.printf("\nAVD '%1$s' deleted.\n", avdInfo.getName());
                return true;
            }
        } catch (AndroidLocationException e) {
            log.error(e, null);
        } catch (IOException e) {
            log.error(e, null);
        } catch (SecurityException e) {
            log.error(e, null);
        }
        return false;
    }
    public boolean moveAvd(AvdInfo avdInfo, String newName, String paramFolderPath, ISdkLog log) {
        try {
            if (paramFolderPath != null) {
                File f = new File(avdInfo.getPath());
                log.warning("Moving '%1$s' to '%2$s'.", avdInfo.getPath(), paramFolderPath);
                if (!f.renameTo(new File(paramFolderPath))) {
                    log.error(null, "Failed to move '%1$s' to '%2$s'.",
                            avdInfo.getPath(), paramFolderPath);
                    return false;
                }
                AvdInfo info = new AvdInfo(avdInfo.getName(), paramFolderPath,
                        avdInfo.getTargetHash(), avdInfo.getTarget(), avdInfo.getProperties());
                replaceAvd(avdInfo, info);
                createAvdIniFile(info);
            }
            if (newName != null) {
                File oldIniFile = avdInfo.getIniFile();
                File newIniFile = AvdInfo.getIniFile(newName);
                log.warning("Moving '%1$s' to '%2$s'.", oldIniFile.getPath(), newIniFile.getPath());
                if (!oldIniFile.renameTo(newIniFile)) {
                    log.error(null, "Failed to move '%1$s' to '%2$s'.",
                            oldIniFile.getPath(), newIniFile.getPath());
                    return false;
                }
                AvdInfo info = new AvdInfo(newName, avdInfo.getPath(),
                        avdInfo.getTargetHash(), avdInfo.getTarget(), avdInfo.getProperties());
                replaceAvd(avdInfo, info);
            }
            log.printf("AVD '%1$s' moved.\n", avdInfo.getName());
        } catch (AndroidLocationException e) {
            log.error(e, null);
        } catch (IOException e) {
            log.error(e, null);
        }
        return true;
    }
    private boolean deleteContentOf(File folder) throws SecurityException {
        for (File f : folder.listFiles()) {
            if (f.isDirectory()) {
                if (deleteContentOf(f) == false) {
                    return false;
                }
            }
            if (f.delete() == false) {
                return false;
            }
        }
        return true;
    }
    private File[] buildAvdFilesList() throws AndroidLocationException {
        String avdRoot = AvdManager.getBaseAvdFolder();
        File folder = new File(avdRoot);
        if (folder.isFile()) {
            throw new AndroidLocationException(
                    String.format("%1$s is not a valid folder.", avdRoot));
        } else if (folder.exists() == false) {
            folder.mkdirs();
            return null;
        }
        File[] avds = folder.listFiles(new FilenameFilter() {
            public boolean accept(File parent, String name) {
                if (INI_NAME_PATTERN.matcher(name).matches()) {
                    boolean isFile = new File(parent, name).isFile();
                    return isFile;
                }
                return false;
            }
        });
        return avds;
    }
    private void buildAvdList(ArrayList<AvdInfo> allList, ISdkLog log)
            throws AndroidLocationException {
        File[] avds = buildAvdFilesList();
        if (avds != null) {
            for (File avd : avds) {
                AvdInfo info = parseAvdInfo(avd, log);
                if (info != null) {
                    allList.add(info);
                }
            }
        }
    }
    private AvdInfo parseAvdInfo(File path, ISdkLog log) {
        Map<String, String> map = SdkManager.parsePropertyFile(path, log);
        String avdPath = map.get(AVD_INFO_PATH);
        String targetHash = map.get(AVD_INFO_TARGET);
        IAndroidTarget target = null;
        File configIniFile = null;
        Map<String, String> properties = null;
        if (targetHash != null) {
            target = mSdkManager.getTargetFromHashString(targetHash);
        }
        if (avdPath != null) {
            configIniFile = new File(avdPath, CONFIG_INI);
        }
        if (configIniFile != null) {
            if (!configIniFile.isFile()) {
                log.warning("Missing file '%1$s'.",  configIniFile.getPath());
            } else {
                properties = SdkManager.parsePropertyFile(configIniFile, log);
            }
        }
        String name = path.getName();
        Matcher matcher = INI_NAME_PATTERN.matcher(path.getName());
        if (matcher.matches()) {
            name = matcher.group(1);
        }
        boolean validImageSysdir = true;
        if (properties != null) {
            String imageSysDir = properties.get(AVD_INI_IMAGES_1);
            if (imageSysDir != null) {
                File f = new File(mSdkManager.getLocation() + File.separator + imageSysDir);
                if (f.isDirectory() == false) {
                    validImageSysdir = false;
                } else {
                    imageSysDir = properties.get(AVD_INI_IMAGES_2);
                    if (imageSysDir != null) {
                        f = new File(mSdkManager.getLocation() + File.separator + imageSysDir);
                        if (f.isDirectory() == false) {
                            validImageSysdir = false;
                        }
                    }
                }
            }
        }
        AvdStatus status;
        if (avdPath == null) {
            status = AvdStatus.ERROR_PATH;
        } else if (configIniFile == null) {
            status = AvdStatus.ERROR_CONFIG;
        } else if (targetHash == null) {
            status = AvdStatus.ERROR_TARGET_HASH;
        } else if (target == null) {
            status = AvdStatus.ERROR_TARGET;
        } else if (properties == null) {
            status = AvdStatus.ERROR_PROPERTIES;
        } else if (validImageSysdir == false) {
            status = AvdStatus.ERROR_IMAGE_DIR;
        } else {
            status = AvdStatus.OK;
        }
        AvdInfo info = new AvdInfo(
                name,
                avdPath,
                targetHash,
                target,
                properties,
                status);
        return info;
    }
    private static void writeIniFile(File iniFile, Map<String, String> values)
            throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(iniFile),
                SdkConstants.INI_CHARSET);
        for (Entry<String, String> entry : values.entrySet()) {
            writer.write(String.format("%1$s=%2$s\n", entry.getKey(), entry.getValue()));
        }
        writer.close();
    }
    private boolean createSdCard(String toolLocation, String size, String location, ISdkLog log) {
        try {
            String[] command = new String[3];
            command[0] = toolLocation;
            command[1] = size;
            command[2] = location;
            Process process = Runtime.getRuntime().exec(command);
            ArrayList<String> errorOutput = new ArrayList<String>();
            ArrayList<String> stdOutput = new ArrayList<String>();
            int status = grabProcessOutput(process, errorOutput, stdOutput,
                    true );
            if (status == 0) {
                return true;
            } else {
                for (String error : errorOutput) {
                    log.error(null, error);
                }
            }
        } catch (InterruptedException e) {
        } catch (IOException e) {
        }
        log.error(null, "Failed to create the SD card.");
        return false;
    }
    private int grabProcessOutput(final Process process, final ArrayList<String> errorOutput,
            final ArrayList<String> stdOutput, boolean waitforReaders)
            throws InterruptedException {
        assert errorOutput != null;
        assert stdOutput != null;
        Thread t1 = new Thread("") { 
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getErrorStream());
                BufferedReader errReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = errReader.readLine();
                        if (line != null) {
                            errorOutput.add(line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
            }
        };
        Thread t2 = new Thread("") { 
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getInputStream());
                BufferedReader outReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = outReader.readLine();
                        if (line != null) {
                            stdOutput.add(line);
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
            }
        };
        t1.start();
        t2.start();
        if (waitforReaders) {
            try {
                t1.join();
            } catch (InterruptedException e) {
            }
            try {
                t2.join();
            } catch (InterruptedException e) {
            }
        }
        return process.waitFor();
    }
    public boolean removeAvd(AvdInfo avdInfo) {
        synchronized (mAllAvdList) {
            if (mAllAvdList.remove(avdInfo)) {
                mValidAvdList = mBrokenAvdList = null;
                return true;
            }
        }
        return false;
    }
    public void updateAvd(String name, ISdkLog log) throws IOException {
        AvdInfo avd = null;
        synchronized (mAllAvdList) {
            for (AvdInfo info : mAllAvdList) {
                if (info.getName().equals(name)) {
                    avd = info;
                    break;
                }
            }
        }
        if (avd == null) {
            log.error(null, "There is no Android Virtual Device named '%s'.", name);
            return;
        }
        updateAvd(avd, log);
    }
    public void updateAvd(AvdInfo avd, ISdkLog log) throws IOException {
        Map<String, String> oldProperties = avd.getProperties();
        Map<String, String> properties = new HashMap<String, String>();
        if (oldProperties != null) {
            properties.putAll(oldProperties);
        }
        AvdStatus status;
        if (setImagePathProperties(avd.getTarget(), properties, log)) {
            if (properties.containsKey(AVD_INI_IMAGES_1)) {
                log.printf("Updated '%1$s' with value '%2$s'\n", AVD_INI_IMAGES_1,
                        properties.get(AVD_INI_IMAGES_1));
            }
            if (properties.containsKey(AVD_INI_IMAGES_2)) {
                log.printf("Updated '%1$s' with value '%2$s'\n", AVD_INI_IMAGES_2,
                        properties.get(AVD_INI_IMAGES_2));
            }
            status = AvdStatus.OK;
        } else {
            log.error(null, "Unable to find non empty system images folders for %1$s",
                    avd.getName());
            status = AvdStatus.ERROR_IMAGE_DIR;
        }
        File configIniFile = new File(avd.getPath(), CONFIG_INI);
        writeIniFile(configIniFile, properties);
        AvdInfo newAvd = new AvdInfo(
                avd.getName(),
                avd.getPath(),
                avd.getTargetHash(),
                avd.getTarget(),
                properties,
                status);
        replaceAvd(avd, newAvd);
    }
    private boolean setImagePathProperties(IAndroidTarget target,
            Map<String, String> properties,
            ISdkLog log) {
        properties.remove(AVD_INI_IMAGES_1);
        properties.remove(AVD_INI_IMAGES_2);
        try {
            String property = AVD_INI_IMAGES_1;
            String imagePath = getImageRelativePath(target);
            if (imagePath != null) {
                properties.put(property, imagePath);
                property = AVD_INI_IMAGES_2;
            }
            IAndroidTarget parent = target.getParent();
            if (parent != null) {
                imagePath = getImageRelativePath(parent);
                if (imagePath != null) {
                    properties.put(property, imagePath);
                }
            }
            return properties.containsKey(AVD_INI_IMAGES_1);
        } catch (InvalidTargetPathException e) {
            log.error(e, e.getMessage());
        }
        return false;
    }
    private void replaceAvd(AvdInfo oldAvd, AvdInfo newAvd) {
        synchronized (mAllAvdList) {
            mAllAvdList.remove(oldAvd);
            mAllAvdList.add(newAvd);
            mValidAvdList = mBrokenAvdList = null;
        }
    }
}
