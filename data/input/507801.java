public class PhotoPriorityResolver  {
    private static final String TAG = "PhotoPriorityResolver";
    public static final int DEFAULT_PRIORITY = 7;
    private static final String METADATA_CONTACTS = "android.provider.CONTACTS_STRUCTURE";
    private static final String PICTURE_TAG = "Picture";
    private static final String PRIORITY_ATTR = "priority";
    private Context mContext;
    private HashMap<String, Integer> mPhotoPriorities = Maps.newHashMap();
    public PhotoPriorityResolver(Context context) {
        mContext = context;
    }
    public synchronized int getPhotoPriority(String accountType) {
        if (accountType == null) {
            return DEFAULT_PRIORITY;
        }
        Integer priority = mPhotoPriorities.get(accountType);
        if (priority == null) {
            priority = resolvePhotoPriority(accountType);
            mPhotoPriorities.put(accountType, priority);
        }
        return priority;
     }
    private int resolvePhotoPriority(String accountType) {
        final AccountManager am = AccountManager.get(mContext);
        for (AuthenticatorDescription auth : am.getAuthenticatorTypes()) {
            if (accountType.equals(auth.type)) {
                return resolvePhotoPriorityFromMetaData(auth.packageName);
            }
        }
        return DEFAULT_PRIORITY;
    }
     int resolvePhotoPriorityFromMetaData(String packageName) {
        final PackageManager pm = mContext.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_SERVICES
                    | PackageManager.GET_META_DATA);
            if (pi != null && pi.services != null) {
                for (ServiceInfo si : pi.services) {
                    final XmlResourceParser parser = si.loadXmlMetaData(pm, METADATA_CONTACTS);
                    if (parser != null) {
                        return loadPhotoPriorityFromXml(mContext, parser);
                    }
                }
            }
        } catch (NameNotFoundException e) {
            Log.w(TAG, "Problem loading photo priorities: " + e.toString());
        }
        return DEFAULT_PRIORITY;
    }
    private int loadPhotoPriorityFromXml(Context context, XmlPullParser parser) {
        int priority = DEFAULT_PRIORITY;
        try {
            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG
                    && type != XmlPullParser.END_DOCUMENT) {
            }
            if (type != XmlPullParser.START_TAG) {
                throw new IllegalStateException("No start tag found");
            }
            final int depth = parser.getDepth();
            while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
                    && type != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();
                if (type == XmlPullParser.START_TAG && PICTURE_TAG.equals(name)) {
                    int attributeCount = parser.getAttributeCount();
                    for (int i = 0; i < attributeCount; i++) {
                        String attr = parser.getAttributeName(i);
                        if (PRIORITY_ATTR.equals(attr)) {
                            priority = XmlUtils.convertValueToInt(parser.getAttributeValue(i),
                                    DEFAULT_PRIORITY);
                        } else {
                            throw new IllegalStateException("Unsupported attribute " + attr);
                        }
                    }
                }
            }
        } catch (XmlPullParserException e) {
            throw new IllegalStateException("Problem reading XML", e);
        } catch (IOException e) {
            throw new IllegalStateException("Problem reading XML", e);
        }
        return priority;
    }
}
