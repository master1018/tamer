public class PasswordEntryKeyboard extends Keyboard {
    private static final String TAG = "PasswordEntryKeyboard";
    private static final int SHIFT_OFF = 0;
    private static final int SHIFT_ON = 1;
    private static final int SHIFT_LOCKED = 2;
    public static final int KEYCODE_SPACE = ' ';
    private Drawable mShiftIcon;
    private Drawable mShiftLockIcon;
    private Drawable mShiftLockPreviewIcon;
    private Drawable mOldShiftIcon;
    private Drawable mOldShiftPreviewIcon;
    private Drawable mSpaceIcon;
    private Key mShiftKey;
    private Key mEnterKey;
    private Key mF1Key;
    private Key mSpaceKey;
    private Locale mLocale;
    private Resources mRes;
    private int mExtensionResId;
    private int mShiftState = SHIFT_OFF;
    static int sSpacebarVerticalCorrection;
    public PasswordEntryKeyboard(Context context, int xmlLayoutResId) {
        this(context, xmlLayoutResId, 0);
    }
    public PasswordEntryKeyboard(Context context, int xmlLayoutResId, int mode) {
        super(context, xmlLayoutResId, mode);
        final Resources res = context.getResources();
        mRes = res;
        mShiftIcon = res.getDrawable(R.drawable.sym_keyboard_shift);
        mShiftLockIcon = res.getDrawable(R.drawable.sym_keyboard_shift_locked);
        mShiftLockPreviewIcon = res.getDrawable(R.drawable.sym_keyboard_feedback_shift_locked);
        mShiftLockPreviewIcon.setBounds(0, 0,
                mShiftLockPreviewIcon.getIntrinsicWidth(),
                mShiftLockPreviewIcon.getIntrinsicHeight());
        mSpaceIcon = res.getDrawable(R.drawable.sym_keyboard_space);
        sSpacebarVerticalCorrection = res.getDimensionPixelOffset(
                R.dimen.password_keyboard_spacebar_vertical_correction);
    }
    public PasswordEntryKeyboard(Context context, int layoutTemplateResId,
            CharSequence characters, int columns, int horizontalPadding) {
        super(context, layoutTemplateResId, characters, columns, horizontalPadding);
    }
    @Override
    protected Key createKeyFromXml(Resources res, Row parent, int x, int y,
            XmlResourceParser parser) {
        LatinKey key = new LatinKey(res, parent, x, y, parser);
        final int code = key.codes[0];
        if (code >=0 && code != '\n' && (code < 32 || code > 127)) {
            key.label = " ";
            key.setEnabled(false);
        }
        switch (key.codes[0]) {
            case 10:
                mEnterKey = key;
                break;
            case PasswordEntryKeyboardView.KEYCODE_F1:
                mF1Key = key;
                break;
            case 32:
                mSpaceKey = key;
                break;
        }
        return key;
    }
    void setEnterKeyResources(Resources res, int previewId, int iconId, int labelId) {
        if (mEnterKey != null) {
            mEnterKey.popupCharacters = null;
            mEnterKey.popupResId = 0;
            mEnterKey.text = null;
            mEnterKey.iconPreview = res.getDrawable(previewId);
            mEnterKey.icon = res.getDrawable(iconId);
            mEnterKey.label = res.getText(labelId);
            if (mEnterKey.iconPreview != null) {
                mEnterKey.iconPreview.setBounds(0, 0,
                        mEnterKey.iconPreview.getIntrinsicWidth(),
                        mEnterKey.iconPreview.getIntrinsicHeight());
            }
        }
    }
    void enableShiftLock() {
        int index = getShiftKeyIndex();
        if (index >= 0) {
            mShiftKey = getKeys().get(index);
            if (mShiftKey instanceof LatinKey) {
                ((LatinKey)mShiftKey).enableShiftLock();
            }
            mOldShiftIcon = mShiftKey.icon;
            mOldShiftPreviewIcon = mShiftKey.iconPreview;
        }
    }
    void setShiftLocked(boolean shiftLocked) {
        if (mShiftKey != null) {
            if (shiftLocked) {
                mShiftKey.on = true;
                mShiftKey.icon = mShiftLockIcon;
                mShiftState = SHIFT_LOCKED;
            } else {
                mShiftKey.on = false;
                mShiftKey.icon = mShiftLockIcon;
                mShiftState = SHIFT_ON;
            }
        }
    }
    @Override
    public boolean setShifted(boolean shiftState) {
        boolean shiftChanged = false;
        if (mShiftKey != null) {
            if (shiftState == false) {
                shiftChanged = mShiftState != SHIFT_OFF;
                mShiftState = SHIFT_OFF;
                mShiftKey.on = false;
                mShiftKey.icon = mOldShiftIcon;
            } else if (mShiftState == SHIFT_OFF) {
                shiftChanged = mShiftState == SHIFT_OFF;
                mShiftState = SHIFT_ON;
                mShiftKey.on = false;
                mShiftKey.icon = mShiftIcon;
            }
        } else {
            return super.setShifted(shiftState);
        }
        return shiftChanged;
    }
    @Override
    public boolean isShifted() {
        if (mShiftKey != null) {
            return mShiftState != SHIFT_OFF;
        } else {
            return super.isShifted();
        }
    }
    static class LatinKey extends Keyboard.Key {
        private boolean mShiftLockEnabled;
        private boolean mEnabled = true;
        public LatinKey(Resources res, Keyboard.Row parent, int x, int y,
                XmlResourceParser parser) {
            super(res, parent, x, y, parser);
            if (popupCharacters != null && popupCharacters.length() == 0) {
                popupResId = 0;
            }
        }
        void setEnabled(boolean enabled) {
            mEnabled = enabled;
        }
        void enableShiftLock() {
            mShiftLockEnabled = true;
        }
        @Override
        public void onReleased(boolean inside) {
            if (!mShiftLockEnabled) {
                super.onReleased(inside);
            } else {
                pressed = !pressed;
            }
        }
        @Override
        public boolean isInside(int x, int y) {
            if (!mEnabled) {
                return false;
            }
            final int code = codes[0];
            if (code == KEYCODE_SHIFT || code == KEYCODE_DELETE) {
                y -= height / 10;
                if (code == KEYCODE_SHIFT) x += width / 6;
                if (code == KEYCODE_DELETE) x -= width / 6;
            } else if (code == KEYCODE_SPACE) {
                y += PasswordEntryKeyboard.sSpacebarVerticalCorrection;
            }
            return super.isInside(x, y);
        }
    }
}
