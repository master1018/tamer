class KeyIconRecord {
    int keyCode;
    Drawable icon;
    Drawable iconPopup;
}
class KeyRecord {
    int keyId;
    SoftKey softKey;
}
public class SkbTemplate {
    private int mSkbTemplateId;
    private Drawable mSkbBg;
    private Drawable mBalloonBg;
    private Drawable mPopupBg;
    private float mXMargin = 0;
    private float mYMargin = 0;
    private Vector<SoftKeyType> mKeyTypeList = new Vector<SoftKeyType>();
    private Vector<KeyIconRecord> mKeyIconRecords = new Vector<KeyIconRecord>();
    private Vector<KeyRecord> mKeyRecords = new Vector<KeyRecord>();
    public SkbTemplate(int skbTemplateId) {
        mSkbTemplateId = skbTemplateId;
    }
    public int getSkbTemplateId() {
        return mSkbTemplateId;
    }
    public void setBackgrounds(Drawable skbBg, Drawable balloonBg,
            Drawable popupBg) {
        mSkbBg = skbBg;
        mBalloonBg = balloonBg;
        mPopupBg = popupBg;
    }
    public Drawable getSkbBackground() {
        return mSkbBg;
    }
    public Drawable getBalloonBackground() {
        return mBalloonBg;
    }
    public Drawable getPopupBackground() {
        return mPopupBg;
    }
    public void setMargins(float xMargin, float yMargin) {
        mXMargin = xMargin;
        mYMargin = yMargin;
    }
    public float getXMargin() {
        return mXMargin;
    }
    public float getYMargin() {
        return mYMargin;
    }
    public SoftKeyType createKeyType(int id, Drawable bg, Drawable hlBg) {
        return new SoftKeyType(id, bg, hlBg);
    }
    public boolean addKeyType(SoftKeyType keyType) {
        if (mKeyTypeList.size() != keyType.mKeyTypeId) return false;
        mKeyTypeList.add(keyType);
        return true;
    }
    public SoftKeyType getKeyType(int typeId) {
        if (typeId < 0 || typeId > mKeyTypeList.size()) return null;
        return mKeyTypeList.elementAt(typeId);
    }
    public void addDefaultKeyIcons(int keyCode, Drawable icon,
            Drawable iconPopup) {
        if (null == icon || null == iconPopup) return;
        KeyIconRecord iconRecord = new KeyIconRecord();
        iconRecord.icon = icon;
        iconRecord.iconPopup = iconPopup;
        iconRecord.keyCode = keyCode;
        int size = mKeyIconRecords.size();
        int pos = 0;
        while (pos < size) {
            if (mKeyIconRecords.get(pos).keyCode >= keyCode) break;
            pos++;
        }
        mKeyIconRecords.add(pos, iconRecord);
    }
    public Drawable getDefaultKeyIcon(int keyCode) {
        int size = mKeyIconRecords.size();
        int pos = 0;
        while (pos < size) {
            KeyIconRecord iconRecord = mKeyIconRecords.get(pos);
            if (iconRecord.keyCode < keyCode) {
                pos++;
                continue;
            }
            if (iconRecord.keyCode == keyCode) {
                return iconRecord.icon;
            }
            return null;
        }
        return null;
    }
    public Drawable getDefaultKeyIconPopup(int keyCode) {
        int size = mKeyIconRecords.size();
        int pos = 0;
        while (pos < size) {
            KeyIconRecord iconRecord = mKeyIconRecords.get(pos);
            if (iconRecord.keyCode < keyCode) {
                pos++;
                continue;
            }
            if (iconRecord.keyCode == keyCode) {
                return iconRecord.iconPopup;
            }
            return null;
        }
        return null;
    }
    public void addDefaultKey(int keyId, SoftKey softKey) {
        if (null == softKey) return;
        KeyRecord keyRecord = new KeyRecord();
        keyRecord.keyId = keyId;
        keyRecord.softKey = softKey;
        int size = mKeyRecords.size();
        int pos = 0;
        while (pos < size) {
            if (mKeyRecords.get(pos).keyId >= keyId) break;
            pos++;
        }
        mKeyRecords.add(pos, keyRecord);
    }
    public SoftKey getDefaultKey(int keyId) {
        int size = mKeyRecords.size();
        int pos = 0;
        while (pos < size) {
            KeyRecord keyRecord = mKeyRecords.get(pos);
            if (keyRecord.keyId < keyId) {
                pos++;
                continue;
            }
            if (keyRecord.keyId == keyId) {
                return keyRecord.softKey;
            }
            return null;
        }
        return null;
    }
}
class SoftKeyType {
    public static final int KEYTYPE_ID_NORMAL_KEY = 0;
    public int mKeyTypeId;
    public Drawable mKeyBg;
    public Drawable mKeyHlBg;
    public int mColor;
    public int mColorHl;
    public int mColorBalloon;
    SoftKeyType(int id, Drawable bg, Drawable hlBg) {
        mKeyTypeId = id;
        mKeyBg = bg;
        mKeyHlBg = hlBg;
    }
    public void setColors(int color, int colorHl, int colorBalloon) {
        mColor = color;
        mColorHl = colorHl;
        mColorBalloon = colorBalloon;
    }
}
