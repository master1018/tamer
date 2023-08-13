public class TextModel extends RegionMediaModel {
    private static final String TAG = "Mms/text";
    private CharSequence mText;
    private final int mCharset;
    public TextModel(Context context, String contentType, String src, RegionModel region) {
        this(context, contentType, src, CharacterSets.UTF_8, new byte[0], region);
    }
    public TextModel(Context context, String contentType, String src,
            int charset, byte[] data, RegionModel region) {
        super(context, SmilHelper.ELEMENT_TAG_TEXT, contentType, src,
                data != null ? data : new byte[0], region);
        if (charset == CharacterSets.ANY_CHARSET) {
            charset = CharacterSets.ISO_8859_1;
        }
        mCharset = charset;
        mText = extractTextFromData(data);
    }
    private CharSequence extractTextFromData(byte[] data) {
        if (data != null) {
            try {
                if (CharacterSets.ANY_CHARSET == mCharset) {
                    return new String(data); 
                } else {
                    String name = CharacterSets.getMimeName(mCharset);
                    return new String(data, name);
                }
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unsupported encoding: " + mCharset, e);
                return new String(data); 
            }
        }
        return "";
    }
    public TextModel(Context context, String contentType, String src, int charset,
            DrmWrapper wrapper, RegionModel regionModel) throws IOException {
        super(context, SmilHelper.ELEMENT_TAG_TEXT, contentType, src, wrapper, regionModel);
        if (charset == CharacterSets.ANY_CHARSET) {
            charset = CharacterSets.ISO_8859_1;
        }
        mCharset = charset;
    }
    public String getText() {
        if (mText == null) {
            try {
                mText = extractTextFromData(getData());
            } catch (DrmException e) {
                Log.e(TAG, e.getMessage(), e);
                mText = e.getMessage();
            }
        }
        if (!(mText instanceof String)) {
            mText = mText.toString();
        }
        return mText.toString();
    }
    public void setText(CharSequence text) {
        mText = text;
        notifyModelChanged(true);
    }
    public void cloneText() {
        mText = new String(mText.toString());
    }
    public int getCharset() {
        return mCharset;
    }
    public void handleEvent(Event evt) {
        if (evt.getType().equals(SmilMediaElementImpl.SMIL_MEDIA_START_EVENT)) {
            mVisible = true;
        } else if (mFill != ElementTime.FILL_FREEZE) {
            mVisible = false;
        }
        notifyModelChanged(false);
    }
}
