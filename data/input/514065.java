public class SoftKeyboard {
    private int mSkbXmlId;
    private boolean mCacheFlag;
    private boolean mStickyFlag;
    private int mCacheId;
    private boolean mNewlyLoadedFlag = true;
    private int mSkbCoreWidth;
    private int mSkbCoreHeight;
    private SkbTemplate mSkbTemplate;
    private boolean mIsQwerty;
    private boolean mIsQwertyUpperCase;
    private int mEnabledRowId;
    private List<KeyRow> mKeyRows;
    public Drawable mSkbBg;
    private Drawable mBalloonBg;
    private Drawable mPopupBg;
    private float mKeyXMargin = 0;
    private float mKeyYMargin = 0;
    private Rect mTmpRect = new Rect();
    public SoftKeyboard(int skbXmlId, SkbTemplate skbTemplate, int skbWidth,
            int skbHeight) {
        mSkbXmlId = skbXmlId;
        mSkbTemplate = skbTemplate;
        mSkbCoreWidth = skbWidth;
        mSkbCoreHeight = skbHeight;
    }
    public void setFlags(boolean cacheFlag, boolean stickyFlag,
            boolean isQwerty, boolean isQwertyUpperCase) {
        mCacheFlag = cacheFlag;
        mStickyFlag = stickyFlag;
        mIsQwerty = isQwerty;
        mIsQwertyUpperCase = isQwertyUpperCase;
    }
    public boolean getCacheFlag() {
        return mCacheFlag;
    }
    public void setCacheId(int cacheId) {
        mCacheId = cacheId;
    }
    public boolean getStickyFlag() {
        return mStickyFlag;
    }
    public void setSkbBackground(Drawable skbBg) {
        mSkbBg = skbBg;
    }
    public void setPopupBackground(Drawable popupBg) {
        mPopupBg = popupBg;
    }
    public void setKeyBalloonBackground(Drawable balloonBg) {
        mBalloonBg = balloonBg;
    }
    public void setKeyMargins(float xMargin, float yMargin) {
        mKeyXMargin = xMargin;
        mKeyYMargin = yMargin;
    }
    public int getCacheId() {
        return mCacheId;
    }
    public void reset() {
        if (null != mKeyRows) mKeyRows.clear();
    }
    public void setNewlyLoadedFlag(boolean newlyLoadedFlag) {
        mNewlyLoadedFlag = newlyLoadedFlag;
    }
    public boolean getNewlyLoadedFlag() {
        return mNewlyLoadedFlag;
    }
    public void beginNewRow(int rowId, float yStartingPos) {
        if (null == mKeyRows) mKeyRows = new ArrayList<KeyRow>();
        KeyRow keyRow = new KeyRow();
        keyRow.mRowId = rowId;
        keyRow.mTopF = yStartingPos;
        keyRow.mBottomF = yStartingPos;
        keyRow.mSoftKeys = new ArrayList<SoftKey>();
        mKeyRows.add(keyRow);
    }
    public boolean addSoftKey(SoftKey softKey) {
        if (mKeyRows.size() == 0) return false;
        KeyRow keyRow = mKeyRows.get(mKeyRows.size() - 1);
        if (null == keyRow) return false;
        List<SoftKey> softKeys = keyRow.mSoftKeys;
        softKey.setSkbCoreSize(mSkbCoreWidth, mSkbCoreHeight);
        softKeys.add(softKey);
        if (softKey.mTopF < keyRow.mTopF) {
            keyRow.mTopF = softKey.mTopF;
        }
        if (softKey.mBottomF > keyRow.mBottomF) {
            keyRow.mBottomF = softKey.mBottomF;
        }
        return true;
    }
    public int getSkbXmlId() {
        return mSkbXmlId;
    }
    public void setSkbCoreSize(int skbCoreWidth, int skbCoreHeight) {
        if (null == mKeyRows
                || (skbCoreWidth == mSkbCoreWidth && skbCoreHeight == mSkbCoreHeight)) {
            return;
        }
        for (int row = 0; row < mKeyRows.size(); row++) {
            KeyRow keyRow = mKeyRows.get(row);
            keyRow.mBottom = (int) (skbCoreHeight * keyRow.mBottomF);
            keyRow.mTop = (int) (skbCoreHeight * keyRow.mTopF);
            List<SoftKey> softKeys = keyRow.mSoftKeys;
            for (int i = 0; i < softKeys.size(); i++) {
                SoftKey softKey = softKeys.get(i);
                softKey.setSkbCoreSize(skbCoreWidth, skbCoreHeight);
            }
        }
        mSkbCoreWidth = skbCoreWidth;
        mSkbCoreHeight = skbCoreHeight;
    }
    public int getSkbCoreWidth() {
        return mSkbCoreWidth;
    }
    public int getSkbCoreHeight() {
        return mSkbCoreHeight;
    }
    public int getSkbTotalWidth() {
        Rect padding = getPadding();
        return mSkbCoreWidth + padding.left + padding.right;
    }
    public int getSkbTotalHeight() {
        Rect padding = getPadding();
        return mSkbCoreHeight + padding.top + padding.bottom;
    }
    public int getKeyXMargin() {
        Environment env = Environment.getInstance();
        return (int) (mKeyXMargin * mSkbCoreWidth * env.getKeyXMarginFactor());
    }
    public int getKeyYMargin() {
        Environment env = Environment.getInstance();
        return (int) (mKeyYMargin * mSkbCoreHeight * env.getKeyYMarginFactor());
    }
    public Drawable getSkbBackground() {
        if (null != mSkbBg) return mSkbBg;
        return mSkbTemplate.getSkbBackground();
    }
    public Drawable getBalloonBackground() {
        if (null != mBalloonBg) return mBalloonBg;
        return mSkbTemplate.getBalloonBackground();
    }
    public Drawable getPopupBackground() {
        if (null != mPopupBg) return mPopupBg;
        return mSkbTemplate.getPopupBackground();
    }
    public int getRowNum() {
        if (null != mKeyRows) {
            return mKeyRows.size();
        }
        return 0;
    }
    public KeyRow getKeyRowForDisplay(int row) {
        if (null != mKeyRows && mKeyRows.size() > row) {
            KeyRow keyRow = mKeyRows.get(row);
            if (KeyRow.ALWAYS_SHOW_ROW_ID == keyRow.mRowId
                    || keyRow.mRowId == mEnabledRowId) {
                return keyRow;
            }
        }
        return null;
    }
    public SoftKey getKey(int row, int location) {
        if (null != mKeyRows && mKeyRows.size() > row) {
            List<SoftKey> softKeys = mKeyRows.get(row).mSoftKeys;
            if (softKeys.size() > location) {
                return softKeys.get(location);
            }
        }
        return null;
    }
    public SoftKey mapToKey(int x, int y) {
        if (null == mKeyRows) {
            return null;
        }
        int rowNum = mKeyRows.size();
        for (int row = 0; row < rowNum; row++) {
            KeyRow keyRow = mKeyRows.get(row);
            if (KeyRow.ALWAYS_SHOW_ROW_ID != keyRow.mRowId
                    && keyRow.mRowId != mEnabledRowId) continue;
            if (keyRow.mTop > y && keyRow.mBottom <= y) continue;
            List<SoftKey> softKeys = keyRow.mSoftKeys;
            int keyNum = softKeys.size();
            for (int i = 0; i < keyNum; i++) {
                SoftKey sKey = softKeys.get(i);
                if (sKey.mLeft <= x && sKey.mTop <= y && sKey.mRight > x
                        && sKey.mBottom > y) {
                    return sKey;
                }
            }
        }
        SoftKey nearestKey = null;
        float nearestDis = Float.MAX_VALUE;
        for (int row = 0; row < rowNum; row++) {
            KeyRow keyRow = mKeyRows.get(row);
            if (KeyRow.ALWAYS_SHOW_ROW_ID != keyRow.mRowId
                    && keyRow.mRowId != mEnabledRowId) continue;
            if (keyRow.mTop > y && keyRow.mBottom <= y) continue;
            List<SoftKey> softKeys = keyRow.mSoftKeys;
            int keyNum = softKeys.size();
            for (int i = 0; i < keyNum; i++) {
                SoftKey sKey = softKeys.get(i);
                int disx = (sKey.mLeft + sKey.mRight) / 2 - x;
                int disy = (sKey.mTop + sKey.mBottom) / 2 - y;
                float dis = disx * disx + disy * disy;
                if (dis < nearestDis) {
                    nearestDis = dis;
                    nearestKey = sKey;
                }
            }
        }
        return nearestKey;
    }
    public void switchQwertyMode(int toggle_state_id, boolean upperCase) {
        if (!mIsQwerty) return;
        int rowNum = mKeyRows.size();
        for (int row = 0; row < rowNum; row++) {
            KeyRow keyRow = mKeyRows.get(row);
            List<SoftKey> softKeys = keyRow.mSoftKeys;
            int keyNum = softKeys.size();
            for (int i = 0; i < keyNum; i++) {
                SoftKey sKey = softKeys.get(i);
                if (sKey instanceof SoftKeyToggle) {
                    ((SoftKeyToggle) sKey).enableToggleState(toggle_state_id,
                            true);
                }
                if (sKey.mKeyCode >= KeyEvent.KEYCODE_A
                        && sKey.mKeyCode <= KeyEvent.KEYCODE_Z) {
                    sKey.changeCase(upperCase);
                }
            }
        }
    }
    public void enableToggleState(int toggleStateId, boolean resetIfNotFound) {
        int rowNum = mKeyRows.size();
        for (int row = 0; row < rowNum; row++) {
            KeyRow keyRow = mKeyRows.get(row);
            List<SoftKey> softKeys = keyRow.mSoftKeys;
            int keyNum = softKeys.size();
            for (int i = 0; i < keyNum; i++) {
                SoftKey sKey = softKeys.get(i);
                if (sKey instanceof SoftKeyToggle) {
                    ((SoftKeyToggle) sKey).enableToggleState(toggleStateId,
                            resetIfNotFound);
                }
            }
        }
    }
    public void disableToggleState(int toggleStateId, boolean resetIfNotFound) {
        int rowNum = mKeyRows.size();
        for (int row = 0; row < rowNum; row++) {
            KeyRow keyRow = mKeyRows.get(row);
            List<SoftKey> softKeys = keyRow.mSoftKeys;
            int keyNum = softKeys.size();
            for (int i = 0; i < keyNum; i++) {
                SoftKey sKey = softKeys.get(i);
                if (sKey instanceof SoftKeyToggle) {
                    ((SoftKeyToggle) sKey).disableToggleState(toggleStateId,
                            resetIfNotFound);
                }
            }
        }
    }
    public void enableToggleStates(ToggleStates toggleStates) {
        if (null == toggleStates) return;
        enableRow(toggleStates.mRowIdToEnable);
        boolean isQwerty = toggleStates.mQwerty;
        boolean isQwertyUpperCase = toggleStates.mQwertyUpperCase;
        boolean needUpdateQwerty = (isQwerty && mIsQwerty && (mIsQwertyUpperCase != isQwertyUpperCase));
        int states[] = toggleStates.mKeyStates;
        int statesNum = toggleStates.mKeyStatesNum;
        int rowNum = mKeyRows.size();
        for (int row = 0; row < rowNum; row++) {
            KeyRow keyRow = mKeyRows.get(row);
            if (KeyRow.ALWAYS_SHOW_ROW_ID != keyRow.mRowId
                    && keyRow.mRowId != mEnabledRowId) {
                continue;
            }
            List<SoftKey> softKeys = keyRow.mSoftKeys;
            int keyNum = softKeys.size();
            for (int keyPos = 0; keyPos < keyNum; keyPos++) {
                SoftKey sKey = softKeys.get(keyPos);
                if (sKey instanceof SoftKeyToggle) {
                    for (int statePos = 0; statePos < statesNum; statePos++) {
                        ((SoftKeyToggle) sKey).enableToggleState(
                                states[statePos], statePos == 0);
                    }
                    if (0 == statesNum) {
                        ((SoftKeyToggle) sKey).disableAllToggleStates();
                    }
                }
                if (needUpdateQwerty) {
                    if (sKey.mKeyCode >= KeyEvent.KEYCODE_A
                            && sKey.mKeyCode <= KeyEvent.KEYCODE_Z) {
                        sKey.changeCase(isQwertyUpperCase);
                    }
                }
            }
        }
        mIsQwertyUpperCase = isQwertyUpperCase;
    }
    private Rect getPadding() {
        mTmpRect.set(0, 0, 0, 0);
        Drawable skbBg = getSkbBackground();
        if (null == skbBg) return mTmpRect;
        skbBg.getPadding(mTmpRect);
        return mTmpRect;
    }
    private boolean enableRow(int rowId) {
        if (KeyRow.ALWAYS_SHOW_ROW_ID == rowId) return false;
        boolean enabled = false;
        int rowNum = mKeyRows.size();
        for (int row = rowNum - 1; row >= 0; row--) {
            if (mKeyRows.get(row).mRowId == rowId) {
                enabled = true;
                break;
            }
        }
        if (enabled) {
            mEnabledRowId = rowId;
        }
        return enabled;
    }
    @Override
    public String toString() {
        String str = "------------------SkbInfo----------------------\n";
        String endStr = "-----------------------------------------------\n";
        str += "Width: " + String.valueOf(mSkbCoreWidth) + "\n";
        str += "Height: " + String.valueOf(mSkbCoreHeight) + "\n";
        str += "KeyRowNum: " + mKeyRows == null ? "0" : String.valueOf(mKeyRows
                .size())
                + "\n";
        if (null == mKeyRows) return str + endStr;
        int rowNum = mKeyRows.size();
        for (int row = 0; row < rowNum; row++) {
            KeyRow keyRow = mKeyRows.get(row);
            List<SoftKey> softKeys = keyRow.mSoftKeys;
            int keyNum = softKeys.size();
            for (int i = 0; i < softKeys.size(); i++) {
                str += "-key " + String.valueOf(i) + ":"
                        + softKeys.get(i).toString();
            }
        }
        return str + endStr;
    }
    public String toShortString() {
        return super.toString();
    }
    class KeyRow {
        static final int ALWAYS_SHOW_ROW_ID = -1;
        static final int DEFAULT_ROW_ID = 0;
        List<SoftKey> mSoftKeys;
        int mRowId;
        float mTopF;
        float mBottomF;
        int mTop;
        int mBottom;
    }
}
