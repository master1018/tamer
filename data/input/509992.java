public class TextKeyListener extends BaseKeyListener implements SpanWatcher {
    private static TextKeyListener[] sInstance =
        new TextKeyListener[Capitalize.values().length * 2];
     static final Object ACTIVE = new NoCopySpan.Concrete();
     static final Object CAPPED = new NoCopySpan.Concrete();
     static final Object INHIBIT_REPLACEMENT = new NoCopySpan.Concrete();
     static final Object LAST_TYPED = new NoCopySpan.Concrete();
    private Capitalize mAutoCap;
    private boolean mAutoText;
    private int mPrefs;
    private boolean mPrefsInited;
     static final int AUTO_CAP = 1;
     static final int AUTO_TEXT = 2;
     static final int AUTO_PERIOD = 4;
     static final int SHOW_PASSWORD = 8;
    private WeakReference<ContentResolver> mResolver;
    private TextKeyListener.SettingsObserver mObserver;
    public TextKeyListener(Capitalize cap, boolean autotext) {
        mAutoCap = cap;
        mAutoText = autotext;
    }
    public static TextKeyListener getInstance(boolean autotext,
                                              Capitalize cap) {
        int off = cap.ordinal() * 2 + (autotext ? 1 : 0);
        if (sInstance[off] == null) {
            sInstance[off] = new TextKeyListener(cap, autotext);
        }
        return sInstance[off];
    }
    public static TextKeyListener getInstance() {
        return getInstance(false, Capitalize.NONE);
    }
    public static boolean shouldCap(Capitalize cap, CharSequence cs, int off) {
        int i;
        char c;
        if (cap == Capitalize.NONE) {
            return false;
        }
        if (cap == Capitalize.CHARACTERS) {
            return true;
        }
        return TextUtils.getCapsMode(cs, off, cap == Capitalize.WORDS
                ? TextUtils.CAP_MODE_WORDS : TextUtils.CAP_MODE_SENTENCES)
                != 0;
    }
    public int getInputType() {
        return makeTextContentType(mAutoCap, mAutoText);
    }
    @Override
    public boolean onKeyDown(View view, Editable content,
                             int keyCode, KeyEvent event) {
        KeyListener im = getKeyListener(event);
        return im.onKeyDown(view, content, keyCode, event);
    }
    @Override
    public boolean onKeyUp(View view, Editable content,
                           int keyCode, KeyEvent event) {
        KeyListener im = getKeyListener(event);
        return im.onKeyUp(view, content, keyCode, event);
    }
    @Override
    public boolean onKeyOther(View view, Editable content, KeyEvent event) {
        KeyListener im = getKeyListener(event);
        return im.onKeyOther(view, content, event);
    }
    public static void clear(Editable e) {
        e.clear();
        e.removeSpan(ACTIVE);
        e.removeSpan(CAPPED);
        e.removeSpan(INHIBIT_REPLACEMENT);
        e.removeSpan(LAST_TYPED);
        QwertyKeyListener.Replaced[] repl = e.getSpans(0, e.length(),
                                   QwertyKeyListener.Replaced.class);
        final int count = repl.length;
        for (int i = 0; i < count; i++) {
            e.removeSpan(repl[i]);
        }
    }
    public void onSpanAdded(Spannable s, Object what, int start, int end) { }
    public void onSpanRemoved(Spannable s, Object what, int start, int end) { }
    public void onSpanChanged(Spannable s, Object what, int start, int end,
                              int st, int en) {
        if (what == Selection.SELECTION_END) {
            s.removeSpan(ACTIVE);
        }
    }
    private KeyListener getKeyListener(KeyEvent event) {
        KeyCharacterMap kmap = KeyCharacterMap.load(event.getKeyboardDevice());
        int kind = kmap.getKeyboardType();
        if (kind == KeyCharacterMap.ALPHA) {
            return QwertyKeyListener.getInstance(mAutoText, mAutoCap);
        } else if (kind == KeyCharacterMap.NUMERIC) {
            return MultiTapKeyListener.getInstance(mAutoText, mAutoCap);
        }
        return NullKeyListener.getInstance();
    }
    public enum Capitalize {
        NONE, SENTENCES, WORDS, CHARACTERS,
    }
    private static class NullKeyListener implements KeyListener
    {
        public int getInputType() {
            return InputType.TYPE_NULL;
        }
        public boolean onKeyDown(View view, Editable content,
                                 int keyCode, KeyEvent event) {
            return false;
        }
        public boolean onKeyUp(View view, Editable content, int keyCode,
                                        KeyEvent event) {
            return false;
        }
        public boolean onKeyOther(View view, Editable content, KeyEvent event) {
            return false;
        }
        public void clearMetaKeyState(View view, Editable content, int states) {
        }
        public static NullKeyListener getInstance() {
            if (sInstance != null)
                return sInstance;
            sInstance = new NullKeyListener();
            return sInstance;
        }
        private static NullKeyListener sInstance;
    }
    public void release() {
        if (mResolver != null) {
            final ContentResolver contentResolver = mResolver.get();
            if (contentResolver != null) {
                contentResolver.unregisterContentObserver(mObserver);
                mResolver.clear();
            }
            mObserver = null;
            mResolver = null;
            mPrefsInited = false;
        }
    }
    private void initPrefs(Context context) {
        final ContentResolver contentResolver = context.getContentResolver();
        mResolver = new WeakReference<ContentResolver>(contentResolver);
        mObserver = new SettingsObserver();
        contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, mObserver);
        updatePrefs(contentResolver);
        mPrefsInited = true;
    }
    private class SettingsObserver extends ContentObserver {
        public SettingsObserver() {
            super(new Handler());
        }
        @Override
        public void onChange(boolean selfChange) {
            if (mResolver != null) {
                final ContentResolver contentResolver = mResolver.get();
                if (contentResolver == null) {
                    mPrefsInited = false;
                } else {
                    updatePrefs(contentResolver);
                }
            } else {
                mPrefsInited = false;
            }
        }
    }
    private void updatePrefs(ContentResolver resolver) {
        boolean cap = System.getInt(resolver, System.TEXT_AUTO_CAPS, 1) > 0;
        boolean text = System.getInt(resolver, System.TEXT_AUTO_REPLACE, 1) > 0;
        boolean period = System.getInt(resolver, System.TEXT_AUTO_PUNCTUATE, 1) > 0;
        boolean pw = System.getInt(resolver, System.TEXT_SHOW_PASSWORD, 1) > 0;
        mPrefs = (cap ? AUTO_CAP : 0) |
                 (text ? AUTO_TEXT : 0) |
                 (period ? AUTO_PERIOD : 0) |
                 (pw ? SHOW_PASSWORD : 0);
    }
     int getPrefs(Context context) {
        synchronized (this) {
            if (!mPrefsInited || mResolver.get() == null) {
                initPrefs(context);
            }
        }
        return mPrefs;
    }
}
