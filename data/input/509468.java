public class ExternalSource extends FallbackSource {
    private static final String ACTION_SYNC_ADAPTER = "android.content.SyncAdapter";
    private static final String METADATA_CONTACTS = "android.provider.CONTACTS_STRUCTURE";
    private interface InflateTags {
        final String CONTACTS_SOURCE = "ContactsSource";
        final String CONTACTS_DATA_KIND = "ContactsDataKind";
    }
    public ExternalSource(String resPackageName) {
        this.resPackageName = resPackageName;
        this.summaryResPackageName = resPackageName;
    }
    @Override
    public void inflate(Context context, int inflateLevel) {
        final PackageManager pm = context.getPackageManager();
        final Intent syncAdapter = new Intent(ACTION_SYNC_ADAPTER);
        final List<ResolveInfo> matches = pm.queryIntentServices(syncAdapter,
                PackageManager.GET_META_DATA);
        for (ResolveInfo info : matches) {
            final XmlResourceParser parser = info.serviceInfo.loadXmlMetaData(pm,
                    METADATA_CONTACTS);
            if (parser == null) continue;
            inflate(context, parser);
        }
        inflateStructuredName(context, inflateLevel);
        inflatePhoto(context, inflateLevel);
        setInflatedLevel(inflateLevel);
    }
    protected void inflate(Context context, XmlPullParser parser) {
        final AttributeSet attrs = Xml.asAttributeSet(parser);
        try {
            int type;
            while ((type = parser.next()) != XmlPullParser.START_TAG
                    && type != XmlPullParser.END_DOCUMENT) {
            }
            if (type != XmlPullParser.START_TAG) {
                throw new IllegalStateException("No start tag found");
            }
            if (!InflateTags.CONTACTS_SOURCE.equals(parser.getName())) {
                throw new IllegalStateException("Top level element must be "
                        + InflateTags.CONTACTS_SOURCE);
            }
            final int depth = parser.getDepth();
            while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
                    && type != XmlPullParser.END_DOCUMENT) {
                if (type == XmlPullParser.END_TAG
                        || !InflateTags.CONTACTS_DATA_KIND.equals(parser.getName())) {
                    continue;
                }
                final TypedArray a = context.obtainStyledAttributes(attrs,
                        android.R.styleable.ContactsDataKind);
                final DataKind kind = new DataKind();
                kind.mimeType = a
                        .getString(com.android.internal.R.styleable.ContactsDataKind_mimeType);
                kind.iconRes = a.getResourceId(
                        com.android.internal.R.styleable.ContactsDataKind_icon, -1);
                final String summaryColumn = a
                        .getString(com.android.internal.R.styleable.ContactsDataKind_summaryColumn);
                if (summaryColumn != null) {
                    kind.actionHeader = new FallbackSource.SimpleInflater(summaryColumn);
                }
                final String detailColumn = a
                        .getString(com.android.internal.R.styleable.ContactsDataKind_detailColumn);
                final boolean detailSocialSummary = a.getBoolean(
                        com.android.internal.R.styleable.ContactsDataKind_detailSocialSummary,
                        false);
                if (detailSocialSummary) {
                    kind.actionBodySocial = true;
                }
                if (detailColumn != null) {
                    kind.actionBody = new FallbackSource.SimpleInflater(detailColumn);
                }
                addKind(kind);
            }
        } catch (XmlPullParserException e) {
            throw new IllegalStateException("Problem reading XML", e);
        } catch (IOException e) {
            throw new IllegalStateException("Problem reading XML", e);
        }
    }
    @Override
    public int getHeaderColor(Context context) {
        return 0xff6d86b4;
    }
    @Override
    public int getSideBarColor(Context context) {
        return 0xff6d86b4;
    }
}
