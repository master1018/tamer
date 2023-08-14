public class CorporaUpdateReceiver extends BroadcastReceiver {
    private static final boolean DBG = false;
    private static final String TAG = "QSB.CorporaUpdateReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (SearchManager.INTENT_ACTION_SEARCHABLES_CHANGED.equals(action)
                || SearchManager.INTENT_ACTION_SEARCH_SETTINGS_CHANGED.equals(action)) {
            if (DBG) Log.d(TAG, "onReceive(" + intent + ")");
            updateCorpora(context);
            SearchWidgetProvider.updateSearchWidgets(context);
        }
    }
    private void updateCorpora(Context context) {
        QsbApplication.get(context).updateCorpora();
    }
}
