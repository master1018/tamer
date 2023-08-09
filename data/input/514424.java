public class TextAppearanceSpan extends MetricAffectingSpan implements ParcelableSpan {
    private final String mTypeface;
    private final int mStyle;
    private final int mTextSize;
    private final ColorStateList mTextColor;
    private final ColorStateList mTextColorLink;
    public TextAppearanceSpan(Context context, int appearance) {
        this(context, appearance, -1);
    }
    public TextAppearanceSpan(Context context, int appearance,
                              int colorList) {
        ColorStateList textColor;
        TypedArray a =
            context.obtainStyledAttributes(appearance,
                                           com.android.internal.R.styleable.TextAppearance);
        textColor = a.getColorStateList(com.android.internal.R.styleable.
                                        TextAppearance_textColor);
        mTextColorLink = a.getColorStateList(com.android.internal.R.styleable.
                                        TextAppearance_textColorLink);
        mTextSize = a.getDimensionPixelSize(com.android.internal.R.styleable.
                                        TextAppearance_textSize, -1);
        mStyle = a.getInt(com.android.internal.R.styleable.TextAppearance_textStyle, 0);
        int tf = a.getInt(com.android.internal.R.styleable.TextAppearance_typeface, 0);
        switch (tf) {
            case 1:
                mTypeface = "sans";
                break;
            case 2:
                mTypeface = "serif";
                break;
            case 3:
                mTypeface = "monospace";
                break;
            default:
                mTypeface = null;
                break;
        }
        a.recycle();
        if (colorList >= 0) {
            a = context.obtainStyledAttributes(com.android.internal.R.style.Theme,
                                            com.android.internal.R.styleable.Theme);
            textColor = a.getColorStateList(colorList);
            a.recycle();
        }
        mTextColor = textColor;
    }
    public TextAppearanceSpan(String family, int style, int size,
                              ColorStateList color, ColorStateList linkColor) {
        mTypeface = family;
        mStyle = style;
        mTextSize = size;
        mTextColor = color;
        mTextColorLink = linkColor;
    }
    public TextAppearanceSpan(Parcel src) {
        mTypeface = src.readString();
        mStyle = src.readInt();
        mTextSize = src.readInt();
        if (src.readInt() != 0) {
            mTextColor = ColorStateList.CREATOR.createFromParcel(src);
        } else {
            mTextColor = null;
        }
        if (src.readInt() != 0) {
            mTextColorLink = ColorStateList.CREATOR.createFromParcel(src);
        } else {
            mTextColorLink = null;
        }
    }
    public int getSpanTypeId() {
        return TextUtils.TEXT_APPEARANCE_SPAN;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTypeface);
        dest.writeInt(mStyle);
        dest.writeInt(mTextSize);
        if (mTextColor != null) {
            dest.writeInt(1);
            mTextColor.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        if (mTextColorLink != null) {
            dest.writeInt(1);
            mTextColorLink.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
    }
    public String getFamily() {
        return mTypeface;
    }
    public ColorStateList getTextColor() {
        return mTextColor;
    }
    public ColorStateList getLinkTextColor() {
        return mTextColorLink;
    }
    public int getTextSize() {
        return mTextSize;
    }
    public int getTextStyle() {
        return mStyle;
    }
    @Override
    public void updateDrawState(TextPaint ds) {
        updateMeasureState(ds);
        if (mTextColor != null) {
            ds.setColor(mTextColor.getColorForState(ds.drawableState, 0));
        }
        if (mTextColorLink != null) {
            ds.linkColor = mTextColor.getColorForState(ds.drawableState, 0);
        }
    }
    @Override
    public void updateMeasureState(TextPaint ds) {
        if (mTypeface != null || mStyle != 0) {
            Typeface tf = ds.getTypeface();
            int style = 0;
            if (tf != null) {
                style = tf.getStyle();
            }
            style |= mStyle;
            if (mTypeface != null) {
                tf = Typeface.create(mTypeface, style);
            } else if (tf == null) {
                tf = Typeface.defaultFromStyle(style);
            } else {
                tf = Typeface.create(tf, style);
            }
            int fake = style & ~tf.getStyle();
            if ((fake & Typeface.BOLD) != 0) {
                ds.setFakeBoldText(true);
            }
            if ((fake & Typeface.ITALIC) != 0) {
                ds.setTextSkewX(-0.25f);
            }
            ds.setTypeface(tf);
        }
        if (mTextSize > 0) {
            ds.setTextSize(mTextSize);
        }
    }
}
