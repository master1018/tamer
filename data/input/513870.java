public class LabeledIntent extends Intent {
    private String mSourcePackage;
    private int mLabelRes;
    private CharSequence mNonLocalizedLabel;
    private int mIcon;
    public LabeledIntent(Intent origIntent, String sourcePackage,
            int labelRes, int icon) {
        super(origIntent);
        mSourcePackage = sourcePackage;
        mLabelRes = labelRes;
        mNonLocalizedLabel = null;
        mIcon = icon;
    }
    public LabeledIntent(Intent origIntent, String sourcePackage,
            CharSequence nonLocalizedLabel, int icon) {
        super(origIntent);
        mSourcePackage = sourcePackage;
        mLabelRes = 0;
        mNonLocalizedLabel = nonLocalizedLabel;
        mIcon = icon;
    }
    public LabeledIntent(String sourcePackage, int labelRes, int icon) {
        mSourcePackage = sourcePackage;
        mLabelRes = labelRes;
        mNonLocalizedLabel = null;
        mIcon = icon;
    }
    public LabeledIntent(String sourcePackage,
            CharSequence nonLocalizedLabel, int icon) {
        mSourcePackage = sourcePackage;
        mLabelRes = 0;
        mNonLocalizedLabel = nonLocalizedLabel;
        mIcon = icon;
    }
    public String getSourcePackage() {
        return mSourcePackage;
    }
    public int getLabelResource() {
        return mLabelRes;
    }
    public CharSequence getNonLocalizedLabel() {
        return mNonLocalizedLabel;
    }
    public int getIconResource() {
        return mIcon;
    }
    public CharSequence loadLabel(PackageManager pm) {
        if (mNonLocalizedLabel != null) {
            return mNonLocalizedLabel;
        }
        if (mLabelRes != 0 && mSourcePackage != null) {
            CharSequence label = pm.getText(mSourcePackage, mLabelRes, null);
            if (label != null) {
                return label;
            }
        }
        return null;
    }
    public Drawable loadIcon(PackageManager pm) {
        if (mIcon != 0 && mSourcePackage != null) {
            Drawable icon = pm.getDrawable(mSourcePackage, mIcon, null);
            if (icon != null) {
                return icon;
            }
        }
        return null;
    }
    public void writeToParcel(Parcel dest, int parcelableFlags) {
        super.writeToParcel(dest, parcelableFlags);
        dest.writeString(mSourcePackage);
        dest.writeInt(mLabelRes);
        TextUtils.writeToParcel(mNonLocalizedLabel, dest, parcelableFlags);
        dest.writeInt(mIcon);
    }
    protected LabeledIntent(Parcel in) {
        readFromParcel(in);
    }
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
        mSourcePackage = in.readString();
        mLabelRes = in.readInt();
        mNonLocalizedLabel = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        mIcon = in.readInt();
    }
    public static final Creator<LabeledIntent> CREATOR
            = new Creator<LabeledIntent>() {
        public LabeledIntent createFromParcel(Parcel source) {
            return new LabeledIntent(source);
        }
        public LabeledIntent[] newArray(int size) {
            return new LabeledIntent[size];
        }
    };
}
