public class AccountSecurity extends Activity {
    private static final String EXTRA_ACCOUNT_ID = "com.android.email.activity.setup.ACCOUNT_ID";
    private static final int REQUEST_ENABLE = 1;
    public static Intent actionUpdateSecurityIntent(Context context, long accountId) {
        Intent intent = new Intent(context, AccountSecurity.class);
        intent.putExtra(EXTRA_ACCOUNT_ID, accountId);
        return intent;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent i = getIntent();
        long accountId = i.getLongExtra(EXTRA_ACCOUNT_ID, -1);
        SecurityPolicy security = SecurityPolicy.getInstance(this);
        security.clearNotification(accountId);
        if (accountId != -1) {
            Account account = Account.restoreAccountWithId(this, accountId);
            if (account != null) {
                if (account.mSecurityFlags != 0) {
                    if (!security.isActiveAdmin()) {
                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
                                security.getAdminComponent());
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                            this.getString(R.string.account_security_policy_explanation_fmt,
                                    account.getDisplayName()));
                        startActivityForResult(intent, REQUEST_ENABLE);
                        return;
                    } else {
                        setActivePolicies();
                    }
                }
            }
        }
        finish();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    setActivePolicies();
                } else {
                    final long accountId = getIntent().getLongExtra(EXTRA_ACCOUNT_ID, -1);
                    if (accountId != -1) {
                        new Thread() {
                            @Override
                            public void run() {
                                SecurityPolicy.getInstance(AccountSecurity.this)
                                        .policiesRequired(accountId);
                            }
                        }.start();
                    }
                }
        }
        finish();
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void setActivePolicies() {
        SecurityPolicy sp = SecurityPolicy.getInstance(this);
        if (sp.isActive(null)) {
            sp.clearAccountHoldFlags();
            return;
        }
        sp.setActivePolicies();
        if (sp.isActive(null)) {
            sp.clearAccountHoldFlags();
            return;
        }
        Intent intent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
        startActivity(intent);
    }
}
