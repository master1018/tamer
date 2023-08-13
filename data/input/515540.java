class LauncherAppWidgetInfo extends ItemInfo {
    int appWidgetId;
    AppWidgetHostView hostView = null;
    LauncherAppWidgetInfo(int appWidgetId) {
        itemType = LauncherSettings.Favorites.ITEM_TYPE_APPWIDGET;
        this.appWidgetId = appWidgetId;
    }
    @Override
    void onAddToDatabase(ContentValues values) {
        super.onAddToDatabase(values);
        values.put(LauncherSettings.Favorites.APPWIDGET_ID, appWidgetId);
    }
    @Override
    public String toString() {
        return "AppWidget(id=" + Integer.toString(appWidgetId) + ")";
    }
    @Override
    void unbind() {
        super.unbind();
        hostView = null;
    }
}
