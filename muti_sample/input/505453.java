public class CertInstallerMain extends CertFile implements Runnable {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) return;
        new Thread(new Runnable() {
            public void run() {
                runOnUiThread(CertInstallerMain.this);
            }
        }).start();
    }
    public void run() {
        Intent intent = getIntent();
        String action = (intent == null) ? null : intent.getAction();
        if (Credentials.INSTALL_ACTION.equals(action)) {
            Bundle bundle = intent.getExtras();
            if ((bundle == null) || bundle.isEmpty()) {
                if (!isSdCardPresent()) {
                    Toast.makeText(this, R.string.sdcard_not_present,
                            Toast.LENGTH_SHORT).show();
                } else {
                    List<File> allFiles = getAllCertFiles();
                    if (allFiles.isEmpty()) {
                        Toast.makeText(this, R.string.no_cert_file_found,
                                Toast.LENGTH_SHORT).show();
                    } else if (allFiles.size() == 1) {
                        installFromFile(allFiles.get(0));
                        return;
                    } else {
                        startActivity(new Intent(this, CertFileList.class));
                    }
                }
            } else {
                Intent newIntent = new Intent(this, CertInstaller.class);
                newIntent.putExtras(intent);
                startActivity(newIntent);
            }
        }
        finish();
    }
    @Override
    protected void onInstallationDone(boolean success) {
        finish();
    }
    @Override
    protected void onError(int errorId) {
        finish();
    }
}
