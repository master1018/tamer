public abstract class Store {
    public static final String STORE_SCHEME_IMAP = "imap";
    public static final String STORE_SCHEME_POP3 = "pop3";
    public static final String STORE_SCHEME_EAS = "eas";
    public static final String STORE_SCHEME_LOCAL = "local";
    public static final String STORE_SECURITY_SSL = "+ssl";
    public static final String STORE_SECURITY_TLS = "+tls";
    public static final String STORE_SECURITY_TRUST_CERTIFICATES = "+trustallcerts";
    public static final int FETCH_BODY_SANE_SUGGESTED_SIZE = (50 * 1024);
    private static java.util.HashMap<String, Store> mStores =
        new java.util.HashMap<String, Store>();
    public static Store newInstance(String uri, Context context, PersistentDataCallbacks callbacks)
            throws MessagingException {
        throw new MessagingException("Store.newInstance: Unknown scheme in " + uri);
    }
    private static Store instantiateStore(String className, String uri, Context context, 
            PersistentDataCallbacks callbacks)
        throws MessagingException {
        Object o = null;
        try {
            Class<?> c = Class.forName(className);
            java.lang.reflect.Method m =
                c.getMethod("newInstance", String.class, Context.class, 
                        PersistentDataCallbacks.class);
            o = m.invoke(null, uri, context, callbacks);
        } catch (Exception e) {
            Log.d(Email.LOG_TAG, String.format(
                    "exception %s invoking %s.newInstance.(String, Context) method for %s",
                    e.toString(), className, uri));
            throw new MessagingException("can not instantiate Store object for " + uri);
        }
        if (!(o instanceof Store)) {
            throw new MessagingException(
                    uri + ": " + className + " create incompatible object");
        }
        return (Store) o;
    }
    public static class StoreInfo {
        public String mScheme;
        public String mClassName;
        public boolean mPushSupported = false;
        public int mVisibleLimitDefault;
        public int mVisibleLimitIncrement;
        public int mAccountInstanceLimit;
        public static StoreInfo getStoreInfo(String scheme, Context context) {
            StoreInfo result = getStoreInfo(R.xml.stores_product, scheme, context);
            if (result == null) {
                result = getStoreInfo(R.xml.stores, scheme, context);
            }
            return result;
        }
        public static StoreInfo getStoreInfo(int resourceId, String scheme, Context context) {
            try {
                XmlResourceParser xml = context.getResources().getXml(resourceId);
                int xmlEventType;
                while ((xmlEventType = xml.next()) != XmlResourceParser.END_DOCUMENT) {
                    if (xmlEventType == XmlResourceParser.START_TAG && 
                            "store".equals(xml.getName())) {
                        String xmlScheme = xml.getAttributeValue(null, "scheme");
                        if (scheme != null && scheme.startsWith(xmlScheme)) {
                            StoreInfo result = new StoreInfo();
                            result.mScheme = xmlScheme;
                            result.mClassName = xml.getAttributeValue(null, "class");
                            result.mPushSupported = xml.getAttributeBooleanValue(
                                    null, "push", false);
                            result.mVisibleLimitDefault = xml.getAttributeIntValue(
                                    null, "visibleLimitDefault", Email.VISIBLE_LIMIT_DEFAULT);
                            result.mVisibleLimitIncrement = xml.getAttributeIntValue(
                                    null, "visibleLimitIncrement", Email.VISIBLE_LIMIT_INCREMENT);
                            result.mAccountInstanceLimit = xml.getAttributeIntValue(
                                    null, "accountInstanceLimit", -1);
                            return result;
                        }
                    }
                }
            } catch (XmlPullParserException e) {
            } catch (IOException e) {
            }
            return null;
        }
    }
    public synchronized static Store getInstance(String uri, Context context, 
            PersistentDataCallbacks callbacks)
        throws MessagingException {
        Store store = mStores.get(uri);
        if (store == null) {
            StoreInfo info = StoreInfo.getStoreInfo(uri, context);
            if (info != null) {
                store = instantiateStore(info.mClassName, uri, context, callbacks);
            }
            if (store != null) {
                mStores.put(uri, store);
            }
        } else {
            store.setPersistentDataCallbacks(callbacks);
        }
        if (store == null) {
            throw new MessagingException("Unable to locate an applicable Store for " + uri);
        }
        return store;
    }
    public synchronized static void removeInstance(String storeUri) {
        mStores.remove(storeUri);
    }
    public Class<? extends android.app.Activity> getSettingActivityClass() {
        return com.android.email.activity.setup.AccountSetupIncoming.class;
    }
    public StoreSynchronizer getMessageSynchronizer() {
        return null;
    }
    public boolean requireStructurePrefetch() {
        return false;
    }
    public boolean requireCopyMessageToSentFolder() {
        return true;
    }
    public abstract Folder getFolder(String name) throws MessagingException;
    public abstract Folder[] getPersonalNamespaces() throws MessagingException;
    public abstract void checkSettings() throws MessagingException;
    public void delete() throws MessagingException {
    }
    protected void setPersistentDataCallbacks(PersistentDataCallbacks callbacks) {
    }
    public interface PersistentDataCallbacks {
        public void setPersistentString(String key, String value);
        public String getPersistentString(String key, String defaultValue);
    }
    public Bundle autoDiscover(Context context, String emailAddress, String password)
            throws MessagingException {
        return null;
    }
}
