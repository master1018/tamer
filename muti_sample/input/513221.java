public class LegacyConversions {
    private static final boolean DEBUG_ATTACHMENTS = false;
    private static final HashMap<String, Integer>
            sServerMailboxNames = new HashMap<String, Integer>();
     static final String BODY_QUOTED_PART_REPLY = "quoted-reply";
     static final String BODY_QUOTED_PART_FORWARD = "quoted-forward";
     static final String BODY_QUOTED_PART_INTRO = "quoted-intro";
    private static final String[] ATTACHMENT_META_COLUMNS_PROJECTION = {
        OpenableColumns.DISPLAY_NAME,
        OpenableColumns.SIZE
    };
    private static final int ATTACHMENT_META_COLUMNS_SIZE = 1;
    public static boolean updateMessageFields(EmailContent.Message localMessage, Message message,
                long accountId, long mailboxId) throws MessagingException {
        Address[] from = message.getFrom();
        Address[] to = message.getRecipients(Message.RecipientType.TO);
        Address[] cc = message.getRecipients(Message.RecipientType.CC);
        Address[] bcc = message.getRecipients(Message.RecipientType.BCC);
        Address[] replyTo = message.getReplyTo();
        String subject = message.getSubject();
        Date sentDate = message.getSentDate();
        Date internalDate = message.getInternalDate();
        if (from != null && from.length > 0) {
            localMessage.mDisplayName = from[0].toFriendly();
        }
        if (sentDate != null) {
            localMessage.mTimeStamp = sentDate.getTime();
        }
        if (subject != null) {
            localMessage.mSubject = subject;
        }
        localMessage.mFlagRead = message.isSet(Flag.SEEN);
        if (localMessage.mFlagLoaded != EmailContent.Message.FLAG_LOADED_COMPLETE) {
            if (localMessage.mDisplayName == null || "".equals(localMessage.mDisplayName)) {
                localMessage.mFlagLoaded = EmailContent.Message.FLAG_LOADED_UNLOADED;
            } else {
                localMessage.mFlagLoaded = EmailContent.Message.FLAG_LOADED_PARTIAL;
            }
        }
        localMessage.mFlagFavorite = message.isSet(Flag.FLAGGED);
        localMessage.mServerId = message.getUid();
        if (internalDate != null) {
            localMessage.mServerTimeStamp = internalDate.getTime();
        }
        String messageId = ((MimeMessage)message).getMessageId();
        if (messageId != null) {
            localMessage.mMessageId = messageId;
        }
        localMessage.mMailboxKey = mailboxId;
        localMessage.mAccountKey = accountId;
        if (from != null && from.length > 0) {
            localMessage.mFrom = Address.pack(from);
        }
        localMessage.mTo = Address.pack(to);
        localMessage.mCc = Address.pack(cc);
        localMessage.mBcc = Address.pack(bcc);
        localMessage.mReplyTo = Address.pack(replyTo);
        return true;
    }
    public static boolean updateBodyFields(EmailContent.Body body,
            EmailContent.Message localMessage, ArrayList<Part> viewables)
            throws MessagingException {
        body.mMessageKey = localMessage.mId;
        StringBuffer sbHtml = null;
        StringBuffer sbText = null;
        StringBuffer sbHtmlReply = null;
        StringBuffer sbTextReply = null;
        StringBuffer sbIntroText = null;
        for (Part viewable : viewables) {
            String text = MimeUtility.getTextFromPart(viewable);
            String[] replyTags = viewable.getHeader(MimeHeader.HEADER_ANDROID_BODY_QUOTED_PART);
            String replyTag = null;
            if (replyTags != null && replyTags.length > 0) {
                replyTag = replyTags[0];
            }
            boolean isHtml = "text/html".equalsIgnoreCase(viewable.getMimeType());
            if (replyTag != null) {
                boolean isQuotedReply = BODY_QUOTED_PART_REPLY.equalsIgnoreCase(replyTag);
                boolean isQuotedForward = BODY_QUOTED_PART_FORWARD.equalsIgnoreCase(replyTag);
                boolean isQuotedIntro = BODY_QUOTED_PART_INTRO.equalsIgnoreCase(replyTag);
                if (isQuotedReply || isQuotedForward) {
                    if (isHtml) {
                        sbHtmlReply = appendTextPart(sbHtmlReply, text);
                    } else {
                        sbTextReply = appendTextPart(sbTextReply, text);
                    }
                    localMessage.mFlags &= ~EmailContent.Message.FLAG_TYPE_MASK;
                    localMessage.mFlags |= isQuotedReply
                            ? EmailContent.Message.FLAG_TYPE_REPLY
                            : EmailContent.Message.FLAG_TYPE_FORWARD;
                    continue;
                }
                if (isQuotedIntro) {
                    sbIntroText = appendTextPart(sbIntroText, text);
                    continue;
                }
            }
            if (isHtml) {
                sbHtml = appendTextPart(sbHtml, text);
            } else {
                sbText = appendTextPart(sbText, text);
            }
        }
        if (sbText != null && sbText.length() != 0) {
            body.mTextContent = sbText.toString();
        }
        if (sbHtml != null && sbHtml.length() != 0) {
            body.mHtmlContent = sbHtml.toString();
        }
        if (sbHtmlReply != null && sbHtmlReply.length() != 0) {
            body.mHtmlReply = sbHtmlReply.toString();
        }
        if (sbTextReply != null && sbTextReply.length() != 0) {
            body.mTextReply = sbTextReply.toString();
        }
        if (sbIntroText != null && sbIntroText.length() != 0) {
            body.mIntroText = sbIntroText.toString();
        }
        return true;
    }
    private static StringBuffer appendTextPart(StringBuffer sb, String newText) {
        if (newText == null) {
            return sb;
        }
        else if (sb == null) {
            sb = new StringBuffer(newText);
        } else {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(newText);
        }
        return sb;
    }
    public static void updateAttachments(Context context, EmailContent.Message localMessage,
            ArrayList<Part> attachments, boolean upgrading) throws MessagingException, IOException {
        localMessage.mAttachments = null;
        for (Part attachmentPart : attachments) {
            addOneAttachment(context, localMessage, attachmentPart, upgrading);
        }
    }
    private static void addOneAttachment(Context context, EmailContent.Message localMessage,
            Part part, boolean upgrading) throws MessagingException, IOException {
        Attachment localAttachment = new Attachment();
        String contentType = MimeUtility.unfoldAndDecode(part.getContentType());
        String name = MimeUtility.getHeaderParameter(contentType, "name");
        if (name == null) {
            String contentDisposition = MimeUtility.unfoldAndDecode(part.getDisposition());
            name = MimeUtility.getHeaderParameter(contentDisposition, "filename");
        }
        Uri contentUri = null;
        String contentUriString = null;
        if (upgrading) {
            Body body = part.getBody();
            if (body instanceof LocalStore.LocalAttachmentBody) {
                LocalStore.LocalAttachmentBody localBody = (LocalStore.LocalAttachmentBody) body;
                contentUri = localBody.getContentUri();
                if (contentUri != null) {
                    contentUriString = contentUri.toString();
                }
            }
        }
        long size = 0;
        if (upgrading) {
            if (contentUri != null) {
                Cursor metadataCursor = context.getContentResolver().query(contentUri,
                        ATTACHMENT_META_COLUMNS_PROJECTION, null, null, null);
                if (metadataCursor != null) {
                    try {
                        if (metadataCursor.moveToFirst()) {
                            size = metadataCursor.getInt(ATTACHMENT_META_COLUMNS_SIZE);
                        }
                    } finally {
                        metadataCursor.close();
                    }
                }
            }
        } else {
            String disposition = part.getDisposition();
            if (disposition != null) {
                String s = MimeUtility.getHeaderParameter(disposition, "size");
                if (s != null) {
                    size = Long.parseLong(s);
                }
            }
        }
        String[] partIds = part.getHeader(MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA);
        String partId = partIds != null ? partIds[0] : null;
        localAttachment.mFileName = name;
        localAttachment.mMimeType = part.getMimeType();
        localAttachment.mSize = size;           
        localAttachment.mContentId = part.getContentId();
        localAttachment.mContentUri = contentUriString;
        localAttachment.mMessageKey = localMessage.mId;
        localAttachment.mLocation = partId;
        localAttachment.mEncoding = "B";        
        if (DEBUG_ATTACHMENTS) {
            Log.d(Email.LOG_TAG, "Add attachment " + localAttachment);
        }
        Uri uri = ContentUris.withAppendedId(Attachment.MESSAGE_ID_URI, localMessage.mId);
        Cursor cursor = context.getContentResolver().query(uri, Attachment.CONTENT_PROJECTION,
                null, null, null);
        boolean attachmentFoundInDb = false;
        try {
            while (cursor.moveToNext()) {
                Attachment dbAttachment = new Attachment().restore(cursor);
                if (stringNotEqual(dbAttachment.mFileName, localAttachment.mFileName)) continue;
                if (stringNotEqual(dbAttachment.mMimeType, localAttachment.mMimeType)) continue;
                if (stringNotEqual(dbAttachment.mContentId, localAttachment.mContentId)) continue;
                if (stringNotEqual(dbAttachment.mLocation, localAttachment.mLocation)) continue;
                attachmentFoundInDb = true;
                localAttachment.mId = dbAttachment.mId;
                if (DEBUG_ATTACHMENTS) {
                    Log.d(Email.LOG_TAG, "Skipped, found db attachment " + dbAttachment);
                }
                break;
            }
        } finally {
            cursor.close();
        }
        if (!attachmentFoundInDb) {
            localAttachment.save(context);
        }
        if (!upgrading) {
            saveAttachmentBody(context, part, localAttachment, localMessage.mAccountKey);
        }
        if (localMessage.mAttachments == null) {
            localMessage.mAttachments = new ArrayList<Attachment>();
        }
        localMessage.mAttachments.add(localAttachment);
        localMessage.mFlagAttachment = true;
    }
     static boolean stringNotEqual(String a, String b) {
        if (a == null && b == null) return false;       
        if (a == null) a = "";
        if (b == null) b = "";
        return !a.equals(b);
    }
    public static void saveAttachmentBody(Context context, Part part, Attachment localAttachment,
            long accountId) throws MessagingException, IOException {
        if (part.getBody() != null) {
            long attachmentId = localAttachment.mId;
            InputStream in = part.getBody().getInputStream();
            File saveIn = AttachmentProvider.getAttachmentDirectory(context, accountId);
            if (!saveIn.exists()) {
                saveIn.mkdirs();
            }
            File saveAs = AttachmentProvider.getAttachmentFilename(context, accountId,
                    attachmentId);
            saveAs.createNewFile();
            FileOutputStream out = new FileOutputStream(saveAs);
            long copySize = IOUtils.copy(in, out);
            in.close();
            out.close();
            String contentUriString = AttachmentProvider.getAttachmentUri(
                    accountId, attachmentId).toString();
            localAttachment.mSize = copySize;
            localAttachment.mContentUri = contentUriString;
            ContentValues cv = new ContentValues();
            cv.put(AttachmentColumns.SIZE, copySize);
            cv.put(AttachmentColumns.CONTENT_URI, contentUriString);
            Uri uri = ContentUris.withAppendedId(Attachment.CONTENT_URI, attachmentId);
            context.getContentResolver().update(uri, cv, null, null);
        }
    }
    public static Message makeMessage(Context context, EmailContent.Message localMessage)
            throws MessagingException {
        MimeMessage message = new MimeMessage();
        message.setSubject(localMessage.mSubject == null ? "" : localMessage.mSubject);
        Address[] from = Address.unpack(localMessage.mFrom);
        if (from.length > 0) {
            message.setFrom(from[0]);
        }
        message.setSentDate(new Date(localMessage.mTimeStamp));
        message.setUid(localMessage.mServerId);
        message.setFlag(Flag.DELETED,
                localMessage.mFlagLoaded == EmailContent.Message.FLAG_LOADED_DELETED);
        message.setFlag(Flag.SEEN, localMessage.mFlagRead);
        message.setFlag(Flag.FLAGGED, localMessage.mFlagFavorite);
        message.setRecipients(RecipientType.TO, Address.unpack(localMessage.mTo));
        message.setRecipients(RecipientType.CC, Address.unpack(localMessage.mCc));
        message.setRecipients(RecipientType.BCC, Address.unpack(localMessage.mBcc));
        message.setReplyTo(Address.unpack(localMessage.mReplyTo));
        message.setInternalDate(new Date(localMessage.mServerTimeStamp));
        message.setMessageId(localMessage.mMessageId);
        message.setHeader(MimeHeader.HEADER_CONTENT_TYPE, "multipart/mixed");
        MimeMultipart mp = new MimeMultipart();
        mp.setSubType("mixed");
        message.setBody(mp);
        try {
            addTextBodyPart(mp, "text/html", null,
                    EmailContent.Body.restoreBodyHtmlWithMessageId(context, localMessage.mId));
        } catch (RuntimeException rte) {
            Log.d(Email.LOG_TAG, "Exception while reading html body " + rte.toString());
        }
        try {
            addTextBodyPart(mp, "text/plain", null,
                    EmailContent.Body.restoreBodyTextWithMessageId(context, localMessage.mId));
        } catch (RuntimeException rte) {
            Log.d(Email.LOG_TAG, "Exception while reading text body " + rte.toString());
        }
        boolean isReply = (localMessage.mFlags & EmailContent.Message.FLAG_TYPE_REPLY) != 0;
        boolean isForward = (localMessage.mFlags & EmailContent.Message.FLAG_TYPE_FORWARD) != 0;
        if (isReply || isForward) {
            try {
                addTextBodyPart(mp, "text/plain", BODY_QUOTED_PART_INTRO,
                        EmailContent.Body.restoreIntroTextWithMessageId(context, localMessage.mId));
            } catch (RuntimeException rte) {
                Log.d(Email.LOG_TAG, "Exception while reading text reply " + rte.toString());
            }
            String replyTag = isReply ? BODY_QUOTED_PART_REPLY : BODY_QUOTED_PART_FORWARD;
            try {
                addTextBodyPart(mp, "text/html", replyTag,
                        EmailContent.Body.restoreReplyHtmlWithMessageId(context, localMessage.mId));
            } catch (RuntimeException rte) {
                Log.d(Email.LOG_TAG, "Exception while reading html reply " + rte.toString());
            }
            try {
                addTextBodyPart(mp, "text/plain", replyTag,
                        EmailContent.Body.restoreReplyTextWithMessageId(context, localMessage.mId));
            } catch (RuntimeException rte) {
                Log.d(Email.LOG_TAG, "Exception while reading text reply " + rte.toString());
            }
        }
        return message;
    }
    private static void addTextBodyPart(MimeMultipart mp, String contentType, String quotedPartTag,
            String partText) throws MessagingException {
        if (partText == null) {
            return;
        }
        TextBody body = new TextBody(partText);
        MimeBodyPart bp = new MimeBodyPart(body, contentType);
        if (quotedPartTag != null) {
            bp.addHeader(MimeHeader.HEADER_ANDROID_BODY_QUOTED_PART, quotedPartTag);
        }
        mp.addBodyPart(bp);
    }
     static Account makeLegacyAccount(Context context,
            EmailContent.Account fromAccount) {
        Account result = new Account(context);
        result.setDescription(fromAccount.getDisplayName());
        result.setEmail(fromAccount.getEmailAddress());
        result.setSyncWindow(fromAccount.getSyncLookback());
        result.setAutomaticCheckIntervalMinutes(fromAccount.getSyncInterval());
        result.setNotifyNewMail(0 !=
            (fromAccount.getFlags() & EmailContent.Account.FLAGS_NOTIFY_NEW_MAIL));
        result.setVibrate(0 !=
            (fromAccount.getFlags() & EmailContent.Account.FLAGS_VIBRATE_ALWAYS));
        result.setVibrateWhenSilent(0 !=
            (fromAccount.getFlags() & EmailContent.Account.FLAGS_VIBRATE_WHEN_SILENT));
        result.setDeletePolicy(fromAccount.getDeletePolicy());
        result.mUuid = fromAccount.getUuid();
        result.setName(fromAccount.mSenderName);
        result.setRingtone(fromAccount.mRingtoneUri);
        result.mProtocolVersion = fromAccount.mProtocolVersion;
        result.mSecurityFlags = fromAccount.mSecurityFlags;
        result.mSignature = fromAccount.mSignature;
        result.setStoreUri(fromAccount.getStoreUri(context));
        result.setSenderUri(fromAccount.getSenderUri(context));
        return result;
    }
    public static EmailContent.Account makeAccount(Context context, Account fromAccount) {
        EmailContent.Account result = new EmailContent.Account();
        result.setDisplayName(fromAccount.getDescription());
        result.setEmailAddress(fromAccount.getEmail());
        result.mSyncKey = null;
        result.setSyncLookback(fromAccount.getSyncWindow());
        result.setSyncInterval(fromAccount.getAutomaticCheckIntervalMinutes());
        int flags = 0;
        if (fromAccount.isNotifyNewMail())  flags |= EmailContent.Account.FLAGS_NOTIFY_NEW_MAIL;
        if (fromAccount.isVibrate())        flags |= EmailContent.Account.FLAGS_VIBRATE_ALWAYS;
        if (fromAccount.isVibrateWhenSilent())
            flags |= EmailContent.Account.FLAGS_VIBRATE_WHEN_SILENT;
        result.setFlags(flags);
        result.setDeletePolicy(fromAccount.getDeletePolicy());
        result.mCompatibilityUuid = fromAccount.getUuid();
        result.setSenderName(fromAccount.getName());
        result.setRingtone(fromAccount.getRingtone());
        result.mProtocolVersion = fromAccount.mProtocolVersion;
        result.mNewMessageCount = 0;
        result.mSecurityFlags = fromAccount.mSecurityFlags;
        result.mSecuritySyncKey = null;
        result.mSignature = fromAccount.mSignature;
        result.setStoreUri(context, fromAccount.getStoreUri());
        result.setSenderUri(context, fromAccount.getSenderUri());
        return result;
    }
    public static EmailContent.Mailbox makeMailbox(Context context, EmailContent.Account toAccount,
            Folder fromFolder) throws MessagingException {
        EmailContent.Mailbox result = new EmailContent.Mailbox();
        result.mDisplayName = fromFolder.getName();
        result.mAccountKey = toAccount.mId;
        result.mType = inferMailboxTypeFromName(context, fromFolder.getName());
        result.mSyncTime = 0;
        result.mUnreadCount = fromFolder.getUnreadMessageCount();
        result.mFlagVisible = true;
        result.mFlags = 0;
        result.mVisibleLimit = Email.VISIBLE_LIMIT_DEFAULT;
        return result;
    }
    public static synchronized int inferMailboxTypeFromName(Context context, String mailboxName) {
        if (sServerMailboxNames.size() == 0) {
            sServerMailboxNames.put(
                    context.getString(R.string.mailbox_name_server_inbox).toLowerCase(),
                    Mailbox.TYPE_INBOX);
            sServerMailboxNames.put(
                    context.getString(R.string.mailbox_name_server_outbox).toLowerCase(),
                    Mailbox.TYPE_OUTBOX);
            sServerMailboxNames.put(
                    context.getString(R.string.mailbox_name_server_drafts).toLowerCase(),
                    Mailbox.TYPE_DRAFTS);
            sServerMailboxNames.put(
                    context.getString(R.string.mailbox_name_server_trash).toLowerCase(),
                    Mailbox.TYPE_TRASH);
            sServerMailboxNames.put(
                    context.getString(R.string.mailbox_name_server_sent).toLowerCase(),
                    Mailbox.TYPE_SENT);
            sServerMailboxNames.put(
                    context.getString(R.string.mailbox_name_server_junk).toLowerCase(),
                    Mailbox.TYPE_JUNK);
        }
        if (mailboxName == null || mailboxName.length() == 0) {
            return EmailContent.Mailbox.TYPE_MAIL;
        }
        String lowerCaseName = mailboxName.toLowerCase();
        Integer type = sServerMailboxNames.get(lowerCaseName);
        if (type != null) {
            return type;
        }
        return EmailContent.Mailbox.TYPE_MAIL;
    }
}
