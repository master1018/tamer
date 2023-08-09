class ShortcutInfo extends ItemInfo {
    CharSequence title;
    Intent intent;
    boolean customIcon;
    boolean usingFallbackIcon;
    boolean onExternalStorage;
    Intent.ShortcutIconResource iconResource;
    private Bitmap mIcon;
    ShortcutInfo() {
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_SHORTCUT;
    }
    public ShortcutInfo(ShortcutInfo info) {
        super(info);
        title = info.title.toString();
        intent = new Intent(info.intent);
        if (info.iconResource != null) {
            iconResource = new Intent.ShortcutIconResource();
            iconResource.packageName = info.iconResource.packageName;
            iconResource.resourceName = info.iconResource.resourceName;
        }
        mIcon = info.mIcon; 
        customIcon = info.customIcon;
    }
    public ShortcutInfo(ApplicationInfo info) {
        super(info);
        title = info.title.toString();
        intent = new Intent(info.intent);
        customIcon = false;
    }
    public void setIcon(Bitmap b) {
        mIcon = b;
    }
    public Bitmap getIcon(IconCache iconCache) {
        if (mIcon == null) {
            mIcon = iconCache.getIcon(this.intent);
        }
        return mIcon;
    }
    final void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;
    }
    @Override
    void onAddToDatabase(ContentValues values) {
        super.onAddToDatabase(values);
        String titleStr = title != null ? title.toString() : null;
        values.put(LauncherSettings.BaseLauncherColumns.TITLE, titleStr);
        String uri = intent != null ? intent.toUri(0) : null;
        values.put(LauncherSettings.BaseLauncherColumns.INTENT, uri);
        if (customIcon) {
            values.put(LauncherSettings.BaseLauncherColumns.ICON_TYPE,
                    LauncherSettings.BaseLauncherColumns.ICON_TYPE_BITMAP);
            writeBitmap(values, mIcon);
        } else {
            if (onExternalStorage && !usingFallbackIcon) {
                writeBitmap(values, mIcon);
            }
            values.put(LauncherSettings.BaseLauncherColumns.ICON_TYPE,
                    LauncherSettings.BaseLauncherColumns.ICON_TYPE_RESOURCE);
            if (iconResource != null) {
                values.put(LauncherSettings.BaseLauncherColumns.ICON_PACKAGE,
                        iconResource.packageName);
                values.put(LauncherSettings.BaseLauncherColumns.ICON_RESOURCE,
                        iconResource.resourceName);
            }
        }
    }
    @Override
    public String toString() {
        return "ShortcutInfo(title=" + title.toString() + ")";
    }
    @Override
    void unbind() {
        super.unbind();
    }
    public static void dumpShortcutInfoList(String tag, String label,
            ArrayList<ShortcutInfo> list) {
        Log.d(tag, label + " size=" + list.size());
        for (ShortcutInfo info: list) {
            Log.d(tag, "   title=\"" + info.title + " icon=" + info.mIcon
                    + " customIcon=" + info.customIcon);
        }
    }
}
