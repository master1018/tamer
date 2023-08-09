public class CertFileList extends CertFile
        implements Preference.OnPreferenceClickListener {
    private static final String TAG = "CertFileList";
    private static final String DOWNLOAD_DIR = "download";
    private static final int MAX_FILE_SIZE = 1000000;
    private static final int REQUEST_INSTALL_CODE = 1;
    private SdCardMonitor mSdCardMonitor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pick_file_pref);
        createFileList();
        startSdCardMonitor();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopSdCardMonitor();
    }
    @Override
    protected void onInstallationDone(boolean fileDeleted) {
        if (!fileDeleted) {
            if (isSdCardPresent()) {
                setAllFilesEnabled(true);
            } else {
                Toast.makeText(this, R.string.sdcard_not_present,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
    @Override
    protected void onError(int errorId) {
        if (errorId == CERT_FILE_MISSING_ERROR) createFileList();
    }
    private void setAllFilesEnabled(boolean enabled) {
        PreferenceScreen root = getPreferenceScreen();
        for (int i = 0, n = root.getPreferenceCount(); i < n; i++) {
            root.getPreference(i).setEnabled(enabled);
        }
    }
    public boolean onPreferenceClick(Preference pref) {
        File file = new File(Environment.getExternalStorageDirectory(),
                pref.getTitle().toString());
        if (file.isDirectory()) {
            Log.w(TAG, "impossible to pick a directory! " + file);
        } else {
            setAllFilesEnabled(false);
            installFromFile(file);
        }
        return true;
    }
    private void createFileList() {
        if (isFinishing()) {
            Log.d(TAG, "finishing, exit createFileList()");
            return;
        } else if (!isSdCardPresent()) {
            Toast.makeText(this, R.string.sdcard_not_present,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        try {
            PreferenceScreen root = getPreferenceScreen();
            root.removeAll();
            List<File> allFiles = getAllCertFiles();
            if (allFiles.isEmpty()) {
                Toast.makeText(this, R.string.no_cert_file_found,
                        Toast.LENGTH_SHORT).show();
            } else {
                int prefixEnd = Environment.getExternalStorageDirectory()
                        .getCanonicalPath().length() + 1;
                for (File file : allFiles) {
                    Preference pref = new Preference(this);
                    pref.setTitle(file.getCanonicalPath().substring(prefixEnd));
                    root.addPreference(pref);
                    pref.setOnPreferenceClickListener(this);
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "createFileList(): " + e);
            throw new RuntimeException(e);
        }
    }
    private void startSdCardMonitor() {
        if (mSdCardMonitor == null) mSdCardMonitor = new SdCardMonitor();
        mSdCardMonitor.startWatching();
    }
    private void stopSdCardMonitor() {
        if (mSdCardMonitor != null) mSdCardMonitor.stopWatching();
    }
    private class SdCardMonitor {
        FileObserver mRootMonitor;
        FileObserver mDownloadMonitor;
        SdCardMonitor() {
            File root = Environment.getExternalStorageDirectory();
            mRootMonitor = new FileObserver(root.getPath()) {
                @Override
                public void onEvent(int evt, String path) {
                    commonHandler(evt, path);
                }
            };
            File download = new File(root, DOWNLOAD_DIR);
            mDownloadMonitor = new FileObserver(download.getPath()) {
                @Override
                public void onEvent(int evt, String path) {
                    commonHandler(evt, path);
                }
            };
        }
        private void commonHandler(int evt, String path) {
            switch (evt) {
                case FileObserver.CREATE:
                case FileObserver.DELETE:
                    if (isFileAcceptable(path)) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                createFileList();
                            }
                        });
                    }
                    break;
            }
        };
        void startWatching() {
            mRootMonitor.startWatching();
            mDownloadMonitor.startWatching();
        }
        void stopWatching() {
            mRootMonitor.stopWatching();
            mDownloadMonitor.stopWatching();
        }
    }
}
