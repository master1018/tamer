public class ContactsDictionary extends ExpandableDictionary {
    private static final String[] PROJECTION = {
        Contacts._ID,
        Contacts.DISPLAY_NAME,
    };
    private static final int INDEX_NAME = 1;
    private ContentObserver mObserver;
    private long mLastLoadedContacts;
    public ContactsDictionary(Context context) {
        super(context);
        ContentResolver cres = context.getContentResolver();
        cres.registerContentObserver(Contacts.CONTENT_URI, true, mObserver = new ContentObserver(null) {
            @Override
            public void onChange(boolean self) {
                setRequiresReload(true);
            }
        });
        loadDictionary();
    }
    public synchronized void close() {
        if (mObserver != null) {
            getContext().getContentResolver().unregisterContentObserver(mObserver);
            mObserver = null;
        }
        super.close();
    }
    @Override
    public void startDictionaryLoadingTaskLocked() {
        long now = SystemClock.uptimeMillis();
        if (mLastLoadedContacts == 0
                || now - mLastLoadedContacts > 30 * 60 * 1000 ) {
            super.startDictionaryLoadingTaskLocked();
        }
    }
    @Override
    public void loadDictionaryAsync() {
        Cursor cursor = getContext().getContentResolver()
                .query(Contacts.CONTENT_URI, PROJECTION, null, null, null);
        if (cursor != null) {
            addWords(cursor);
        }
        mLastLoadedContacts = SystemClock.uptimeMillis();
    }
    private void addWords(Cursor cursor) {
        clearDictionary();
        final int maxWordLength = getMaxWordLength();
        if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast()) {
                String name = cursor.getString(INDEX_NAME);
                if (name != null) {
                    int len = name.length();
                    for (int i = 0; i < len; i++) {
                        if (Character.isLetter(name.charAt(i))) {
                            int j;
                            for (j = i + 1; j < len; j++) {
                                char c = name.charAt(j);
                                if (!(c == '-' || c == '\'' ||
                                      Character.isLetter(c))) {
                                    break;
                                }
                            }
                            String word = name.substring(i, j);
                            i = j - 1;
                            final int wordLen = word.length();
                            if (wordLen < maxWordLength && wordLen > 1) {
                                super.addWord(word, 128);
                            }
                        }
                    }
                }
                cursor.moveToNext();
            }
        }
        cursor.close();
    }
}
