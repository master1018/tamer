public class MessagingListener {
    public void listFoldersStarted(long accountId) {
    }
    public void listFoldersFailed(long accountId, String message) {
    }
    public void listFoldersFinished(long accountId) {
    }
    public void synchronizeMailboxStarted(long accountId, long mailboxId)
            {
    }
    public void synchronizeMailboxFinished(long accountId,
            long mailboxId, int totalMessagesInMailbox, int numNewMessages) {
    }
    public void synchronizeMailboxFailed(long accountId, long mailboxId,
            Exception e) {
    }
    public void loadMessageForViewStarted(long messageId) {
    }
    public void loadMessageForViewFinished(long messageId) {
    }
    public void loadMessageForViewFailed(long messageId, String message) {
    }
    public void checkMailStarted(Context context, long accountId, long tag) {
    }
    public void checkMailFinished(Context context, long accountId, long mailboxId, long tag) {
    }
    public void sendPendingMessagesStarted(long accountId, long messageId) {
    }
    public void sendPendingMessagesCompleted(long accountId) {
    }
    public void sendPendingMessagesFailed(long accountId, long messageId, Exception reason) {
    }
    public void messageUidChanged(long accountId, long mailboxId, String oldUid, String newUid) {
    }
    public void loadAttachmentStarted(
            long accountId,
            long messageId,
            long attachmentId,
            boolean requiresDownload) {
    }
    public void loadAttachmentFinished(
            long accountId,
            long messageId,
            long attachmentId) {
    }
    public void loadAttachmentFailed(
            long accountId,
            long messageId,
            long attachmentId,
            String reason) {
    }
    public void controllerCommandCompleted(boolean moreCommandsToRun) {
    }
}
