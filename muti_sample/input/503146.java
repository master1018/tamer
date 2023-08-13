public final class DeviceAdminInfo implements Parcelable {
    static final String TAG = "DeviceAdminInfo";
    public static final int USES_POLICY_LIMIT_PASSWORD = 0;
    public static final int USES_POLICY_WATCH_LOGIN = 1;
    public static final int USES_POLICY_RESET_PASSWORD = 2;
    public static final int USES_POLICY_FORCE_LOCK = 3;
    public static final int USES_POLICY_WIPE_DATA = 4;
    public static class PolicyInfo {
        public final int ident;
        final public String tag;
        final public int label;
        final public int description;
        public PolicyInfo(int identIn, String tagIn, int labelIn, int descriptionIn) {
            ident = identIn;
            tag = tagIn;
            label = labelIn;
            description = descriptionIn;
        }
    }
    static ArrayList<PolicyInfo> sPoliciesDisplayOrder = new ArrayList<PolicyInfo>();
    static HashMap<String, Integer> sKnownPolicies = new HashMap<String, Integer>();
    static SparseArray<PolicyInfo> sRevKnownPolicies = new SparseArray<PolicyInfo>();
    static {
        sPoliciesDisplayOrder.add(new PolicyInfo(USES_POLICY_WIPE_DATA, "wipe-data",
                com.android.internal.R.string.policylab_wipeData,
                com.android.internal.R.string.policydesc_wipeData));
        sPoliciesDisplayOrder.add(new PolicyInfo(USES_POLICY_RESET_PASSWORD, "reset-password",
                com.android.internal.R.string.policylab_resetPassword,
                com.android.internal.R.string.policydesc_resetPassword));
        sPoliciesDisplayOrder.add(new PolicyInfo(USES_POLICY_LIMIT_PASSWORD, "limit-password",
                com.android.internal.R.string.policylab_limitPassword,
                com.android.internal.R.string.policydesc_limitPassword));
        sPoliciesDisplayOrder.add(new PolicyInfo(USES_POLICY_WATCH_LOGIN, "watch-login",
                com.android.internal.R.string.policylab_watchLogin,
                com.android.internal.R.string.policydesc_watchLogin));
        sPoliciesDisplayOrder.add(new PolicyInfo(USES_POLICY_FORCE_LOCK, "force-lock",
                com.android.internal.R.string.policylab_forceLock,
                com.android.internal.R.string.policydesc_forceLock));
        for (int i=0; i<sPoliciesDisplayOrder.size(); i++) {
            PolicyInfo pi = sPoliciesDisplayOrder.get(i);
            sRevKnownPolicies.put(pi.ident, pi);
            sKnownPolicies.put(pi.tag, pi.ident);
        }
    }
    final ResolveInfo mReceiver;
    boolean mVisible;
    int mUsesPolicies;
    public DeviceAdminInfo(Context context, ResolveInfo receiver)
            throws XmlPullParserException, IOException {
        mReceiver = receiver;
        ActivityInfo ai = receiver.activityInfo;
        PackageManager pm = context.getPackageManager();
        XmlResourceParser parser = null;
        try {
            parser = ai.loadXmlMetaData(pm, DeviceAdminReceiver.DEVICE_ADMIN_META_DATA);
            if (parser == null) {
                throw new XmlPullParserException("No "
                        + DeviceAdminReceiver.DEVICE_ADMIN_META_DATA + " meta-data");
            }
            Resources res = pm.getResourcesForApplication(ai.applicationInfo);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            int type;
            while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
                    && type != XmlPullParser.START_TAG) {
            }
            String nodeName = parser.getName();
            if (!"device-admin".equals(nodeName)) {
                throw new XmlPullParserException(
                        "Meta-data does not start with device-admin tag");
            }
            TypedArray sa = res.obtainAttributes(attrs,
                    com.android.internal.R.styleable.DeviceAdmin);
            mVisible = sa.getBoolean(
                    com.android.internal.R.styleable.DeviceAdmin_visible, true);
            sa.recycle();
            int outerDepth = parser.getDepth();
            while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
                   && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
                if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                    continue;
                }
                String tagName = parser.getName();
                if (tagName.equals("uses-policies")) {
                    int innerDepth = parser.getDepth();
                    while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
                           && (type != XmlPullParser.END_TAG || parser.getDepth() > innerDepth)) {
                        if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                            continue;
                        }
                        String policyName = parser.getName();
                        Integer val = sKnownPolicies.get(policyName);
                        if (val != null) {
                            mUsesPolicies |= 1 << val.intValue();
                        } else {
                            Log.w(TAG, "Unknown tag under uses-policies of "
                                    + getComponent() + ": " + policyName);
                        }
                    }
                }
            }
        } catch (NameNotFoundException e) {
            throw new XmlPullParserException(
                    "Unable to create context for: " + ai.packageName);
        } finally {
            if (parser != null) parser.close();
        }
    }
    DeviceAdminInfo(Parcel source) {
        mReceiver = ResolveInfo.CREATOR.createFromParcel(source);
        mUsesPolicies = source.readInt();
    }
    public String getPackageName() {
        return mReceiver.activityInfo.packageName;
    }
    public String getReceiverName() {
        return mReceiver.activityInfo.name;
    }
    public ActivityInfo getActivityInfo() {
        return mReceiver.activityInfo;
    }
    public ComponentName getComponent() {
        return new ComponentName(mReceiver.activityInfo.packageName,
                mReceiver.activityInfo.name);
    }
    public CharSequence loadLabel(PackageManager pm) {
        return mReceiver.loadLabel(pm);
    }
    public CharSequence loadDescription(PackageManager pm) throws NotFoundException {
        if (mReceiver.activityInfo.descriptionRes != 0) {
            String packageName = mReceiver.resolvePackageName;
            ApplicationInfo applicationInfo = null;
            if (packageName == null) {
                packageName = mReceiver.activityInfo.packageName;
                applicationInfo = mReceiver.activityInfo.applicationInfo;
            }
            return pm.getText(packageName,
                    mReceiver.activityInfo.descriptionRes, applicationInfo);
        }
        throw new NotFoundException();
    }
    public Drawable loadIcon(PackageManager pm) {
        return mReceiver.loadIcon(pm);
    }
    public boolean isVisible() {
        return mVisible;
    }
    public boolean usesPolicy(int policyIdent) {
        return (mUsesPolicies & (1<<policyIdent)) != 0;
    }
    public String getTagForPolicy(int policyIdent) {
        return sRevKnownPolicies.get(policyIdent).tag;
    }
    public ArrayList<PolicyInfo> getUsedPolicies() {
        ArrayList<PolicyInfo> res = new ArrayList<PolicyInfo>();
        for (int i=0; i<sPoliciesDisplayOrder.size(); i++) {
            PolicyInfo pi = sPoliciesDisplayOrder.get(i);
            if (usesPolicy(pi.ident)) {
                res.add(pi);
            }
        }
        return res;
    }
    public void writePoliciesToXml(XmlSerializer out)
            throws IllegalArgumentException, IllegalStateException, IOException {
        out.attribute(null, "flags", Integer.toString(mUsesPolicies));
    }
    public void readPoliciesFromXml(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        mUsesPolicies = Integer.parseInt(
                parser.getAttributeValue(null, "flags"));
    }
    public void dump(Printer pw, String prefix) {
        pw.println(prefix + "Receiver:");
        mReceiver.dump(pw, prefix + "  ");
    }
    @Override
    public String toString() {
        return "DeviceAdminInfo{" + mReceiver.activityInfo.name + "}";
    }
    public void writeToParcel(Parcel dest, int flags) {
        mReceiver.writeToParcel(dest, flags);
        dest.writeInt(mUsesPolicies);
    }
    public static final Parcelable.Creator<DeviceAdminInfo> CREATOR =
            new Parcelable.Creator<DeviceAdminInfo>() {
        public DeviceAdminInfo createFromParcel(Parcel source) {
            return new DeviceAdminInfo(source);
        }
        public DeviceAdminInfo[] newArray(int size) {
            return new DeviceAdminInfo[size];
        }
    };
    public int describeContents() {
        return 0;
    }
}
