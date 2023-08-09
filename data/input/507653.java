public class MessageListAdapter extends CursorAdapter {
    private static final String TAG = "MessageListAdapter";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = Config.LOGV && DEBUG;
    static final String[] PROJECTION = new String[] {
        MmsSms.TYPE_DISCRIMINATOR_COLUMN,
        BaseColumns._ID,
        Conversations.THREAD_ID,
        Sms.ADDRESS,
        Sms.BODY,
        Sms.DATE,
        Sms.READ,
        Sms.TYPE,
        Sms.STATUS,
        Sms.LOCKED,
        Sms.ERROR_CODE,
        Mms.SUBJECT,
        Mms.SUBJECT_CHARSET,
        Mms.DATE,
        Mms.READ,
        Mms.MESSAGE_TYPE,
        Mms.MESSAGE_BOX,
        Mms.DELIVERY_REPORT,
        Mms.READ_REPORT,
        PendingMessages.ERROR_TYPE,
        Mms.LOCKED
    };
    static final int COLUMN_MSG_TYPE            = 0;
    static final int COLUMN_ID                  = 1;
    static final int COLUMN_THREAD_ID           = 2;
    static final int COLUMN_SMS_ADDRESS         = 3;
    static final int COLUMN_SMS_BODY            = 4;
    static final int COLUMN_SMS_DATE            = 5;
    static final int COLUMN_SMS_READ            = 6;
    static final int COLUMN_SMS_TYPE            = 7;
    static final int COLUMN_SMS_STATUS          = 8;
    static final int COLUMN_SMS_LOCKED          = 9;
    static final int COLUMN_SMS_ERROR_CODE      = 10;
    static final int COLUMN_MMS_SUBJECT         = 11;
    static final int COLUMN_MMS_SUBJECT_CHARSET = 12;
    static final int COLUMN_MMS_DATE            = 13;
    static final int COLUMN_MMS_READ            = 14;
    static final int COLUMN_MMS_MESSAGE_TYPE    = 15;
    static final int COLUMN_MMS_MESSAGE_BOX     = 16;
    static final int COLUMN_MMS_DELIVERY_REPORT = 17;
    static final int COLUMN_MMS_READ_REPORT     = 18;
    static final int COLUMN_MMS_ERROR_TYPE      = 19;
    static final int COLUMN_MMS_LOCKED          = 20;
    private static final int CACHE_SIZE         = 50;
    protected LayoutInflater mInflater;
    private final ListView mListView;
    private final LinkedHashMap<Long, MessageItem> mMessageItemCache;
    private final ColumnsMap mColumnsMap;
    private OnDataSetChangedListener mOnDataSetChangedListener;
    private Handler mMsgListItemHandler;
    private Pattern mHighlight;
    private Context mContext;
    private HashMap<String, HashSet<MessageListItem>> mAddressToMessageListItems
        = new HashMap<String, HashSet<MessageListItem>>();
    public MessageListAdapter(
            Context context, Cursor c, ListView listView,
            boolean useDefaultColumnsMap, Pattern highlight) {
        super(context, c, false );
        mContext = context;
        mHighlight = highlight;
        mInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        mListView = listView;
        mMessageItemCache = new LinkedHashMap<Long, MessageItem>(
                    10, 1.0f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                return size() > CACHE_SIZE;
            }
        };
        if (useDefaultColumnsMap) {
            mColumnsMap = new ColumnsMap();
        } else {
            mColumnsMap = new ColumnsMap(c);
        }
        mAvatarCache = new AvatarCache();
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (view instanceof MessageListItem) {
            String type = cursor.getString(mColumnsMap.mColumnMsgType);
            long msgId = cursor.getLong(mColumnsMap.mColumnMsgId);
            MessageItem msgItem = getCachedMessageItem(type, msgId, cursor);
            if (msgItem != null) {
                MessageListItem mli = (MessageListItem) view;
                MessageItem oldMessageItem = mli.getMessageItem();
                if (oldMessageItem != null) {
                    String oldAddress = oldMessageItem.mAddress;
                    if (oldAddress != null) {
                        HashSet<MessageListItem> set = mAddressToMessageListItems.get(oldAddress);
                        if (set != null) {
                            set.remove(mli);
                        }
                    }
                }
                mli.bind(mAvatarCache, msgItem);
                mli.setMsgListItemHandler(mMsgListItemHandler);
                String addr;
                if (!Sms.isOutgoingFolder(msgItem.mBoxId)) {
                    addr = msgItem.mAddress;
                } else {
                    addr = MessageUtils.getLocalNumber();
                }
                HashSet<MessageListItem> set = mAddressToMessageListItems.get(addr);
                if (set == null) {
                    set = new HashSet<MessageListItem>();
                    mAddressToMessageListItems.put(addr, set);
                }
                set.add(mli);
            }
        }
    }
    public interface OnDataSetChangedListener {
        void onDataSetChanged(MessageListAdapter adapter);
        void onContentChanged(MessageListAdapter adapter);
    }
    public void setOnDataSetChangedListener(OnDataSetChangedListener l) {
        mOnDataSetChangedListener = l;
    }
    public void setMsgListItemHandler(Handler handler) {
        mMsgListItemHandler = handler;
    }
    public void notifyImageLoaded(String address) {
        HashSet<MessageListItem> set = mAddressToMessageListItems.get(address);
        if (set != null) {
            for (MessageListItem mli : set) {
                mli.bind(mAvatarCache, mli.getMessageItem());
            }
        }
    }
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (LOCAL_LOGV) {
            Log.v(TAG, "MessageListAdapter.notifyDataSetChanged().");
        }
        mListView.setSelection(mListView.getCount());
        mMessageItemCache.clear();
        if (mOnDataSetChangedListener != null) {
            mOnDataSetChangedListener.onDataSetChanged(this);
        }
    }
    @Override
    protected void onContentChanged() {
        if (getCursor() != null && !getCursor().isClosed()) {
            if (mOnDataSetChangedListener != null) {
                mOnDataSetChangedListener.onContentChanged(this);
            }
        }
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.message_list_item, parent, false);
    }
    public MessageItem getCachedMessageItem(String type, long msgId, Cursor c) {
        MessageItem item = mMessageItemCache.get(getKey(type, msgId));
        if (item == null && c != null && isCursorValid(c)) {
            try {
                item = new MessageItem(mContext, type, c, mColumnsMap, mHighlight);
                mMessageItemCache.put(getKey(item.mType, item.mMsgId), item);
            } catch (MmsException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return item;
    }
    private boolean isCursorValid(Cursor cursor) {
        if (cursor.isClosed() || cursor.isBeforeFirst() || cursor.isAfterLast()) {
            return false;
        }
        return true;
    }
    private static long getKey(String type, long id) {
        if (type.equals("mms")) {
            return -id;
        } else {
            return id;
        }
    }
    public static class ColumnsMap {
        public int mColumnMsgType;
        public int mColumnMsgId;
        public int mColumnSmsAddress;
        public int mColumnSmsBody;
        public int mColumnSmsDate;
        public int mColumnSmsRead;
        public int mColumnSmsType;
        public int mColumnSmsStatus;
        public int mColumnSmsLocked;
        public int mColumnSmsErrorCode;
        public int mColumnMmsSubject;
        public int mColumnMmsSubjectCharset;
        public int mColumnMmsDate;
        public int mColumnMmsRead;
        public int mColumnMmsMessageType;
        public int mColumnMmsMessageBox;
        public int mColumnMmsDeliveryReport;
        public int mColumnMmsReadReport;
        public int mColumnMmsErrorType;
        public int mColumnMmsLocked;
        public ColumnsMap() {
            mColumnMsgType            = COLUMN_MSG_TYPE;
            mColumnMsgId              = COLUMN_ID;
            mColumnSmsAddress         = COLUMN_SMS_ADDRESS;
            mColumnSmsBody            = COLUMN_SMS_BODY;
            mColumnSmsDate            = COLUMN_SMS_DATE;
            mColumnSmsType            = COLUMN_SMS_TYPE;
            mColumnSmsStatus          = COLUMN_SMS_STATUS;
            mColumnSmsLocked          = COLUMN_SMS_LOCKED;
            mColumnSmsErrorCode       = COLUMN_SMS_ERROR_CODE;
            mColumnMmsSubject         = COLUMN_MMS_SUBJECT;
            mColumnMmsSubjectCharset  = COLUMN_MMS_SUBJECT_CHARSET;
            mColumnMmsMessageType     = COLUMN_MMS_MESSAGE_TYPE;
            mColumnMmsMessageBox      = COLUMN_MMS_MESSAGE_BOX;
            mColumnMmsDeliveryReport  = COLUMN_MMS_DELIVERY_REPORT;
            mColumnMmsReadReport      = COLUMN_MMS_READ_REPORT;
            mColumnMmsErrorType       = COLUMN_MMS_ERROR_TYPE;
            mColumnMmsLocked          = COLUMN_MMS_LOCKED;
        }
        public ColumnsMap(Cursor cursor) {
            try {
                mColumnMsgType = cursor.getColumnIndexOrThrow(
                        MmsSms.TYPE_DISCRIMINATOR_COLUMN);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            try {
                mColumnMsgId = cursor.getColumnIndexOrThrow(BaseColumns._ID);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            try {
                mColumnSmsAddress = cursor.getColumnIndexOrThrow(Sms.ADDRESS);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            try {
                mColumnSmsBody = cursor.getColumnIndexOrThrow(Sms.BODY);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            try {
                mColumnSmsDate = cursor.getColumnIndexOrThrow(Sms.DATE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            try {
                mColumnSmsType = cursor.getColumnIndexOrThrow(Sms.TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            try {
                mColumnSmsStatus = cursor.getColumnIndexOrThrow(Sms.STATUS);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            try {
                mColumnSmsLocked = cursor.getColumnIndexOrThrow(Sms.LOCKED);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            try {
                mColumnSmsErrorCode = cursor.getColumnIndexOrThrow(Sms.ERROR_CODE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            try {
                mColumnMmsSubject = cursor.getColumnIndexOrThrow(Mms.SUBJECT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            try {
                mColumnMmsSubjectCharset = cursor.getColumnIndexOrThrow(Mms.SUBJECT_CHARSET);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            try {
                mColumnMmsMessageType = cursor.getColumnIndexOrThrow(Mms.MESSAGE_TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            try {
                mColumnMmsMessageBox = cursor.getColumnIndexOrThrow(Mms.MESSAGE_BOX);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            try {
                mColumnMmsDeliveryReport = cursor.getColumnIndexOrThrow(Mms.DELIVERY_REPORT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            try {
                mColumnMmsReadReport = cursor.getColumnIndexOrThrow(Mms.READ_REPORT);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            try {
                mColumnMmsErrorType = cursor.getColumnIndexOrThrow(PendingMessages.ERROR_TYPE);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
            try {
                mColumnMmsLocked = cursor.getColumnIndexOrThrow(Mms.LOCKED);
            } catch (IllegalArgumentException e) {
                Log.w("colsMap", e.getMessage());
            }
        }
    }
    private AvatarCache mAvatarCache;
    class AvatarCache {
        private static final int TOKEN_PHONE_LOOKUP = 101;
        private static final int TOKEN_EMAIL_LOOKUP = 102;
        private static final int TOKEN_CONTACT_INFO = 201;
        private static final int TOKEN_PHOTO_DATA = 301;
        private final String[] COLUMNS = new String[] {
                  Contacts._ID,
                  Contacts.PHOTO_ID,
        };
        private final int PHOTO_ID = 1;
        private final String[] PHONE_LOOKUP_PROJECTION = new String[] {
            PhoneLookup._ID,
            PhoneLookup.LOOKUP_KEY,
        };
        private static final int PHONE_LOOKUP_CONTACT_ID_COLUMN_INDEX = 0;
        private static final int PHONE_LOOKUP_CONTACT_LOOKUP_KEY_COLUMN_INDEX = 1;
        private final String[] EMAIL_LOOKUP_PROJECTION = new String[] {
            RawContacts.CONTACT_ID,
            Contacts.LOOKUP_KEY,
        };
        private static final int EMAIL_LOOKUP_CONTACT_ID_COLUMN_INDEX = 0;
        private static final int EMAIL_LOOKUP_CONTACT_LOOKUP_KEY_COLUMN_INDEX = 1;
        HashMap<String, ContactData> mImageCache = new HashMap<String, ContactData>();
        public class ContactData {
            private String mAddress;
            private long mContactId;
            private Uri mContactUri;
            private Drawable mPhoto;
            ContactData(String address) {
                mAddress = address;
            }
            public Drawable getAvatar() {
                return mPhoto;
            }
            public Uri getContactUri() {
                return mContactUri;
            }
            private boolean startInitialQuery() {
                if (Mms.isPhoneNumber(mAddress)) {
                    mQueryHandler.startQuery(
                            TOKEN_PHONE_LOOKUP,
                            this,
                            Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(mAddress)),
                            PHONE_LOOKUP_PROJECTION,
                            null,
                            null,
                            null);
                    return true;
                } else if (Mms.isEmailAddress(mAddress)) {
                    mQueryHandler.startQuery(
                            TOKEN_EMAIL_LOOKUP,
                            this,
                            Uri.withAppendedPath(Email.CONTENT_LOOKUP_URI, Uri.encode(mAddress)),
                            EMAIL_LOOKUP_PROJECTION,
                            null,
                            null,
                            null);
                    return true;
                } else {
                    return false;
                }
            }
            private boolean onPhotoDataLoaded(Cursor c) {
                if (c == null || !c.moveToFirst()) return false;
                try {
                    byte[] photoData = c.getBlob(0);
                    Bitmap b = BitmapFactory.decodeByteArray(photoData, 0, photoData.length, null);
                    mPhoto = new BitmapDrawable(mContext.getResources(), b);
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            }
            private boolean onContactInfoLoaded(Cursor c) {
                if (c == null || !c.moveToFirst()) return false;
                long photoId = c.getLong(PHOTO_ID);
                Uri contactUri  = ContentUris.withAppendedId(Data.CONTENT_URI, photoId);
                mQueryHandler.startQuery(
                        TOKEN_PHOTO_DATA,
                        this,
                        contactUri,
                        new String[] { Photo.PHOTO },
                        null,
                        null,
                        null);
                return true;
            }
            private boolean onContactIdLoaded(Cursor c, int contactIdColumn, int lookupKeyColumn) {
                if (c == null || !c.moveToFirst()) return false;
                mContactId = c.getLong(contactIdColumn);
                String lookupKey = c.getString(lookupKeyColumn);
                mContactUri = Contacts.getLookupUri(mContactId, lookupKey);
                mQueryHandler.startQuery(
                        TOKEN_CONTACT_INFO,
                        this,
                        mContactUri,
                        COLUMNS,
                        null,
                        null,
                        null);
                return true;
            }
            private void loadDefaultAvatar() {
                if (mDefaultAvatarDrawable == null) {
                    Bitmap b = BitmapFactory.decodeResource(mContext.getResources(),
                            R.drawable.ic_contact_picture);
                    mDefaultAvatarDrawable = new BitmapDrawable(mContext.getResources(), b);
                }
                mPhoto = mDefaultAvatarDrawable;
            }
        };
        Drawable mDefaultAvatarDrawable = null;
        AsyncQueryHandler mQueryHandler = new AsyncQueryHandler(mContext.getContentResolver()) {
            @Override
            protected void onQueryComplete(int token, Object cookieObject, Cursor cursor) {
                super.onQueryComplete(token, cookieObject, cursor);
                ContactData cookie = (ContactData) cookieObject;
                switch (token) {
                    case TOKEN_PHONE_LOOKUP: {
                        if (!cookie.onContactIdLoaded(
                                cursor,
                                PHONE_LOOKUP_CONTACT_ID_COLUMN_INDEX,
                                PHONE_LOOKUP_CONTACT_LOOKUP_KEY_COLUMN_INDEX)) {
                            cookie.loadDefaultAvatar();
                        }
                        break;
                    }
                    case TOKEN_EMAIL_LOOKUP: {
                        if (!cookie.onContactIdLoaded(
                                cursor,
                                EMAIL_LOOKUP_CONTACT_ID_COLUMN_INDEX,
                                EMAIL_LOOKUP_CONTACT_LOOKUP_KEY_COLUMN_INDEX)) {
                            cookie.loadDefaultAvatar();
                        }
                        break;
                    }
                    case TOKEN_CONTACT_INFO: {
                        if (!cookie.onContactInfoLoaded(cursor)) {
                            cookie.loadDefaultAvatar();
                        }
                        break;
                    }
                    case TOKEN_PHOTO_DATA: {
                        if (!cookie.onPhotoDataLoaded(cursor)) {
                            cookie.loadDefaultAvatar();
                        } else {
                            MessageListAdapter.this.notifyImageLoaded(cookie.mAddress);
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        };
        public ContactData get(final String address) {
            if (mImageCache.containsKey(address)) {
                return mImageCache.get(address);
            } else {
                ContactData cookie = new ContactData(address);
                mImageCache.put(address, cookie);
                cookie.startInitialQuery();
                cookie.loadDefaultAvatar();
                return cookie;
            }
        }
        public AvatarCache() {
        }
    };
}
