public class PackageUtil {
    public static final String PREFIX="com.android.packageinstaller.";
    public static final String INTENT_ATTR_INSTALL_STATUS = PREFIX+"installStatus";
    public static final String INTENT_ATTR_APPLICATION_INFO=PREFIX+"applicationInfo";
    public static final String INTENT_ATTR_PERMISSIONS_LIST=PREFIX+"PermissionsList";
    public static final String INTENT_ATTR_PACKAGE_NAME=PREFIX+"PackageName";
    public static  ApplicationInfo getApplicationInfo(Uri packageURI) {
        final String archiveFilePath = packageURI.getPath();
        PackageParser packageParser = new PackageParser(archiveFilePath);
        File sourceFile = new File(archiveFilePath);
        DisplayMetrics metrics = new DisplayMetrics();
        metrics.setToDefaults();
        PackageParser.Package pkg = packageParser.parsePackage(sourceFile, archiveFilePath, metrics, 0);
        if (pkg == null) {
            return null;
        }
        return pkg.applicationInfo;
    }
    public static  PackageParser.Package getPackageInfo(Uri packageURI) {
        final String archiveFilePath = packageURI.getPath();
        PackageParser packageParser = new PackageParser(archiveFilePath);
        File sourceFile = new File(archiveFilePath);
        DisplayMetrics metrics = new DisplayMetrics();
        metrics.setToDefaults();
        PackageParser.Package pkg =  packageParser.parsePackage(sourceFile,
                archiveFilePath, metrics, 0);
        packageParser = null;
        return pkg;
    }
    public static View initSnippetForInstalledApp(Activity pContext,
            ApplicationInfo appInfo, int snippetId) {
        View appSnippet = pContext.findViewById(snippetId);
        String pkgName = appInfo.packageName;
        PackageManager pm = pContext.getPackageManager();
        CharSequence label = appInfo.loadLabel(pm);
        Drawable icon = appInfo.loadIcon(pm);
        ((ImageView)appSnippet.findViewById(R.id.app_icon)).setImageDrawable(icon);
        ((TextView)appSnippet.findViewById(R.id.app_name)).setText(label);
        return appSnippet;
    }
    public static View initSnippetForNewApp(Activity pContext, AppSnippet as,
            int snippetId) {
        View appSnippet = pContext.findViewById(snippetId);
        ((ImageView)appSnippet.findViewById(R.id.app_icon)).setImageDrawable(as.icon);
        ((TextView)appSnippet.findViewById(R.id.app_name)).setText(as.label);
        return appSnippet;
    }
    public static boolean isPackageAlreadyInstalled(Activity context, String pkgName) {
        List<PackageInfo> installedList = context.getPackageManager().getInstalledPackages(
                PackageManager.GET_UNINSTALLED_PACKAGES);
        int installedListSize = installedList.size();
        for(int i = 0; i < installedListSize; i++) {
            PackageInfo tmp = installedList.get(i);
            if(pkgName.equalsIgnoreCase(tmp.packageName)) {
                return true;
            }
        }
        return false;
    }
    static public class AppSnippet {
        CharSequence label;
        Drawable icon;
        public AppSnippet(CharSequence label, Drawable icon) {
            this.label = label;
            this.icon = icon;
        }
    }
    public static AppSnippet getAppSnippet(Activity pContext, ApplicationInfo appInfo,
            Uri packageURI) {
        final String archiveFilePath = packageURI.getPath();
        Resources pRes = pContext.getResources();
        AssetManager assmgr = new AssetManager();
        assmgr.addAssetPath(archiveFilePath);
        Resources res = new Resources(assmgr, pRes.getDisplayMetrics(), pRes.getConfiguration());
        CharSequence label = null;
        if (appInfo.labelRes != 0) {
            try {
                label = res.getText(appInfo.labelRes);
            } catch (Resources.NotFoundException e) {
            }
        }
        if (label == null) {
            label = (appInfo.nonLocalizedLabel != null) ?
                    appInfo.nonLocalizedLabel : appInfo.packageName;
        }
        Drawable icon = null;
        if (appInfo.icon != 0) {
            try {
                icon = res.getDrawable(appInfo.icon);
            } catch (Resources.NotFoundException e) {
            }
        }
        if (icon == null) {
            icon = pContext.getPackageManager().getDefaultActivityIcon();
        }
        return new PackageUtil.AppSnippet(label, icon);
    }
}
