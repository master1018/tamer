    public static class SyncResults {
        public int mTotalMessages;
        public int mNewMessages;
        public SyncResults(int totalMessages, int newMessages) {
            mTotalMessages = totalMessages;
            mNewMessages = newMessages;
        }
    }
    public SyncResults SynchronizeMessagesSynchronous(
            EmailContent.Account account, EmailContent.Mailbox folder,
            GroupMessagingListener listeners, Context context) throws MessagingException;
}
