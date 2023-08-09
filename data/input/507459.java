public class PicasaSyncAdapter extends AbstractThreadedSyncAdapter {
    private final Context mContext;
    public final static String TAG = "PicasaSyncAdapter";
    public PicasaSyncAdapter(Context applicationContext) {
        super(applicationContext, false);
        mContext = applicationContext;
    }
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient providerClient,
            SyncResult syncResult) {
        if (extras.getBoolean(ContentResolver.SYNC_EXTRAS_INITIALIZE, false)) {
            try {
                Account[] picasaAccounts = AccountManager.get(getContext())
                        .getAccountsByTypeAndFeatures(
                        PicasaService.ACCOUNT_TYPE,
                        new String[] { PicasaService.FEATURE_SERVICE_NAME },
                        null , null ).getResult();
                boolean isPicasaAccount = false;
                for (Account picasaAccount : picasaAccounts) {
                    if (account.equals(picasaAccount)) {
                        isPicasaAccount = true;
                        break;
                    }
                }
                if (isPicasaAccount) {
                    ContentResolver.setIsSyncable(account, authority, 1);
                    ContentResolver.setSyncAutomatically(account, authority, true);
                }
            } catch (OperationCanceledException e) {
                ;
            } catch (IOException e) {
                ;
            } catch (AuthenticatorException e) {
                ;
            }
            return;
        }
        try {
            PicasaService.performSync(mContext, account, extras, syncResult);
        } catch (Exception e) {
            ++syncResult.stats.numIoExceptions;
        }
    }
    public static final class AccountChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    }
}
