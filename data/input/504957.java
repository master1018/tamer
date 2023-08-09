public class UserFolder extends Folder implements DropTarget {
    private static final String TAG = "Launcher.UserFolder";
    public UserFolder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    static UserFolder fromXml(Context context) {
        return (UserFolder) LayoutInflater.from(context).inflate(R.layout.user_folder, null);
    }
    public boolean acceptDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        final ItemInfo item = (ItemInfo) dragInfo;
        final int itemType = item.itemType;
        return (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION ||
                    itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT)
                && item.container != mInfo.id;
    }
    public Rect estimateDropLocation(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo, Rect recycle) {
        return null;
    }
    public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
        ShortcutInfo item;
        if (dragInfo instanceof ApplicationInfo) {
            item = ((ApplicationInfo)dragInfo).makeShortcut();
        } else {
            item = (ShortcutInfo)dragInfo;
        }
        ((ShortcutsAdapter)mContent.getAdapter()).add(item);
        LauncherModel.addOrMoveItemInDatabase(mLauncher, item, mInfo.id, 0, 0, 0);
    }
    public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
    }
    public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
    }
    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
            DragView dragView, Object dragInfo) {
    }
    @Override
    public void onDropCompleted(View target, boolean success) {
        if (success) {
            ShortcutsAdapter adapter = (ShortcutsAdapter)mContent.getAdapter();
            adapter.remove(mDragItem);
        }
    }
    void bind(FolderInfo info) {
        super.bind(info);
        setContentAdapter(new ShortcutsAdapter(mContext, ((UserFolderInfo) info).contents));
    }
    @Override
    void onOpen() {
        super.onOpen();
        requestFocus();
    }
}
