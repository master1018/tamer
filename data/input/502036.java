public class ExchangeUtils {
    public static void startExchangeService(Context context) {
        context.startService(new Intent(context, SyncManager.class));
    }
    public static IEmailService getExchangeEmailService(Context context,
            IEmailServiceCallback callback) {
        IEmailService ret = null;
        ret = new EmailServiceProxy(context, SyncManager.class, callback);
        if (ret == null) {
            ret = NullEmailService.INSTANCE;
        }
        return ret;
    }
    public static void enableEasCalendarSync(Context context) {
        new CalendarSyncEnabler(context).enableEasCalendarSync();
    }
    private static class NullEmailService implements IEmailService {
        public static final NullEmailService INSTANCE = new NullEmailService();
        public Bundle autoDiscover(String userName, String password) throws RemoteException {
            return Bundle.EMPTY;
        }
        public boolean createFolder(long accountId, String name) throws RemoteException {
            return false;
        }
        public boolean deleteFolder(long accountId, String name) throws RemoteException {
            return false;
        }
        public void hostChanged(long accountId) throws RemoteException {
        }
        public void loadAttachment(long attachmentId, String destinationFile,
                String contentUriString) throws RemoteException {
        }
        public void loadMore(long messageId) throws RemoteException {
        }
        public boolean renameFolder(long accountId, String oldName, String newName)
                throws RemoteException {
            return false;
        }
        public void sendMeetingResponse(long messageId, int response) throws RemoteException {
        }
        public void setCallback(IEmailServiceCallback cb) throws RemoteException {
        }
        public void setLogging(int on) throws RemoteException {
        }
        public void startSync(long mailboxId) throws RemoteException {
        }
        public void stopSync(long mailboxId) throws RemoteException {
        }
        public void updateFolderList(long accountId) throws RemoteException {
        }
        public int validate(String protocol, String host, String userName, String password,
                int port, boolean ssl, boolean trustCertificates) throws RemoteException {
            return MessagingException.UNSPECIFIED_EXCEPTION;
        }
        public IBinder asBinder() {
            return null;
        }
    }
}
