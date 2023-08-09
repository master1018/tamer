public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = "SyncAdapter";
    private final AccountManager mAccountManager;
    private final Context mContext;
    private Date mLastUpdated;
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
        ContentProviderClient provider, SyncResult syncResult) {
        List<User> users;
        List<Status> statuses;
        String authtoken = null;
         try {
             authtoken =
                mAccountManager.blockingGetAuthToken(account,
                    Constants.AUTHTOKEN_TYPE, true );
             users =
                NetworkUtilities.fetchFriendUpdates(account, authtoken,
                    mLastUpdated);
            mLastUpdated = new Date();
            Log.d(TAG, "Calling contactManager's sync contacts");
            ContactManager.syncContacts(mContext, account.name, users);
            statuses = NetworkUtilities.fetchFriendStatuses(account, authtoken);
            ContactManager.insertStatuses(mContext, account.name, statuses);
        } catch (final AuthenticatorException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "AuthenticatorException", e);
        } catch (final OperationCanceledException e) {
            Log.e(TAG, "OperationCanceledExcetpion", e);
        } catch (final IOException e) {
            Log.e(TAG, "IOException", e);
            syncResult.stats.numIoExceptions++;
        } catch (final AuthenticationException e) {
            mAccountManager.invalidateAuthToken(Constants.ACCOUNT_TYPE,
                authtoken);
            syncResult.stats.numAuthExceptions++;
            Log.e(TAG, "AuthenticationException", e);
        } catch (final ParseException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "ParseException", e);
        } catch (final JSONException e) {
            syncResult.stats.numParseExceptions++;
            Log.e(TAG, "JSONException", e);
        }
    }
}
