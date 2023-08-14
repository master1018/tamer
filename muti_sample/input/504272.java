public class EmailSyncAdapter extends AbstractSyncAdapter {
    private static final int UPDATES_READ_COLUMN = 0;
    private static final int UPDATES_MAILBOX_KEY_COLUMN = 1;
    private static final int UPDATES_SERVER_ID_COLUMN = 2;
    private static final int UPDATES_FLAG_COLUMN = 3;
    private static final String[] UPDATES_PROJECTION =
        {MessageColumns.FLAG_READ, MessageColumns.MAILBOX_KEY, SyncColumns.SERVER_ID,
            MessageColumns.FLAG_FAVORITE};
    private static final int MESSAGE_ID_SUBJECT_ID_COLUMN = 0;
    private static final int MESSAGE_ID_SUBJECT_SUBJECT_COLUMN = 1;
    private static final String[] MESSAGE_ID_SUBJECT_PROJECTION =
        new String[] { Message.RECORD_ID, MessageColumns.SUBJECT };
    private static final String WHERE_BODY_SOURCE_MESSAGE_KEY = Body.SOURCE_MESSAGE_KEY + "=?";
    String[] mBindArguments = new String[2];
    String[] mBindArgument = new String[1];
    ArrayList<Long> mDeletedIdList = new ArrayList<Long>();
    ArrayList<Long> mUpdatedIdList = new ArrayList<Long>();
    boolean mIsLooping = false;
    public EmailSyncAdapter(Mailbox mailbox, EasSyncService service) {
        super(mailbox, service);
    }
    @Override
    public boolean parse(InputStream is) throws IOException {
        EasEmailSyncParser p = new EasEmailSyncParser(is, this);
        boolean res = p.parse();
        mIsLooping = p.isLooping();
        return res;
    }
    @Override
    public boolean isLooping() {
        return mIsLooping;
    }
    @Override
    public boolean isSyncable() {
        return true;
    }
    public class EasEmailSyncParser extends AbstractSyncParser {
        private static final String WHERE_SERVER_ID_AND_MAILBOX_KEY =
            SyncColumns.SERVER_ID + "=? and " + MessageColumns.MAILBOX_KEY + "=?";
        private String mMailboxIdAsString;
        ArrayList<Message> newEmails = new ArrayList<Message>();
        ArrayList<Long> deletedEmails = new ArrayList<Long>();
        ArrayList<ServerChange> changedEmails = new ArrayList<ServerChange>();
        public EasEmailSyncParser(InputStream in, EmailSyncAdapter adapter) throws IOException {
            super(in, adapter);
            mMailboxIdAsString = Long.toString(mMailbox.mId);
        }
        @Override
        public void wipe() {
            mContentResolver.delete(Message.CONTENT_URI,
                    Message.MAILBOX_KEY + "=" + mMailbox.mId, null);
            mContentResolver.delete(Message.DELETED_CONTENT_URI,
                    Message.MAILBOX_KEY + "=" + mMailbox.mId, null);
            mContentResolver.delete(Message.UPDATED_CONTENT_URI,
                    Message.MAILBOX_KEY + "=" + mMailbox.mId, null);
        }
        public void addData (Message msg) throws IOException {
            ArrayList<Attachment> atts = new ArrayList<Attachment>();
            while (nextTag(Tags.SYNC_APPLICATION_DATA) != END) {
                switch (tag) {
                    case Tags.EMAIL_ATTACHMENTS:
                    case Tags.BASE_ATTACHMENTS: 
                        attachmentsParser(atts, msg);
                        break;
                    case Tags.EMAIL_TO:
                        msg.mTo = Address.pack(Address.parse(getValue()));
                        break;
                    case Tags.EMAIL_FROM:
                        Address[] froms = Address.parse(getValue());
                        if (froms != null && froms.length > 0) {
                          msg.mDisplayName = froms[0].toFriendly();
                        }
                        msg.mFrom = Address.pack(froms);
                        break;
                    case Tags.EMAIL_CC:
                        msg.mCc = Address.pack(Address.parse(getValue()));
                        break;
                    case Tags.EMAIL_REPLY_TO:
                        msg.mReplyTo = Address.pack(Address.parse(getValue()));
                        break;
                    case Tags.EMAIL_DATE_RECEIVED:
                        msg.mTimeStamp = Utility.parseEmailDateTimeToMillis(getValue());
                        break;
                    case Tags.EMAIL_SUBJECT:
                        msg.mSubject = getValue();
                        break;
                    case Tags.EMAIL_READ:
                        msg.mFlagRead = getValueInt() == 1;
                        break;
                    case Tags.BASE_BODY:
                        bodyParser(msg);
                        break;
                    case Tags.EMAIL_FLAG:
                        msg.mFlagFavorite = flagParser();
                        break;
                    case Tags.EMAIL_BODY:
                        String text = getValue();
                        msg.mText = text;
                        break;
                    case Tags.EMAIL_MESSAGE_CLASS:
                        String messageClass = getValue();
                        if (messageClass.equals("IPM.Schedule.Meeting.Request")) {
                            msg.mFlags |= Message.FLAG_INCOMING_MEETING_INVITE;
                        } else if (messageClass.equals("IPM.Schedule.Meeting.Canceled")) {
                            msg.mFlags |= Message.FLAG_INCOMING_MEETING_CANCEL;
                        }
                        break;
                    case Tags.EMAIL_MEETING_REQUEST:
                        meetingRequestParser(msg);
                        break;
                    default:
                        skipTag();
                }
            }
            if (atts.size() > 0) {
                msg.mAttachments = atts;
            }
        }
        private void meetingRequestParser(Message msg) throws IOException {
            PackedString.Builder packedString = new PackedString.Builder();
            while (nextTag(Tags.EMAIL_MEETING_REQUEST) != END) {
                switch (tag) {
                    case Tags.EMAIL_DTSTAMP:
                        packedString.put(MeetingInfo.MEETING_DTSTAMP, getValue());
                        break;
                    case Tags.EMAIL_START_TIME:
                        packedString.put(MeetingInfo.MEETING_DTSTART, getValue());
                        break;
                    case Tags.EMAIL_END_TIME:
                        packedString.put(MeetingInfo.MEETING_DTEND, getValue());
                        break;
                    case Tags.EMAIL_ORGANIZER:
                        packedString.put(MeetingInfo.MEETING_ORGANIZER_EMAIL, getValue());
                        break;
                    case Tags.EMAIL_LOCATION:
                        packedString.put(MeetingInfo.MEETING_LOCATION, getValue());
                        break;
                    case Tags.EMAIL_GLOBAL_OBJID:
                        packedString.put(MeetingInfo.MEETING_UID,
                                CalendarUtilities.getUidFromGlobalObjId(getValue()));
                        break;
                    case Tags.EMAIL_CATEGORIES:
                        nullParser();
                        break;
                    case Tags.EMAIL_RECURRENCES:
                        recurrencesParser();
                        break;
                    default:
                        skipTag();
                }
            }
            if (msg.mSubject != null) {
                packedString.put(MeetingInfo.MEETING_TITLE, msg.mSubject);
            }
            msg.mMeetingInfo = packedString.toString();
        }
        private void nullParser() throws IOException {
            while (nextTag(Tags.EMAIL_CATEGORIES) != END) {
                skipTag();
            }
        }
        private void recurrencesParser() throws IOException {
            while (nextTag(Tags.EMAIL_RECURRENCES) != END) {
                switch (tag) {
                    case Tags.EMAIL_RECURRENCE:
                        nullParser();
                        break;
                    default:
                        skipTag();
                }
            }
        }
        private void addParser(ArrayList<Message> emails) throws IOException {
            Message msg = new Message();
            msg.mAccountKey = mAccount.mId;
            msg.mMailboxKey = mMailbox.mId;
            msg.mFlagLoaded = Message.FLAG_LOADED_COMPLETE;
            while (nextTag(Tags.SYNC_ADD) != END) {
                switch (tag) {
                    case Tags.SYNC_SERVER_ID:
                        msg.mServerId = getValue();
                        break;
                    case Tags.SYNC_APPLICATION_DATA:
                        addData(msg);
                        break;
                    default:
                        skipTag();
                }
            }
            emails.add(msg);
        }
        private Boolean flagParser() throws IOException {
            Boolean state = false;
            while (nextTag(Tags.EMAIL_FLAG) != END) {
                switch (tag) {
                    case Tags.EMAIL_FLAG_STATUS:
                        state = getValueInt() == 2;
                        break;
                    default:
                        skipTag();
                }
            }
            return state;
        }
        private void bodyParser(Message msg) throws IOException {
            String bodyType = Eas.BODY_PREFERENCE_TEXT;
            String body = "";
            while (nextTag(Tags.EMAIL_BODY) != END) {
                switch (tag) {
                    case Tags.BASE_TYPE:
                        bodyType = getValue();
                        break;
                    case Tags.BASE_DATA:
                        body = getValue();
                        break;
                    default:
                        skipTag();
                }
            }
            if (bodyType.equals(Eas.BODY_PREFERENCE_HTML)) {
                msg.mHtml = body;
            } else {
                msg.mText = body;
            }
        }
        private void attachmentsParser(ArrayList<Attachment> atts, Message msg) throws IOException {
            while (nextTag(Tags.EMAIL_ATTACHMENTS) != END) {
                switch (tag) {
                    case Tags.EMAIL_ATTACHMENT:
                    case Tags.BASE_ATTACHMENT:  
                        attachmentParser(atts, msg);
                        break;
                    default:
                        skipTag();
                }
            }
        }
        private void attachmentParser(ArrayList<Attachment> atts, Message msg) throws IOException {
            String fileName = null;
            String length = null;
            String location = null;
            while (nextTag(Tags.EMAIL_ATTACHMENT) != END) {
                switch (tag) {
                    case Tags.EMAIL_DISPLAY_NAME:
                    case Tags.BASE_DISPLAY_NAME:
                        fileName = getValue();
                        break;
                    case Tags.EMAIL_ATT_NAME:
                    case Tags.BASE_FILE_REFERENCE:
                        location = getValue();
                        break;
                    case Tags.EMAIL_ATT_SIZE:
                    case Tags.BASE_ESTIMATED_DATA_SIZE:
                        length = getValue();
                        break;
                    default:
                        skipTag();
                }
            }
            if ((fileName != null) && (length != null) && (location != null)) {
                Attachment att = new Attachment();
                att.mEncoding = "base64";
                att.mSize = Long.parseLong(length);
                att.mFileName = fileName;
                att.mLocation = location;
                att.mMimeType = getMimeTypeFromFileName(fileName);
                atts.add(att);
                msg.mFlagAttachment = true;
            }
        }
        public String getMimeTypeFromFileName(String fileName) {
            String mimeType;
            int lastDot = fileName.lastIndexOf('.');
            String extension = null;
            if ((lastDot > 0) && (lastDot < fileName.length() - 1)) {
                extension = fileName.substring(lastDot + 1).toLowerCase();
            }
            if (extension == null) {
                mimeType = "application/octet-stream";
            } else {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (mimeType == null) {
                    mimeType = "application/" + extension;
                }
            }
            return mimeType;
        }
        private Cursor getServerIdCursor(String serverId, String[] projection) {
            mBindArguments[0] = serverId;
            mBindArguments[1] = mMailboxIdAsString;
            return mContentResolver.query(Message.CONTENT_URI, projection,
                    WHERE_SERVER_ID_AND_MAILBOX_KEY, mBindArguments, null);
        }
         void deleteParser(ArrayList<Long> deletes, int entryTag) throws IOException {
            while (nextTag(entryTag) != END) {
                switch (tag) {
                    case Tags.SYNC_SERVER_ID:
                        String serverId = getValue();
                        Cursor c = getServerIdCursor(serverId, MESSAGE_ID_SUBJECT_PROJECTION);
                        try {
                            if (c.moveToFirst()) {
                                deletes.add(c.getLong(MESSAGE_ID_SUBJECT_ID_COLUMN));
                                if (Eas.USER_LOG) {
                                    userLog("Deleting ", serverId + ", "
                                            + c.getString(MESSAGE_ID_SUBJECT_SUBJECT_COLUMN));
                                }
                            }
                        } finally {
                            c.close();
                        }
                        break;
                    default:
                        skipTag();
                }
            }
        }
        class ServerChange {
            long id;
            Boolean read;
            Boolean flag;
            ServerChange(long _id, Boolean _read, Boolean _flag) {
                id = _id;
                read = _read;
                flag = _flag;
            }
        }
         void changeParser(ArrayList<ServerChange> changes) throws IOException {
            String serverId = null;
            Boolean oldRead = false;
            Boolean oldFlag = false;
            long id = 0;
            while (nextTag(Tags.SYNC_CHANGE) != END) {
                switch (tag) {
                    case Tags.SYNC_SERVER_ID:
                        serverId = getValue();
                        Cursor c = getServerIdCursor(serverId, Message.LIST_PROJECTION);
                        try {
                            if (c.moveToFirst()) {
                                userLog("Changing ", serverId);
                                oldRead = c.getInt(Message.LIST_READ_COLUMN) == Message.READ;
                                oldFlag = c.getInt(Message.LIST_FAVORITE_COLUMN) == 1;
                                id = c.getLong(Message.LIST_ID_COLUMN);
                            }
                        } finally {
                            c.close();
                        }
                        break;
                    case Tags.SYNC_APPLICATION_DATA:
                        changeApplicationDataParser(changes, oldRead, oldFlag, id);
                        break;
                    default:
                        skipTag();
                }
            }
        }
        private void changeApplicationDataParser(ArrayList<ServerChange> changes, Boolean oldRead,
                Boolean oldFlag, long id) throws IOException {
            Boolean read = null;
            Boolean flag = null;
            while (nextTag(Tags.SYNC_APPLICATION_DATA) != END) {
                switch (tag) {
                    case Tags.EMAIL_READ:
                        read = getValueInt() == 1;
                        break;
                    case Tags.EMAIL_FLAG:
                        flag = flagParser();
                        break;
                    default:
                        skipTag();
                }
            }
            if (((read != null) && !oldRead.equals(read)) ||
                    ((flag != null) && !oldFlag.equals(flag))) {
                changes.add(new ServerChange(id, read, flag));
            }
        }
        @Override
        public void commandsParser() throws IOException {
            while (nextTag(Tags.SYNC_COMMANDS) != END) {
                if (tag == Tags.SYNC_ADD) {
                    addParser(newEmails);
                    incrementChangeCount();
                } else if (tag == Tags.SYNC_DELETE || tag == Tags.SYNC_SOFT_DELETE) {
                    deleteParser(deletedEmails, tag);
                    incrementChangeCount();
                } else if (tag == Tags.SYNC_CHANGE) {
                    changeParser(changedEmails);
                    incrementChangeCount();
                } else
                    skipTag();
            }
        }
        @Override
        public void responsesParser() {
        }
        @Override
        public void commit() {
            int notifyCount = 0;
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            for (Message msg: newEmails) {
                if (!msg.mFlagRead) {
                    notifyCount++;
                }
                msg.addSaveOps(ops);
            }
            for (Long id : deletedEmails) {
                ops.add(ContentProviderOperation.newDelete(
                        ContentUris.withAppendedId(Message.CONTENT_URI, id)).build());
                AttachmentProvider.deleteAllAttachmentFiles(mContext, mAccount.mId, id);
            }
            if (!changedEmails.isEmpty()) {
                for (ServerChange change : changedEmails) {
                     ContentValues cv = new ContentValues();
                    if (change.read != null) {
                        cv.put(MessageColumns.FLAG_READ, change.read);
                    }
                    if (change.flag != null) {
                        cv.put(MessageColumns.FLAG_FAVORITE, change.flag);
                    }
                    ops.add(ContentProviderOperation.newUpdate(
                            ContentUris.withAppendedId(Message.CONTENT_URI, change.id))
                                .withValues(cv)
                                .build());
                }
            }
            ContentValues mailboxValues = new ContentValues();
            mailboxValues.put(Mailbox.SYNC_KEY, mMailbox.mSyncKey);
            ops.add(ContentProviderOperation.newUpdate(
                    ContentUris.withAppendedId(Mailbox.CONTENT_URI, mMailbox.mId))
                        .withValues(mailboxValues).build());
            addCleanupOps(ops);
            synchronized (mService.getSynchronizer()) {
                if (mService.isStopped()) return;
                try {
                    mContentResolver.applyBatch(EmailProvider.EMAIL_AUTHORITY, ops);
                    userLog(mMailbox.mDisplayName, " SyncKey saved as: ", mMailbox.mSyncKey);
                } catch (RemoteException e) {
                } catch (OperationApplicationException e) {
                }
            }
            if (notifyCount > 0) {
                ContentValues cv = new ContentValues();
                cv.put(EmailContent.FIELD_COLUMN_NAME, AccountColumns.NEW_MESSAGE_COUNT);
                cv.put(EmailContent.ADD_COLUMN_NAME, notifyCount);
                Uri uri = ContentUris.withAppendedId(Account.ADD_TO_FIELD_URI, mAccount.mId);
                mContentResolver.update(uri, cv, null, null);
                MailService.actionNotifyNewMessages(mContext, mAccount.mId);
            }
        }
    }
    @Override
    public String getCollectionName() {
        return "Email";
    }
    private void addCleanupOps(ArrayList<ContentProviderOperation> ops) {
        for (Long id: mDeletedIdList) {
            ops.add(ContentProviderOperation.newDelete(
                    ContentUris.withAppendedId(Message.DELETED_CONTENT_URI, id)).build());
        }
        for (Long id: mUpdatedIdList) {
            ops.add(ContentProviderOperation.newDelete(
                    ContentUris.withAppendedId(Message.UPDATED_CONTENT_URI, id)).build());
        }
    }
    @Override
    public void cleanup() {
        if (!mDeletedIdList.isEmpty() || !mUpdatedIdList.isEmpty()) {
            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
            addCleanupOps(ops);
            try {
                mContext.getContentResolver()
                    .applyBatch(EmailProvider.EMAIL_AUTHORITY, ops);
            } catch (RemoteException e) {
            } catch (OperationApplicationException e) {
            }
        }
    }
    private String formatTwo(int num) {
        if (num < 10) {
            return "0" + (char)('0' + num);
        } else
            return Integer.toString(num);
    }
    public String formatDateTime(Calendar calendar) {
        StringBuilder sb = new StringBuilder();
        sb.append(calendar.get(Calendar.YEAR));
        sb.append('-');
        sb.append(formatTwo(calendar.get(Calendar.MONTH) + 1));
        sb.append('-');
        sb.append(formatTwo(calendar.get(Calendar.DAY_OF_MONTH)));
        sb.append('T');
        sb.append(formatTwo(calendar.get(Calendar.HOUR_OF_DAY)));
        sb.append(':');
        sb.append(formatTwo(calendar.get(Calendar.MINUTE)));
        sb.append(':');
        sb.append(formatTwo(calendar.get(Calendar.SECOND)));
        sb.append(".000Z");
        return sb.toString();
    }
    private boolean messageReferenced(ContentResolver cr, long id) {
        mBindArgument[0] = Long.toString(id);
        Cursor c = cr.query(Body.CONTENT_URI, Body.ID_PROJECTION, WHERE_BODY_SOURCE_MESSAGE_KEY,
                mBindArgument, null);
        try {
            return c.moveToFirst();
        } finally {
            c.close();
        }
    }
    boolean sendDeletedItems(Serializer s, ArrayList<Long> deletedIds, boolean first)
            throws IOException {
        ContentResolver cr = mContext.getContentResolver();
        Cursor c = cr.query(Message.DELETED_CONTENT_URI, Message.LIST_PROJECTION,
                MessageColumns.MAILBOX_KEY + '=' + mMailbox.mId, null, null);
        deletedIds.clear();
        try {
            while (c.moveToNext()) {
                String serverId = c.getString(Message.LIST_SERVER_ID_COLUMN);
                if (serverId == null) {
                    continue;
                } else if (messageReferenced(cr, c.getLong(Message.CONTENT_ID_COLUMN))) {
                    userLog("Postponing deletion of referenced message: ", serverId);
                    continue;
                } else if (first) {
                    s.start(Tags.SYNC_COMMANDS);
                    first = false;
                }
                s.start(Tags.SYNC_DELETE).data(Tags.SYNC_SERVER_ID, serverId).end();
                deletedIds.add(c.getLong(Message.LIST_ID_COLUMN));
            }
        } finally {
            c.close();
        }
       return first;
    }
    @Override
    public boolean sendLocalChanges(Serializer s) throws IOException {
        ContentResolver cr = mContext.getContentResolver();
        if (mMailbox.mType == Mailbox.TYPE_DRAFTS || mMailbox.mType == Mailbox.TYPE_OUTBOX) {
            return false;
        }
        boolean firstCommand = sendDeletedItems(s, mDeletedIdList, true);
        long trashMailboxId =
            Mailbox.findMailboxOfType(mContext, mMailbox.mAccountKey, Mailbox.TYPE_TRASH);
        Cursor c = cr.query(Message.UPDATED_CONTENT_URI, Message.LIST_PROJECTION,
                MessageColumns.MAILBOX_KEY + '=' + mMailbox.mId, null, null);
        mUpdatedIdList.clear();
        try {
            while (c.moveToNext()) {
                long id = c.getLong(Message.LIST_ID_COLUMN);
                mUpdatedIdList.add(id);
                Cursor currentCursor = cr.query(ContentUris.withAppendedId(Message.CONTENT_URI, id),
                        UPDATES_PROJECTION, null, null, null);
                try {
                    if (!currentCursor.moveToFirst()) {
                         continue;
                    }
                    String serverId = currentCursor.getString(UPDATES_SERVER_ID_COLUMN);
                    if (serverId == null) {
                        continue;
                    }
                    if (currentCursor.getLong(UPDATES_MAILBOX_KEY_COLUMN) == trashMailboxId) {
                         if (firstCommand) {
                            s.start(Tags.SYNC_COMMANDS);
                            firstCommand = false;
                        }
                        s.start(Tags.SYNC_DELETE).data(Tags.SYNC_SERVER_ID, serverId).end();
                        continue;
                    }
                    boolean flagChange = false;
                    boolean readChange = false;
                    int flag = 0;
                    if (mService.mProtocolVersionDouble >= Eas.SUPPORTED_PROTOCOL_EX2007_DOUBLE) {
                        flag = currentCursor.getInt(UPDATES_FLAG_COLUMN);
                        if (flag != c.getInt(Message.LIST_FAVORITE_COLUMN)) {
                            flagChange = true;
                        }
                    }
                    int read = currentCursor.getInt(UPDATES_READ_COLUMN);
                    if (read != c.getInt(Message.LIST_READ_COLUMN)) {
                        readChange = true;
                    }
                    if (!flagChange && !readChange) {
                        continue;
                    }
                    if (firstCommand) {
                        s.start(Tags.SYNC_COMMANDS);
                        firstCommand = false;
                    }
                    s.start(Tags.SYNC_CHANGE)
                        .data(Tags.SYNC_SERVER_ID, c.getString(Message.LIST_SERVER_ID_COLUMN))
                        .start(Tags.SYNC_APPLICATION_DATA);
                    if (readChange) {
                        s.data(Tags.EMAIL_READ, Integer.toString(read));
                    }
                    if (flagChange) {
                        if (flag != 0) {
                            s.start(Tags.EMAIL_FLAG).data(Tags.EMAIL_FLAG_STATUS, "2");
                            s.data(Tags.EMAIL_FLAG_TYPE, "FollowUp");
                            long now = System.currentTimeMillis();
                            Calendar calendar =
                                GregorianCalendar.getInstance(TimeZone.getTimeZone("GMT"));
                            calendar.setTimeInMillis(now);
                            String utc = formatDateTime(calendar);
                            s.data(Tags.TASK_START_DATE, utc).data(Tags.TASK_UTC_START_DATE, utc);
                            calendar.setTimeInMillis(now + 1*WEEKS);
                            utc = formatDateTime(calendar);
                            s.data(Tags.TASK_DUE_DATE, utc).data(Tags.TASK_UTC_DUE_DATE, utc);
                            s.end();
                        } else {
                            s.tag(Tags.EMAIL_FLAG);
                        }
                    }
                    s.end().end(); 
                } finally {
                    currentCursor.close();
                }
            }
        } finally {
            c.close();
        }
        if (!firstCommand) {
            s.end(); 
        }
        return false;
    }
}
