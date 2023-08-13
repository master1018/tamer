public final class SdkManager {
    public final static String PROP_VERSION_SDK = "ro.build.version.sdk";
    public final static String PROP_VERSION_CODENAME = "ro.build.version.codename";
    public final static String PROP_VERSION_RELEASE = "ro.build.version.release";
    private final static String ADDON_NAME = "name";
    private final static String ADDON_VENDOR = "vendor";
    private final static String ADDON_API = "api";
    private final static String ADDON_DESCRIPTION = "description";
    private final static String ADDON_LIBRARIES = "libraries";
    private final static String ADDON_DEFAULT_SKIN = "skin";
    private final static String ADDON_USB_VENDOR = "usb-vendor";
    private final static String ADDON_REVISION = "revision";
    private final static String ADDON_REVISION_OLD = "version";
    private final static Pattern PATTERN_PROP = Pattern.compile(
            "^([a-zA-Z0-9._-]+)\\s*=\\s*(.*)\\s*$");
    private final static Pattern PATTERN_LIB_DATA = Pattern.compile(
            "^([a-zA-Z0-9._-]+\\.jar);(.*)$", Pattern.CASE_INSENSITIVE);
    private final static Pattern PATTERN_USB_IDS = Pattern.compile(
            "^0x[a-f0-9]{4}$", Pattern.CASE_INSENSITIVE);
    private final static String[] sPlatformContentList = new String[] {
        SdkConstants.FN_FRAMEWORK_LIBRARY,
        SdkConstants.FN_FRAMEWORK_AIDL,
        SdkConstants.OS_SDK_TOOLS_FOLDER + SdkConstants.FN_AAPT,
        SdkConstants.OS_SDK_TOOLS_FOLDER + SdkConstants.FN_AIDL,
        SdkConstants.OS_SDK_TOOLS_FOLDER + SdkConstants.FN_DX,
        SdkConstants.OS_SDK_TOOLS_LIB_FOLDER + SdkConstants.FN_DX_JAR,
    };
    private final static String ADB_INI_FILE = "adb_usb.ini";
    private final static String ADB_INI_HEADER =
        "# ANDROID 3RD PARTY USB VENDOR ID LIST -- DO NOT EDIT.\n" +
        "# USE 'android update adb' TO GENERATE.\n" +
        "# 1 USB VENDOR ID PER LINE.\n";
    private final String mSdkLocation;
    private IAndroidTarget[] mTargets;
    private SdkManager(String sdkLocation) {
        mSdkLocation = sdkLocation;
    }
    public static SdkManager createManager(String sdkLocation, ISdkLog log) {
        try {
            SdkManager manager = new SdkManager(sdkLocation);
            ArrayList<IAndroidTarget> list = new ArrayList<IAndroidTarget>();
            loadPlatforms(sdkLocation, list, log);
            loadAddOns(sdkLocation, list, log);
            Collections.sort(list);
            manager.setTargets(list.toArray(new IAndroidTarget[list.size()]));
            manager.loadSamples(log);
            return manager;
        } catch (IllegalArgumentException e) {
            log.error(e, "Error parsing the sdk.");
        }
        return null;
    }
    public String getLocation() {
        return mSdkLocation;
    }
    public IAndroidTarget[] getTargets() {
        return mTargets;
    }
    private void setTargets(IAndroidTarget[] targets) {
        assert targets != null;
        mTargets = targets;
    }
    public IAndroidTarget getTargetFromHashString(String hash) {
        if (hash != null) {
            for (IAndroidTarget target : mTargets) {
                if (hash.equals(target.hashString())) {
                    return target;
                }
            }
        }
        return null;
    }
    public void updateAdb() throws AndroidLocationException, IOException {
        FileWriter writer = null;
        try {
            File adbIni = new File(AndroidLocation.getFolder(), ADB_INI_FILE);
            writer = new FileWriter(adbIni);
            HashSet<Integer> set = new HashSet<Integer>();
            IAndroidTarget[] targets = getTargets();
            for (IAndroidTarget target : targets) {
                if (target.getUsbVendorId() != IAndroidTarget.NO_USB_ID) {
                    set.add(target.getUsbVendorId());
                }
            }
            writer.write(ADB_INI_HEADER);
            for (Integer i : set) {
                writer.write(String.format("0x%04x\n", i));
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
    public void reloadSdk(ISdkLog log) {
        ArrayList<IAndroidTarget> list = new ArrayList<IAndroidTarget>();
        loadPlatforms(mSdkLocation, list, log);
        loadAddOns(mSdkLocation, list, log);
        Collections.sort(list);
        setTargets(list.toArray(new IAndroidTarget[list.size()]));
        loadSamples(log);
    }
    private static void loadPlatforms(String location, ArrayList<IAndroidTarget> list,
            ISdkLog log) {
        File platformFolder = new File(location, SdkConstants.FD_PLATFORMS);
        if (platformFolder.isDirectory()) {
            File[] platforms  = platformFolder.listFiles();
            for (File platform : platforms) {
                if (platform.isDirectory()) {
                    PlatformTarget target = loadPlatform(platform, log);
                    if (target != null) {
                        list.add(target);
                    }
                } else {
                    log.warning("Ignoring platform '%1$s', not a folder.", platform.getName());
                }
            }
            return;
        }
        String message = null;
        if (platformFolder.exists() == false) {
            message = "%s is missing.";
        } else {
            message = "%s is not a folder.";
        }
        throw new IllegalArgumentException(String.format(message,
                platformFolder.getAbsolutePath()));
    }
    private static PlatformTarget loadPlatform(File platform, ISdkLog log) {
        File buildProp = new File(platform, SdkConstants.FN_BUILD_PROP);
        if (buildProp.isFile()) {
            Map<String, String> map = parsePropertyFile(buildProp, log);
            if (map != null) {
                String apiName = map.get(PROP_VERSION_RELEASE);
                if (apiName == null) {
                    log.warning(null,
                            "Ignoring platform '%1$s': %2$s is missing from '%3$s'",
                            platform.getName(), PROP_VERSION_RELEASE,
                            SdkConstants.FN_BUILD_PROP);
                    return null;
                }
                int apiNumber;
                String stringValue = map.get(PROP_VERSION_SDK);
                if (stringValue == null) {
                    log.warning(null,
                            "Ignoring platform '%1$s': %2$s is missing from '%3$s'",
                            platform.getName(), PROP_VERSION_SDK,
                            SdkConstants.FN_BUILD_PROP);
                    return null;
                } else {
                    try {
                         apiNumber = Integer.parseInt(stringValue);
                    } catch (NumberFormatException e) {
                        log.warning(null,
                                "Ignoring platform '%1$s': %2$s is not a valid number in %3$s.",
                                platform.getName(), PROP_VERSION_SDK,
                                SdkConstants.FN_BUILD_PROP);
                        return null;
                    }
                }
                String apiCodename = map.get(PROP_VERSION_CODENAME);
                if (apiCodename != null && apiCodename.equals("REL")) {
                    apiCodename = null; 
                }
                int revision = 1;
                File sourcePropFile = new File(platform, SdkConstants.FN_SOURCE_PROP);
                Map<String, String> sourceProp = parsePropertyFile(sourcePropFile, log);
                if (sourceProp != null) {
                    try {
                        revision = Integer.parseInt(sourceProp.get("Pkg.Revision"));
                    } catch (NumberFormatException e) {
                    }
                    map.putAll(sourceProp);
                }
                File sdkPropFile = new File(platform, SdkConstants.FN_SDK_PROP);
                Map<String, String> antProp = parsePropertyFile(sdkPropFile, log);
                if (antProp != null) {
                    map.putAll(antProp);
                }
                if (checkPlatformContent(platform, log) == false) {
                    return null;
                }
                PlatformTarget target = new PlatformTarget(
                        platform.getAbsolutePath(),
                        map,
                        apiNumber,
                        apiCodename,
                        apiName,
                        revision);
                String[] skins = parseSkinFolder(target.getPath(IAndroidTarget.SKINS));
                target.setSkins(skins);
                return target;
            }
        } else {
            log.warning(null, "Ignoring platform '%1$s': %2$s is missing.", platform.getName(),
                    SdkConstants.FN_BUILD_PROP);
        }
        return null;
    }
    private static void loadAddOns(String location, ArrayList<IAndroidTarget> list, ISdkLog log) {
        File addonFolder = new File(location, SdkConstants.FD_ADDONS);
        if (addonFolder.isDirectory()) {
            File[] addons  = addonFolder.listFiles();
            for (File addon : addons) {
                if (addon.isDirectory()) {
                    AddOnTarget target = loadAddon(addon, list, log);
                    if (target != null) {
                        list.add(target);
                    }
                }
            }
            return;
        }
        String message = null;
        if (addonFolder.exists() == false) {
            message = "%s is missing.";
        } else {
            message = "%s is not a folder.";
        }
        throw new IllegalArgumentException(String.format(message,
                addonFolder.getAbsolutePath()));
    }
    private static AddOnTarget loadAddon(File addon, ArrayList<IAndroidTarget> targetList,
            ISdkLog log) {
        File addOnManifest = new File(addon, SdkConstants.FN_MANIFEST_INI);
        if (addOnManifest.isFile()) {
            Map<String, String> propertyMap = parsePropertyFile(addOnManifest, log);
            if (propertyMap != null) {
                String name = propertyMap.get(ADDON_NAME);
                if (name == null) {
                    displayAddonManifestWarning(log, addon.getName(), ADDON_NAME);
                    return null;
                }
                String vendor = propertyMap.get(ADDON_VENDOR);
                if (vendor == null) {
                    displayAddonManifestWarning(log, addon.getName(), ADDON_VENDOR);
                    return null;
                }
                String api = propertyMap.get(ADDON_API);
                PlatformTarget baseTarget = null;
                if (api == null) {
                    displayAddonManifestWarning(log, addon.getName(), ADDON_API);
                    return null;
                } else {
                    for (IAndroidTarget target : targetList) {
                        if (target.isPlatform() && target.getVersion().equals(api)) {
                            baseTarget = (PlatformTarget)target;
                            break;
                        }
                    }
                    if (baseTarget == null) {
                        log.warning(null,
                                "Ignoring add-on '%1$s': Unable to find base platform with API level '%2$s'",
                                addon.getName(), api);
                        return null;
                    }
                }
                String description = propertyMap.get(ADDON_DESCRIPTION);
                int revisionValue = 1;
                String revision = propertyMap.get(ADDON_REVISION);
                if (revision == null) {
                    revision = propertyMap.get(ADDON_REVISION_OLD);
                }
                if (revision != null) {
                    try {
                        revisionValue = Integer.parseInt(revision);
                    } catch (NumberFormatException e) {
                        log.warning(null,
                                "Ignoring add-on '%1$s': %2$s is not a valid number in %3$s.",
                                addon.getName(), ADDON_REVISION, SdkConstants.FN_BUILD_PROP);
                        return null;
                    }
                }
                String librariesValue = propertyMap.get(ADDON_LIBRARIES);
                Map<String, String[]> libMap = null;
                if (librariesValue != null) {
                    librariesValue = librariesValue.trim();
                    if (librariesValue.length() > 0) {
                        String[] libraries = librariesValue.split(";");
                        if (libraries.length > 0) {
                            libMap = new HashMap<String, String[]>();
                            for (String libName : libraries) {
                                libName = libName.trim();
                                String libData = propertyMap.get(libName);
                                if (libData != null) {
                                    Matcher m = PATTERN_LIB_DATA.matcher(libData);
                                    if (m.matches()) {
                                        libMap.put(libName, new String[] {
                                                m.group(1), m.group(2) });
                                    } else {
                                        log.warning(null,
                                                "Ignoring library '%1$s', property value has wrong format\n\t%2$s",
                                                libName, libData);
                                    }
                                } else {
                                    log.warning(null,
                                            "Ignoring library '%1$s', missing property value",
                                            libName, libData);
                                }
                            }
                        }
                    }
                }
                AddOnTarget target = new AddOnTarget(addon.getAbsolutePath(), name, vendor,
                        revisionValue, description, libMap, baseTarget);
                String[] skins = parseSkinFolder(target.getPath(IAndroidTarget.SKINS));
                String defaultSkin = propertyMap.get(ADDON_DEFAULT_SKIN);
                if (defaultSkin == null) {
                    if (skins.length == 1) {
                        defaultSkin = skins[0];
                    } else {
                        defaultSkin = baseTarget.getDefaultSkin();
                    }
                }
                int usbVendorId = convertId(propertyMap.get(ADDON_USB_VENDOR));
                if (usbVendorId != IAndroidTarget.NO_USB_ID) {
                    target.setUsbVendorId(usbVendorId);
                }
                target.setSkins(skins, defaultSkin);
                return target;
            }
        } else {
            log.warning(null, "Ignoring add-on '%1$s': %2$s is missing.", addon.getName(),
                    SdkConstants.FN_MANIFEST_INI);
        }
        return null;
    }
    private static int convertId(String value) {
        if (value != null && value.length() > 0) {
            if (PATTERN_USB_IDS.matcher(value).matches()) {
                String v = value.substring(2);
                try {
                    return Integer.parseInt(v, 16);
                } catch (NumberFormatException e) {
                }
            }
        }
        return IAndroidTarget.NO_USB_ID;
    }
    private static void displayAddonManifestWarning(ISdkLog log, String addonName, String valueName) {
        log.warning(null, "Ignoring add-on '%1$s': '%2$s' is missing from %3$s.",
                addonName, valueName, SdkConstants.FN_MANIFEST_INI);
    }
    private static boolean checkPlatformContent(File platform, ISdkLog log) {
        for (String relativePath : sPlatformContentList) {
            File f = new File(platform, relativePath);
            if (!f.exists()) {
                log.warning(null,
                        "Ignoring platform '%1$s': %2$s is missing.",
                        platform.getName(), relativePath);
                return false;
            }
        }
        return true;
    }
    public static Map<String, String> parsePropertyFile(File propFile, ISdkLog log) {
        FileInputStream fis = null;
        BufferedReader reader = null;
        try {
            fis = new FileInputStream(propFile);
            reader = new BufferedReader(new InputStreamReader(fis, SdkConstants.INI_CHARSET));
            String line = null;
            Map<String, String> map = new HashMap<String, String>();
            while ((line = reader.readLine()) != null) {
                if (line.length() > 0 && line.charAt(0) != '#') {
                    Matcher m = PATTERN_PROP.matcher(line);
                    if (m.matches()) {
                        map.put(m.group(1), m.group(2));
                    } else {
                        log.warning("Error parsing '%1$s': \"%2$s\" is not a valid syntax",
                                propFile.getAbsolutePath(),
                                line);
                        return null;
                    }
                }
            }
            return map;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            log.warning("Error parsing '%1$s': %2$s.",
                    propFile.getAbsolutePath(),
                    e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
    private static String[] parseSkinFolder(String osPath) {
        File skinRootFolder = new File(osPath);
        if (skinRootFolder.isDirectory()) {
            ArrayList<String> skinList = new ArrayList<String>();
            File[] files = skinRootFolder.listFiles();
            for (File skinFolder : files) {
                if (skinFolder.isDirectory()) {
                    File layout = new File(skinFolder, SdkConstants.FN_SKIN_LAYOUT);
                    if (layout.isFile()) {
                        skinList.add(skinFolder.getName());
                    }
                }
            }
            return skinList.toArray(new String[skinList.size()]);
        }
        return new String[0];
    }
    private void loadSamples(ISdkLog log) {
        File sampleFolder = new File(mSdkLocation, SdkConstants.FD_SAMPLES);
        if (sampleFolder.isDirectory()) {
            File[] platforms  = sampleFolder.listFiles();
            for (File platform : platforms) {
                if (platform.isDirectory()) {
                    AndroidVersion version = getSamplesVersion(platform, log);
                    if (version != null) {
                        for (IAndroidTarget target : mTargets) {
                            if (target.isPlatform() && target.getVersion().equals(version)) {
                                ((PlatformTarget)target).setSamplesPath(platform.getAbsolutePath());
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    private AndroidVersion getSamplesVersion(File folder, ISdkLog log) {
        File sourceProp = new File(folder, SdkConstants.FN_SOURCE_PROP);
        try {
            Properties p = new Properties();
            p.load(new FileInputStream(sourceProp));
            return new AndroidVersion(p);
        } catch (FileNotFoundException e) {
            log.warning("Ignoring sample '%1$s': does not contain %2$s.", 
                    folder.getName(), SdkConstants.FN_SOURCE_PROP);
        } catch (IOException e) {
            log.warning("Ignoring sample '%1$s': failed reading %2$s.", 
                    folder.getName(), SdkConstants.FN_SOURCE_PROP);
        } catch (AndroidVersionException e) {
            log.warning("Ignoring sample '%1$s': no android version found in %2$s.", 
                    folder.getName(), SdkConstants.FN_SOURCE_PROP);
        }
        return null;
    }
}
