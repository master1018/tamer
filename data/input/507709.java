public class Utility {
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public final static String readInputStream(InputStream in, String encoding) throws IOException {
        InputStreamReader reader = new InputStreamReader(in, encoding);
        StringBuffer sb = new StringBuffer();
        int count;
        char[] buf = new char[512];
        while ((count = reader.read(buf)) != -1) {
            sb.append(buf, 0, count);
        }
        return sb.toString();
    }
    public final static boolean arrayContains(Object[] a, Object o) {
        for (int i = 0, count = a.length; i < count; i++) {
            if (a[i].equals(o)) {
                return true;
            }
        }
        return false;
    }
    public static String combine(Object[] parts, char seperator) {
        if (parts == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < parts.length; i++) {
            sb.append(parts[i].toString());
            if (i < parts.length - 1) {
                sb.append(seperator);
            }
        }
        return sb.toString();
    }
    public static String base64Decode(String encoded) {
        if (encoded == null) {
            return null;
        }
        byte[] decoded = Base64.decode(encoded, Base64.DEFAULT);
        return new String(decoded);
    }
    public static String base64Encode(String s) {
        if (s == null) {
            return s;
        }
        return Base64.encodeToString(s.getBytes(), Base64.NO_WRAP);
    }
    public static boolean requiredFieldValid(TextView view) {
        return view.getText() != null && view.getText().length() > 0;
    }
    public static boolean requiredFieldValid(Editable s) {
        return s != null && s.length() > 0;
    }
    public static String quoteString(String s) {
        if (s == null) {
            return null;
        }
        if (!s.matches("^\".*\"$")) {
            return "\"" + s + "\"";
        }
        else {
            return s;
        }
    }
    public static String imapQuoted(String s) {
        String result = s.replaceAll("\\\\", "\\\\\\\\");
        result = result.replaceAll("\"", "\\\\\"");
        return "\"" + result + "\"";
    }
    public static String fastUrlDecode(String s) {
        try {
            byte[] bytes = s.getBytes("UTF-8");
            byte ch;
            int length = 0;
            for (int i = 0, count = bytes.length; i < count; i++) {
                ch = bytes[i];
                if (ch == '%') {
                    int h = (bytes[i + 1] - '0');
                    int l = (bytes[i + 2] - '0');
                    if (h > 9) {
                        h -= 7;
                    }
                    if (l > 9) {
                        l -= 7;
                    }
                    bytes[length] = (byte) ((h << 4) | l);
                    i += 2;
                }
                else if (ch == '+') {
                    bytes[length] = ' ';
                }
                else {
                    bytes[length] = bytes[i];
                }
                length++;
            }
            return new String(bytes, 0, length, "UTF-8");
        }
        catch (UnsupportedEncodingException uee) {
            return null;
        }
    }
    public static boolean isDateToday(Date date) {
        Date today = new Date();
        if (date.getYear() == today.getYear() &&
                date.getMonth() == today.getMonth() &&
                date.getDate() == today.getDate()) {
            return true;
        }
        return false;
    }
    public static void setCompoundDrawablesAlpha(TextView view, int alpha) {
    }
    public static String buildMailboxIdSelection(ContentResolver resolver, long mailboxId) {
        StringBuilder selection = new StringBuilder(
                MessageColumns.FLAG_LOADED + " IN ("
                + Message.FLAG_LOADED_PARTIAL + "," + Message.FLAG_LOADED_COMPLETE
                + ") AND ");
        if (mailboxId == Mailbox.QUERY_ALL_INBOXES
            || mailboxId == Mailbox.QUERY_ALL_DRAFTS
            || mailboxId == Mailbox.QUERY_ALL_OUTBOX) {
            int type;
            if (mailboxId == Mailbox.QUERY_ALL_INBOXES) {
                type = Mailbox.TYPE_INBOX;
            } else if (mailboxId == Mailbox.QUERY_ALL_DRAFTS) {
                type = Mailbox.TYPE_DRAFTS;
            } else {
                type = Mailbox.TYPE_OUTBOX;
            }
            StringBuilder inboxes = new StringBuilder();
            Cursor c = resolver.query(Mailbox.CONTENT_URI,
                        EmailContent.ID_PROJECTION,
                        MailboxColumns.TYPE + "=? AND " + MailboxColumns.FLAG_VISIBLE + "=1",
                        new String[] { Integer.toString(type) }, null);
            while (c.moveToNext()) {
                if (inboxes.length() != 0) {
                    inboxes.append(",");
                }
                inboxes.append(c.getLong(EmailContent.ID_PROJECTION_COLUMN));
            }
            c.close();
            selection.append(MessageColumns.MAILBOX_KEY + " IN ");
            selection.append("(").append(inboxes).append(")");
        } else  if (mailboxId == Mailbox.QUERY_ALL_UNREAD) {
            selection.append(Message.FLAG_READ + "=0");
        } else if (mailboxId == Mailbox.QUERY_ALL_FAVORITES) {
            selection.append(Message.FLAG_FAVORITE + "=1");
        } else {
            selection.append(MessageColumns.MAILBOX_KEY + "=" + mailboxId);
        }
        return selection.toString();
    }
    public static class FolderProperties {
        private static FolderProperties sInstance;
        private String[] mSpecialMailbox = new String[] {};
        private TypedArray mSpecialMailboxDrawable;
        private Drawable mDefaultMailboxDrawable;
        private Drawable mSummaryStarredMailboxDrawable;
        private Drawable mSummaryCombinedInboxDrawable;
        private FolderProperties(Context context) {
            mSpecialMailbox = context.getResources().getStringArray(R.array.mailbox_display_names);
            for (int i = 0; i < mSpecialMailbox.length; ++i) {
                if ("".equals(mSpecialMailbox[i])) {
                    mSpecialMailbox[i] = null;
                }
            }
            mSpecialMailboxDrawable =
                context.getResources().obtainTypedArray(R.array.mailbox_display_icons);
            mDefaultMailboxDrawable =
                context.getResources().getDrawable(R.drawable.ic_list_folder);
            mSummaryStarredMailboxDrawable =
                context.getResources().getDrawable(R.drawable.ic_list_starred);
            mSummaryCombinedInboxDrawable =
                context.getResources().getDrawable(R.drawable.ic_list_combined_inbox);
        }
        public static FolderProperties getInstance(Context context) {
            if (sInstance == null) {
                synchronized (FolderProperties.class) {
                    if (sInstance == null) {
                        sInstance = new FolderProperties(context);
                    }
                }
            }
            return sInstance;
        }
        public String getDisplayName(int type) {
            if (type < mSpecialMailbox.length) {
                return mSpecialMailbox[type];
            }
            return null;
        }
        public Drawable getIconIds(int type) {
            if (type < mSpecialMailboxDrawable.length()) {
                return mSpecialMailboxDrawable.getDrawable(type);
            }
            return mDefaultMailboxDrawable;
        }
        public Drawable getSummaryMailboxIconIds(long mailboxKey) {
            if (mailboxKey == Mailbox.QUERY_ALL_INBOXES) {
                return mSummaryCombinedInboxDrawable;
            } else if (mailboxKey == Mailbox.QUERY_ALL_FAVORITES) {
                return mSummaryStarredMailboxDrawable;
            } else if (mailboxKey == Mailbox.QUERY_ALL_DRAFTS) {
                return mSpecialMailboxDrawable.getDrawable(Mailbox.TYPE_DRAFTS);
            } else if (mailboxKey == Mailbox.QUERY_ALL_OUTBOX) {
                return mSpecialMailboxDrawable.getDrawable(Mailbox.TYPE_OUTBOX);
            }
            return mDefaultMailboxDrawable;
        }
    }
    private final static String HOSTAUTH_WHERE_CREDENTIALS = HostAuthColumns.ADDRESS + " like ?"
            + " and " + HostAuthColumns.LOGIN + " like ?"
            + " and " + HostAuthColumns.PROTOCOL + " not like \"smtp\"";
    private final static String ACCOUNT_WHERE_HOSTAUTH = AccountColumns.HOST_AUTH_KEY_RECV + "=?";
    public static String findDuplicateAccount(Context context, long allowAccountId, String hostName,
            String userLogin) {
        ContentResolver resolver = context.getContentResolver();
        Cursor c = resolver.query(HostAuth.CONTENT_URI, HostAuth.ID_PROJECTION,
                HOSTAUTH_WHERE_CREDENTIALS, new String[] { hostName, userLogin }, null);
        try {
            while (c.moveToNext()) {
                long hostAuthId = c.getLong(HostAuth.ID_PROJECTION_COLUMN);
                Cursor c2 = resolver.query(Account.CONTENT_URI, Account.ID_PROJECTION,
                        ACCOUNT_WHERE_HOSTAUTH, new String[] { Long.toString(hostAuthId) }, null);
                try {
                    while (c2.moveToNext()) {
                        long accountId = c2.getLong(Account.ID_PROJECTION_COLUMN);
                        if (accountId != allowAccountId) {
                            Account account = Account.restoreAccountWithId(context, accountId);
                            if (account != null) {
                                return account.mDisplayName;
                            }
                        }
                    }
                } finally {
                    c2.close();
                }
            }
        } finally {
            c.close();
        }
        return null;
    }
    public static String generateMessageId() {
        StringBuffer sb = new StringBuffer();
        sb.append("<");
        for (int i = 0; i < 24; i++) {
            sb.append(Integer.toString((int)(Math.random() * 35), 36));
        }
        sb.append(".");
        sb.append(Long.toString(System.currentTimeMillis()));
        sb.append("@email.android.com>");
        return sb.toString();
    }
    public static long parseDateTimeToMillis(String date) {
        GregorianCalendar cal = parseDateTimeToCalendar(date);
        return cal.getTimeInMillis();
    }
    public static GregorianCalendar parseDateTimeToCalendar(String date) {
        GregorianCalendar cal = new GregorianCalendar(Integer.parseInt(date.substring(0, 4)),
                Integer.parseInt(date.substring(4, 6)) - 1, Integer.parseInt(date.substring(6, 8)),
                Integer.parseInt(date.substring(9, 11)), Integer.parseInt(date.substring(11, 13)),
                Integer.parseInt(date.substring(13, 15)));
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        return cal;
    }
    public static long parseEmailDateTimeToMillis(String date) {
        GregorianCalendar cal = new GregorianCalendar(Integer.parseInt(date.substring(0, 4)),
                Integer.parseInt(date.substring(5, 7)) - 1, Integer.parseInt(date.substring(8, 10)),
                Integer.parseInt(date.substring(11, 13)), Integer.parseInt(date.substring(14, 16)),
                Integer.parseInt(date.substring(17, 19)));
        cal.setTimeZone(TimeZone.getTimeZone("GMT"));
        return cal.getTimeInMillis();
    }
    public static byte[] toUtf8(String s) {
        if (s == null) {
            return null;
        }
        final ByteBuffer buffer = UTF_8.encode(CharBuffer.wrap(s));
        final byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        return bytes;
    }
    public static String fromUtf8(byte[] b) {
        if (b == null) {
            return null;
        }
        final CharBuffer cb = Utility.UTF_8.decode(ByteBuffer.wrap(b));
        return new String(cb.array(), 0, cb.length());
    }
    public static boolean isFirstUtf8Byte(byte b) {
        return (b & 0xc0) != 0x80;
    }
    public static String byteToHex(int b) {
        return byteToHex(new StringBuilder(), b).toString();
    }
    public static StringBuilder byteToHex(StringBuilder sb, int b) {
        b &= 0xFF;
        sb.append("0123456789ABCDEF".charAt(b >> 4));
        sb.append("0123456789ABCDEF".charAt(b & 0xF));
        return sb;
    }
    public static String replaceBareLfWithCrlf(String str) {
        return str.replace("\r", "").replace("\n", "\r\n");
    }
    public static void cancelTaskInterrupt(AsyncTask<?, ?, ?> task) {
        cancelTask(task, true);
    }
    public static void cancelTask(AsyncTask<?, ?, ?> task, boolean mayInterruptIfRunning) {
        if (task != null && task.getStatus() != AsyncTask.Status.FINISHED) {
            task.cancel(mayInterruptIfRunning);
        }
    }
    public static String getConsistentDeviceId(Context context) {
        final String deviceId;
        try {
            TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm == null) {
                return null;
            }
            deviceId = tm.getDeviceId();
            if (deviceId == null) {
                return null;
            }
        } catch (Exception e) {
            Log.d(Email.LOG_TAG, "Error in TelephonyManager.getDeviceId(): " + e.getMessage());
            return null;
        }
        final MessageDigest sha;
        try {
            sha = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException impossible) {
            return null;
        }
        sha.update(Utility.toUtf8(deviceId));
        final int hash = getSmallHashFromSha1(sha.digest());
        return Integer.toString(hash);
    }
     static int getSmallHashFromSha1(byte[] sha1) {
        final int offset = sha1[19] & 0xf; 
        return ((sha1[offset]  & 0x7f) << 24)
                | ((sha1[offset + 1] & 0xff) << 16)
                | ((sha1[offset + 2] & 0xff) << 8)
                | ((sha1[offset + 3] & 0xff));
    }
}
