public class AccountSettingsUtils {
    public static void commitSettings(Context context, EmailContent.Account account) {
        if (!account.isSaved()) {
            account.save(context);
        } else {
            ContentValues cv = new ContentValues();
            cv.put(AccountColumns.IS_DEFAULT, account.mIsDefault);
            cv.put(AccountColumns.DISPLAY_NAME, account.getDisplayName());
            cv.put(AccountColumns.SENDER_NAME, account.getSenderName());
            cv.put(AccountColumns.SIGNATURE, account.getSignature());
            cv.put(AccountColumns.SYNC_INTERVAL, account.mSyncInterval);
            cv.put(AccountColumns.RINGTONE_URI, account.mRingtoneUri);
            cv.put(AccountColumns.FLAGS, account.mFlags);
            cv.put(AccountColumns.SYNC_LOOKBACK, account.mSyncLookback);
            account.update(context, cv);
        }
        AccountBackupRestore.backupAccounts(context);
    }
    public static Provider findProviderForDomain(Context context, String domain) {
        Provider p = VendorPolicyLoader.getInstance(context).findProviderForDomain(domain);
        if (p == null) {
            p = findProviderForDomain(context, domain, R.xml.providers_product);
        }
        if (p == null) {
            p = findProviderForDomain(context, domain, R.xml.providers);
        }
        return p;
    }
    private static Provider findProviderForDomain(Context context, String domain, int resourceId) {
        try {
            XmlResourceParser xml = context.getResources().getXml(resourceId);
            int xmlEventType;
            Provider provider = null;
            while ((xmlEventType = xml.next()) != XmlResourceParser.END_DOCUMENT) {
                if (xmlEventType == XmlResourceParser.START_TAG
                        && "provider".equals(xml.getName())
                        && domain.equalsIgnoreCase(getXmlAttribute(context, xml, "domain"))) {
                    provider = new Provider();
                    provider.id = getXmlAttribute(context, xml, "id");
                    provider.label = getXmlAttribute(context, xml, "label");
                    provider.domain = getXmlAttribute(context, xml, "domain");
                    provider.note = getXmlAttribute(context, xml, "note");
                }
                else if (xmlEventType == XmlResourceParser.START_TAG
                        && "incoming".equals(xml.getName())
                        && provider != null) {
                    provider.incomingUriTemplate = new URI(getXmlAttribute(context, xml, "uri"));
                    provider.incomingUsernameTemplate = getXmlAttribute(context, xml, "username");
                }
                else if (xmlEventType == XmlResourceParser.START_TAG
                        && "outgoing".equals(xml.getName())
                        && provider != null) {
                    provider.outgoingUriTemplate = new URI(getXmlAttribute(context, xml, "uri"));
                    provider.outgoingUsernameTemplate = getXmlAttribute(context, xml, "username");
                }
                else if (xmlEventType == XmlResourceParser.END_TAG
                        && "provider".equals(xml.getName())
                        && provider != null) {
                    return provider;
                }
            }
        }
        catch (Exception e) {
            Log.e(Email.LOG_TAG, "Error while trying to load provider settings.", e);
        }
        return null;
    }
    private static String getXmlAttribute(Context context, XmlResourceParser xml, String name) {
        int resId = xml.getAttributeResourceValue(null, name, 0);
        if (resId == 0) {
            return xml.getAttributeValue(null, name);
        }
        else {
            return context.getString(resId);
        }
    }
    public static class Provider implements Serializable {
        private static final long serialVersionUID = 8511656164616538989L;
        public String id;
        public String label;
        public String domain;
        public URI incomingUriTemplate;
        public String incomingUsernameTemplate;
        public URI outgoingUriTemplate;
        public String outgoingUsernameTemplate;
        public String note;
    }
}
