public class EasOutboxService extends EasSyncService {
    public static final int SEND_FAILED = 1;
    public static final String MAILBOX_KEY_AND_NOT_SEND_FAILED =
        MessageColumns.MAILBOX_KEY + "=? and (" + SyncColumns.SERVER_ID + " is null or " +
            SyncColumns.SERVER_ID + "!=" + SEND_FAILED + ')';
    public static final String[] BODY_SOURCE_PROJECTION =
        new String[] {BodyColumns.SOURCE_MESSAGE_KEY};
    public static final String WHERE_MESSAGE_KEY = Body.MESSAGE_KEY + "=?";
    public static final int SEND_MAIL_TIMEOUT = 15*MINUTES;
    public EasOutboxService(Context _context, Mailbox _mailbox) {
        super(_context, _mailbox);
    }
    private void sendCallback(long msgId, String subject, int status) {
        try {
            SyncManager.callback().sendMessageStatus(msgId, subject, status, 0);
        } catch (RemoteException e) {
        }
    }
    int sendMessage(File cacheDir, long msgId) throws IOException, MessagingException {
        int result;
        sendCallback(msgId, null, EmailServiceStatus.IN_PROGRESS);
        File tmpFile = File.createTempFile("eas_", "tmp", cacheDir);
        try {
            String[] cols = getRowColumns(Message.CONTENT_URI, msgId, MessageColumns.FLAGS,
                    MessageColumns.SUBJECT);
            int flags = Integer.parseInt(cols[0]);
            String subject = cols[1];
            boolean reply = (flags & Message.FLAG_TYPE_REPLY) != 0;
            boolean forward = (flags & Message.FLAG_TYPE_FORWARD) != 0;
            String itemId = null;
            String collectionId = null;
            if (reply || forward) {
                cols = getRowColumns(Body.CONTENT_URI, BODY_SOURCE_PROJECTION,
                        WHERE_MESSAGE_KEY, new String[] {Long.toString(msgId)});
                if (cols != null) {
                    long refId = Long.parseLong(cols[0]);
                    cols = getRowColumns(Message.CONTENT_URI, refId, SyncColumns.SERVER_ID,
                            MessageColumns.MAILBOX_KEY);
                    if (cols != null) {
                        itemId = cols[0];
                        long boxId = Long.parseLong(cols[1]);
                        cols = getRowColumns(Mailbox.CONTENT_URI, boxId, MailboxColumns.SERVER_ID);
                        if (cols != null) {
                            collectionId = cols[0];
                        }
                    }
                }
            }
            boolean smartSend = itemId != null && collectionId != null;
            FileOutputStream fileStream = new FileOutputStream(tmpFile);
            Rfc822Output.writeTo(mContext, msgId, fileStream, !smartSend, true);
            fileStream.close();
            FileInputStream inputStream = new FileInputStream(tmpFile);
            InputStreamEntity inputEntity =
                new InputStreamEntity(inputStream, tmpFile.length());
            String cmd = "SendMail&SaveInSent=T";
            if (smartSend) {
                cmd = reply ? "SmartReply" : "SmartForward";
                cmd += "&ItemId=" + itemId + "&CollectionId=" + collectionId + "&SaveInSent=T";
            }
            userLog("Send cmd: " + cmd);
            HttpResponse resp = sendHttpClientPost(cmd, inputEntity, SEND_MAIL_TIMEOUT);
            inputStream.close();
            int code = resp.getStatusLine().getStatusCode();
            if (code == HttpStatus.SC_OK) {
                userLog("Deleting message...");
                mContentResolver.delete(ContentUris.withAppendedId(Message.CONTENT_URI, msgId),
                        null, null);
                result = EmailServiceStatus.SUCCESS;
                sendCallback(-1, subject, EmailServiceStatus.SUCCESS);
            } else {
                userLog("Message sending failed, code: " + code);
                ContentValues cv = new ContentValues();
                cv.put(SyncColumns.SERVER_ID, SEND_FAILED);
                Message.update(mContext, Message.CONTENT_URI, msgId, cv);
                if (isAuthError(code)) {
                    result = EmailServiceStatus.LOGIN_FAILED;
                } else {
                    result = EmailServiceStatus.SUCCESS;
                }
                sendCallback(msgId, null, result);
            }
        } catch (IOException e) {
            sendCallback(msgId, null, EmailServiceStatus.CONNECTION_ERROR);
            throw e;
        } finally {
            if (tmpFile.exists()) {
                tmpFile.delete();
            }
        }
        return result;
    }
    @Override
    public void run() {
        setupService();
        File cacheDir = mContext.getCacheDir();
        try {
            mDeviceId = SyncManager.getDeviceId();
            Cursor c = mContext.getContentResolver().query(Message.CONTENT_URI,
                    Message.ID_COLUMN_PROJECTION, MAILBOX_KEY_AND_NOT_SEND_FAILED,
                    new String[] {Long.toString(mMailbox.mId)}, null);
             try {
                while (c.moveToNext()) {
                    long msgId = c.getLong(0);
                    if (msgId != 0) {
                        int result = sendMessage(cacheDir, msgId);
                        if (result == EmailServiceStatus.LOGIN_FAILED) {
                            mExitStatus = EXIT_LOGIN_FAILURE;
                            return;
                        } else if (result == EmailServiceStatus.REMOTE_EXCEPTION) {
                            mExitStatus = EXIT_EXCEPTION;
                            return;
                        }
                    }
                }
            } finally {
                 c.close();
            }
            mExitStatus = EXIT_DONE;
        } catch (IOException e) {
            mExitStatus = EXIT_IO_ERROR;
        } catch (Exception e) {
            userLog("Exception caught in EasOutboxService", e);
            mExitStatus = EXIT_EXCEPTION;
        } finally {
            userLog(mMailbox.mDisplayName, ": sync finished");
            userLog("Outbox exited with status ", mExitStatus);
            SyncManager.done(this);
        }
    }
    public static void sendMessage(Context context, long accountId, Message msg) {
        Mailbox mailbox = Mailbox.restoreMailboxOfType(context, accountId, Mailbox.TYPE_OUTBOX);
        if (mailbox != null) {
            msg.mMailboxKey = mailbox.mId;
            msg.mAccountKey = accountId;
            msg.save(context);
        }
    }
}