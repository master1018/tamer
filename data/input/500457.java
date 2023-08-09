class LauncherSettings {
    static interface BaseLauncherColumns extends BaseColumns {
        static final String TITLE = "title";
        static final String INTENT = "intent";
        static final String ITEM_TYPE = "itemType";
        static final int ITEM_TYPE_APPLICATION = 0;
        static final int ITEM_TYPE_SHORTCUT = 1;
        static final String ICON_TYPE = "iconType";
        static final int ICON_TYPE_RESOURCE = 0;
        static final int ICON_TYPE_BITMAP = 1;
        static final String ICON_PACKAGE = "iconPackage";
        static final String ICON_RESOURCE = "iconResource";
        static final String ICON = "icon";
    }
    static final class Favorites implements BaseLauncherColumns {
        static final Uri CONTENT_URI = Uri.parse("content:
                LauncherProvider.AUTHORITY + "/" + LauncherProvider.TABLE_FAVORITES +
                "?" + LauncherProvider.PARAMETER_NOTIFY + "=true");
        static final Uri CONTENT_URI_NO_NOTIFICATION = Uri.parse("content:
                LauncherProvider.AUTHORITY + "/" + LauncherProvider.TABLE_FAVORITES +
                "?" + LauncherProvider.PARAMETER_NOTIFY + "=false");
        static Uri getContentUri(long id, boolean notify) {
            return Uri.parse("content:
                    "/" + LauncherProvider.TABLE_FAVORITES + "/" + id + "?" +
                    LauncherProvider.PARAMETER_NOTIFY + "=" + notify);
        }
        static final String CONTAINER = "container";
        static final int CONTAINER_DESKTOP = -100;
        static final String SCREEN = "screen";
        static final String CELLX = "cellX";
        static final String CELLY = "cellY";
        static final String SPANX = "spanX";
        static final String SPANY = "spanY";
        static final int ITEM_TYPE_USER_FOLDER = 2;
        static final int ITEM_TYPE_LIVE_FOLDER = 3;
        static final int ITEM_TYPE_APPWIDGET = 4;
        static final int ITEM_TYPE_WIDGET_CLOCK = 1000;
        static final int ITEM_TYPE_WIDGET_SEARCH = 1001;
        static final int ITEM_TYPE_WIDGET_PHOTO_FRAME = 1002;
        static final String APPWIDGET_ID = "appWidgetId";
        @Deprecated
        static final String IS_SHORTCUT = "isShortcut";
        static final String URI = "uri";
        static final String DISPLAY_MODE = "displayMode";
    }
}
