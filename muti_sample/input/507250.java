public class AddAccountSettings extends AccountPreferenceBase {
    private static final String TAG = "AccountSettings";
    private static final boolean LDEBUG = Log.isLoggable(TAG, Log.DEBUG);
    private String[] mAuthorities;
    private PreferenceGroup mAddAccountGroup;
    private ArrayList<ProviderEntry> mProviderList = new ArrayList<ProviderEntry>();;
    private static class ProviderEntry {
        private final CharSequence name;
        private final String type;
        ProviderEntry(CharSequence providerName, String accountType) {
            name = providerName;
            type = accountType;
        }
    }
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.add_account_screen);
        addPreferencesFromResource(R.xml.add_account_settings);
        mAuthorities = getIntent().getStringArrayExtra(AUTHORITIES_FILTER_KEY);
        mAddAccountGroup = getPreferenceScreen();
        updateAuthDescriptions();
    }
    @Override
    protected void onAuthDescriptionsUpdated() {
        for (int i = 0; i < mAuthDescs.length; i++) {
            String accountType = mAuthDescs[i].type;
            CharSequence providerName = getLabelForType(accountType);
            ArrayList<String> accountAuths = getAuthoritiesForAccountType(accountType);
            boolean addAccountPref = true;
            if (mAuthorities != null && mAuthorities.length > 0 && accountAuths != null) {
                addAccountPref = false;
                for (int k = 0; k < mAuthorities.length; k++) {
                    if (accountAuths.contains(mAuthorities[k])) {
                        addAccountPref = true;
                        break;
                    }
                }
            }
            if (addAccountPref) {
                mProviderList.add(new ProviderEntry(providerName, accountType));
            } else {
                if (LDEBUG) Log.v(TAG, "Skipped pref " + providerName + ": has no authority we need");
            }
        }
        if (mProviderList.size() == 1) {
            addAccount(mProviderList.get(0).type);
            finish();
        } else if (mProviderList.size() > 0) {
            mAddAccountGroup.removeAll();
            for (ProviderEntry pref : mProviderList) {
                Drawable drawable = getDrawableForType(pref.type);
                ProviderPreference p = new ProviderPreference(this, pref.type, drawable, pref.name);
                mAddAccountGroup.addPreference(p);
            }
        } else {
            String auths = new String();
            for (String a : mAuthorities) auths += a + " ";
            Log.w(TAG, "No providers found for authorities: " + auths);
        }
    }
    private AccountManagerCallback<Bundle> mCallback = new AccountManagerCallback<Bundle>() {
        public void run(AccountManagerFuture<Bundle> future) {
            boolean accountAdded = false;
            try {
                Bundle bundle = future.getResult();
                bundle.keySet();
                accountAdded = true;
                if (LDEBUG) Log.d(TAG, "account added: " + bundle);
            } catch (OperationCanceledException e) {
                if (LDEBUG) Log.d(TAG, "addAccount was canceled");
            } catch (IOException e) {
                if (LDEBUG) Log.d(TAG, "addAccount failed: " + e);
            } catch (AuthenticatorException e) {
                if (LDEBUG) Log.d(TAG, "addAccount failed: " + e);
            } finally {
                finish();
            }
        }
    };
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferences, Preference preference) {
        if (preference instanceof ProviderPreference) {
            ProviderPreference pref = (ProviderPreference) preference;
            if (LDEBUG) Log.v(TAG, "Attempting to add account of type " + pref.getAccountType());
            addAccount(pref.getAccountType());
        }
        return true;
    }
    private void addAccount(String accountType) {
        AccountManager.get(this).addAccount(
                accountType,
                null, 
                null, 
                null, 
                this,
                mCallback,
                null );
    }
}
