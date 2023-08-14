class LiveFolderInfo extends FolderInfo {
    Intent baseIntent;
    Uri uri;
    int displayMode;
    Bitmap icon;
    Intent.ShortcutIconResource iconResource;
    LiveFolderInfo() {
        itemType = LauncherSettings.Favorites.ITEM_TYPE_LIVE_FOLDER;
    }
    @Override
    void onAddToDatabase(ContentValues values) {
        super.onAddToDatabase(values);
        values.put(LauncherSettings.Favorites.TITLE, title.toString());
        values.put(LauncherSettings.Favorites.URI, uri.toString());
        if (baseIntent != null) {
            values.put(LauncherSettings.Favorites.INTENT, baseIntent.toUri(0));
        }
        values.put(LauncherSettings.Favorites.ICON_TYPE, LauncherSettings.Favorites.ICON_TYPE_RESOURCE);
        values.put(LauncherSettings.Favorites.DISPLAY_MODE, displayMode);
        if (iconResource != null) {
            values.put(LauncherSettings.Favorites.ICON_PACKAGE, iconResource.packageName);
            values.put(LauncherSettings.Favorites.ICON_RESOURCE, iconResource.resourceName);
        }
    }
}
