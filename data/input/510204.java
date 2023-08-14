class ShortcutManager extends ContentObserver {
    private static final String TAG = "ShortcutManager";
    private static final int COLUMN_SHORTCUT = 0;
    private static final int COLUMN_INTENT = 1;
    private static final String[] sProjection = new String[] {
        Settings.Bookmarks.SHORTCUT, Settings.Bookmarks.INTENT
    };
    private Context mContext;
    private Cursor mCursor;
    private SparseArray<Intent> mShortcutIntents;
    public ShortcutManager(Context context, Handler handler) {
        super(handler);
        mContext = context;
        mShortcutIntents = new SparseArray<Intent>();
    }
    public void observe() {
        mCursor = mContext.getContentResolver().query(
                Settings.Bookmarks.CONTENT_URI, sProjection, null, null, null);
        mCursor.registerContentObserver(this);
        updateShortcuts();
    }
    @Override
    public void onChange(boolean selfChange) {
        updateShortcuts();
    }
    private void updateShortcuts() {
        Cursor c = mCursor;
        if (!c.requery()) {
            Log.e(TAG, "ShortcutObserver could not re-query shortcuts.");
            return;
        }
        mShortcutIntents.clear();
        while (c.moveToNext()) {
            int shortcut = c.getInt(COLUMN_SHORTCUT);
            if (shortcut == 0) continue;
            String intentURI = c.getString(COLUMN_INTENT);
            Intent intent = null;
            try {
                intent = Intent.getIntent(intentURI);
            } catch (URISyntaxException e) {
                Log.w(TAG, "Intent URI for shortcut invalid.", e);
            }
            if (intent == null) continue;
            mShortcutIntents.put(shortcut, intent);
        }
    }
    public Intent getIntent(int keyCode, int modifiers) {
        KeyCharacterMap kcm = KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
        int shortcut = kcm.get(keyCode, modifiers);
        Intent intent = shortcut != 0 ? mShortcutIntents.get(shortcut) : null; 
        if (intent != null) return intent;
        shortcut = Character.toLowerCase(kcm.get(keyCode, 0));
        return shortcut != 0 ? mShortcutIntents.get(shortcut) : null;
    }
}
