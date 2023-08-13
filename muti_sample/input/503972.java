public class DefaultContainerService extends IntentService {
    private static final String TAG = "DefContainer";
    private static final boolean localLOGV = true;
    private IMediaContainerService.Stub mBinder = new IMediaContainerService.Stub() {
        public String copyResourceToContainer(final Uri packageURI,
                final String cid,
                final String key, final String resFileName) {
            if (packageURI == null || cid == null) {
                return null;
            }
            return copyResourceInner(packageURI, cid, key, resFileName);
        }
        public boolean copyResource(final Uri packageURI,
                ParcelFileDescriptor outStream) {
            if (packageURI == null ||  outStream == null) {
                return false;
            }
            ParcelFileDescriptor.AutoCloseOutputStream
            autoOut = new ParcelFileDescriptor.AutoCloseOutputStream(outStream);
            return copyFile(packageURI, autoOut);
        }
        public PackageInfoLite getMinimalPackageInfo(final Uri fileUri, int flags) {
            PackageInfoLite ret = new PackageInfoLite();
            if (fileUri == null) {
                Log.i(TAG, "Invalid package uri " + fileUri);
                ret.recommendedInstallLocation = PackageHelper.RECOMMEND_FAILED_INVALID_APK;
                return ret;
            }
            String scheme = fileUri.getScheme();
            if (scheme != null && !scheme.equals("file")) {
                Log.w(TAG, "Falling back to installing on internal storage only");
                ret.recommendedInstallLocation = PackageHelper.RECOMMEND_INSTALL_INTERNAL;
                return ret;
            }
            String archiveFilePath = fileUri.getPath();
            PackageParser packageParser = new PackageParser(archiveFilePath);
            File sourceFile = new File(archiveFilePath);
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            PackageParser.PackageLite pkg = packageParser.parsePackageLite(
                    archiveFilePath, 0);
            ret.packageName = pkg.packageName;
            ret.installLocation = pkg.installLocation;
            packageParser = null;
            Runtime.getRuntime().gc();
            if (pkg == null) {
                Log.w(TAG, "Failed to parse package");
                ret.recommendedInstallLocation = PackageHelper.RECOMMEND_FAILED_INVALID_APK;
                return ret;
            }
            ret.packageName = pkg.packageName;
            ret.recommendedInstallLocation = recommendAppInstallLocation(pkg.installLocation, archiveFilePath, flags);
            return ret;
        }
        public boolean checkFreeStorage(boolean external, Uri fileUri) {
            return checkFreeStorageInner(external, fileUri);
        }
    };
    public DefaultContainerService() {
        super("DefaultContainerService");
        setIntentRedelivery(true);
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        if (PackageManager.ACTION_CLEAN_EXTERNAL_STORAGE.equals(intent.getAction())) {
            IPackageManager pm = IPackageManager.Stub.asInterface(
                    ServiceManager.getService("package"));
            String pkg = null;
            try {
                while ((pkg=pm.nextPackageToClean(pkg)) != null) {
                    eraseFiles(Environment.getExternalStorageAppDataDirectory(pkg));
                    eraseFiles(Environment.getExternalStorageAppMediaDirectory(pkg));
                }
            } catch (RemoteException e) {
            }
        }
    }
    void eraseFiles(File path) {
        if (path.isDirectory()) {
            String[] files = path.list();
            if (files != null) {
                for (String file : files) {
                    eraseFiles(new File(path, file));
                }
            }
        }
        path.delete();
    }
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    private String copyResourceInner(Uri packageURI, String newCid, String key, String resFileName) {
        String status = Environment.getExternalStorageState();
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            Log.w(TAG, "Make sure sdcard is mounted.");
            return null;
        }
        String codePath = packageURI.getPath();
        File codeFile = new File(codePath);
        String newCachePath = null;
        if ((newCachePath = PackageHelper.createSdDir(codeFile,
                newCid, key, Process.myUid())) == null) {
            Log.e(TAG, "Failed to create container " + newCid);
            return null;
        }
        if (localLOGV) Log.i(TAG, "Created container for " + newCid
                + " at path : " + newCachePath);
        File resFile = new File(newCachePath, resFileName);
        if (!FileUtils.copyFile(new File(codePath), resFile)) {
            Log.e(TAG, "Failed to copy " + codePath + " to " + resFile);
            PackageHelper.destroySdDir(newCid);
            return null;
        }
        if (localLOGV) Log.i(TAG, "Copied " + codePath + " to " + resFile);
        if (!PackageHelper.finalizeSdDir(newCid)) {
            Log.e(TAG, "Failed to finalize " + newCid + " at path " + newCachePath);
            PackageHelper.destroySdDir(newCid);
        }
        if (localLOGV) Log.i(TAG, "Finalized container " + newCid);
        if (PackageHelper.isContainerMounted(newCid)) {
            if (localLOGV) Log.i(TAG, "Unmounting " + newCid +
                    " at path " + newCachePath);
            Runtime.getRuntime().gc();
            PackageHelper.unMountSdDir(newCid);
        } else {
            if (localLOGV) Log.i(TAG, "Container " + newCid + " not mounted");
        }
        return newCachePath;
    }
    public static boolean copyToFile(InputStream inputStream, FileOutputStream out) {
        try {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) >= 0) {
                out.write(buffer, 0, bytesRead);
            }
            return true;
        } catch (IOException e) {
            Log.i(TAG, "Exception : " + e + " when copying file");
            return false;
        }
    }
    public static boolean copyToFile(File srcFile, FileOutputStream out) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(srcFile);
            return copyToFile(inputStream, out);
        } catch (IOException e) {
            return false;
        } finally {
            try { if (inputStream != null) inputStream.close(); } catch (IOException e) {}
        }
    }
    private  boolean copyFile(Uri pPackageURI, FileOutputStream outStream) {
        String scheme = pPackageURI.getScheme();
        if (scheme == null || scheme.equals("file")) {
            final File srcPackageFile = new File(pPackageURI.getPath());
            if (!copyToFile(srcPackageFile, outStream)) {
                Log.e(TAG, "Couldn't copy file: " + srcPackageFile);
                return false;
            }
        } else if (scheme.equals("content")) {
            ParcelFileDescriptor fd = null;
            try {
                fd = getContentResolver().openFileDescriptor(pPackageURI, "r");
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Couldn't open file descriptor from download service. Failed with exception " + e);
                return false;
            }
            if (fd == null) {
                Log.e(TAG, "Couldn't open file descriptor from download service (null).");
                return false;
            } else {
                if (localLOGV) {
                    Log.v(TAG, "Opened file descriptor from download service.");
                }
                ParcelFileDescriptor.AutoCloseInputStream
                dlStream = new ParcelFileDescriptor.AutoCloseInputStream(fd);
                if (!copyToFile(dlStream, outStream)) {
                    Log.e(TAG, "Couldn't copy " + pPackageURI + " to temp file.");
                    return false;
                }
            }
        } else {
            Log.e(TAG, "Package URI is not 'file:' or 'content:' - " + pPackageURI);
            return false;
        }
        return true;
    }
    private static final double LOW_NAND_FLASH_TRESHOLD = 0.1;
    private static final long INSTALL_ON_SD_THRESHOLD = (1024 * 1024);
    private static final int ERR_LOC = -1;
    private int recommendAppInstallLocation(int installLocation,
            String archiveFilePath, int flags) {
        boolean checkInt = false;
        boolean checkExt = false;
        boolean checkBoth = false;
        check_inner : {
            if ((flags & PackageManager.INSTALL_FORWARD_LOCK) != 0) {
                checkInt = true;
                break check_inner;
            } else if ((flags & PackageManager.INSTALL_INTERNAL) != 0) {
                checkInt = true;
                break check_inner;
            } else if ((flags & PackageManager.INSTALL_EXTERNAL) != 0) {
                checkExt = true;
                break check_inner;
            }
            if (installLocation == PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY) {
                checkInt = true;
                break check_inner;
            } else if (installLocation == PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL) {
                checkExt = true;
                checkBoth = true;
                break check_inner;
            } else if (installLocation == PackageInfo.INSTALL_LOCATION_AUTO) {
                checkInt = true;
                checkBoth = true;
                break check_inner;
            }
            int installPreference = Settings.System.getInt(getApplicationContext()
                    .getContentResolver(),
                    Settings.Secure.DEFAULT_INSTALL_LOCATION,
                    PackageHelper.APP_INSTALL_AUTO);
            if (installPreference == PackageHelper.APP_INSTALL_INTERNAL) {
                checkInt = true;
                break check_inner;
            } else if (installPreference == PackageHelper.APP_INSTALL_EXTERNAL) {
                checkExt = true;
                break check_inner;
            }
            checkInt = true;
        }
        String status = Environment.getExternalStorageState();
        long availSDSize = -1;
        boolean mediaAvailable = false;
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            StatFs sdStats = new StatFs(
                    Environment.getExternalStorageDirectory().getPath());
            availSDSize = (long)sdStats.getAvailableBlocks() *
                    (long)sdStats.getBlockSize();
            mediaAvailable = true;
        }
        StatFs internalStats = new StatFs(Environment.getDataDirectory().getPath());
        long totalInternalSize = (long)internalStats.getBlockCount() *
                (long)internalStats.getBlockSize();
        long availInternalSize = (long)internalStats.getAvailableBlocks() *
                (long)internalStats.getBlockSize();
        double pctNandFree = (double)availInternalSize / (double)totalInternalSize;
        File apkFile = new File(archiveFilePath);
        long pkgLen = apkFile.length();
        long reqInstallSize = pkgLen;
        long reqInternalSize = 0;
        boolean intThresholdOk = (pctNandFree >= LOW_NAND_FLASH_TRESHOLD);
        boolean intAvailOk = ((reqInstallSize + reqInternalSize) < availInternalSize);
        boolean fitsOnSd = false;
        if (mediaAvailable && (reqInstallSize < availSDSize)) {
            if (reqInternalSize == 0) {
                fitsOnSd = true;
            } else if ((reqInternalSize < availInternalSize) && intThresholdOk) {
                fitsOnSd = true;
            }
        }
        boolean fitsOnInt = intThresholdOk && intAvailOk;
        if (checkInt) {
            if (fitsOnInt) {
                return PackageHelper.RECOMMEND_INSTALL_INTERNAL;
            }
        } else if (checkExt) {
            if (fitsOnSd) {
                return PackageHelper.RECOMMEND_INSTALL_EXTERNAL;
            }
        }
        if (checkBoth) {
            if (fitsOnInt) {
                return PackageHelper.RECOMMEND_INSTALL_INTERNAL;
            }
            if (fitsOnSd) {
                return PackageHelper.RECOMMEND_INSTALL_EXTERNAL;
            }
        }
        if ((checkExt || checkBoth) && !mediaAvailable) {
            return PackageHelper.RECOMMEND_MEDIA_UNAVAILABLE;
        }
        return PackageHelper.RECOMMEND_FAILED_INSUFFICIENT_STORAGE;
    }
    private boolean checkFreeStorageInner(boolean external, Uri packageURI) {
        File apkFile = new File(packageURI.getPath());
        long size = apkFile.length();
        if (external) {
            String status = Environment.getExternalStorageState();
            long availSDSize = -1;
            if (status.equals(Environment.MEDIA_MOUNTED)) {
                StatFs sdStats = new StatFs(
                        Environment.getExternalStorageDirectory().getPath());
                availSDSize = (long)sdStats.getAvailableBlocks() *
                (long)sdStats.getBlockSize();
            }
            return availSDSize > size;
        }
        StatFs internalStats = new StatFs(Environment.getDataDirectory().getPath());
        long totalInternalSize = (long)internalStats.getBlockCount() *
        (long)internalStats.getBlockSize();
        long availInternalSize = (long)internalStats.getAvailableBlocks() *
        (long)internalStats.getBlockSize();
        double pctNandFree = (double)availInternalSize / (double)totalInternalSize;
        long reqInstallSize = size;
        long reqInternalSize = 0;
        boolean intThresholdOk = (pctNandFree >= LOW_NAND_FLASH_TRESHOLD);
        boolean intAvailOk = ((reqInstallSize + reqInternalSize) < availInternalSize);
        return intThresholdOk && intAvailOk;
    }
}
