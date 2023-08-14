 class AccountAuthenticatorCache
        extends RegisteredServicesCache<AuthenticatorDescription> {
    private static final String TAG = "Account";
    private static final MySerializer sSerializer = new MySerializer();
    public AccountAuthenticatorCache(Context context) {
        super(context, AccountManager.ACTION_AUTHENTICATOR_INTENT,
                AccountManager.AUTHENTICATOR_META_DATA_NAME,
                AccountManager.AUTHENTICATOR_ATTRIBUTES_NAME, sSerializer);
    }
    public AuthenticatorDescription parseServiceAttributes(Resources res,
            String packageName, AttributeSet attrs) {
        TypedArray sa = res.obtainAttributes(attrs,
                com.android.internal.R.styleable.AccountAuthenticator);
        try {
            final String accountType =
                    sa.getString(com.android.internal.R.styleable.AccountAuthenticator_accountType);
            final int labelId = sa.getResourceId(
                    com.android.internal.R.styleable.AccountAuthenticator_label, 0);
            final int iconId = sa.getResourceId(
                    com.android.internal.R.styleable.AccountAuthenticator_icon, 0);
            final int smallIconId = sa.getResourceId(
                    com.android.internal.R.styleable.AccountAuthenticator_smallIcon, 0);
            final int prefId = sa.getResourceId(
                    com.android.internal.R.styleable.AccountAuthenticator_accountPreferences, 0);
            if (TextUtils.isEmpty(accountType)) {
                return null;
            }
            return new AuthenticatorDescription(accountType, packageName, labelId, iconId, 
                    smallIconId, prefId);
        } finally {
            sa.recycle();
        }
    }
    private static class MySerializer implements XmlSerializerAndParser<AuthenticatorDescription> {
        public void writeAsXml(AuthenticatorDescription item, XmlSerializer out)
                throws IOException {
            out.attribute(null, "type", item.type);
        }
        public AuthenticatorDescription createFromXml(XmlPullParser parser)
                throws IOException, XmlPullParserException {
            return AuthenticatorDescription.newKey(parser.getAttributeValue(null, "type"));
        }
    }
}
