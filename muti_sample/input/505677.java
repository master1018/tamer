public abstract class EmailContent {
    public static final String AUTHORITY = EmailProvider.EMAIL_AUTHORITY;
    public static final Uri CONTENT_URI = Uri.parse("content:
    public static final String RECORD_ID = "_id";
    private static final String[] COUNT_COLUMNS = new String[]{"count(*)"};
    public static final String[] ID_PROJECTION = new String[] {
        RECORD_ID
    };
    public static final int ID_PROJECTION_COLUMN = 0;
    private static final String ID_SELECTION = RECORD_ID + " =?";
    public static final String FIELD_COLUMN_NAME = "field";
    public static final String ADD_COLUMN_NAME = "add";
    private static final int NOT_SAVED = -1;
    public Uri mBaseUri;
    private Uri mUri = null;
    public long mId = NOT_SAVED;
    public abstract ContentValues toContentValues();
    public abstract <T extends EmailContent> T restore (Cursor cursor);
    public Uri getUri() {
        if (mUri == null) {
            mUri = ContentUris.withAppendedId(mBaseUri, mId);
        }
        return mUri;
    }
    public boolean isSaved() {
        return mId != NOT_SAVED;
    }
    @SuppressWarnings("unchecked")
    static public <T extends EmailContent> T getContent(Cursor cursor, Class<T> klass) {
        try {
            T content = klass.newInstance();
            content.mId = cursor.getLong(0);
            return (T)content.restore(cursor);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }
    public Uri save(Context context) {
        if (isSaved()) {
            throw new UnsupportedOperationException();
        }
        Uri res = context.getContentResolver().insert(mBaseUri, toContentValues());
        mId = Long.parseLong(res.getPathSegments().get(1));
        return res;
    }
    public int update(Context context, ContentValues contentValues) {
        if (!isSaved()) {
            throw new UnsupportedOperationException();
        }
        return context.getContentResolver().update(getUri(), contentValues, null, null);
    }
    static public int update(Context context, Uri baseUri, long id, ContentValues contentValues) {
        return context.getContentResolver()
            .update(ContentUris.withAppendedId(baseUri, id), contentValues, null, null);
    }
    static public int count(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = context.getContentResolver()
            .query(uri, COUNT_COLUMNS, selection, selectionArgs, null);
        try {
            if (!cursor.moveToFirst()) {
                return 0;
            }
            return cursor.getInt(0);
        } finally {
            cursor.close();
        }
    }
    private EmailContent() {
    }
    public interface SyncColumns {
        public static final String ID = "_id";
        public static final String SERVER_ID = "syncServerId";
        public static final String SERVER_TIMESTAMP = "syncServerTimeStamp";
    }
    public interface BodyColumns {
        public static final String ID = "_id";
        public static final String MESSAGE_KEY = "messageKey";
        public static final String HTML_CONTENT = "htmlContent";
        public static final String TEXT_CONTENT = "textContent";
        public static final String HTML_REPLY = "htmlReply";
        public static final String TEXT_REPLY = "textReply";
        public static final String SOURCE_MESSAGE_KEY = "sourceMessageKey";
        public static final String INTRO_TEXT = "introText";
    }
    public static final class Body extends EmailContent implements BodyColumns {
        public static final String TABLE_NAME = "Body";
        public static final Uri CONTENT_URI = Uri.parse(EmailContent.CONTENT_URI + "/body");
        public static final int CONTENT_ID_COLUMN = 0;
        public static final int CONTENT_MESSAGE_KEY_COLUMN = 1;
        public static final int CONTENT_HTML_CONTENT_COLUMN = 2;
        public static final int CONTENT_TEXT_CONTENT_COLUMN = 3;
        public static final int CONTENT_HTML_REPLY_COLUMN = 4;
        public static final int CONTENT_TEXT_REPLY_COLUMN = 5;
        public static final int CONTENT_SOURCE_KEY_COLUMN = 6;
        public static final int CONTENT_INTRO_TEXT_COLUMN = 7;
        public static final String[] CONTENT_PROJECTION = new String[] {
            RECORD_ID, BodyColumns.MESSAGE_KEY, BodyColumns.HTML_CONTENT, BodyColumns.TEXT_CONTENT,
            BodyColumns.HTML_REPLY, BodyColumns.TEXT_REPLY, BodyColumns.SOURCE_MESSAGE_KEY,
            BodyColumns.INTRO_TEXT
        };
        public static final String[] COMMON_PROJECTION_TEXT = new String[] {
            RECORD_ID, BodyColumns.TEXT_CONTENT
        };
        public static final String[] COMMON_PROJECTION_HTML = new String[] {
            RECORD_ID, BodyColumns.HTML_CONTENT
        };
        public static final String[] COMMON_PROJECTION_REPLY_TEXT = new String[] {
            RECORD_ID, BodyColumns.TEXT_REPLY
        };
        public static final String[] COMMON_PROJECTION_REPLY_HTML = new String[] {
            RECORD_ID, BodyColumns.HTML_REPLY
        };
        public static final String[] COMMON_PROJECTION_INTRO = new String[] {
            RECORD_ID, BodyColumns.INTRO_TEXT
        };
        public static final int COMMON_PROJECTION_COLUMN_TEXT = 1;
        private static final String[] PROJECTION_SOURCE_KEY =
            new String[] { BodyColumns.SOURCE_MESSAGE_KEY };
        public long mMessageKey;
        public String mHtmlContent;
        public String mTextContent;
        public String mHtmlReply;
        public String mTextReply;
        public long mSourceKey;
        public String mIntroText;
        public Body() {
            mBaseUri = CONTENT_URI;
        }
        @Override
        public ContentValues toContentValues() {
            ContentValues values = new ContentValues();
            values.put(BodyColumns.MESSAGE_KEY, mMessageKey);
            values.put(BodyColumns.HTML_CONTENT, mHtmlContent);
            values.put(BodyColumns.TEXT_CONTENT, mTextContent);
            values.put(BodyColumns.HTML_REPLY, mHtmlReply);
            values.put(BodyColumns.TEXT_REPLY, mTextReply);
            values.put(BodyColumns.SOURCE_MESSAGE_KEY, mSourceKey);
            values.put(BodyColumns.INTRO_TEXT, mIntroText);
            return values;
        }
        private static Body restoreBodyWithCursor(Cursor cursor) {
            try {
                if (cursor.moveToFirst()) {
                    return getContent(cursor, Body.class);
                } else {
                    return null;
                }
            } finally {
                cursor.close();
            }
        }
        public static Body restoreBodyWithId(Context context, long id) {
            Uri u = ContentUris.withAppendedId(Body.CONTENT_URI, id);
            Cursor c = context.getContentResolver().query(u, Body.CONTENT_PROJECTION,
                    null, null, null);
            return restoreBodyWithCursor(c);
        }
        public static Body restoreBodyWithMessageId(Context context, long messageId) {
            Cursor c = context.getContentResolver().query(Body.CONTENT_URI,
                    Body.CONTENT_PROJECTION, Body.MESSAGE_KEY + "=?",
                    new String[] {Long.toString(messageId)}, null);
            return restoreBodyWithCursor(c);
        }
        public static long lookupBodyIdWithMessageId(ContentResolver resolver, long messageId) {
            Cursor c = resolver.query(Body.CONTENT_URI, ID_PROJECTION,
                    Body.MESSAGE_KEY + "=?",
                    new String[] {Long.toString(messageId)}, null);
            try {
                return c.moveToFirst() ? c.getLong(ID_PROJECTION_COLUMN) : -1;
            } finally {
                c.close();
            }
        }
        public static void updateBodyWithMessageId(Context context, long messageId,
                ContentValues values) {
            ContentResolver resolver = context.getContentResolver();
            long bodyId = lookupBodyIdWithMessageId(resolver, messageId);
            values.put(BodyColumns.MESSAGE_KEY, messageId);
            if (bodyId == -1) {
                resolver.insert(CONTENT_URI, values);
            } else {
                final Uri uri = ContentUris.withAppendedId(CONTENT_URI, bodyId);
                resolver.update(uri, values, null, null);
            }
        }
        public static long restoreBodySourceKey(Context context, long messageId) {
            Cursor c = context.getContentResolver().query(Body.CONTENT_URI,
                    Body.PROJECTION_SOURCE_KEY,
                    Body.MESSAGE_KEY + "=?", new String[] {Long.toString(messageId)}, null);
            try {
                if (c.moveToFirst()) {
                    return c.getLong(0);
                } else {
                    return 0;
                }
            } finally {
                c.close();
            }
        }
        private static String restoreTextWithMessageId(Context context, long messageId,
                String[] projection) {
            Cursor c = context.getContentResolver().query(Body.CONTENT_URI, projection,
                    Body.MESSAGE_KEY + "=?", new String[] {Long.toString(messageId)}, null);
            try {
                if (c.moveToFirst()) {
                    return c.getString(COMMON_PROJECTION_COLUMN_TEXT);
                } else {
                    return null;
                }
            } finally {
                c.close();
            }
        }
        public static String restoreBodyTextWithMessageId(Context context, long messageId) {
            return restoreTextWithMessageId(context, messageId, Body.COMMON_PROJECTION_TEXT);
        }
        public static String restoreBodyHtmlWithMessageId(Context context, long messageId) {
            return restoreTextWithMessageId(context, messageId, Body.COMMON_PROJECTION_HTML);
        }
        public static String restoreReplyTextWithMessageId(Context context, long messageId) {
            return restoreTextWithMessageId(context, messageId, Body.COMMON_PROJECTION_REPLY_TEXT);
        }
        public static String restoreReplyHtmlWithMessageId(Context context, long messageId) {
            return restoreTextWithMessageId(context, messageId, Body.COMMON_PROJECTION_REPLY_HTML);
        }
        public static String restoreIntroTextWithMessageId(Context context, long messageId) {
            return restoreTextWithMessageId(context, messageId, Body.COMMON_PROJECTION_INTRO);
        }
        @Override
        @SuppressWarnings("unchecked")
        public EmailContent.Body restore(Cursor c) {
            mBaseUri = EmailContent.Body.CONTENT_URI;
            mMessageKey = c.getLong(CONTENT_MESSAGE_KEY_COLUMN);
            mHtmlContent = c.getString(CONTENT_HTML_CONTENT_COLUMN);
            mTextContent = c.getString(CONTENT_TEXT_CONTENT_COLUMN);
            mHtmlReply = c.getString(CONTENT_HTML_REPLY_COLUMN);
            mTextReply = c.getString(CONTENT_TEXT_REPLY_COLUMN);
            mSourceKey = c.getLong(CONTENT_SOURCE_KEY_COLUMN);
            mIntroText = c.getString(CONTENT_INTRO_TEXT_COLUMN);
            return this;
        }
        public boolean update() {
            return false;
        }
    }
    public interface MessageColumns {
        public static final String ID = "_id";
        public static final String DISPLAY_NAME = "displayName";
        public static final String TIMESTAMP = "timeStamp";
        public static final String SUBJECT = "subject";
        public static final String FLAG_READ = "flagRead";
        public static final String FLAG_LOADED = "flagLoaded";
        public static final String FLAG_FAVORITE = "flagFavorite";
        public static final String FLAG_ATTACHMENT = "flagAttachment";
        public static final String FLAGS = "flags";
        public static final String CLIENT_ID = "clientId";
        public static final String MESSAGE_ID = "messageId";
        public static final String MAILBOX_KEY = "mailboxKey";
        public static final String ACCOUNT_KEY = "accountKey";
        public static final String FROM_LIST = "fromList";
        public static final String TO_LIST = "toList";
        public static final String CC_LIST = "ccList";
        public static final String BCC_LIST = "bccList";
        public static final String REPLY_TO_LIST = "replyToList";
        public static final String MEETING_INFO = "meetingInfo";
    }
    public static final class Message extends EmailContent implements SyncColumns, MessageColumns {
        public static final String TABLE_NAME = "Message";
        public static final String UPDATED_TABLE_NAME = "Message_Updates";
        public static final String DELETED_TABLE_NAME = "Message_Deletes";
        public static final Uri CONTENT_URI = Uri.parse(EmailContent.CONTENT_URI + "/message");
        public static final Uri SYNCED_CONTENT_URI =
            Uri.parse(EmailContent.CONTENT_URI + "/syncedMessage");
        public static final Uri DELETED_CONTENT_URI =
            Uri.parse(EmailContent.CONTENT_URI + "/deletedMessage");
        public static final Uri UPDATED_CONTENT_URI =
            Uri.parse(EmailContent.CONTENT_URI + "/updatedMessage");
        public static final String KEY_TIMESTAMP_DESC = MessageColumns.TIMESTAMP + " desc";
        public static final int CONTENT_ID_COLUMN = 0;
        public static final int CONTENT_DISPLAY_NAME_COLUMN = 1;
        public static final int CONTENT_TIMESTAMP_COLUMN = 2;
        public static final int CONTENT_SUBJECT_COLUMN = 3;
        public static final int CONTENT_FLAG_READ_COLUMN = 4;
        public static final int CONTENT_FLAG_LOADED_COLUMN = 5;
        public static final int CONTENT_FLAG_FAVORITE_COLUMN = 6;
        public static final int CONTENT_FLAG_ATTACHMENT_COLUMN = 7;
        public static final int CONTENT_FLAGS_COLUMN = 8;
        public static final int CONTENT_SERVER_ID_COLUMN = 9;
        public static final int CONTENT_CLIENT_ID_COLUMN = 10;
        public static final int CONTENT_MESSAGE_ID_COLUMN = 11;
        public static final int CONTENT_MAILBOX_KEY_COLUMN = 12;
        public static final int CONTENT_ACCOUNT_KEY_COLUMN = 13;
        public static final int CONTENT_FROM_LIST_COLUMN = 14;
        public static final int CONTENT_TO_LIST_COLUMN = 15;
        public static final int CONTENT_CC_LIST_COLUMN = 16;
        public static final int CONTENT_BCC_LIST_COLUMN = 17;
        public static final int CONTENT_REPLY_TO_COLUMN = 18;
        public static final int CONTENT_SERVER_TIMESTAMP_COLUMN = 19;
        public static final int CONTENT_MEETING_INFO_COLUMN = 20;
        public static final String[] CONTENT_PROJECTION = new String[] {
            RECORD_ID,
            MessageColumns.DISPLAY_NAME, MessageColumns.TIMESTAMP,
            MessageColumns.SUBJECT, MessageColumns.FLAG_READ,
            MessageColumns.FLAG_LOADED, MessageColumns.FLAG_FAVORITE,
            MessageColumns.FLAG_ATTACHMENT, MessageColumns.FLAGS,
            SyncColumns.SERVER_ID, MessageColumns.CLIENT_ID,
            MessageColumns.MESSAGE_ID, MessageColumns.MAILBOX_KEY,
            MessageColumns.ACCOUNT_KEY, MessageColumns.FROM_LIST,
            MessageColumns.TO_LIST, MessageColumns.CC_LIST,
            MessageColumns.BCC_LIST, MessageColumns.REPLY_TO_LIST,
            SyncColumns.SERVER_TIMESTAMP, MessageColumns.MEETING_INFO
        };
        public static final int LIST_ID_COLUMN = 0;
        public static final int LIST_DISPLAY_NAME_COLUMN = 1;
        public static final int LIST_TIMESTAMP_COLUMN = 2;
        public static final int LIST_SUBJECT_COLUMN = 3;
        public static final int LIST_READ_COLUMN = 4;
        public static final int LIST_LOADED_COLUMN = 5;
        public static final int LIST_FAVORITE_COLUMN = 6;
        public static final int LIST_ATTACHMENT_COLUMN = 7;
        public static final int LIST_FLAGS_COLUMN = 8;
        public static final int LIST_MAILBOX_KEY_COLUMN = 9;
        public static final int LIST_ACCOUNT_KEY_COLUMN = 10;
        public static final int LIST_SERVER_ID_COLUMN = 11;
        public static final String[] LIST_PROJECTION = new String[] {
            RECORD_ID,
            MessageColumns.DISPLAY_NAME, MessageColumns.TIMESTAMP,
            MessageColumns.SUBJECT, MessageColumns.FLAG_READ,
            MessageColumns.FLAG_LOADED, MessageColumns.FLAG_FAVORITE,
            MessageColumns.FLAG_ATTACHMENT, MessageColumns.FLAGS,
            MessageColumns.MAILBOX_KEY, MessageColumns.ACCOUNT_KEY,
            SyncColumns.SERVER_ID
        };
        public static final int ID_COLUMNS_ID_COLUMN = 0;
        public static final int ID_COLUMNS_SYNC_SERVER_ID = 1;
        public static final String[] ID_COLUMNS_PROJECTION = new String[] {
            RECORD_ID, SyncColumns.SERVER_ID
        };
        public static final int ID_MAILBOX_COLUMN_ID = 0;
        public static final int ID_MAILBOX_COLUMN_MAILBOX_KEY = 1;
        public static final String[] ID_MAILBOX_PROJECTION = new String[] {
            RECORD_ID, MessageColumns.MAILBOX_KEY
        };
        public static final String[] ID_COLUMN_PROJECTION = new String[] { RECORD_ID };
        public String mDisplayName;
        public long mTimeStamp;
        public String mSubject;
        public boolean mFlagRead = false;
        public int mFlagLoaded = FLAG_LOADED_UNLOADED;
        public boolean mFlagFavorite = false;
        public boolean mFlagAttachment = false;
        public int mFlags = 0;
        public String mServerId;
        public long mServerTimeStamp;
        public String mClientId;
        public String mMessageId;
        public long mMailboxKey;
        public long mAccountKey;
        public String mFrom;
        public String mTo;
        public String mCc;
        public String mBcc;
        public String mReplyTo;
        public String mMeetingInfo;
        transient public String mText;
        transient public String mHtml;
        transient public String mTextReply;
        transient public String mHtmlReply;
        transient public long mSourceKey;
        transient public ArrayList<Attachment> mAttachments = null;
        transient public String mIntroText;
        public static final int UNREAD = 0;
        public static final int READ = 1;
        public static final int FLAG_LOADED_UNLOADED = 0;
        public static final int FLAG_LOADED_COMPLETE = 1;
        public static final int FLAG_LOADED_PARTIAL = 2;
        public static final int FLAG_LOADED_DELETED = 3;
        public static final int FLAG_TYPE_ORIGINAL = 0;
        public static final int FLAG_TYPE_REPLY = 1<<0;
        public static final int FLAG_TYPE_FORWARD = 1<<1;
        public static final int FLAG_TYPE_MASK = FLAG_TYPE_REPLY | FLAG_TYPE_FORWARD;
        public static final int FLAG_INCOMING_MEETING_INVITE = 1<<2;
        public static final int FLAG_INCOMING_MEETING_CANCEL = 1<<3;
        public static final int FLAG_INCOMING_MEETING_MASK =
            FLAG_INCOMING_MEETING_INVITE | FLAG_INCOMING_MEETING_CANCEL;
        public static final int FLAG_OUTGOING_MEETING_INVITE = 1<<4;
        public static final int FLAG_OUTGOING_MEETING_CANCEL = 1<<5;
        public static final int FLAG_OUTGOING_MEETING_ACCEPT = 1<<6;
        public static final int FLAG_OUTGOING_MEETING_DECLINE = 1<<7;
        public static final int FLAG_OUTGOING_MEETING_TENTATIVE = 1<<8;
        public static final int FLAG_OUTGOING_MEETING_MASK =
            FLAG_OUTGOING_MEETING_INVITE | FLAG_OUTGOING_MEETING_CANCEL |
            FLAG_OUTGOING_MEETING_ACCEPT | FLAG_OUTGOING_MEETING_DECLINE |
            FLAG_OUTGOING_MEETING_TENTATIVE;
        public static final int FLAG_OUTGOING_MEETING_REQUEST_MASK =
            FLAG_OUTGOING_MEETING_INVITE | FLAG_OUTGOING_MEETING_CANCEL;
        public Message() {
            mBaseUri = CONTENT_URI;
        }
        @Override
        public ContentValues toContentValues() {
            ContentValues values = new ContentValues();
            values.put(MessageColumns.DISPLAY_NAME, mDisplayName);
            values.put(MessageColumns.TIMESTAMP, mTimeStamp);
            values.put(MessageColumns.SUBJECT, mSubject);
            values.put(MessageColumns.FLAG_READ, mFlagRead);
            values.put(MessageColumns.FLAG_LOADED, mFlagLoaded);
            values.put(MessageColumns.FLAG_FAVORITE, mFlagFavorite);
            values.put(MessageColumns.FLAG_ATTACHMENT, mFlagAttachment);
            values.put(MessageColumns.FLAGS, mFlags);
            values.put(SyncColumns.SERVER_ID, mServerId);
            values.put(SyncColumns.SERVER_TIMESTAMP, mServerTimeStamp);
            values.put(MessageColumns.CLIENT_ID, mClientId);
            values.put(MessageColumns.MESSAGE_ID, mMessageId);
            values.put(MessageColumns.MAILBOX_KEY, mMailboxKey);
            values.put(MessageColumns.ACCOUNT_KEY, mAccountKey);
            values.put(MessageColumns.FROM_LIST, mFrom);
            values.put(MessageColumns.TO_LIST, mTo);
            values.put(MessageColumns.CC_LIST, mCc);
            values.put(MessageColumns.BCC_LIST, mBcc);
            values.put(MessageColumns.REPLY_TO_LIST, mReplyTo);
            values.put(MessageColumns.MEETING_INFO, mMeetingInfo);
            return values;
        }
        public static Message restoreMessageWithId(Context context, long id) {
            Uri u = ContentUris.withAppendedId(Message.CONTENT_URI, id);
            Cursor c = context.getContentResolver().query(u, Message.CONTENT_PROJECTION,
                    null, null, null);
            try {
                if (c.moveToFirst()) {
                    return getContent(c, Message.class);
                } else {
                    return null;
                }
            } finally {
                c.close();
            }
        }
        @Override
        @SuppressWarnings("unchecked")
        public EmailContent.Message restore(Cursor c) {
            mBaseUri = CONTENT_URI;
            mId = c.getLong(CONTENT_ID_COLUMN);
            mDisplayName = c.getString(CONTENT_DISPLAY_NAME_COLUMN);
            mTimeStamp = c.getLong(CONTENT_TIMESTAMP_COLUMN);
            mSubject = c.getString(CONTENT_SUBJECT_COLUMN);
            mFlagRead = c.getInt(CONTENT_FLAG_READ_COLUMN) == 1;
            mFlagLoaded = c.getInt(CONTENT_FLAG_LOADED_COLUMN);
            mFlagFavorite = c.getInt(CONTENT_FLAG_FAVORITE_COLUMN) == 1;
            mFlagAttachment = c.getInt(CONTENT_FLAG_ATTACHMENT_COLUMN) == 1;
            mFlags = c.getInt(CONTENT_FLAGS_COLUMN);
            mServerId = c.getString(CONTENT_SERVER_ID_COLUMN);
            mServerTimeStamp = c.getLong(CONTENT_SERVER_TIMESTAMP_COLUMN);
            mClientId = c.getString(CONTENT_CLIENT_ID_COLUMN);
            mMessageId = c.getString(CONTENT_MESSAGE_ID_COLUMN);
            mMailboxKey = c.getLong(CONTENT_MAILBOX_KEY_COLUMN);
            mAccountKey = c.getLong(CONTENT_ACCOUNT_KEY_COLUMN);
            mFrom = c.getString(CONTENT_FROM_LIST_COLUMN);
            mTo = c.getString(CONTENT_TO_LIST_COLUMN);
            mCc = c.getString(CONTENT_CC_LIST_COLUMN);
            mBcc = c.getString(CONTENT_BCC_LIST_COLUMN);
            mReplyTo = c.getString(CONTENT_REPLY_TO_COLUMN);
            mMeetingInfo = c.getString(CONTENT_MEETING_INFO_COLUMN);
            return this;
        }
        public boolean update() {
            return false;
        }
        @Override
        public Uri save(Context context) {
            boolean doSave = !isSaved();
            if (mText == null && mHtml == null && mTextReply == null && mHtmlReply == null &&
                    (mAttachments == null || mAttachments.isEmpty())) {
                if (doSave) {
                    return super.save(context);
                } else {
                    if (update(context, toContentValues()) == 1) {
                        return getUri();
                    }
                    return null;
                }
            }
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            addSaveOps(ops);
            try {
                ContentProviderResult[] results =
                    context.getContentResolver().applyBatch(EmailProvider.EMAIL_AUTHORITY, ops);
                if (doSave) {
                    Uri u = results[0].uri;
                    mId = Long.parseLong(u.getPathSegments().get(1));
                    if (mAttachments != null) {
                        int resultOffset = 2;
                        for (Attachment a : mAttachments) {
                            u = results[resultOffset++].uri;
                            if (u != null) {
                                a.mId = Long.parseLong(u.getPathSegments().get(1));
                            }
                            a.mMessageKey = mId;
                        }
                    }
                    return u;
                } else {
                    return null;
                }
            } catch (RemoteException e) {
            } catch (OperationApplicationException e) {
            }
            return null;
        }
        public void addSaveOps(ArrayList<ContentProviderOperation> ops) {
            ContentProviderOperation.Builder b = ContentProviderOperation.newInsert(mBaseUri);
            ops.add(b.withValues(toContentValues()).build());
            ContentValues cv = new ContentValues();
            if (mText != null) {
                cv.put(Body.TEXT_CONTENT, mText);
            }
            if (mHtml != null) {
                cv.put(Body.HTML_CONTENT, mHtml);
            }
            if (mTextReply != null) {
                cv.put(Body.TEXT_REPLY, mTextReply);
            }
            if (mHtmlReply != null) {
                cv.put(Body.HTML_REPLY, mHtmlReply);
            }
            if (mSourceKey != 0) {
                cv.put(Body.SOURCE_MESSAGE_KEY, mSourceKey);
            }
            if (mIntroText != null) {
                cv.put(Body.INTRO_TEXT, mIntroText);
            }
            b = ContentProviderOperation.newInsert(Body.CONTENT_URI);
            b.withValues(cv);
            ContentValues backValues = new ContentValues();
            int messageBackValue = ops.size() - 1;
            backValues.put(Body.MESSAGE_KEY, messageBackValue);
            ops.add(b.withValueBackReferences(backValues).build());
            if (mAttachments != null) {
                for (Attachment att: mAttachments) {
                    ops.add(ContentProviderOperation.newInsert(Attachment.CONTENT_URI)
                        .withValues(att.toContentValues())
                        .withValueBackReference(Attachment.MESSAGE_KEY, messageBackValue)
                        .build());
                }
            }
        }
    }
    public interface AccountColumns {
        public static final String ID = "_id";
        public static final String DISPLAY_NAME = "displayName";
        public static final String EMAIL_ADDRESS = "emailAddress";
        public static final String SYNC_KEY = "syncKey";
        public static final String SYNC_LOOKBACK = "syncLookback";
        public static final String SYNC_INTERVAL = "syncInterval";
        public static final String HOST_AUTH_KEY_RECV = "hostAuthKeyRecv";
        public static final String HOST_AUTH_KEY_SEND = "hostAuthKeySend";
        public static final String FLAGS = "flags";
        public static final String IS_DEFAULT = "isDefault";
        public static final String COMPATIBILITY_UUID = "compatibilityUuid";
        public static final String SENDER_NAME = "senderName";
        public static final String RINGTONE_URI = "ringtoneUri";
        public static final String PROTOCOL_VERSION = "protocolVersion";
        public static final String NEW_MESSAGE_COUNT = "newMessageCount";
        public static final String SECURITY_FLAGS = "securityFlags";
        public static final String SECURITY_SYNC_KEY = "securitySyncKey";
        public static final String SIGNATURE = "signature";
    }
    public static final class Account extends EmailContent implements AccountColumns, Parcelable {
        public static final String TABLE_NAME = "Account";
        public static final Uri CONTENT_URI = Uri.parse(EmailContent.CONTENT_URI + "/account");
        public static final Uri ADD_TO_FIELD_URI =
            Uri.parse(EmailContent.CONTENT_URI + "/accountIdAddToField");
        public final static int FLAGS_NOTIFY_NEW_MAIL = 1;
        public final static int FLAGS_VIBRATE_ALWAYS = 2;
        public static final int FLAGS_DELETE_POLICY_MASK = 4+8;
        public static final int FLAGS_DELETE_POLICY_SHIFT = 2;
        public static final int FLAGS_INCOMPLETE = 16;
        public static final int FLAGS_SECURITY_HOLD = 32;
        public static final int FLAGS_VIBRATE_WHEN_SILENT = 64;
        public static final int DELETE_POLICY_NEVER = 0;
        public static final int DELETE_POLICY_7DAYS = 1;        
        public static final int DELETE_POLICY_ON_DELETE = 2;
        public static final int CHECK_INTERVAL_NEVER = -1;
        public static final int CHECK_INTERVAL_PUSH = -2;
        public String mDisplayName;
        public String mEmailAddress;
        public String mSyncKey;
        public int mSyncLookback;
        public int mSyncInterval;
        public long mHostAuthKeyRecv;
        public long mHostAuthKeySend;
        public int mFlags;
        public boolean mIsDefault;          
        public String mCompatibilityUuid;
        public String mSenderName;
        public String mRingtoneUri;
        public String mProtocolVersion;
        public int mNewMessageCount;
        public int mSecurityFlags;
        public String mSecuritySyncKey;
        public String mSignature;
        public transient HostAuth mHostAuthRecv;
        public transient HostAuth mHostAuthSend;
        public static final int CONTENT_ID_COLUMN = 0;
        public static final int CONTENT_DISPLAY_NAME_COLUMN = 1;
        public static final int CONTENT_EMAIL_ADDRESS_COLUMN = 2;
        public static final int CONTENT_SYNC_KEY_COLUMN = 3;
        public static final int CONTENT_SYNC_LOOKBACK_COLUMN = 4;
        public static final int CONTENT_SYNC_INTERVAL_COLUMN = 5;
        public static final int CONTENT_HOST_AUTH_KEY_RECV_COLUMN = 6;
        public static final int CONTENT_HOST_AUTH_KEY_SEND_COLUMN = 7;
        public static final int CONTENT_FLAGS_COLUMN = 8;
        public static final int CONTENT_IS_DEFAULT_COLUMN = 9;
        public static final int CONTENT_COMPATIBILITY_UUID_COLUMN = 10;
        public static final int CONTENT_SENDER_NAME_COLUMN = 11;
        public static final int CONTENT_RINGTONE_URI_COLUMN = 12;
        public static final int CONTENT_PROTOCOL_VERSION_COLUMN = 13;
        public static final int CONTENT_NEW_MESSAGE_COUNT_COLUMN = 14;
        public static final int CONTENT_SECURITY_FLAGS_COLUMN = 15;
        public static final int CONTENT_SECURITY_SYNC_KEY_COLUMN = 16;
        public static final int CONTENT_SIGNATURE_COLUMN = 17;
        public static final String[] CONTENT_PROJECTION = new String[] {
            RECORD_ID, AccountColumns.DISPLAY_NAME,
            AccountColumns.EMAIL_ADDRESS, AccountColumns.SYNC_KEY, AccountColumns.SYNC_LOOKBACK,
            AccountColumns.SYNC_INTERVAL, AccountColumns.HOST_AUTH_KEY_RECV,
            AccountColumns.HOST_AUTH_KEY_SEND, AccountColumns.FLAGS, AccountColumns.IS_DEFAULT,
            AccountColumns.COMPATIBILITY_UUID, AccountColumns.SENDER_NAME,
            AccountColumns.RINGTONE_URI, AccountColumns.PROTOCOL_VERSION,
            AccountColumns.NEW_MESSAGE_COUNT, AccountColumns.SECURITY_FLAGS,
            AccountColumns.SECURITY_SYNC_KEY, AccountColumns.SIGNATURE
        };
        public static final int CONTENT_MAILBOX_TYPE_COLUMN = 1;
        public static final String[] ID_TYPE_PROJECTION = new String[] {
            RECORD_ID, MailboxColumns.TYPE
        };
        public static final String MAILBOX_SELECTION =
            MessageColumns.MAILBOX_KEY + " =?";
        public static final String UNREAD_COUNT_SELECTION =
            MessageColumns.MAILBOX_KEY + " =? and " + MessageColumns.FLAG_READ + "= 0";
        public static final String UUID_SELECTION = AccountColumns.COMPATIBILITY_UUID + " =?";
        private static final String[] DEFAULT_ID_PROJECTION = new String[] {
            RECORD_ID, IS_DEFAULT
        };
        public Account() {
            mBaseUri = CONTENT_URI;
            mRingtoneUri = "content:
            mSyncInterval = -1;
            mSyncLookback = -1;
            mFlags = FLAGS_NOTIFY_NEW_MAIL;
            mCompatibilityUuid = UUID.randomUUID().toString();
        }
        public static Account restoreAccountWithId(Context context, long id) {
            Uri u = ContentUris.withAppendedId(Account.CONTENT_URI, id);
            Cursor c = context.getContentResolver().query(u, Account.CONTENT_PROJECTION,
                    null, null, null);
            try {
                if (c.moveToFirst()) {
                    return getContent(c, Account.class);
                } else {
                    return null;
                }
            } finally {
                c.close();
            }
        }
        public void refresh(Context context) {
            Cursor c = context.getContentResolver().query(this.getUri(), Account.CONTENT_PROJECTION,
                    null, null, null);
            try {
                c.moveToFirst();
                restore(c);
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }
        @Override
        @SuppressWarnings("unchecked")
        public EmailContent.Account restore(Cursor cursor) {
            mId = cursor.getLong(CONTENT_ID_COLUMN);
            mBaseUri = CONTENT_URI;
            mDisplayName = cursor.getString(CONTENT_DISPLAY_NAME_COLUMN);
            mEmailAddress = cursor.getString(CONTENT_EMAIL_ADDRESS_COLUMN);
            mSyncKey = cursor.getString(CONTENT_SYNC_KEY_COLUMN);
            mSyncLookback = cursor.getInt(CONTENT_SYNC_LOOKBACK_COLUMN);
            mSyncInterval = cursor.getInt(CONTENT_SYNC_INTERVAL_COLUMN);
            mHostAuthKeyRecv = cursor.getLong(CONTENT_HOST_AUTH_KEY_RECV_COLUMN);
            mHostAuthKeySend = cursor.getLong(CONTENT_HOST_AUTH_KEY_SEND_COLUMN);
            mFlags = cursor.getInt(CONTENT_FLAGS_COLUMN);
            mIsDefault = cursor.getInt(CONTENT_IS_DEFAULT_COLUMN) == 1;
            mCompatibilityUuid = cursor.getString(CONTENT_COMPATIBILITY_UUID_COLUMN);
            mSenderName = cursor.getString(CONTENT_SENDER_NAME_COLUMN);
            mRingtoneUri = cursor.getString(CONTENT_RINGTONE_URI_COLUMN);
            mProtocolVersion = cursor.getString(CONTENT_PROTOCOL_VERSION_COLUMN);
            mNewMessageCount = cursor.getInt(CONTENT_NEW_MESSAGE_COUNT_COLUMN);
            mSecurityFlags = cursor.getInt(CONTENT_SECURITY_FLAGS_COLUMN);
            mSecuritySyncKey = cursor.getString(CONTENT_SECURITY_SYNC_KEY_COLUMN);
            mSignature = cursor.getString(CONTENT_SIGNATURE_COLUMN);
            return this;
        }
        private long getId(Uri u) {
            return Long.parseLong(u.getPathSegments().get(1));
        }
        public String getDisplayName() {
            return mDisplayName;
        }
        public void setDisplayName(String description) {
            mDisplayName = description;
        }
        public String getEmailAddress() {
            return mEmailAddress;
        }
        public void setEmailAddress(String emailAddress) {
            mEmailAddress = emailAddress;
        }
        public String getSenderName() {
            return mSenderName;
        }
        public void setSenderName(String name) {
            mSenderName = name;
        }
        public String getSignature() {
            return mSignature;
        }
        public void setSignature(String signature) {
            mSignature = signature;
        }
        public int getSyncInterval()
        {
            return mSyncInterval;
        }
        public void setSyncInterval(int minutes)
        {
            mSyncInterval = minutes;
        }
        public int getSyncLookback() {
            return mSyncLookback;
        }
        public void setSyncLookback(int value) {
            mSyncLookback = value;
        }
        public int getFlags() {
            return mFlags;
        }
        public void setFlags(int newFlags) {
            mFlags = newFlags;
        }
        public String getRingtone() {
            return mRingtoneUri;
        }
        public void setRingtone(String newUri) {
            mRingtoneUri = newUri;
        }
        public void setDeletePolicy(int newPolicy) {
            mFlags &= ~FLAGS_DELETE_POLICY_MASK;
            mFlags |= (newPolicy << FLAGS_DELETE_POLICY_SHIFT) & FLAGS_DELETE_POLICY_MASK;
        }
        public int getDeletePolicy() {
            return (mFlags & FLAGS_DELETE_POLICY_MASK) >> FLAGS_DELETE_POLICY_SHIFT;
        }
        public String getUuid() {
            return mCompatibilityUuid;
        }
        public String getStoreUri(Context context) {
            if (mHostAuthRecv == null) {
                mHostAuthRecv = HostAuth.restoreHostAuthWithId(context, mHostAuthKeyRecv);
            }
            if (mHostAuthRecv != null) {
                String storeUri = mHostAuthRecv.getStoreUri();
                if (storeUri != null) {
                    return storeUri;
                }
            }
            return "";
        }
        public String getSenderUri(Context context) {
            if (mHostAuthSend == null) {
                mHostAuthSend = HostAuth.restoreHostAuthWithId(context, mHostAuthKeySend);
            }
            if (mHostAuthSend != null) {
                String senderUri = mHostAuthSend.getStoreUri();
                if (senderUri != null) {
                    return senderUri;
                }
            }
            return "";
        }
        @Deprecated
        public void setStoreUri(Context context, String storeUri) {
            if (mHostAuthRecv == null) {
                if (mHostAuthKeyRecv != 0) {
                    mHostAuthRecv = HostAuth.restoreHostAuthWithId(context, mHostAuthKeyRecv);
                } else {
                    mHostAuthRecv = new EmailContent.HostAuth();
                }
            }
            if (mHostAuthRecv != null) {
                mHostAuthRecv.setStoreUri(storeUri);
            }
        }
        @Deprecated
        public void setSenderUri(Context context, String senderUri) {
            if (mHostAuthSend == null) {
                if (mHostAuthKeySend != 0) {
                    mHostAuthSend = HostAuth.restoreHostAuthWithId(context, mHostAuthKeySend);
                } else {
                    mHostAuthSend = new EmailContent.HostAuth();
                }
            }
            if (mHostAuthSend != null) {
                mHostAuthSend.setStoreUri(senderUri);
            }
        }
        public String getLocalStoreUri(Context context) {
            return "local:
        }
        public void setDefaultAccount(boolean newDefaultState) {
            mIsDefault = newDefaultState;
        }
        static private long getDefaultAccountWhere(Context context, String where) {
            Cursor cursor = context.getContentResolver().query(CONTENT_URI,
                    DEFAULT_ID_PROJECTION,
                    where, null, null);
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getLong(0);   
                }
            } finally {
                cursor.close();
            }
            return -1;
        }
        public Uri getShortcutSafeUri() {
            return getShortcutSafeUriFromUuid(mCompatibilityUuid);
        }
        public static Uri getShortcutSafeUriFromUuid(String uuid) {
            return CONTENT_URI.buildUpon().appendEncodedPath(uuid).build();
        }
        public static long getAccountIdFromShortcutSafeUri(Context context, Uri uri) {
            if (!"content".equals(uri.getScheme())
                    || !EmailContent.AUTHORITY.equals(uri.getAuthority())) {
                return -1;
            }
            final List<String> ps = uri.getPathSegments();
            if (ps.size() != 2 || !"account".equals(ps.get(0))) {
                return -1;
            }
            final String id = ps.get(1);
            try {
                return Long.parseLong(id);
            } catch (NumberFormatException ok) {
            }
            Cursor cursor = context.getContentResolver().query(CONTENT_URI, ID_PROJECTION,
                    UUID_SELECTION, new String[] {id}, null);
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getLong(0);   
                }
            } finally {
                cursor.close();
            }
            return -1; 
        }
        static public long getDefaultAccountId(Context context) {
            long id = getDefaultAccountWhere(context, AccountColumns.IS_DEFAULT + "=1");
            if (id == -1) {
                id = getDefaultAccountWhere(context, null);
            }
            return id;
        }
        public static boolean isValidId(Context context, long accountId) {
            Cursor cursor = context.getContentResolver().query(CONTENT_URI, ID_PROJECTION,
                    ID_SELECTION, new String[] {"" + accountId}, null);
            try {
                if (cursor.moveToFirst()) {
                    return true;
                }
            } finally {
                cursor.close();
            }
            return false; 
        }
        @Override
        public int update(Context context, ContentValues cv) {
            if (cv.containsKey(AccountColumns.IS_DEFAULT) &&
                    cv.getAsBoolean(AccountColumns.IS_DEFAULT)) {
                ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                ContentValues cv1 = new ContentValues();
                cv1.put(AccountColumns.IS_DEFAULT, false);
                ops.add(ContentProviderOperation.newUpdate(CONTENT_URI).withValues(cv1).build());
                ops.add(ContentProviderOperation
                        .newUpdate(ContentUris.withAppendedId(CONTENT_URI, mId))
                        .withValues(cv).build());
                try {
                    context.getContentResolver().applyBatch(EmailProvider.EMAIL_AUTHORITY, ops);
                    return 1;
                } catch (RemoteException e) {
                } catch (OperationApplicationException e) {
                }
                return 0;
            }
            return super.update(context, cv);
        }
        @Override
        public Uri save(Context context) {
            if (isSaved()) {
                throw new UnsupportedOperationException();
            }
            if (mHostAuthRecv == null && mHostAuthSend == null && mIsDefault == false) {
                    return super.save(context);
            }
            int index = 0;
            int recvIndex = -1;
            int sendIndex = -1;
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            if (mHostAuthRecv != null) {
                recvIndex = index++;
                ops.add(ContentProviderOperation.newInsert(mHostAuthRecv.mBaseUri)
                        .withValues(mHostAuthRecv.toContentValues())
                        .build());
            }
            if (mHostAuthSend != null) {
                sendIndex = index++;
                ops.add(ContentProviderOperation.newInsert(mHostAuthRecv.mBaseUri)
                        .withValues(mHostAuthSend.toContentValues())
                        .build());
            }
            if (mIsDefault) {
                index++;
                ContentValues cv1 = new ContentValues();
                cv1.put(AccountColumns.IS_DEFAULT, 0);
                ops.add(ContentProviderOperation.newUpdate(CONTENT_URI).withValues(cv1).build());
            }
            ContentValues cv = null;
            if (recvIndex >= 0 || sendIndex >= 0) {
                cv = new ContentValues();
                if (recvIndex >= 0) {
                    cv.put(Account.HOST_AUTH_KEY_RECV, recvIndex);
                }
                if (sendIndex >= 0) {
                    cv.put(Account.HOST_AUTH_KEY_SEND, sendIndex);
                }
            }
            ContentProviderOperation.Builder b = ContentProviderOperation.newInsert(mBaseUri);
            b.withValues(toContentValues());
            if (cv != null) {
                b.withValueBackReferences(cv);
            }
            ops.add(b.build());
            try {
                ContentProviderResult[] results =
                    context.getContentResolver().applyBatch(EmailProvider.EMAIL_AUTHORITY, ops);
                if (recvIndex >= 0) {
                    long newId = getId(results[recvIndex].uri);
                    mHostAuthKeyRecv = newId;
                    mHostAuthRecv.mId = newId;
                }
                if (sendIndex >= 0) {
                    long newId = getId(results[sendIndex].uri);
                    mHostAuthKeySend = newId;
                    mHostAuthSend.mId = newId;
                }
                Uri u = results[index].uri;
                mId = getId(u);
                return u;
            } catch (RemoteException e) {
            } catch (OperationApplicationException e) {
            }
            return null;
        }
        @Override
        public ContentValues toContentValues() {
            ContentValues values = new ContentValues();
            values.put(AccountColumns.DISPLAY_NAME, mDisplayName);
            values.put(AccountColumns.EMAIL_ADDRESS, mEmailAddress);
            values.put(AccountColumns.SYNC_KEY, mSyncKey);
            values.put(AccountColumns.SYNC_LOOKBACK, mSyncLookback);
            values.put(AccountColumns.SYNC_INTERVAL, mSyncInterval);
            values.put(AccountColumns.HOST_AUTH_KEY_RECV, mHostAuthKeyRecv);
            values.put(AccountColumns.HOST_AUTH_KEY_SEND, mHostAuthKeySend);
            values.put(AccountColumns.FLAGS, mFlags);
            values.put(AccountColumns.IS_DEFAULT, mIsDefault);
            values.put(AccountColumns.COMPATIBILITY_UUID, mCompatibilityUuid);
            values.put(AccountColumns.SENDER_NAME, mSenderName);
            values.put(AccountColumns.RINGTONE_URI, mRingtoneUri);
            values.put(AccountColumns.PROTOCOL_VERSION, mProtocolVersion);
            values.put(AccountColumns.NEW_MESSAGE_COUNT, mNewMessageCount);
            values.put(AccountColumns.SECURITY_FLAGS, mSecurityFlags);
            values.put(AccountColumns.SECURITY_SYNC_KEY, mSecuritySyncKey);
            values.put(AccountColumns.SIGNATURE, mSignature);
            return values;
        }
        public int describeContents() {
            return 0;
        }
        public static final Parcelable.Creator<EmailContent.Account> CREATOR
                = new Parcelable.Creator<EmailContent.Account>() {
            public EmailContent.Account createFromParcel(Parcel in) {
                return new EmailContent.Account(in);
            }
            public EmailContent.Account[] newArray(int size) {
                return new EmailContent.Account[size];
            }
        };
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(mId);
            dest.writeString(mDisplayName);
            dest.writeString(mEmailAddress);
            dest.writeString(mSyncKey);
            dest.writeInt(mSyncLookback);
            dest.writeInt(mSyncInterval);
            dest.writeLong(mHostAuthKeyRecv);
            dest.writeLong(mHostAuthKeySend);
            dest.writeInt(mFlags);
            dest.writeByte(mIsDefault ? (byte)1 : (byte)0);
            dest.writeString(mCompatibilityUuid);
            dest.writeString(mSenderName);
            dest.writeString(mRingtoneUri);
            dest.writeString(mProtocolVersion);
            dest.writeInt(mNewMessageCount);
            dest.writeInt(mSecurityFlags);
            dest.writeString(mSecuritySyncKey);
            dest.writeString(mSignature);
            if (mHostAuthRecv != null) {
                dest.writeByte((byte)1);
                mHostAuthRecv.writeToParcel(dest, flags);
            } else {
                dest.writeByte((byte)0);
            }
            if (mHostAuthSend != null) {
                dest.writeByte((byte)1);
                mHostAuthSend.writeToParcel(dest, flags);
            } else {
                dest.writeByte((byte)0);
            }
        }
        public Account(Parcel in) {
            mBaseUri = EmailContent.Account.CONTENT_URI;
            mId = in.readLong();
            mDisplayName = in.readString();
            mEmailAddress = in.readString();
            mSyncKey = in.readString();
            mSyncLookback = in.readInt();
            mSyncInterval = in.readInt();
            mHostAuthKeyRecv = in.readLong();
            mHostAuthKeySend = in.readLong();
            mFlags = in.readInt();
            mIsDefault = in.readByte() == 1;
            mCompatibilityUuid = in.readString();
            mSenderName = in.readString();
            mRingtoneUri = in.readString();
            mProtocolVersion = in.readString();
            mNewMessageCount = in.readInt();
            mSecurityFlags = in.readInt();
            mSecuritySyncKey = in.readString();
            mSignature = in.readString();
            mHostAuthRecv = null;
            if (in.readByte() == 1) {
                mHostAuthRecv = new EmailContent.HostAuth(in);
            }
            mHostAuthSend = null;
            if (in.readByte() == 1) {
                mHostAuthSend = new EmailContent.HostAuth(in);
            }
        }
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder('[');
            if (mHostAuthRecv != null && mHostAuthRecv.mProtocol != null) {
                sb.append(mHostAuthRecv.mProtocol);
                sb.append(':');
            }
            if (mDisplayName != null)   sb.append(mDisplayName);
            sb.append(':');
            if (mEmailAddress != null)  sb.append(mEmailAddress);
            sb.append(':');
            if (mSenderName != null)    sb.append(mSenderName);
            sb.append(']');
            return sb.toString();
        }
    }
    public interface AttachmentColumns {
        public static final String ID = "_id";
        public static final String FILENAME = "fileName";
        public static final String MIME_TYPE = "mimeType";
        public static final String SIZE = "size";
        public static final String CONTENT_ID = "contentId";
        public static final String CONTENT_URI = "contentUri";
        public static final String MESSAGE_KEY = "messageKey";
        public static final String LOCATION = "location";
        public static final String ENCODING = "encoding";
        public static final String CONTENT = "content";
        public static final String FLAGS = "flags";
        public static final String CONTENT_BYTES = "content_bytes";
    }
    public static final class Attachment extends EmailContent implements AttachmentColumns {
        public static final String TABLE_NAME = "Attachment";
        public static final Uri CONTENT_URI = Uri.parse(EmailContent.CONTENT_URI + "/attachment");
        public static final Uri MESSAGE_ID_URI = Uri.parse(
                EmailContent.CONTENT_URI + "/attachment/message");
        public String mFileName;
        public String mMimeType;
        public long mSize;
        public String mContentId;
        public String mContentUri;
        public long mMessageKey;
        public String mLocation;
        public String mEncoding;
        public String mContent; 
        public int mFlags;
        public byte[] mContentBytes;
        public static final int CONTENT_ID_COLUMN = 0;
        public static final int CONTENT_FILENAME_COLUMN = 1;
        public static final int CONTENT_MIME_TYPE_COLUMN = 2;
        public static final int CONTENT_SIZE_COLUMN = 3;
        public static final int CONTENT_CONTENT_ID_COLUMN = 4;
        public static final int CONTENT_CONTENT_URI_COLUMN = 5;
        public static final int CONTENT_MESSAGE_ID_COLUMN = 6;
        public static final int CONTENT_LOCATION_COLUMN = 7;
        public static final int CONTENT_ENCODING_COLUMN = 8;
        public static final int CONTENT_CONTENT_COLUMN = 9; 
        public static final int CONTENT_FLAGS_COLUMN = 10;
        public static final int CONTENT_CONTENT_BYTES_COLUMN = 11;
        public static final String[] CONTENT_PROJECTION = new String[] {
            RECORD_ID, AttachmentColumns.FILENAME, AttachmentColumns.MIME_TYPE,
            AttachmentColumns.SIZE, AttachmentColumns.CONTENT_ID, AttachmentColumns.CONTENT_URI,
            AttachmentColumns.MESSAGE_KEY, AttachmentColumns.LOCATION, AttachmentColumns.ENCODING,
            AttachmentColumns.CONTENT, AttachmentColumns.FLAGS, AttachmentColumns.CONTENT_BYTES
        };
        public static final int FLAG_ICS_ALTERNATIVE_PART = 1<<0;
        public Attachment() {
            mBaseUri = CONTENT_URI;
        }
        public static Attachment restoreAttachmentWithId (Context context, long id) {
            Uri u = ContentUris.withAppendedId(Attachment.CONTENT_URI, id);
            Cursor c = context.getContentResolver().query(u, Attachment.CONTENT_PROJECTION,
                    null, null, null);
            try {
                if (c.moveToFirst()) {
                    return getContent(c, Attachment.class);
                } else {
                    return null;
                }
            } finally {
                c.close();
            }
        }
        public static Attachment[] restoreAttachmentsWithMessageId(Context context,
                long messageId) {
            Uri uri = ContentUris.withAppendedId(MESSAGE_ID_URI, messageId);
            Cursor c = context.getContentResolver().query(uri, CONTENT_PROJECTION,
                    null, null, null);
            try {
                int count = c.getCount();
                Attachment[] attachments = new Attachment[count];
                for (int i = 0; i < count; ++i) {
                    c.moveToNext();
                    attachments[i] = new Attachment().restore(c);
                }
                return attachments;
            } finally {
                c.close();
            }
        }
        public static File createUniqueFile(String filename) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File directory = Environment.getExternalStorageDirectory();
                File file = new File(directory, filename);
                if (!file.exists()) {
                    return file;
                }
                int index = filename.lastIndexOf('.');
                String name = filename;
                String extension = "";
                if (index != -1) {
                    name = filename.substring(0, index);
                    extension = filename.substring(index);
                }
                for (int i = 2; i < Integer.MAX_VALUE; i++) {
                    file = new File(directory, name + '-' + i + extension);
                    if (!file.exists()) {
                        return file;
                    }
                }
                return null;
            }
            return null;
        }
        @Override
        @SuppressWarnings("unchecked")
        public EmailContent.Attachment restore(Cursor cursor) {
            mBaseUri = CONTENT_URI;
            mId = cursor.getLong(CONTENT_ID_COLUMN);
            mFileName= cursor.getString(CONTENT_FILENAME_COLUMN);
            mMimeType = cursor.getString(CONTENT_MIME_TYPE_COLUMN);
            mSize = cursor.getLong(CONTENT_SIZE_COLUMN);
            mContentId = cursor.getString(CONTENT_CONTENT_ID_COLUMN);
            mContentUri = cursor.getString(CONTENT_CONTENT_URI_COLUMN);
            mMessageKey = cursor.getLong(CONTENT_MESSAGE_ID_COLUMN);
            mLocation = cursor.getString(CONTENT_LOCATION_COLUMN);
            mEncoding = cursor.getString(CONTENT_ENCODING_COLUMN);
            mContent = cursor.getString(CONTENT_CONTENT_COLUMN);
            mFlags = cursor.getInt(CONTENT_FLAGS_COLUMN);
            mContentBytes = cursor.getBlob(CONTENT_CONTENT_BYTES_COLUMN);
            return this;
        }
        @Override
        public ContentValues toContentValues() {
            ContentValues values = new ContentValues();
            values.put(AttachmentColumns.FILENAME, mFileName);
            values.put(AttachmentColumns.MIME_TYPE, mMimeType);
            values.put(AttachmentColumns.SIZE, mSize);
            values.put(AttachmentColumns.CONTENT_ID, mContentId);
            values.put(AttachmentColumns.CONTENT_URI, mContentUri);
            values.put(AttachmentColumns.MESSAGE_KEY, mMessageKey);
            values.put(AttachmentColumns.LOCATION, mLocation);
            values.put(AttachmentColumns.ENCODING, mEncoding);
            values.put(AttachmentColumns.CONTENT, mContent);
            values.put(AttachmentColumns.FLAGS, mFlags);
            values.put(AttachmentColumns.CONTENT_BYTES, mContentBytes);
            return values;
        }
        public int describeContents() {
             return 0;
        }
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(mId);
            dest.writeString(mFileName);
            dest.writeString(mMimeType);
            dest.writeLong(mSize);
            dest.writeString(mContentId);
            dest.writeString(mContentUri);
            dest.writeLong(mMessageKey);
            dest.writeString(mLocation);
            dest.writeString(mEncoding);
            dest.writeString(mContent);
            dest.writeInt(mFlags);
            if (mContentBytes == null) {
                dest.writeInt(-1);
            } else {
                dest.writeInt(mContentBytes.length);
                dest.writeByteArray(mContentBytes);
            }
        }
        public Attachment(Parcel in) {
            mBaseUri = EmailContent.Attachment.CONTENT_URI;
            mId = in.readLong();
            mFileName = in.readString();
            mMimeType = in.readString();
            mSize = in.readLong();
            mContentId = in.readString();
            mContentUri = in.readString();
            mMessageKey = in.readLong();
            mLocation = in.readString();
            mEncoding = in.readString();
            mContent = in.readString();
            mFlags = in.readInt();
            final int contentBytesLen = in.readInt();
            if (contentBytesLen == -1) {
                mContentBytes = null;
            } else {
                mContentBytes = new byte[contentBytesLen];
                in.readByteArray(mContentBytes);
            }
         }
        public static final Parcelable.Creator<EmailContent.Attachment> CREATOR
        = new Parcelable.Creator<EmailContent.Attachment>() {
            public EmailContent.Attachment createFromParcel(Parcel in) {
                return new EmailContent.Attachment(in);
            }
            public EmailContent.Attachment[] newArray(int size) {
                return new EmailContent.Attachment[size];
            }
        };
        @Override
        public String toString() {
            return "[" + mFileName + ", " + mMimeType + ", " + mSize + ", " + mContentId + ", "
                    + mContentUri + ", " + mMessageKey + ", " + mLocation + ", " + mEncoding  + ", "
                    + mFlags + ", " + mContentBytes + "]";
        }
    }
    public interface MailboxColumns {
        public static final String ID = "_id";
        static final String DISPLAY_NAME = "displayName";
        public static final String SERVER_ID = "serverId";
        public static final String PARENT_SERVER_ID = "parentServerId";
        public static final String ACCOUNT_KEY = "accountKey";
        public static final String TYPE = "type";
        public static final String DELIMITER = "delimiter";
        public static final String SYNC_KEY = "syncKey";
        public static final String SYNC_LOOKBACK = "syncLookback";
        public static final String SYNC_INTERVAL = "syncInterval";
        public static final String SYNC_TIME = "syncTime";
        public static final String UNREAD_COUNT = "unreadCount";
        public static final String FLAG_VISIBLE = "flagVisible";
        public static final String FLAGS = "flags";
        public static final String VISIBLE_LIMIT = "visibleLimit";
        public static final String SYNC_STATUS = "syncStatus";
    }
    public static final class Mailbox extends EmailContent implements SyncColumns, MailboxColumns {
        public static final String TABLE_NAME = "Mailbox";
        public static final Uri CONTENT_URI = Uri.parse(EmailContent.CONTENT_URI + "/mailbox");
        public static final Uri ADD_TO_FIELD_URI =
            Uri.parse(EmailContent.CONTENT_URI + "/mailboxIdAddToField");
        public String mDisplayName;
        public String mServerId;
        public String mParentServerId;
        public long mAccountKey;
        public int mType;
        public int mDelimiter;
        public String mSyncKey;
        public int mSyncLookback;
        public int mSyncInterval;
        public long mSyncTime;
        public int mUnreadCount;
        public boolean mFlagVisible = true;
        public int mFlags;
        public int mVisibleLimit;
        public String mSyncStatus;
        public static final int CONTENT_ID_COLUMN = 0;
        public static final int CONTENT_DISPLAY_NAME_COLUMN = 1;
        public static final int CONTENT_SERVER_ID_COLUMN = 2;
        public static final int CONTENT_PARENT_SERVER_ID_COLUMN = 3;
        public static final int CONTENT_ACCOUNT_KEY_COLUMN = 4;
        public static final int CONTENT_TYPE_COLUMN = 5;
        public static final int CONTENT_DELIMITER_COLUMN = 6;
        public static final int CONTENT_SYNC_KEY_COLUMN = 7;
        public static final int CONTENT_SYNC_LOOKBACK_COLUMN = 8;
        public static final int CONTENT_SYNC_INTERVAL_COLUMN = 9;
        public static final int CONTENT_SYNC_TIME_COLUMN = 10;
        public static final int CONTENT_UNREAD_COUNT_COLUMN = 11;
        public static final int CONTENT_FLAG_VISIBLE_COLUMN = 12;
        public static final int CONTENT_FLAGS_COLUMN = 13;
        public static final int CONTENT_VISIBLE_LIMIT_COLUMN = 14;
        public static final int CONTENT_SYNC_STATUS_COLUMN = 15;
        public static final String[] CONTENT_PROJECTION = new String[] {
            RECORD_ID, MailboxColumns.DISPLAY_NAME, MailboxColumns.SERVER_ID,
            MailboxColumns.PARENT_SERVER_ID, MailboxColumns.ACCOUNT_KEY, MailboxColumns.TYPE,
            MailboxColumns.DELIMITER, MailboxColumns.SYNC_KEY, MailboxColumns.SYNC_LOOKBACK,
            MailboxColumns.SYNC_INTERVAL, MailboxColumns.SYNC_TIME,MailboxColumns.UNREAD_COUNT,
            MailboxColumns.FLAG_VISIBLE, MailboxColumns.FLAGS, MailboxColumns.VISIBLE_LIMIT,
            MailboxColumns.SYNC_STATUS
        };
        public static final long NO_MAILBOX = -1;
        public static final int CHECK_INTERVAL_NEVER = -1;
        public static final int CHECK_INTERVAL_PUSH = -2;
        public static final int CHECK_INTERVAL_PING = -3;
        public static final int CHECK_INTERVAL_PUSH_HOLD = -4;
        private static final String WHERE_TYPE_AND_ACCOUNT_KEY =
            MailboxColumns.TYPE + "=? and " + MailboxColumns.ACCOUNT_KEY + "=?";
        public static final int TYPE_INBOX = 0;
        public static final int TYPE_MAIL = 1;
        public static final int TYPE_PARENT = 2;
        public static final int TYPE_DRAFTS = 3;
        public static final int TYPE_OUTBOX = 4;
        public static final int TYPE_SENT = 5;
        public static final int TYPE_TRASH = 6;
        public static final int TYPE_JUNK = 7;
        public static final int TYPE_NOT_EMAIL = 0x40;
        public static final int TYPE_CALENDAR = 0x41;
        public static final int TYPE_CONTACTS = 0x42;
        public static final int TYPE_TASKS = 0x43;
        public static final int TYPE_EAS_ACCOUNT_MAILBOX = 0x44;
        public static final int FLAG_HAS_CHILDREN = 1<<0;
        public static final int FLAG_CHILDREN_VISIBLE = 1<<1;
        public static final int FLAG_CANT_PUSH = 1<<2;
        public static final long QUERY_ALL_INBOXES = -2;
        public static final long QUERY_ALL_UNREAD = -3;
        public static final long QUERY_ALL_FAVORITES = -4;
        public static final long QUERY_ALL_DRAFTS = -5;
        public static final long QUERY_ALL_OUTBOX = -6;
        public Mailbox() {
            mBaseUri = CONTENT_URI;
        }
        public static Mailbox restoreMailboxWithId(Context context, long id) {
            Uri u = ContentUris.withAppendedId(Mailbox.CONTENT_URI, id);
            Cursor c = context.getContentResolver().query(u, Mailbox.CONTENT_PROJECTION,
                    null, null, null);
            try {
                if (c.moveToFirst()) {
                    return EmailContent.getContent(c, Mailbox.class);
                } else {
                    return null;
                }
            } finally {
                c.close();
            }
        }
        @Override
        @SuppressWarnings("unchecked")
        public EmailContent.Mailbox restore(Cursor cursor) {
            mBaseUri = CONTENT_URI;
            mId = cursor.getLong(CONTENT_ID_COLUMN);
            mDisplayName = cursor.getString(CONTENT_DISPLAY_NAME_COLUMN);
            mServerId = cursor.getString(CONTENT_SERVER_ID_COLUMN);
            mParentServerId = cursor.getString(CONTENT_PARENT_SERVER_ID_COLUMN);
            mAccountKey = cursor.getLong(CONTENT_ACCOUNT_KEY_COLUMN);
            mType = cursor.getInt(CONTENT_TYPE_COLUMN);
            mDelimiter = cursor.getInt(CONTENT_DELIMITER_COLUMN);
            mSyncKey = cursor.getString(CONTENT_SYNC_KEY_COLUMN);
            mSyncLookback = cursor.getInt(CONTENT_SYNC_LOOKBACK_COLUMN);
            mSyncInterval = cursor.getInt(CONTENT_SYNC_INTERVAL_COLUMN);
            mSyncTime = cursor.getLong(CONTENT_SYNC_TIME_COLUMN);
            mUnreadCount = cursor.getInt(CONTENT_UNREAD_COUNT_COLUMN);
            mFlagVisible = cursor.getInt(CONTENT_FLAG_VISIBLE_COLUMN) == 1;
            mFlags = cursor.getInt(CONTENT_FLAGS_COLUMN);
            mVisibleLimit = cursor.getInt(CONTENT_VISIBLE_LIMIT_COLUMN);
            mSyncStatus = cursor.getString(CONTENT_SYNC_STATUS_COLUMN);
            return this;
        }
        @Override
        public ContentValues toContentValues() {
            ContentValues values = new ContentValues();
            values.put(MailboxColumns.DISPLAY_NAME, mDisplayName);
            values.put(MailboxColumns.SERVER_ID, mServerId);
            values.put(MailboxColumns.PARENT_SERVER_ID, mParentServerId);
            values.put(MailboxColumns.ACCOUNT_KEY, mAccountKey);
            values.put(MailboxColumns.TYPE, mType);
            values.put(MailboxColumns.DELIMITER, mDelimiter);
            values.put(MailboxColumns.SYNC_KEY, mSyncKey);
            values.put(MailboxColumns.SYNC_LOOKBACK, mSyncLookback);
            values.put(MailboxColumns.SYNC_INTERVAL, mSyncInterval);
            values.put(MailboxColumns.SYNC_TIME, mSyncTime);
            values.put(MailboxColumns.UNREAD_COUNT, mUnreadCount);
            values.put(MailboxColumns.FLAG_VISIBLE, mFlagVisible);
            values.put(MailboxColumns.FLAGS, mFlags);
            values.put(MailboxColumns.VISIBLE_LIMIT, mVisibleLimit);
            values.put(MailboxColumns.SYNC_STATUS, mSyncStatus);
            return values;
        }
        public static long findMailboxOfType(Context context, long accountId, int type) {
            long mailboxId = NO_MAILBOX;
            String[] bindArguments = new String[] {Long.toString(type), Long.toString(accountId)};
            Cursor c = context.getContentResolver().query(Mailbox.CONTENT_URI,
                    ID_PROJECTION, WHERE_TYPE_AND_ACCOUNT_KEY, bindArguments, null);
            try {
                if (c.moveToFirst()) {
                    mailboxId = c.getLong(ID_PROJECTION_COLUMN);
                }
            } finally {
                c.close();
            }
            return mailboxId;
        }
        public static Mailbox restoreMailboxOfType(Context context, long accountId, int type) {
            long mailboxId = findMailboxOfType(context, accountId, type);
            if (mailboxId != Mailbox.NO_MAILBOX) {
                return Mailbox.restoreMailboxWithId(context, mailboxId);
            }
            return null;
        }
    }
    public interface HostAuthColumns {
        public static final String ID = "_id";
        static final String PROTOCOL = "protocol";
        static final String ADDRESS = "address";
        static final String PORT = "port";
        static final String FLAGS = "flags";
        static final String LOGIN = "login";
        static final String PASSWORD = "password";
        static final String DOMAIN = "domain";
        static final String ACCOUNT_KEY = "accountKey";
    }
    public static final class HostAuth extends EmailContent implements HostAuthColumns, Parcelable {
        public static final String TABLE_NAME = "HostAuth";
        public static final Uri CONTENT_URI = Uri.parse(EmailContent.CONTENT_URI + "/hostauth");
        public static final int FLAG_SSL = 1;
        public static final int FLAG_TLS = 2;
        public static final int FLAG_AUTHENTICATE = 4;
        public static final int FLAG_TRUST_ALL_CERTIFICATES = 8;
        public String mProtocol;
        public String mAddress;
        public int mPort;
        public int mFlags;
        public String mLogin;
        public String mPassword;
        public String mDomain;
        public long mAccountKey;        
        public static final int CONTENT_ID_COLUMN = 0;
        public static final int CONTENT_PROTOCOL_COLUMN = 1;
        public static final int CONTENT_ADDRESS_COLUMN = 2;
        public static final int CONTENT_PORT_COLUMN = 3;
        public static final int CONTENT_FLAGS_COLUMN = 4;
        public static final int CONTENT_LOGIN_COLUMN = 5;
        public static final int CONTENT_PASSWORD_COLUMN = 6;
        public static final int CONTENT_DOMAIN_COLUMN = 7;
        public static final int CONTENT_ACCOUNT_KEY_COLUMN = 8;
        public static final String[] CONTENT_PROJECTION = new String[] {
            RECORD_ID, HostAuthColumns.PROTOCOL, HostAuthColumns.ADDRESS, HostAuthColumns.PORT,
            HostAuthColumns.FLAGS, HostAuthColumns.LOGIN,
            HostAuthColumns.PASSWORD, HostAuthColumns.DOMAIN,
            HostAuthColumns.ACCOUNT_KEY
        };
        public HostAuth() {
            mBaseUri = CONTENT_URI;
            mPort = -1;
        }
        public static HostAuth restoreHostAuthWithId(Context context, long id) {
            Uri u = ContentUris.withAppendedId(EmailContent.HostAuth.CONTENT_URI, id);
            Cursor c = context.getContentResolver().query(u, HostAuth.CONTENT_PROJECTION,
                    null, null, null);
            try {
                if (c.moveToFirst()) {
                    return getContent(c, HostAuth.class);
                } else {
                    return null;
                }
            } finally {
                c.close();
            }
        }
        @Override
        @SuppressWarnings("unchecked")
        public EmailContent.HostAuth restore(Cursor cursor) {
            mBaseUri = CONTENT_URI;
            mId = cursor.getLong(CONTENT_ID_COLUMN);
            mProtocol = cursor.getString(CONTENT_PROTOCOL_COLUMN);
            mAddress = cursor.getString(CONTENT_ADDRESS_COLUMN);
            mPort = cursor.getInt(CONTENT_PORT_COLUMN);
            mFlags = cursor.getInt(CONTENT_FLAGS_COLUMN);
            mLogin = cursor.getString(CONTENT_LOGIN_COLUMN);
            mPassword = cursor.getString(CONTENT_PASSWORD_COLUMN);
            mDomain = cursor.getString(CONTENT_DOMAIN_COLUMN);
            mAccountKey = cursor.getLong(CONTENT_ACCOUNT_KEY_COLUMN);
            return this;
        }
        @Override
        public ContentValues toContentValues() {
            ContentValues values = new ContentValues();
            values.put(HostAuthColumns.PROTOCOL, mProtocol);
            values.put(HostAuthColumns.ADDRESS, mAddress);
            values.put(HostAuthColumns.PORT, mPort);
            values.put(HostAuthColumns.FLAGS, mFlags);
            values.put(HostAuthColumns.LOGIN, mLogin);
            values.put(HostAuthColumns.PASSWORD, mPassword);
            values.put(HostAuthColumns.DOMAIN, mDomain);
            values.put(HostAuthColumns.ACCOUNT_KEY, mAccountKey);
            return values;
        }
        public String getStoreUri() {
            String security;
            switch (mFlags & (FLAG_SSL | FLAG_TLS | FLAG_TRUST_ALL_CERTIFICATES)) {
                case FLAG_SSL:
                    security = "+ssl+";
                    break;
                case FLAG_SSL | FLAG_TRUST_ALL_CERTIFICATES:
                    security = "+ssl+trustallcerts";
                    break;
                case FLAG_TLS:
                    security = "+tls+";
                    break;
                case FLAG_TLS | FLAG_TRUST_ALL_CERTIFICATES:
                    security = "+tls+trustallcerts";
                    break;
                default:
                    security = "";
                    break;
            }
            String userInfo = null;
            if ((mFlags & FLAG_AUTHENTICATE) != 0) {
                String trimUser = (mLogin != null) ? mLogin.trim() : "";
                String trimPassword = (mPassword != null) ? mPassword.trim() : "";
                userInfo = trimUser + ":" + trimPassword;
            }
            String address = (mAddress != null) ? mAddress.trim() : null;
            String path = (mDomain != null) ? "/" + mDomain : null;
            URI uri;
            try {
                uri = new URI(
                        mProtocol + security,
                        userInfo,
                        address,
                        mPort,
                        path,
                        null,
                        null);
                return uri.toString();
            } catch (URISyntaxException e) {
                return null;
            }
        }
        @Deprecated
        public void setStoreUri(String uriString) {
            try {
                URI uri = new URI(uriString);
                mLogin = null;
                mPassword = null;
                mFlags &= ~FLAG_AUTHENTICATE;
                if (uri.getUserInfo() != null) {
                    String[] userInfoParts = uri.getUserInfo().split(":", 2);
                    mLogin = userInfoParts[0];
                    mFlags |= FLAG_AUTHENTICATE;
                    if (userInfoParts.length > 1) {
                        mPassword = userInfoParts[1];
                    }
                }
                String[] schemeParts = uri.getScheme().split("\\+");
                mProtocol = (schemeParts.length >= 1) ? schemeParts[0] : null;
                mFlags &= ~(FLAG_SSL | FLAG_TLS | FLAG_TRUST_ALL_CERTIFICATES);
                boolean ssl = false;
                if (schemeParts.length >= 2) {
                    String part1 = schemeParts[1];
                    if ("ssl".equals(part1)) {
                        ssl = true;
                        mFlags |= FLAG_SSL;
                    } else if ("tls".equals(part1)) {
                        mFlags |= FLAG_TLS;
                    }
                    if (schemeParts.length >= 3) {
                        String part2 = schemeParts[2];
                        if ("trustallcerts".equals(part2)) {
                            mFlags |= FLAG_TRUST_ALL_CERTIFICATES;
                        }
                    }
                }
                mAddress = uri.getHost();
                mPort = uri.getPort();
                if (mPort == -1) {
                    if ("pop3".equals(mProtocol)) {
                        mPort = ssl ? 995 : 110;
                    } else if ("imap".equals(mProtocol)) {
                        mPort = ssl ? 993 : 143;
                    } else if ("eas".equals(mProtocol)) {
                        mPort = ssl ? 443 : 80;
                    }  else if ("smtp".equals(mProtocol)) {
                        mPort = ssl ? 465 : 587;
                    }
                }
                if (uri.getPath() != null && uri.getPath().length() > 0) {
                    mDomain = uri.getPath().substring(1);
                }
            } catch (URISyntaxException use) {
                throw new Error(use);
            }
        }
        public int describeContents() {
            return 0;
        }
        public static final Parcelable.Creator<EmailContent.HostAuth> CREATOR
                = new Parcelable.Creator<EmailContent.HostAuth>() {
            public EmailContent.HostAuth createFromParcel(Parcel in) {
                return new EmailContent.HostAuth(in);
            }
            public EmailContent.HostAuth[] newArray(int size) {
                return new EmailContent.HostAuth[size];
            }
        };
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(mId);
            dest.writeString(mProtocol);
            dest.writeString(mAddress);
            dest.writeInt(mPort);
            dest.writeInt(mFlags);
            dest.writeString(mLogin);
            dest.writeString(mPassword);
            dest.writeString(mDomain);
            dest.writeLong(mAccountKey);
        }
        public HostAuth(Parcel in) {
            mBaseUri = CONTENT_URI;
            mId = in.readLong();
            mProtocol = in.readString();
            mAddress = in.readString();
            mPort = in.readInt();
            mFlags = in.readInt();
            mLogin = in.readString();
            mPassword = in.readString();
            mDomain = in.readString();
            mAccountKey = in.readLong();
        }
        @Override
        public String toString() {
            return getStoreUri();
        }
    }
}
