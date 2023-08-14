class UserFolderInfo extends FolderInfo {
    ArrayList<ShortcutInfo> contents = new ArrayList<ShortcutInfo>();
    UserFolderInfo() {
        itemType = LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER;
    }
    public void add(ShortcutInfo item) {
        contents.add(item);
    }
    public void remove(ShortcutInfo item) {
        contents.remove(item);
    }
    @Override
    void onAddToDatabase(ContentValues values) { 
        super.onAddToDatabase(values);
        values.put(LauncherSettings.Favorites.TITLE, title.toString());
    }
}
