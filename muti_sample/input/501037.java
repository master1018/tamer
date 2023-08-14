public class QuickLaunchSettings extends PreferenceActivity implements
        AdapterView.OnItemLongClickListener, DialogInterface.OnClickListener {
    private static final String TAG = "QuickLaunchSettings";
    private static final String KEY_SHORTCUT_CATEGORY = "shortcut_category";
    private static final int DIALOG_CLEAR_SHORTCUT = 0;
    private static final int REQUEST_PICK_BOOKMARK = 1;
    private static final int COLUMN_SHORTCUT = 0;
    private static final int COLUMN_TITLE = 1;
    private static final int COLUMN_INTENT = 2;
    private static final String[] sProjection = new String[] {
            Bookmarks.SHORTCUT, Bookmarks.TITLE, Bookmarks.INTENT
    };
    private static final String sShortcutSelection = Bookmarks.SHORTCUT + "=?";
    private Handler mUiHandler = new Handler();
    private static final String DEFAULT_BOOKMARK_FOLDER = "@quicklaunch";
    private Cursor mBookmarksCursor;
    private BookmarksObserver mBookmarksObserver;
    private SparseBooleanArray mBookmarkedShortcuts;
    private PreferenceGroup mShortcutGroup;
    private SparseArray<ShortcutPreference> mShortcutToPreference;
    private CharSequence mClearDialogBookmarkTitle;
    private static final String CLEAR_DIALOG_BOOKMARK_TITLE = "CLEAR_DIALOG_BOOKMARK_TITLE";
    private char mClearDialogShortcut;
    private static final String CLEAR_DIALOG_SHORTCUT = "CLEAR_DIALOG_SHORTCUT";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.quick_launch_settings);
        mShortcutGroup = (PreferenceGroup) findPreference(KEY_SHORTCUT_CATEGORY);
        mShortcutToPreference = new SparseArray<ShortcutPreference>();
        mBookmarksObserver = new BookmarksObserver(mUiHandler);
        initShortcutPreferences();
        mBookmarksCursor = managedQuery(Bookmarks.CONTENT_URI, sProjection, null, null);
        getListView().setOnItemLongClickListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        getContentResolver().registerContentObserver(Bookmarks.CONTENT_URI, true,
                mBookmarksObserver);
        refreshShortcuts();
    }
    @Override
    protected void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(mBookmarksObserver);
    }
    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        mClearDialogBookmarkTitle = state.getString(CLEAR_DIALOG_BOOKMARK_TITLE);
        mClearDialogShortcut = (char) state.getInt(CLEAR_DIALOG_SHORTCUT, 0);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(CLEAR_DIALOG_BOOKMARK_TITLE, mClearDialogBookmarkTitle);
        outState.putInt(CLEAR_DIALOG_SHORTCUT, mClearDialogShortcut);
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_CLEAR_SHORTCUT: {
                return new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.quick_launch_clear_dialog_title))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(getString(R.string.quick_launch_clear_dialog_message,
                                mClearDialogShortcut, mClearDialogBookmarkTitle))
                        .setPositiveButton(R.string.quick_launch_clear_ok_button, this)
                        .setNegativeButton(R.string.quick_launch_clear_cancel_button, this)
                        .create();
            }
        }
        return super.onCreateDialog(id);
    }
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_CLEAR_SHORTCUT: {
                AlertDialog alertDialog = (AlertDialog) dialog;
                alertDialog.setMessage(getString(R.string.quick_launch_clear_dialog_message,
                        mClearDialogShortcut, mClearDialogBookmarkTitle));
            }
        }
    }
    private void showClearDialog(ShortcutPreference pref) {
        if (!pref.hasBookmark()) return;
        mClearDialogBookmarkTitle = pref.getTitle();
        mClearDialogShortcut = pref.getShortcut();
        showDialog(DIALOG_CLEAR_SHORTCUT);
    }
    public void onClick(DialogInterface dialog, int which) {
        if (mClearDialogShortcut > 0 && which == AlertDialog.BUTTON1) {
            clearShortcut(mClearDialogShortcut);
        }
        mClearDialogBookmarkTitle = null;
        mClearDialogShortcut = 0;
    }
    private void clearShortcut(char shortcut) {
        getContentResolver().delete(Bookmarks.CONTENT_URI, sShortcutSelection,
                new String[] { String.valueOf((int) shortcut) });
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (!(preference instanceof ShortcutPreference)) return false;
        ShortcutPreference pref = (ShortcutPreference) preference;
        Intent intent = new Intent(this, BookmarkPicker.class);
        intent.putExtra(BookmarkPicker.EXTRA_SHORTCUT, pref.getShortcut());
        startActivityForResult(intent, REQUEST_PICK_BOOKMARK);
        return true;
    }
    public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {
        Preference pref = (Preference) getPreferenceScreen().getRootAdapter().getItem(position);
        if (!(pref instanceof ShortcutPreference)) return false;
        showClearDialog((ShortcutPreference) pref);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_PICK_BOOKMARK) {
            if (data == null) {
                Log.w(TAG, "Result from bookmark picker does not have an intent.");
                return;
            }
            char shortcut = data.getCharExtra(BookmarkPicker.EXTRA_SHORTCUT, (char) 0);
            updateShortcut(shortcut, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    private void updateShortcut(char shortcut, Intent intent) {
        Bookmarks.add(getContentResolver(), intent, "", DEFAULT_BOOKMARK_FOLDER, shortcut, 0);
    }
    private ShortcutPreference getOrCreatePreference(char shortcut) {
        ShortcutPreference pref = mShortcutToPreference.get(shortcut);
        if (pref != null) {
            return pref;
        } else {
            Log.w(TAG, "Unknown shortcut '" + shortcut + "', creating preference anyway");
            return createPreference(shortcut);
        }
    }
    private ShortcutPreference createPreference(char shortcut) {
        ShortcutPreference pref = new ShortcutPreference(QuickLaunchSettings.this, shortcut);
        mShortcutGroup.addPreference(pref);
        mShortcutToPreference.put(shortcut, pref);
        return pref;
    }
    private void initShortcutPreferences() {
        SparseBooleanArray shortcutSeen = new SparseBooleanArray();
        KeyCharacterMap keyMap = KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
        for (int keyCode = KeyEvent.getMaxKeyCode() - 1; keyCode >= 0; keyCode--) {
            char shortcut = (char) Character.toLowerCase(keyMap.getDisplayLabel(keyCode));
            if (shortcut == 0 || shortcutSeen.get(shortcut, false)) continue;
            if (!Character.isLetterOrDigit(shortcut)) continue;
            shortcutSeen.put(shortcut, true);
            createPreference(shortcut);
        }
    }
    private synchronized void refreshShortcuts() {
        Cursor c = mBookmarksCursor;
        if (c == null) {
            return;
        }
        if (!c.requery()) {
            Log.e(TAG, "Could not requery cursor when refreshing shortcuts.");
            return;
        }
        SparseBooleanArray noLongerBookmarkedShortcuts = mBookmarkedShortcuts;
        SparseBooleanArray newBookmarkedShortcuts = new SparseBooleanArray(); 
        while (c.moveToNext()) {
            char shortcut = Character.toLowerCase((char) c.getInt(COLUMN_SHORTCUT));
            if (shortcut == 0) continue;
            ShortcutPreference pref = getOrCreatePreference(shortcut);
            CharSequence title = Bookmarks.getTitle(this, c);
            int intentColumn = c.getColumnIndex(Bookmarks.INTENT);
            String intentUri = c.getString(intentColumn);
            PackageManager packageManager = getPackageManager();
            try {
                Intent intent = Intent.getIntent(intentUri);
                ResolveInfo info = packageManager.resolveActivity(intent, 0);
                if (info != null) {
                    title = info.loadLabel(packageManager);
                }
            } catch (URISyntaxException e) {
            }
            pref.setTitle(title);
            pref.setSummary(getString(R.string.quick_launch_shortcut,
                    String.valueOf(shortcut)));
            pref.setHasBookmark(true);
            newBookmarkedShortcuts.put(shortcut, true);
            if (noLongerBookmarkedShortcuts != null) {
                noLongerBookmarkedShortcuts.put(shortcut, false);
            }
        }
        if (noLongerBookmarkedShortcuts != null) {
            for (int i = noLongerBookmarkedShortcuts.size() - 1; i >= 0; i--) {
                if (noLongerBookmarkedShortcuts.valueAt(i)) {
                    char shortcut = (char) noLongerBookmarkedShortcuts.keyAt(i);
                    ShortcutPreference pref = mShortcutToPreference.get(shortcut);
                    if (pref != null) {
                        pref.setHasBookmark(false);
                    }
                }
            }
        }
        mBookmarkedShortcuts = newBookmarkedShortcuts;
        c.deactivate();
    }
    private class BookmarksObserver extends ContentObserver {
        public BookmarksObserver(Handler handler) {
            super(handler);
        }
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            refreshShortcuts();
        }
    }
}
