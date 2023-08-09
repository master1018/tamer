public final class LiveFolders implements BaseColumns {
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String INTENT = "intent";
    public static final String ICON_BITMAP = "icon_bitmap";
    public static final String ICON_PACKAGE = "icon_package";
    public static final String ICON_RESOURCE = "icon_resource";
    public static final int DISPLAY_MODE_GRID = 0x1;
    public static final int DISPLAY_MODE_LIST = 0x2;
    public static final String EXTRA_LIVE_FOLDER_NAME = "android.intent.extra.livefolder.NAME";
    public static final String EXTRA_LIVE_FOLDER_ICON = "android.intent.extra.livefolder.ICON";
    public static final String EXTRA_LIVE_FOLDER_DISPLAY_MODE =
            "android.intent.extra.livefolder.DISPLAY_MODE";
    public static final String EXTRA_LIVE_FOLDER_BASE_INTENT =
            "android.intent.extra.livefolder.BASE_INTENT";
    @SdkConstant(SdkConstant.SdkConstantType.ACTIVITY_INTENT_ACTION)
    public static final String ACTION_CREATE_LIVE_FOLDER =
            "android.intent.action.CREATE_LIVE_FOLDER";
    private LiveFolders() {
    }
}
