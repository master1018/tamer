public class CredentialInstaller extends Activity {
    private static final String TAG = "CredentialInstaller";
    private static final String UNLOCKING = "ulck";
    private KeyStore mKeyStore = KeyStore.getInstance();
    private boolean mUnlocking = false;
    @Override
    protected void onResume() {
        super.onResume();
        if (!"com.android.certinstaller".equals(getCallingPackage())) finish();
        if (isKeyStoreUnlocked()) {
            install();
        } else if (!mUnlocking) {
            mUnlocking = true;
            Credentials.getInstance().unlock(this);
            return;
        }
        finish();
    }
    @Override
    protected void onSaveInstanceState(Bundle outStates) {
        super.onSaveInstanceState(outStates);
        outStates.putBoolean(UNLOCKING, mUnlocking);
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedStates) {
        super.onRestoreInstanceState(savedStates);
        mUnlocking = savedStates.getBoolean(UNLOCKING);
    }
    private void install() {
        Intent intent = getIntent();
        Bundle bundle = (intent == null) ? null : intent.getExtras();
        if (bundle == null) return;
        for (String key : bundle.keySet()) {
            byte[] data = bundle.getByteArray(key);
            if (data == null) continue;
            boolean success = mKeyStore.put(key.getBytes(), data);
            Log.d(TAG, "install " + key + ": " + data.length + "  success? " + success);
            if (!success) return;
        }
        setResult(RESULT_OK);
    }
    private boolean isKeyStoreUnlocked() {
        return (mKeyStore.test() == KeyStore.NO_ERROR);
    }
}
