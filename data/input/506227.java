public class AccountPreference extends Preference {
    private static final String TAG = "AccountPreference";
    public static final int SYNC_ENABLED = 0; 
    public static final int SYNC_DISABLED = 1; 
    public static final int SYNC_ERROR = 2; 
    private int mStatus;
    private Account mAccount;
    private ArrayList<String> mAuthorities;
    private Drawable mProviderIcon;
    private ImageView mSyncStatusIcon;
    private ImageView mProviderIconView;
    public AccountPreference(Context context, Account account, Drawable icon,
            ArrayList<String> authorities) {
        super(context);
        mAccount = account;
        mAuthorities = authorities;
        mProviderIcon = icon;
        setLayoutResource(R.layout.account_preference);
        setTitle(mAccount.name);
        setSummary("");
        Intent intent = new Intent("android.settings.ACCOUNT_SYNC_SETTINGS");
        intent.putExtra("account", mAccount);
        setIntent(intent);
        setPersistent(false);
        setSyncStatus(SYNC_DISABLED);
    }
    public Account getAccount() {
        return mAccount;
    }
    public ArrayList<String> getAuthorities() {
        return mAuthorities;
    }
    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        setSummary(getSyncStatusMessage(mStatus));
        mProviderIconView = (ImageView) view.findViewById(R.id.providerIcon);
        mProviderIconView.setImageDrawable(mProviderIcon);
        mSyncStatusIcon = (ImageView) view.findViewById(R.id.syncStatusIcon);
        mSyncStatusIcon.setImageResource(getSyncStatusIcon(mStatus));
    }
    public void setProviderIcon(Drawable icon) {
        mProviderIcon = icon;
        if (mProviderIconView != null) {
            mProviderIconView.setImageDrawable(icon);
        }
    }
    public void setSyncStatus(int status) {
        mStatus = status;
        if (mSyncStatusIcon != null) {
            mSyncStatusIcon.setImageResource(getSyncStatusIcon(status));
        }
        setSummary(getSyncStatusMessage(status));
    }
    private int getSyncStatusMessage(int status) {
        int res;
        switch (status) {
            case SYNC_ENABLED:
                res = R.string.sync_enabled;
                break;
            case SYNC_DISABLED:
                res = R.string.sync_disabled;
                break;
            case SYNC_ERROR:
                res = R.string.sync_error;
                break;
            default:
                res = R.string.sync_error;
                Log.e(TAG, "Unknown sync status: " + status);
        }
        return res;
    }
    private int getSyncStatusIcon(int status) {
        int res;
        switch (status) {
            case SYNC_ENABLED:
                res = R.drawable.ic_sync_green;
                break;
            case SYNC_DISABLED:
                res = R.drawable.ic_sync_grey;
                break;
            case SYNC_ERROR:
                res = R.drawable.ic_sync_red;
                break;
            default:
                res = R.drawable.ic_sync_red;
                Log.e(TAG, "Unknown sync status: " + status);
        }
        return res;
    }
    @Override
    public int compareTo(Preference other) {
        if (!(other instanceof AccountPreference)) {
            return 1;
        }
        return mAccount.name.compareTo(((AccountPreference) other).mAccount.name);
    }
}
