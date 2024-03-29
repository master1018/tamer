public class AccountSyncSettings extends AccountPreferenceBase implements OnClickListener {
    private static final String ACCOUNT_KEY = "account";
    private static final String TAG = "AccountSettings";
    private static final String CHANGE_PASSWORD_KEY = "changePassword";
    private static final int MENU_SYNC_NOW_ID = Menu.FIRST;
    private static final int MENU_SYNC_CANCEL_ID = Menu.FIRST + 1;
    private static final int REALLY_REMOVE_DIALOG = 100;
    private static final int FAILED_REMOVAL_DIALOG = 101;
    private static final int CANT_DO_ONETIME_SYNC_DIALOG = 102;
    private static final boolean LDEBUG = Log.isLoggable(TAG, Log.DEBUG);
    private TextView mUserId;
    private TextView mProviderId;
    private ImageView mProviderIcon;
    private TextView mErrorInfoView;
    protected View mRemoveAccountArea;
    private java.text.DateFormat mDateFormat;
    private java.text.DateFormat mTimeFormat;
    private Preference mAuthenticatorPreferences;
    private Account mAccount;
    private Account[] mAccounts;
    private Button mRemoveAccountButton;
    private ArrayList<SyncStateCheckBoxPreference> mCheckBoxes =
                new ArrayList<SyncStateCheckBoxPreference>();
    private ArrayList<String> mInvisibleAdapters = Lists.newArrayList();
    public void onClick(View v) {
        if (v == mRemoveAccountButton) {
            showDialog(REALLY_REMOVE_DIALOG);
        }
    }
    @Override
    protected Dialog onCreateDialog(final int id) {
        Dialog dialog = null;
        if (id == REALLY_REMOVE_DIALOG) {
            dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.really_remove_account_title)
                .setMessage(R.string.really_remove_account_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.remove_account_label,
                        new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AccountManager.get(AccountSyncSettings.this).removeAccount(mAccount,
                                new AccountManagerCallback<Boolean>() {
                            public void run(AccountManagerFuture<Boolean> future) {
                                boolean failed = true;
                                try {
                                    if (future.getResult() == true) {
                                        failed = false;
                                    }
                                } catch (OperationCanceledException e) {
                                } catch (IOException e) {
                                } catch (AuthenticatorException e) {
                                }
                                if (failed) {
                                    showDialog(FAILED_REMOVAL_DIALOG);
                                } else {
                                    finish();
                                }
                            }
                        }, null);
                    }
                })
                .create();
        } else if (id == FAILED_REMOVAL_DIALOG) {
            dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.really_remove_account_title)
                .setPositiveButton(android.R.string.ok, null)
                .setMessage(R.string.remove_account_failed)
                .create();
        } else if (id == CANT_DO_ONETIME_SYNC_DIALOG) {
            dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.cant_sync_dialog_title)
                .setMessage(R.string.cant_sync_dialog_message)
                .setPositiveButton(android.R.string.ok, null)
                .create();
        }
        return dialog;
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.account_sync_screen);
        addPreferencesFromResource(R.xml.account_sync_settings);
        mErrorInfoView = (TextView) findViewById(R.id.sync_settings_error_info);
        mErrorInfoView.setVisibility(View.GONE);
        mErrorInfoView.setCompoundDrawablesWithIntrinsicBounds(
                getResources().getDrawable(R.drawable.ic_list_syncerror), null, null, null);
        mUserId = (TextView) findViewById(R.id.user_id);
        mProviderId = (TextView) findViewById(R.id.provider_id);
        mProviderIcon = (ImageView) findViewById(R.id.provider_icon);
        mRemoveAccountArea = (View) findViewById(R.id.remove_account_area);
        mRemoveAccountButton = (Button) findViewById(R.id.remove_account_button);
        mRemoveAccountButton.setOnClickListener(this);
        mDateFormat = DateFormat.getDateFormat(this);
        mTimeFormat = DateFormat.getTimeFormat(this);
        mAccount = (Account) getIntent().getParcelableExtra(ACCOUNT_KEY);
        if (mAccount != null) {
            if (LDEBUG) Log.v(TAG, "Got account: " + mAccount);
            mUserId.setText(mAccount.name);
            mProviderId.setText(mAccount.type);
        }
        AccountManager.get(this).addOnAccountsUpdatedListener(this, null, false);
        updateAuthDescriptions();
        onAccountsUpdated(AccountManager.get(this).getAccounts());
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        AccountManager.get(this).removeOnAccountsUpdatedListener(this);
    }
    private void addSyncStateCheckBox(Account account, String authority) {
        SyncStateCheckBoxPreference item =
                new SyncStateCheckBoxPreference(this, account, authority);
        item.setPersistent(false);
        final ProviderInfo providerInfo = getPackageManager().resolveContentProvider(authority, 0);
        CharSequence providerLabel = providerInfo != null
                ? providerInfo.loadLabel(getPackageManager()) : null;
        if (TextUtils.isEmpty(providerLabel)) {
            Log.e(TAG, "Provider needs a label for authority '" + authority + "'");
            providerLabel = authority;
        }
        String title = getString(R.string.sync_item_title, providerLabel);
        item.setTitle(title);
        item.setKey(authority);
        getPreferenceScreen().addPreference(item);
        mCheckBoxes.add(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_SYNC_NOW_ID, 0, getString(R.string.sync_menu_sync_now))
                .setIcon(com.android.internal.R.drawable.ic_menu_refresh);
        menu.add(0, MENU_SYNC_CANCEL_ID, 0, getString(R.string.sync_menu_sync_cancel))
                .setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean syncActive = ContentResolver.getCurrentSync() != null;
        menu.findItem(MENU_SYNC_NOW_ID).setVisible(!syncActive);
        menu.findItem(MENU_SYNC_CANCEL_ID).setVisible(syncActive);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_SYNC_NOW_ID:
                startSyncForEnabledProviders();
                return true;
            case MENU_SYNC_CANCEL_ID:
                cancelSyncForEnabledProviders();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferences, Preference preference) {
        if (preference instanceof SyncStateCheckBoxPreference) {
            SyncStateCheckBoxPreference syncPref = (SyncStateCheckBoxPreference) preference;
            String authority = syncPref.getAuthority();
            Account account = syncPref.getAccount();
            boolean syncAutomatically = ContentResolver.getSyncAutomatically(account, authority);
            if (syncPref.isOneTimeSyncMode()) {
                requestOrCancelSync(account, authority, true);
            } else {
                boolean syncOn = syncPref.isChecked();
                boolean oldSyncState = syncAutomatically;
                if (syncOn != oldSyncState) {
                    ContentResolver.setSyncAutomatically(account, authority, syncOn);
                    if (!ContentResolver.getMasterSyncAutomatically() || !syncOn) {
                        requestOrCancelSync(account, authority, syncOn);
                    }
                }
            }
            return true;
        } else {
            return super.onPreferenceTreeClick(preferences, preference);
        }
    }
    private void startSyncForEnabledProviders() {
        requestOrCancelSyncForEnabledProviders(true );
    }
    private void cancelSyncForEnabledProviders() {
        requestOrCancelSyncForEnabledProviders(false );
    }
    private void requestOrCancelSyncForEnabledProviders(boolean startSync) {
        int count = getPreferenceScreen().getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference pref = getPreferenceScreen().getPreference(i);
            if (! (pref instanceof SyncStateCheckBoxPreference)) {
                continue;
            }
            SyncStateCheckBoxPreference syncPref = (SyncStateCheckBoxPreference) pref;
            if (!syncPref.isChecked()) {
                continue;
            }
            requestOrCancelSync(syncPref.getAccount(), syncPref.getAuthority(), startSync);
        }
        if (mAccount != null) {
            for (String authority : mInvisibleAdapters) {
                requestOrCancelSync(mAccount, authority, startSync);
            }
        }
    }
    private void requestOrCancelSync(Account account, String authority, boolean flag) {
        if (flag) {
            Bundle extras = new Bundle();
            extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
            ContentResolver.requestSync(account, authority, extras);
        } else {
            ContentResolver.cancelSync(account, authority);
        }
    }
    @Override
    protected void onSyncStateUpdated() {
        Date date = new Date();
        SyncInfo currentSync = ContentResolver.getCurrentSync();
        boolean syncIsFailing = false;
        updateAccountCheckboxes(mAccounts);
        for (int i = 0, count = getPreferenceScreen().getPreferenceCount(); i < count; i++) {
            Preference pref = getPreferenceScreen().getPreference(i);
            if (! (pref instanceof SyncStateCheckBoxPreference)) {
                continue;
            }
            SyncStateCheckBoxPreference syncPref = (SyncStateCheckBoxPreference) pref;
            String authority = syncPref.getAuthority();
            Account account = syncPref.getAccount();
            SyncStatusInfo status = ContentResolver.getSyncStatus(account, authority);
            boolean syncEnabled = ContentResolver.getSyncAutomatically(account, authority);
            boolean authorityIsPending = status == null ? false : status.pending;
            boolean initialSync = status == null ? false : status.initialize;
            boolean activelySyncing = currentSync != null
                    && new Account(currentSync.account.name, currentSync.account.type).equals(account)
                    && currentSync.authority.equals(authority);
            boolean lastSyncFailed = status != null
                    && status.lastFailureTime != 0
                    && status.getLastFailureMesgAsInt(0)
                       != ContentResolver.SYNC_ERROR_SYNC_ALREADY_IN_PROGRESS;
            if (!syncEnabled) lastSyncFailed = false;
            if (lastSyncFailed && !activelySyncing && !authorityIsPending) {
                syncIsFailing = true;
            }
            if (LDEBUG) {
                Log.d(TAG, "Update sync status: " + account + " " + authority +
                        " active = " + activelySyncing + " pend =" +  authorityIsPending);
            }
            final long successEndTime = (status == null) ? 0 : status.lastSuccessTime;
            if (successEndTime != 0) {
                date.setTime(successEndTime);
                final String timeString = mDateFormat.format(date) + " "
                        + mTimeFormat.format(date);
                syncPref.setSummary(timeString);
            } else {
                syncPref.setSummary("");
            }
            int syncState = ContentResolver.getIsSyncable(account, authority);
            syncPref.setActive(activelySyncing && (syncState >= 0) &&
                    !initialSync);
            syncPref.setPending(authorityIsPending && (syncState >= 0) &&
                    !initialSync);
            syncPref.setFailed(lastSyncFailed);
            ConnectivityManager connManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            final boolean masterSyncAutomatically = ContentResolver.getMasterSyncAutomatically();
            final boolean backgroundDataEnabled = connManager.getBackgroundDataSetting();
            final boolean oneTimeSyncMode = !masterSyncAutomatically || !backgroundDataEnabled;
            syncPref.setOneTimeSyncMode(oneTimeSyncMode);
            syncPref.setChecked(oneTimeSyncMode || syncEnabled);
        }
        mErrorInfoView.setVisibility(syncIsFailing ? View.VISIBLE : View.GONE);
    }
    @Override
    public void onAccountsUpdated(Account[] accounts) {
        super.onAccountsUpdated(accounts);
        mAccounts = accounts;
        updateAccountCheckboxes(accounts);
        onSyncStateUpdated();
    }
    private void updateAccountCheckboxes(Account[] accounts) {
        mInvisibleAdapters.clear();
        SyncAdapterType[] syncAdapters = ContentResolver.getSyncAdapterTypes();
        HashMap<String, ArrayList<String>> accountTypeToAuthorities =
            Maps.newHashMap();
        for (int i = 0, n = syncAdapters.length; i < n; i++) {
            final SyncAdapterType sa = syncAdapters[i];
            if (sa.isUserVisible()) {
                ArrayList<String> authorities = accountTypeToAuthorities.get(sa.accountType);
                if (authorities == null) {
                    authorities = new ArrayList<String>();
                    accountTypeToAuthorities.put(sa.accountType, authorities);
                }
                if (LDEBUG) {
                    Log.d(TAG, "onAccountUpdated: added authority " + sa.authority
                            + " to accountType " + sa.accountType);
                }
                authorities.add(sa.authority);
            } else {
                mInvisibleAdapters.add(sa.authority);
            }
        }
        for (int i = 0, n = mCheckBoxes.size(); i < n; i++) {
            getPreferenceScreen().removePreference(mCheckBoxes.get(i));
        }
        mCheckBoxes.clear();
        for (int i = 0, n = accounts.length; i < n; i++) {
            final Account account = accounts[i];
            if (LDEBUG) Log.d(TAG, "looking for sync adapters that match account " + account);
            final ArrayList<String> authorities = accountTypeToAuthorities.get(account.type);
            if (authorities != null && (mAccount == null || mAccount.equals(account))) {
                for (int j = 0, m = authorities.size(); j < m; j++) {
                    final String authority = authorities.get(j);
                    int syncState = ContentResolver.getIsSyncable(account, authority);
                    if (LDEBUG) Log.d(TAG, "  found authority " + authority + " " + syncState);
                    if (syncState > 0) {
                        addSyncStateCheckBox(account, authority);
                    }
                }
            }
        }
    }
    @Override
    protected void onAuthDescriptionsUpdated() {
        super.onAuthDescriptionsUpdated();
        getPreferenceScreen().removeAll();
        mProviderIcon.setImageDrawable(getDrawableForType(mAccount.type));
        mProviderId.setText(getLabelForType(mAccount.type));
        PreferenceScreen prefs = addPreferencesForType(mAccount.type);
        if (prefs != null) {
            updatePreferenceIntents(prefs);
        }
        addPreferencesFromResource(R.xml.account_sync_settings);
    }
    private void updatePreferenceIntents(PreferenceScreen prefs) {
        for (int i = 0; i < prefs.getPreferenceCount(); i++) {
            Intent intent = prefs.getPreference(i).getIntent();
            if (intent != null) {
                intent.putExtra(ACCOUNT_KEY, mAccount);
                intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK);
            }
        }
    }
}
