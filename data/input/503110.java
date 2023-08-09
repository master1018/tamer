public final class Configuration implements Parcelable, Comparable<Configuration> {
    public float fontScale;
    public int mcc;
    public int mnc;
    public Locale locale;
    public boolean userSetLocale;
    public static final int SCREENLAYOUT_SIZE_MASK = 0x0f;
    public static final int SCREENLAYOUT_SIZE_UNDEFINED = 0x00;
    public static final int SCREENLAYOUT_SIZE_SMALL = 0x01;
    public static final int SCREENLAYOUT_SIZE_NORMAL = 0x02;
    public static final int SCREENLAYOUT_SIZE_LARGE = 0x03;
    public static final int SCREENLAYOUT_LONG_MASK = 0x30;
    public static final int SCREENLAYOUT_LONG_UNDEFINED = 0x00;
    public static final int SCREENLAYOUT_LONG_NO = 0x10;
    public static final int SCREENLAYOUT_LONG_YES = 0x20;
    public static final int SCREENLAYOUT_COMPAT_NEEDED = 0x10000000;
    public int screenLayout;
    public static final int TOUCHSCREEN_UNDEFINED = 0;
    public static final int TOUCHSCREEN_NOTOUCH = 1;
    public static final int TOUCHSCREEN_STYLUS = 2;
    public static final int TOUCHSCREEN_FINGER = 3;
    public int touchscreen;
    public static final int KEYBOARD_UNDEFINED = 0;
    public static final int KEYBOARD_NOKEYS = 1;
    public static final int KEYBOARD_QWERTY = 2;
    public static final int KEYBOARD_12KEY = 3;
    public int keyboard;
    public static final int KEYBOARDHIDDEN_UNDEFINED = 0;
    public static final int KEYBOARDHIDDEN_NO = 1;
    public static final int KEYBOARDHIDDEN_YES = 2;
    public static final int KEYBOARDHIDDEN_SOFT = 3;
    public int keyboardHidden;
    public static final int HARDKEYBOARDHIDDEN_UNDEFINED = 0;
    public static final int HARDKEYBOARDHIDDEN_NO = 1;
    public static final int HARDKEYBOARDHIDDEN_YES = 2;
    public int hardKeyboardHidden;
    public static final int NAVIGATION_UNDEFINED = 0;
    public static final int NAVIGATION_NONAV = 1;
    public static final int NAVIGATION_DPAD = 2;
    public static final int NAVIGATION_TRACKBALL = 3;
    public static final int NAVIGATION_WHEEL = 4;
    public int navigation;
    public static final int NAVIGATIONHIDDEN_UNDEFINED = 0;
    public static final int NAVIGATIONHIDDEN_NO = 1;
    public static final int NAVIGATIONHIDDEN_YES = 2;
    public int navigationHidden;
    public static final int ORIENTATION_UNDEFINED = 0;
    public static final int ORIENTATION_PORTRAIT = 1;
    public static final int ORIENTATION_LANDSCAPE = 2;
    public static final int ORIENTATION_SQUARE = 3;
    public int orientation;
    public static final int UI_MODE_TYPE_MASK = 0x0f;
    public static final int UI_MODE_TYPE_UNDEFINED = 0x00;
    public static final int UI_MODE_TYPE_NORMAL = 0x01;
    public static final int UI_MODE_TYPE_DESK = 0x02;
    public static final int UI_MODE_TYPE_CAR = 0x03;
    public static final int UI_MODE_NIGHT_MASK = 0x30;
    public static final int UI_MODE_NIGHT_UNDEFINED = 0x00;
    public static final int UI_MODE_NIGHT_NO = 0x10;
    public static final int UI_MODE_NIGHT_YES = 0x20;
    public int uiMode;
    public int seq;
    public Configuration() {
        setToDefaults();
    }
    public Configuration(Configuration o) {
        setTo(o);
    }
    public void setTo(Configuration o) {
        fontScale = o.fontScale;
        mcc = o.mcc;
        mnc = o.mnc;
        if (o.locale != null) {
            locale = (Locale) o.locale.clone();
        }
        userSetLocale = o.userSetLocale;
        touchscreen = o.touchscreen;
        keyboard = o.keyboard;
        keyboardHidden = o.keyboardHidden;
        hardKeyboardHidden = o.hardKeyboardHidden;
        navigation = o.navigation;
        navigationHidden = o.navigationHidden;
        orientation = o.orientation;
        screenLayout = o.screenLayout;
        uiMode = o.uiMode;
        seq = o.seq;
    }
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("{ scale=");
        sb.append(fontScale);
        sb.append(" imsi=");
        sb.append(mcc);
        sb.append("/");
        sb.append(mnc);
        sb.append(" loc=");
        sb.append(locale);
        sb.append(" touch=");
        sb.append(touchscreen);
        sb.append(" keys=");
        sb.append(keyboard);
        sb.append("/");
        sb.append(keyboardHidden);
        sb.append("/");
        sb.append(hardKeyboardHidden);
        sb.append(" nav=");
        sb.append(navigation);
        sb.append("/");
        sb.append(navigationHidden);
        sb.append(" orien=");
        sb.append(orientation);
        sb.append(" layout=");
        sb.append(screenLayout);
        sb.append(" uiMode=");
        sb.append(uiMode);
        if (seq != 0) {
            sb.append(" seq=");
            sb.append(seq);
        }
        sb.append('}');
        return sb.toString();
    }
    public void setToDefaults() {
        fontScale = 1;
        mcc = mnc = 0;
        locale = null;
        userSetLocale = false;
        touchscreen = TOUCHSCREEN_UNDEFINED;
        keyboard = KEYBOARD_UNDEFINED;
        keyboardHidden = KEYBOARDHIDDEN_UNDEFINED;
        hardKeyboardHidden = HARDKEYBOARDHIDDEN_UNDEFINED;
        navigation = NAVIGATION_UNDEFINED;
        navigationHidden = NAVIGATIONHIDDEN_UNDEFINED;
        orientation = ORIENTATION_UNDEFINED;
        screenLayout = SCREENLAYOUT_SIZE_UNDEFINED;
        uiMode = UI_MODE_TYPE_UNDEFINED;
        seq = 0;
    }
    @Deprecated public void makeDefault() {
        setToDefaults();
    }
    public int updateFrom(Configuration delta) {
        int changed = 0;
        if (delta.fontScale > 0 && fontScale != delta.fontScale) {
            changed |= ActivityInfo.CONFIG_FONT_SCALE;
            fontScale = delta.fontScale;
        }
        if (delta.mcc != 0 && mcc != delta.mcc) {
            changed |= ActivityInfo.CONFIG_MCC;
            mcc = delta.mcc;
        }
        if (delta.mnc != 0 && mnc != delta.mnc) {
            changed |= ActivityInfo.CONFIG_MNC;
            mnc = delta.mnc;
        }
        if (delta.locale != null
                && (locale == null || !locale.equals(delta.locale))) {
            changed |= ActivityInfo.CONFIG_LOCALE;
            locale = delta.locale != null
                    ? (Locale) delta.locale.clone() : null;
        }
        if (delta.userSetLocale && (!userSetLocale || ((changed & ActivityInfo.CONFIG_LOCALE) != 0)))
        {
            userSetLocale = true;
            changed |= ActivityInfo.CONFIG_LOCALE;
        }
        if (delta.touchscreen != TOUCHSCREEN_UNDEFINED
                && touchscreen != delta.touchscreen) {
            changed |= ActivityInfo.CONFIG_TOUCHSCREEN;
            touchscreen = delta.touchscreen;
        }
        if (delta.keyboard != KEYBOARD_UNDEFINED
                && keyboard != delta.keyboard) {
            changed |= ActivityInfo.CONFIG_KEYBOARD;
            keyboard = delta.keyboard;
        }
        if (delta.keyboardHidden != KEYBOARDHIDDEN_UNDEFINED
                && keyboardHidden != delta.keyboardHidden) {
            changed |= ActivityInfo.CONFIG_KEYBOARD_HIDDEN;
            keyboardHidden = delta.keyboardHidden;
        }
        if (delta.hardKeyboardHidden != HARDKEYBOARDHIDDEN_UNDEFINED
                && hardKeyboardHidden != delta.hardKeyboardHidden) {
            changed |= ActivityInfo.CONFIG_KEYBOARD_HIDDEN;
            hardKeyboardHidden = delta.hardKeyboardHidden;
        }
        if (delta.navigation != NAVIGATION_UNDEFINED
                && navigation != delta.navigation) {
            changed |= ActivityInfo.CONFIG_NAVIGATION;
            navigation = delta.navigation;
        }
        if (delta.navigationHidden != NAVIGATIONHIDDEN_UNDEFINED
                && navigationHidden != delta.navigationHidden) {
            changed |= ActivityInfo.CONFIG_KEYBOARD_HIDDEN;
            navigationHidden = delta.navigationHidden;
        }
        if (delta.orientation != ORIENTATION_UNDEFINED
                && orientation != delta.orientation) {
            changed |= ActivityInfo.CONFIG_ORIENTATION;
            orientation = delta.orientation;
        }
        if (delta.screenLayout != SCREENLAYOUT_SIZE_UNDEFINED
                && screenLayout != delta.screenLayout) {
            changed |= ActivityInfo.CONFIG_SCREEN_LAYOUT;
            screenLayout = delta.screenLayout;
        }
        if (delta.uiMode != (UI_MODE_TYPE_UNDEFINED|UI_MODE_NIGHT_UNDEFINED)
                && uiMode != delta.uiMode) {
            changed |= ActivityInfo.CONFIG_UI_MODE;
            if ((delta.uiMode&UI_MODE_TYPE_MASK) != UI_MODE_TYPE_UNDEFINED) {
                uiMode = (uiMode&~UI_MODE_TYPE_MASK)
                        | (delta.uiMode&UI_MODE_TYPE_MASK);
            }
            if ((delta.uiMode&UI_MODE_NIGHT_MASK) != UI_MODE_NIGHT_UNDEFINED) {
                uiMode = (uiMode&~UI_MODE_NIGHT_MASK)
                        | (delta.uiMode&UI_MODE_NIGHT_MASK);
            }
        }
        if (delta.seq != 0) {
            seq = delta.seq;
        }
        return changed;
    }
    public int diff(Configuration delta) {
        int changed = 0;
        if (delta.fontScale > 0 && fontScale != delta.fontScale) {
            changed |= ActivityInfo.CONFIG_FONT_SCALE;
        }
        if (delta.mcc != 0 && mcc != delta.mcc) {
            changed |= ActivityInfo.CONFIG_MCC;
        }
        if (delta.mnc != 0 && mnc != delta.mnc) {
            changed |= ActivityInfo.CONFIG_MNC;
        }
        if (delta.locale != null
                && (locale == null || !locale.equals(delta.locale))) {
            changed |= ActivityInfo.CONFIG_LOCALE;
        }
        if (delta.touchscreen != TOUCHSCREEN_UNDEFINED
                && touchscreen != delta.touchscreen) {
            changed |= ActivityInfo.CONFIG_TOUCHSCREEN;
        }
        if (delta.keyboard != KEYBOARD_UNDEFINED
                && keyboard != delta.keyboard) {
            changed |= ActivityInfo.CONFIG_KEYBOARD;
        }
        if (delta.keyboardHidden != KEYBOARDHIDDEN_UNDEFINED
                && keyboardHidden != delta.keyboardHidden) {
            changed |= ActivityInfo.CONFIG_KEYBOARD_HIDDEN;
        }
        if (delta.hardKeyboardHidden != HARDKEYBOARDHIDDEN_UNDEFINED
                && hardKeyboardHidden != delta.hardKeyboardHidden) {
            changed |= ActivityInfo.CONFIG_KEYBOARD_HIDDEN;
        }
        if (delta.navigation != NAVIGATION_UNDEFINED
                && navigation != delta.navigation) {
            changed |= ActivityInfo.CONFIG_NAVIGATION;
        }
        if (delta.navigationHidden != NAVIGATIONHIDDEN_UNDEFINED
                && navigationHidden != delta.navigationHidden) {
            changed |= ActivityInfo.CONFIG_KEYBOARD_HIDDEN;
        }
        if (delta.orientation != ORIENTATION_UNDEFINED
                && orientation != delta.orientation) {
            changed |= ActivityInfo.CONFIG_ORIENTATION;
        }
        if (delta.screenLayout != SCREENLAYOUT_SIZE_UNDEFINED
                && screenLayout != delta.screenLayout) {
            changed |= ActivityInfo.CONFIG_SCREEN_LAYOUT;
        }
        if (delta.uiMode != (UI_MODE_TYPE_UNDEFINED|UI_MODE_NIGHT_UNDEFINED)
                && uiMode != delta.uiMode) {
            changed |= ActivityInfo.CONFIG_UI_MODE;
        }
        return changed;
    }
    public static boolean needNewResources(int configChanges, int interestingChanges) {
        return (configChanges & (interestingChanges|ActivityInfo.CONFIG_FONT_SCALE)) != 0;
    }
    public boolean isOtherSeqNewer(Configuration other) {
        if (other == null) {
            return false;
        }
        if (other.seq == 0) {
            return true;
        }
        if (seq == 0) {
            return true;
        }
        int diff = other.seq - seq;
        if (diff > 0x10000) {
            return false;
        }
        return diff > 0;
    }
    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(fontScale);
        dest.writeInt(mcc);
        dest.writeInt(mnc);
        if (locale == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            dest.writeString(locale.getLanguage());
            dest.writeString(locale.getCountry());
            dest.writeString(locale.getVariant());
        }
        if(userSetLocale) {
            dest.writeInt(1);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(touchscreen);
        dest.writeInt(keyboard);
        dest.writeInt(keyboardHidden);
        dest.writeInt(hardKeyboardHidden);
        dest.writeInt(navigation);
        dest.writeInt(navigationHidden);
        dest.writeInt(orientation);
        dest.writeInt(screenLayout);
        dest.writeInt(uiMode);
        dest.writeInt(seq);
    }
    public void readFromParcel(Parcel source) {
        fontScale = source.readFloat();
        mcc = source.readInt();
        mnc = source.readInt();
        if (source.readInt() != 0) {
            locale = new Locale(source.readString(), source.readString(),
                    source.readString());
        }
        userSetLocale = (source.readInt()==1);
        touchscreen = source.readInt();
        keyboard = source.readInt();
        keyboardHidden = source.readInt();
        hardKeyboardHidden = source.readInt();
        navigation = source.readInt();
        navigationHidden = source.readInt();
        orientation = source.readInt();
        screenLayout = source.readInt();
        uiMode = source.readInt();
        seq = source.readInt();
    }
    public static final Parcelable.Creator<Configuration> CREATOR
            = new Parcelable.Creator<Configuration>() {
        public Configuration createFromParcel(Parcel source) {
            return new Configuration(source);
        }
        public Configuration[] newArray(int size) {
            return new Configuration[size];
        }
    };
    private Configuration(Parcel source) {
        readFromParcel(source);
    }
    public int compareTo(Configuration that) {
        int n;
        float a = this.fontScale;
        float b = that.fontScale;
        if (a < b) return -1;
        if (a > b) return 1;
        n = this.mcc - that.mcc;
        if (n != 0) return n;
        n = this.mnc - that.mnc;
        if (n != 0) return n;
        if (this.locale == null) {
            if (that.locale != null) return 1;
        } else if (that.locale == null) {
            return -1;
        } else {
            n = this.locale.getLanguage().compareTo(that.locale.getLanguage());
            if (n != 0) return n;
            n = this.locale.getCountry().compareTo(that.locale.getCountry());
            if (n != 0) return n;
            n = this.locale.getVariant().compareTo(that.locale.getVariant());
            if (n != 0) return n;
        }
        n = this.touchscreen - that.touchscreen;
        if (n != 0) return n;
        n = this.keyboard - that.keyboard;
        if (n != 0) return n;
        n = this.keyboardHidden - that.keyboardHidden;
        if (n != 0) return n;
        n = this.hardKeyboardHidden - that.hardKeyboardHidden;
        if (n != 0) return n;
        n = this.navigation - that.navigation;
        if (n != 0) return n;
        n = this.navigationHidden - that.navigationHidden;
        if (n != 0) return n;
        n = this.orientation - that.orientation;
        if (n != 0) return n;
        n = this.screenLayout - that.screenLayout;
        if (n != 0) return n;
        n = this.uiMode - that.uiMode;
        return n;
    }
    public boolean equals(Configuration that) {
        if (that == null) return false;
        if (that == this) return true;
        return this.compareTo(that) == 0;
    }
    public boolean equals(Object that) {
        try {
            return equals((Configuration)that);
        } catch (ClassCastException e) {
        }
        return false;
    }
    public int hashCode() {
        return ((int)this.fontScale) + this.mcc + this.mnc
                + (this.locale != null ? this.locale.hashCode() : 0)
                + this.touchscreen
                + this.keyboard + this.keyboardHidden + this.hardKeyboardHidden
                + this.navigation + this.navigationHidden
                + this.orientation + this.screenLayout + this.uiMode;
    }
}
