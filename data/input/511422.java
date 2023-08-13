public final class Telephony {
    private static final String TAG = "Telephony";
    private static final boolean DEBUG = true;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    public Telephony() {
    }
    public interface TextBasedSmsColumns {
        public static final String TYPE = "type";
        public static final int MESSAGE_TYPE_ALL    = 0;
        public static final int MESSAGE_TYPE_INBOX  = 1;
        public static final int MESSAGE_TYPE_SENT   = 2;
        public static final int MESSAGE_TYPE_DRAFT  = 3;
        public static final int MESSAGE_TYPE_OUTBOX = 4;
        public static final int MESSAGE_TYPE_FAILED = 5; 
        public static final int MESSAGE_TYPE_QUEUED = 6; 
        public static final String THREAD_ID = "thread_id";
        public static final String ADDRESS = "address";
        public static final String PERSON_ID = "person";
        public static final String DATE = "date";
        public static final String READ = "read";
        public static final String SEEN = "seen";
        public static final String STATUS = "status";
        public static final int STATUS_NONE = -1;
        public static final int STATUS_COMPLETE = 0;
        public static final int STATUS_PENDING = 64;
        public static final int STATUS_FAILED = 128;
        public static final String SUBJECT = "subject";
        public static final String BODY = "body";
        public static final String PERSON = "person";
        public static final String PROTOCOL = "protocol";
        public static final String REPLY_PATH_PRESENT = "reply_path_present";
        public static final String SERVICE_CENTER = "service_center";
        public static final String LOCKED = "locked";
        public static final String ERROR_CODE = "error_code";
        public static final String META_DATA = "meta_data";
}
    public static final class Sms implements BaseColumns, TextBasedSmsColumns {
        public static final Cursor query(ContentResolver cr, String[] projection) {
            return cr.query(CONTENT_URI, projection, null, null, DEFAULT_SORT_ORDER);
        }
        public static final Cursor query(ContentResolver cr, String[] projection,
                String where, String orderBy) {
            return cr.query(CONTENT_URI, projection, where,
                                         null, orderBy == null ? DEFAULT_SORT_ORDER : orderBy);
        }
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        public static final String DEFAULT_SORT_ORDER = "date DESC";
        public static Uri addMessageToUri(ContentResolver resolver,
                Uri uri, String address, String body, String subject,
                Long date, boolean read, boolean deliveryReport) {
            return addMessageToUri(resolver, uri, address, body, subject,
                    date, read, deliveryReport, -1L);
        }
        public static Uri addMessageToUri(ContentResolver resolver,
                Uri uri, String address, String body, String subject,
                Long date, boolean read, boolean deliveryReport, long threadId) {
            ContentValues values = new ContentValues(7);
            values.put(ADDRESS, address);
            if (date != null) {
                values.put(DATE, date);
            }
            values.put(READ, read ? Integer.valueOf(1) : Integer.valueOf(0));
            values.put(SUBJECT, subject);
            values.put(BODY, body);
            if (deliveryReport) {
                values.put(STATUS, STATUS_PENDING);
            }
            if (threadId != -1L) {
                values.put(THREAD_ID, threadId);
            }
            return resolver.insert(uri, values);
        }
        public static boolean moveMessageToFolder(Context context,
                Uri uri, int folder, int error) {
            if (uri == null) {
                return false;
            }
            boolean markAsUnread = false;
            boolean markAsRead = false;
            switch(folder) {
            case MESSAGE_TYPE_INBOX:
            case MESSAGE_TYPE_DRAFT:
                break;
            case MESSAGE_TYPE_OUTBOX:
            case MESSAGE_TYPE_SENT:
                markAsRead = true;
                break;
            case MESSAGE_TYPE_FAILED:
            case MESSAGE_TYPE_QUEUED:
                markAsUnread = true;
                break;
            default:
                return false;
            }
            ContentValues values = new ContentValues(3);
            values.put(TYPE, folder);
            if (markAsUnread) {
                values.put(READ, Integer.valueOf(0));
            } else if (markAsRead) {
                values.put(READ, Integer.valueOf(1));
            }
            values.put(ERROR_CODE, error);
            return 1 == SqliteWrapper.update(context, context.getContentResolver(),
                            uri, values, null, null);
        }
        public static boolean isOutgoingFolder(int messageType) {
            return  (messageType == MESSAGE_TYPE_FAILED)
                    || (messageType == MESSAGE_TYPE_OUTBOX)
                    || (messageType == MESSAGE_TYPE_SENT)
                    || (messageType == MESSAGE_TYPE_QUEUED);
        }
        public static final class Inbox implements BaseColumns, TextBasedSmsColumns {
            public static final Uri CONTENT_URI =
                Uri.parse("content:
            public static final String DEFAULT_SORT_ORDER = "date DESC";
            public static Uri addMessage(ContentResolver resolver,
                    String address, String body, String subject, Long date,
                    boolean read) {
                return addMessageToUri(resolver, CONTENT_URI, address, body,
                        subject, date, read, false);
            }
        }
        public static final class Sent implements BaseColumns, TextBasedSmsColumns {
            public static final Uri CONTENT_URI =
                    Uri.parse("content:
            public static final String DEFAULT_SORT_ORDER = "date DESC";
            public static Uri addMessage(ContentResolver resolver,
                    String address, String body, String subject, Long date) {
                return addMessageToUri(resolver, CONTENT_URI, address, body,
                        subject, date, true, false);
            }
        }
        public static final class Draft implements BaseColumns, TextBasedSmsColumns {
            public static final Uri CONTENT_URI =
                    Uri.parse("content:
            public static final String DEFAULT_SORT_ORDER = "date DESC";
            public static Uri addMessage(ContentResolver resolver,
                    String address, String body, String subject, Long date) {
                return addMessageToUri(resolver, CONTENT_URI, address, body,
                        subject, date, true, false);
            }
            public static boolean saveMessage(ContentResolver resolver,
                    Uri uri, String body) {
                ContentValues values = new ContentValues(2);
                values.put(BODY, body);
                values.put(DATE, System.currentTimeMillis());
                return resolver.update(uri, values, null, null) == 1;
            }
        }
        public static final class Outbox implements BaseColumns, TextBasedSmsColumns {
            public static final Uri CONTENT_URI =
                Uri.parse("content:
            public static final String DEFAULT_SORT_ORDER = "date DESC";
            public static Uri addMessage(ContentResolver resolver,
                    String address, String body, String subject, Long date,
                    boolean deliveryReport, long threadId) {
                return addMessageToUri(resolver, CONTENT_URI, address, body,
                        subject, date, true, deliveryReport, threadId);
            }
        }
        public static final class Conversations
                implements BaseColumns, TextBasedSmsColumns {
            public static final Uri CONTENT_URI =
                Uri.parse("content:
            public static final String DEFAULT_SORT_ORDER = "date DESC";
            public static final String SNIPPET = "snippet";
            public static final String MESSAGE_COUNT = "msg_count";
        }
        public static final class Intents {
            public static final int RESULT_SMS_HANDLED = 1;
            public static final int RESULT_SMS_GENERIC_ERROR = 2;
            public static final int RESULT_SMS_OUT_OF_MEMORY = 3;
            public static final int RESULT_SMS_UNSUPPORTED = 4;
            @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
            public static final String SMS_RECEIVED_ACTION =
                    "android.provider.Telephony.SMS_RECEIVED";
            @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
            public static final String DATA_SMS_RECEIVED_ACTION =
                    "android.intent.action.DATA_SMS_RECEIVED";
            @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
            public static final String WAP_PUSH_RECEIVED_ACTION =
                    "android.provider.Telephony.WAP_PUSH_RECEIVED";
            @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
            public static final String SIM_FULL_ACTION =
                "android.provider.Telephony.SIM_FULL";
            @SdkConstant(SdkConstantType.BROADCAST_INTENT_ACTION)
            public static final String SMS_REJECTED_ACTION =
                "android.provider.Telephony.SMS_REJECTED";
            public static final SmsMessage[] getMessagesFromIntent(
                    Intent intent) {
                Object[] messages = (Object[]) intent.getSerializableExtra("pdus");
                byte[][] pduObjs = new byte[messages.length][];
                for (int i = 0; i < messages.length; i++) {
                    pduObjs[i] = (byte[]) messages[i];
                }
                byte[][] pdus = new byte[pduObjs.length][];
                int pduCount = pdus.length;
                SmsMessage[] msgs = new SmsMessage[pduCount];
                for (int i = 0; i < pduCount; i++) {
                    pdus[i] = pduObjs[i];
                    msgs[i] = SmsMessage.createFromPdu(pdus[i]);
                }
                return msgs;
            }
        }
    }
    public interface BaseMmsColumns extends BaseColumns {
        public static final int MESSAGE_BOX_ALL    = 0;
        public static final int MESSAGE_BOX_INBOX  = 1;
        public static final int MESSAGE_BOX_SENT   = 2;
        public static final int MESSAGE_BOX_DRAFTS = 3;
        public static final int MESSAGE_BOX_OUTBOX = 4;
        public static final String DATE = "date";
        public static final String MESSAGE_BOX = "msg_box";
        public static final String READ = "read";
        public static final String SEEN = "seen";
        public static final String MESSAGE_ID = "m_id";
        public static final String SUBJECT = "sub";
        public static final String SUBJECT_CHARSET = "sub_cs";
        public static final String CONTENT_TYPE = "ct_t";
        public static final String CONTENT_LOCATION = "ct_l";
        public static final String FROM = "from";
        public static final String TO = "to";
        public static final String CC = "cc";
        public static final String BCC = "bcc";
        public static final String EXPIRY = "exp";
        public static final String MESSAGE_CLASS = "m_cls";
        public static final String MESSAGE_TYPE = "m_type";
        public static final String MMS_VERSION = "v";
        public static final String MESSAGE_SIZE = "m_size";
        public static final String PRIORITY = "pri";
        public static final String READ_REPORT = "rr";
        public static final String REPORT_ALLOWED = "rpt_a";
        public static final String RESPONSE_STATUS = "resp_st";
        public static final String STATUS = "st";
        public static final String TRANSACTION_ID = "tr_id";
        public static final String RETRIEVE_STATUS = "retr_st";
        public static final String RETRIEVE_TEXT = "retr_txt";
        public static final String RETRIEVE_TEXT_CHARSET = "retr_txt_cs";
        public static final String READ_STATUS = "read_status";
        public static final String CONTENT_CLASS = "ct_cls";
        public static final String DELIVERY_REPORT = "d_rpt";
        public static final String DELIVERY_TIME_TOKEN = "d_tm_tok";
        public static final String DELIVERY_TIME = "d_tm";
        public static final String RESPONSE_TEXT = "resp_txt";
        public static final String SENDER_VISIBILITY = "s_vis";
        public static final String REPLY_CHARGING = "r_chg";
        public static final String REPLY_CHARGING_DEADLINE_TOKEN = "r_chg_dl_tok";
        public static final String REPLY_CHARGING_DEADLINE = "r_chg_dl";
        public static final String REPLY_CHARGING_ID = "r_chg_id";
        public static final String REPLY_CHARGING_SIZE = "r_chg_sz";
        public static final String PREVIOUSLY_SENT_BY = "p_s_by";
        public static final String PREVIOUSLY_SENT_DATE = "p_s_d";
        public static final String STORE = "store";
        public static final String MM_STATE = "mm_st";
        public static final String MM_FLAGS_TOKEN = "mm_flg_tok";
        public static final String MM_FLAGS = "mm_flg";
        public static final String STORE_STATUS = "store_st";
        public static final String STORE_STATUS_TEXT = "store_st_txt";
        public static final String STORED = "stored";
        public static final String TOTALS = "totals";
        public static final String MBOX_TOTALS = "mb_t";
        public static final String MBOX_TOTALS_TOKEN = "mb_t_tok";
        public static final String QUOTAS = "qt";
        public static final String MBOX_QUOTAS = "mb_qt";
        public static final String MBOX_QUOTAS_TOKEN = "mb_qt_tok";
        public static final String MESSAGE_COUNT = "m_cnt";
        public static final String START = "start";
        public static final String DISTRIBUTION_INDICATOR = "d_ind";
        public static final String ELEMENT_DESCRIPTOR = "e_des";
        public static final String LIMIT = "limit";
        public static final String RECOMMENDED_RETRIEVAL_MODE = "r_r_mod";
        public static final String RECOMMENDED_RETRIEVAL_MODE_TEXT = "r_r_mod_txt";
        public static final String STATUS_TEXT = "st_txt";
        public static final String APPLIC_ID = "apl_id";
        public static final String REPLY_APPLIC_ID = "r_apl_id";
        public static final String AUX_APPLIC_ID = "aux_apl_id";
        public static final String DRM_CONTENT = "drm_c";
        public static final String ADAPTATION_ALLOWED = "adp_a";
        public static final String REPLACE_ID = "repl_id";
        public static final String CANCEL_ID = "cl_id";
        public static final String CANCEL_STATUS = "cl_st";
        public static final String THREAD_ID = "thread_id";
        public static final String LOCKED = "locked";
        public static final String META_DATA = "meta_data";
    }
    public interface CanonicalAddressesColumns extends BaseColumns {
        public static final String ADDRESS = "address";
    }
    public interface ThreadsColumns extends BaseColumns {
        public static final String DATE = "date";
        public static final String RECIPIENT_IDS = "recipient_ids";
        public static final String MESSAGE_COUNT = "message_count";
        public static final String READ = "read";
        public static final String SNIPPET = "snippet";
        public static final String SNIPPET_CHARSET = "snippet_cs";
        public static final String TYPE = "type";
        public static final String ERROR = "error";
        public static final String HAS_ATTACHMENT = "has_attachment";
    }
    public static final class Threads implements ThreadsColumns {
        private static final String[] ID_PROJECTION = { BaseColumns._ID };
        private static final String STANDARD_ENCODING = "UTF-8";
        private static final Uri THREAD_ID_CONTENT_URI = Uri.parse(
                "content:
        public static final Uri CONTENT_URI = Uri.withAppendedPath(
                MmsSms.CONTENT_URI, "conversations");
        public static final Uri OBSOLETE_THREADS_URI = Uri.withAppendedPath(
                CONTENT_URI, "obsolete");
        public static final int COMMON_THREAD    = 0;
        public static final int BROADCAST_THREAD = 1;
        private Threads() {
        }
        public static long getOrCreateThreadId(Context context, String recipient) {
            Set<String> recipients = new HashSet<String>();
            recipients.add(recipient);
            return getOrCreateThreadId(context, recipients);
        }
        public static long getOrCreateThreadId(
                Context context, Set<String> recipients) {
            Uri.Builder uriBuilder = THREAD_ID_CONTENT_URI.buildUpon();
            for (String recipient : recipients) {
                if (Mms.isEmailAddress(recipient)) {
                    recipient = Mms.extractAddrSpec(recipient);
                }
                uriBuilder.appendQueryParameter("recipient", recipient);
            }
            Uri uri = uriBuilder.build();
            if (DEBUG) {
                Log.v(TAG, "getOrCreateThreadId uri: " + uri);
            }
            Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
                    uri, ID_PROJECTION, null, null, null);
            if (DEBUG) {
                Log.v(TAG, "getOrCreateThreadId cursor cnt: " + cursor.getCount());
            }
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        return cursor.getLong(0);
                    } else {
                        Log.e(TAG, "getOrCreateThreadId returned no rows!");
                    }
                } finally {
                    cursor.close();
                }
            }
            Log.e(TAG, "getOrCreateThreadId failed with uri " + uri.toString());
            throw new IllegalArgumentException("Unable to find or allocate a thread ID.");
        }
    }
    public static final class Mms implements BaseMmsColumns {
        public static final Uri CONTENT_URI = Uri.parse("content:
        public static final Uri REPORT_REQUEST_URI = Uri.withAppendedPath(
                                            CONTENT_URI, "report-request");
        public static final Uri REPORT_STATUS_URI = Uri.withAppendedPath(
                                            CONTENT_URI, "report-status");
        public static final String DEFAULT_SORT_ORDER = "date DESC";
        public static final Pattern NAME_ADDR_EMAIL_PATTERN =
                Pattern.compile("\\s*(\"[^\"]*\"|[^<>\"]+)\\s*<([^<>]+)>\\s*");
        public static final Pattern QUOTED_STRING_PATTERN =
                Pattern.compile("\\s*\"([^\"]*)\"\\s*");
        public static final Cursor query(
                ContentResolver cr, String[] projection) {
            return cr.query(CONTENT_URI, projection, null, null, DEFAULT_SORT_ORDER);
        }
        public static final Cursor query(
                ContentResolver cr, String[] projection,
                String where, String orderBy) {
            return cr.query(CONTENT_URI, projection,
                    where, null, orderBy == null ? DEFAULT_SORT_ORDER : orderBy);
        }
        public static final String getMessageBoxName(int msgBox) {
            switch (msgBox) {
                case MESSAGE_BOX_ALL:
                    return "all";
                case MESSAGE_BOX_INBOX:
                    return "inbox";
                case MESSAGE_BOX_SENT:
                    return "sent";
                case MESSAGE_BOX_DRAFTS:
                    return "drafts";
                case MESSAGE_BOX_OUTBOX:
                    return "outbox";
                default:
                    throw new IllegalArgumentException("Invalid message box: " + msgBox);
            }
        }
        public static String extractAddrSpec(String address) {
            Matcher match = NAME_ADDR_EMAIL_PATTERN.matcher(address);
            if (match.matches()) {
                return match.group(2);
            }
            return address;
        }
        public static boolean isEmailAddress(String address) {
            if (TextUtils.isEmpty(address)) {
                return false;
            }
            String s = extractAddrSpec(address);
            Matcher match = Patterns.EMAIL_ADDRESS.matcher(s);
            return match.matches();
        }
        public static boolean isPhoneNumber(String number) {
            if (TextUtils.isEmpty(number)) {
                return false;
            }
            Matcher match = Patterns.PHONE.matcher(number);
            return match.matches();
        }
        public static final class Inbox implements BaseMmsColumns {
            public static final Uri
                    CONTENT_URI = Uri.parse("content:
            public static final String DEFAULT_SORT_ORDER = "date DESC";
        }
        public static final class Sent implements BaseMmsColumns {
            public static final Uri
                    CONTENT_URI = Uri.parse("content:
            public static final String DEFAULT_SORT_ORDER = "date DESC";
        }
        public static final class Draft implements BaseMmsColumns {
            public static final Uri
                    CONTENT_URI = Uri.parse("content:
            public static final String DEFAULT_SORT_ORDER = "date DESC";
        }
        public static final class Outbox implements BaseMmsColumns {
            public static final Uri
                    CONTENT_URI = Uri.parse("content:
            public static final String DEFAULT_SORT_ORDER = "date DESC";
        }
        public static final class Addr implements BaseColumns {
            public static final String MSG_ID = "msg_id";
            public static final String CONTACT_ID = "contact_id";
            public static final String ADDRESS = "address";
            public static final String TYPE = "type";
            public static final String CHARSET = "charset";
        }
        public static final class Part implements BaseColumns {
            public static final String MSG_ID = "mid";
            public static final String SEQ = "seq";
            public static final String CONTENT_TYPE = "ct";
            public static final String NAME = "name";
            public static final String CHARSET = "chset";
            public static final String FILENAME = "fn";
            public static final String CONTENT_DISPOSITION = "cd";
            public static final String CONTENT_ID = "cid";
            public static final String CONTENT_LOCATION = "cl";
            public static final String CT_START = "ctt_s";
            public static final String CT_TYPE = "ctt_t";
            public static final String _DATA = "_data";
            public static final String TEXT = "text";
        }
        public static final class Rate {
            public static final Uri CONTENT_URI = Uri.withAppendedPath(
                    Mms.CONTENT_URI, "rate");
            public static final String SENT_TIME = "sent_time";
        }
        public static final class ScrapSpace {
            public static final Uri CONTENT_URI = Uri.parse("content:
            public static final String SCRAP_FILE_PATH = "/sdcard/mms/scrapSpace/.temp.jpg";
        }
        public static final class Intents {
            private Intents() {
            }
            public static final String EXTRA_CONTENTS = "contents";
            public static final String EXTRA_TYPES    = "types";
            public static final String EXTRA_CC       = "cc";
            public static final String EXTRA_BCC      = "bcc";
            public static final String EXTRA_SUBJECT  = "subject";
            public static final String
            CONTENT_CHANGED_ACTION = "android.intent.action.CONTENT_CHANGED";
            public static final String DELETED_CONTENTS = "deleted_contents";
        }
    }
    public static final class MmsSms implements BaseColumns {
        public static final String TYPE_DISCRIMINATOR_COLUMN =
                "transport_type";
        public static final Uri CONTENT_URI = Uri.parse("content:
        public static final Uri CONTENT_CONVERSATIONS_URI = Uri.parse(
                "content:
        public static final Uri CONTENT_FILTER_BYPHONE_URI = Uri.parse(
                "content:
        public static final Uri CONTENT_UNDELIVERED_URI = Uri.parse(
                "content:
        public static final Uri CONTENT_DRAFT_URI = Uri.parse(
                "content:
        public static final Uri CONTENT_LOCKED_URI = Uri.parse(
                "content:
        public static final Uri SEARCH_URI = Uri.parse(
                "content:
        public static final int SMS_PROTO = 0;
        public static final int MMS_PROTO = 1;
        public static final int NO_ERROR                      = 0;
        public static final int ERR_TYPE_GENERIC              = 1;
        public static final int ERR_TYPE_SMS_PROTO_TRANSIENT  = 2;
        public static final int ERR_TYPE_MMS_PROTO_TRANSIENT  = 3;
        public static final int ERR_TYPE_TRANSPORT_FAILURE    = 4;
        public static final int ERR_TYPE_GENERIC_PERMANENT    = 10;
        public static final int ERR_TYPE_SMS_PROTO_PERMANENT  = 11;
        public static final int ERR_TYPE_MMS_PROTO_PERMANENT  = 12;
        public static final class PendingMessages implements BaseColumns {
            public static final Uri CONTENT_URI = Uri.withAppendedPath(
                    MmsSms.CONTENT_URI, "pending");
            public static final String PROTO_TYPE = "proto_type";
            public static final String MSG_ID = "msg_id";
            public static final String MSG_TYPE = "msg_type";
            public static final String ERROR_TYPE = "err_type";
            public static final String ERROR_CODE = "err_code";
            public static final String RETRY_INDEX = "retry_index";
            public static final String DUE_TIME = "due_time";
            public static final String LAST_TRY = "last_try";
        }
        public static final class WordsTable {
            public static final String ID = "_id";
            public static final String SOURCE_ROW_ID = "source_id";
            public static final String TABLE_ID = "table_to_use";
            public static final String INDEXED_TEXT = "index_text";
        }
    }
    public static final class Carriers implements BaseColumns {
        public static final Uri CONTENT_URI =
            Uri.parse("content:
        public static final String DEFAULT_SORT_ORDER = "name ASC";
        public static final String NAME = "name";
        public static final String APN = "apn";
        public static final String PROXY = "proxy";
        public static final String PORT = "port";
        public static final String MMSPROXY = "mmsproxy";
        public static final String MMSPORT = "mmsport";
        public static final String SERVER = "server";
        public static final String USER = "user";
        public static final String PASSWORD = "password";
        public static final String MMSC = "mmsc";
        public static final String MCC = "mcc";
        public static final String MNC = "mnc";
        public static final String NUMERIC = "numeric";
        public static final String AUTH_TYPE = "authtype";
        public static final String TYPE = "type";
        public static final String CURRENT = "current";
    }
    public static final class Intents {
        private Intents() {
        }
        public static final String SECRET_CODE_ACTION =
                "android.provider.Telephony.SECRET_CODE";
        public static final String SPN_STRINGS_UPDATED_ACTION =
                "android.provider.Telephony.SPN_STRINGS_UPDATED";
        public static final String EXTRA_SHOW_PLMN  = "showPlmn";
        public static final String EXTRA_PLMN       = "plmn";
        public static final String EXTRA_SHOW_SPN   = "showSpn";
        public static final String EXTRA_SPN        = "spn";
    }
}
