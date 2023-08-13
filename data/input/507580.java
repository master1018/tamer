public abstract class Sender {
    protected static final int SOCKET_CONNECT_TIMEOUT = 10000;
    private static java.util.HashMap<String, Sender> mSenders =
        new java.util.HashMap<String, Sender>();
    public static Sender newInstance(Context context, String uri)
            throws MessagingException {
        throw new MessagingException("Sender.newInstance: Unknown scheme in " + uri);
    }
    private static Sender instantiateSender(Context context, String className, String uri)
        throws MessagingException {
        Object o = null;
        try {
            Class<?> c = Class.forName(className);
            java.lang.reflect.Method m =
                c.getMethod("newInstance", Context.class, String.class);
            o = m.invoke(null, context, uri);
        } catch (Exception e) {
            Log.d(Email.LOG_TAG, String.format(
                    "exception %s invoking %s.newInstance.(Context, String) method for %s",
                    e.toString(), className, uri));
            throw new MessagingException("can not instantiate Sender object for " + uri);
        }
        if (!(o instanceof Sender)) {
            throw new MessagingException(
                    uri + ": " + className + " create incompatible object");
        }
        return (Sender) o;
    }
    private static Sender findSender(Context context, int resourceId, String uri)
            throws MessagingException {
        Sender sender = null;
        try {
            XmlResourceParser xml = context.getResources().getXml(resourceId);
            int xmlEventType;
            while ((xmlEventType = xml.next()) != XmlResourceParser.END_DOCUMENT) {
                if (xmlEventType == XmlResourceParser.START_TAG &&
                    "sender".equals(xml.getName())) {
                    String scheme = xml.getAttributeValue(null, "scheme");
                    if (uri.startsWith(scheme)) {
                        String className = xml.getAttributeValue(null, "class");
                        sender = instantiateSender(context, className, uri);
                    }
                }
            }
        } catch (XmlPullParserException e) {
        } catch (IOException e) {
        }
        return sender;
    }
    public synchronized static Sender getInstance(Context context, String uri)
            throws MessagingException {
       Sender sender = mSenders.get(uri);
       if (sender == null) {
           sender = findSender(context, R.xml.senders_product, uri);
           if (sender == null) {
               sender = findSender(context, R.xml.senders, uri);
           }
           if (sender != null) {
               mSenders.put(uri, sender);
           }
       }
       if (sender == null) {
            throw new MessagingException("Unable to locate an applicable Transport for " + uri);
       }
       return sender;
    }
    public Class<? extends android.app.Activity> getSettingActivityClass() {
        return com.android.email.activity.setup.AccountSetupOutgoing.class;
    }
    public abstract void open() throws MessagingException;
    public String validateSenderLimit(long messageId) {
        return null;
    }
    public void checkSenderLimitation(long messageId) throws LimitViolationException {
    }
    public static class LimitViolationException extends MessagingException {
        public final int mMsgResourceId;
        public final long mActual;
        public final long mLimit;
        private LimitViolationException(int msgResourceId, long actual, long limit) {
            super(UNSPECIFIED_EXCEPTION);
            mMsgResourceId = msgResourceId;
            mActual = actual;
            mLimit = limit;
        }
        public static void check(int msgResourceId, long actual, long limit)
            throws LimitViolationException {
            if (actual > limit) {
                throw new LimitViolationException(msgResourceId, actual, limit);
            }
        }
    }
    public abstract void sendMessage(long messageId) throws MessagingException;
    public abstract void close() throws MessagingException;
}
