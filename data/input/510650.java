public class CertInstaller extends Activity
        implements DialogInterface.OnClickListener {
    private static final String TAG = "CertInstaller";
    private static final int STATE_INIT = 1;
    private static final int STATE_RUNNING = 2;
    private static final int STATE_PAUSED = 3;
    private static final int NAME_CREDENTIAL_DIALOG = 1;
    private static final int PKCS12_PASSWORD_DIALOG = 2;
    private static final int PROGRESS_BAR_DIALOG = 3;
    private static final int REQUEST_SYSTEM_INSTALL_CODE = 1;
    private static final String NEXT_ACTION_KEY = "na";
    private static final byte[] PKEY_MAP_KEY = "PKEY_MAP".getBytes();
    private KeyStore mKeyStore = KeyStore.getInstance();
    private ViewHelper mView = new ViewHelper();
    private int mButtonClicked;
    private int mState;
    private CredentialHelper mCredentials;
    private MyAction mNextAction;
    @Override
    protected void onCreate(Bundle savedStates) {
        super.onCreate(savedStates);
        mCredentials = new CredentialHelper(getIntent());
        mState = (savedStates == null) ? STATE_INIT : STATE_RUNNING;
        if (mState == STATE_INIT) {
            if (!mCredentials.containsAnyRawData()) {
                toastErrorAndFinish(R.string.no_cert_to_saved);
                finish();
            } else if (mCredentials.hasPkcs12KeyStore()) {
                showDialog(PKCS12_PASSWORD_DIALOG);
            } else {
                MyAction action = new InstallOthersAction();
                if (needsKeyStoreAccess()) {
                    sendUnlockKeyStoreIntent();
                    mNextAction = action;
                } else {
                    action.run(this);
                }
            }
        } else {
            mCredentials.onRestoreStates(savedStates);
            mNextAction = (MyAction)
                    savedStates.getSerializable(NEXT_ACTION_KEY);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mState == STATE_INIT) {
            mState = STATE_RUNNING;
        } else {
            if (mNextAction != null) mNextAction.run(this);
        }
    }
    private boolean needsKeyStoreAccess() {
        return ((mCredentials.hasKeyPair() || mCredentials.hasUserCertificate())
                && (mKeyStore.test() != KeyStore.NO_ERROR));
    }
    @Override
    protected void onPause() {
        super.onPause();
        mState = STATE_PAUSED;
    }
    @Override
    protected void onSaveInstanceState(Bundle outStates) {
        super.onSaveInstanceState(outStates);
        mCredentials.onSaveStates(outStates);
        if (mNextAction != null) {
            outStates.putSerializable(NEXT_ACTION_KEY, mNextAction);
        }
    }
    @Override
    protected Dialog onCreateDialog (int dialogId) {
        switch (dialogId) {
            case PKCS12_PASSWORD_DIALOG:
                return createPkcs12PasswordDialog();
            case NAME_CREDENTIAL_DIALOG:
                return createNameCredentialDialog();
            case PROGRESS_BAR_DIALOG:
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(getString(R.string.extracting_pkcs12));
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);
                return dialog;
            default:
                return null;
        }
    }
    @Override
    protected void onPrepareDialog (int dialogId, Dialog dialog) {
        super.onPrepareDialog(dialogId, dialog);
        mButtonClicked = DialogInterface.BUTTON_NEGATIVE;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent data) {
        if (requestCode == REQUEST_SYSTEM_INSTALL_CODE) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "credential is added: " + mCredentials.getName());
                Toast.makeText(this, getString(R.string.cert_is_added,
                        mCredentials.getName()), Toast.LENGTH_LONG).show();
                setResult(RESULT_OK);
            } else {
                Log.d(TAG, "credential not saved, err: " + resultCode);
                toastErrorAndFinish(R.string.cert_not_saved);
            }
        } else {
            Log.w(TAG, "unknown request code: " + requestCode);
        }
        finish();
    }
    void installOthers() {
        if (mCredentials.hasKeyPair()) {
            saveKeyPair();
            finish();
        } else {
            X509Certificate cert = mCredentials.getUserCertificate();
            if (cert != null) {
                String key = Util.toMd5(cert.getPublicKey().getEncoded());
                Map<String, byte[]> map = getPkeyMap();
                byte[] privatekey = map.get(key);
                if (privatekey != null) {
                    Log.d(TAG, "found matched key: " + privatekey);
                    map.remove(key);
                    savePkeyMap(map);
                    mCredentials.setPrivateKey(privatekey);
                } else {
                    Log.d(TAG, "didn't find matched private key: " + key);
                }
            }
            nameCredential();
        }
    }
    private void sendUnlockKeyStoreIntent() {
        Credentials.getInstance().unlock(this);
    }
    private void nameCredential() {
        if (!mCredentials.hasAnyForSystemInstall()) {
            toastErrorAndFinish(R.string.no_cert_to_saved);
        } else {
            showDialog(NAME_CREDENTIAL_DIALOG);
        }
    }
    private void saveKeyPair() {
        byte[] privatekey = mCredentials.getData(Credentials.PRIVATE_KEY);
        String key = Util.toMd5(mCredentials.getData(Credentials.PUBLIC_KEY));
        Map<String, byte[]> map = getPkeyMap();
        map.put(key, privatekey);
        savePkeyMap(map);
        Log.d(TAG, "save privatekey: " + key + " --> #keys:" + map.size());
    }
    private void savePkeyMap(Map<String, byte[]> map) {
        byte[] bytes = Util.toBytes((Serializable) map);
        if (!mKeyStore.put(PKEY_MAP_KEY, bytes)) {
            Log.w(TAG, "savePkeyMap(): failed to write pkey map");
        }
    }
    private Map<String, byte[]> getPkeyMap() {
        byte[] bytes = mKeyStore.get(PKEY_MAP_KEY);
        if (bytes != null) {
            Map<String, byte[]> map =
                    (Map<String, byte[]>) Util.fromBytes(bytes);
            if (map != null) return map;
        }
        return new MyMap();
    }
    void extractPkcs12InBackground(final String password) {
        showDialog(PROGRESS_BAR_DIALOG);
        new Thread(new Runnable() {
            public void run() {
                final boolean success = mCredentials.extractPkcs12(password);
                runOnUiThread(new Runnable() {
                    public void run() {
                        MyAction action = new OnExtractionDoneAction(success);
                        if (mState == STATE_PAUSED) {
                            mNextAction = action;
                        } else {
                            action.run(CertInstaller.this);
                        }
                    }
                });
            }
        }).start();
    }
    void onExtractionDone(boolean success) {
        mNextAction = null;
        removeDialog(PROGRESS_BAR_DIALOG);
        if (success) {
            removeDialog(PKCS12_PASSWORD_DIALOG);
            nameCredential();
        } else {
            mView.setText(R.id.credential_password, "");
            mView.showError(R.string.password_error);
            showDialog(PKCS12_PASSWORD_DIALOG);
        }
    }
    public void onClick(DialogInterface dialog, int which) {
        mButtonClicked = which;
    }
    private Dialog createPkcs12PasswordDialog() {
        View view = View.inflate(this, R.layout.password_dialog, null);
        mView.setView(view);
        DialogInterface.OnDismissListener onDismissHandler =
                new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mButtonClicked == DialogInterface.BUTTON_NEGATIVE) {
                    toastErrorAndFinish(R.string.cert_not_saved);
                    return;
                }
                final String password = mView.getText(R.id.credential_password);
                if (TextUtils.isEmpty(password)) {
                    mView.showError(R.string.password_empty_error);
                    showDialog(PKCS12_PASSWORD_DIALOG);
                } else {
                    mNextAction = new Pkcs12ExtractAction(password);
                    mNextAction.run(CertInstaller.this);
                }
            }
        };
        String title = mCredentials.getName();
        title = TextUtils.isEmpty(title)
                ? getString(R.string.pkcs12_password_dialog_title)
                : getString(R.string.pkcs12_file_password_dialog_title, title);
        Dialog d = new AlertDialog.Builder(this)
                .setView(view)
                .setTitle(title)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, this)
                .create();
        d.setOnDismissListener(onDismissHandler);
        return d;
    }
    private Dialog createNameCredentialDialog() {
        View view = View.inflate(this, R.layout.name_credential_dialog, null);
        mView.setView(view);
        mView.setText(R.id.credential_info,
                mCredentials.getDescription(this).toString());
        DialogInterface.OnDismissListener onDismissHandler =
                new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                if (mButtonClicked == DialogInterface.BUTTON_NEGATIVE) {
                    toastErrorAndFinish(R.string.cert_not_saved);
                    return;
                }
                String name = mView.getText(R.id.credential_name);
                if (TextUtils.isEmpty(name)) {
                    mView.showError(R.string.name_empty_error);
                    showDialog(NAME_CREDENTIAL_DIALOG);
                } else {
                    removeDialog(NAME_CREDENTIAL_DIALOG);
                    mCredentials.setName(name);
                    try {
                        startActivityForResult(
                                mCredentials.createSystemInstallIntent(),
                                REQUEST_SYSTEM_INSTALL_CODE);
                    } catch (ActivityNotFoundException e) {
                        Log.w(TAG, "systemInstall(): " + e);
                        toastErrorAndFinish(R.string.cert_not_saved);
                    }
                }
            }
        };
        mView.setText(R.id.credential_name, getDefaultName());
        Dialog d = new AlertDialog.Builder(this)
                .setView(view)
                .setTitle(R.string.name_credential_dialog_title)
                .setPositiveButton(android.R.string.ok, this)
                .setNegativeButton(android.R.string.cancel, this)
                .create();
        d.setOnDismissListener(onDismissHandler);
        return d;
    }
    private String getDefaultName() {
        String name = mCredentials.getName();
        if (TextUtils.isEmpty(name)) {
            return null;
        } else {
            int index = name.lastIndexOf(".");
            if (index > 0) name = name.substring(0, index);
            return name;
        }
    }
    private void toastErrorAndFinish(int msgId) {
        Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show();
        finish();
    }
    private static class MyMap extends LinkedHashMap<String, byte[]>
            implements Serializable {
        private static final long serialVersionUID = 1L;
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return (size() > 3);
        }
    }
    private interface MyAction extends Serializable {
        void run(CertInstaller host);
    }
    private static class Pkcs12ExtractAction implements MyAction {
        private String mPassword;
        private transient boolean hasRun;
        Pkcs12ExtractAction(String password) {
            mPassword = password;
        }
        public void run(CertInstaller host) {
            if (hasRun) return;
            hasRun = true;
            host.extractPkcs12InBackground(mPassword);
        }
    }
    private static class InstallOthersAction implements MyAction {
        public void run(CertInstaller host) {
            host.mNextAction = null;
            host.installOthers();
        }
    }
    private static class OnExtractionDoneAction implements MyAction {
        private boolean mSuccess;
        OnExtractionDoneAction(boolean success) {
            mSuccess = success;
        }
        public void run(CertInstaller host) {
            host.onExtractionDone(mSuccess);
        }
    }
}
