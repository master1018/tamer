public class SettingsLicenseActivity extends AlertActivity {
    private static final String TAG = "SettingsLicenseActivity";
    private static final boolean LOGV = false || Config.LOGV;
    private static final String DEFAULT_LICENSE_PATH = "/system/etc/NOTICE.html.gz";
    private static final String PROPERTY_LICENSE_PATH = "ro.config.license_path";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String fileName = SystemProperties.get(PROPERTY_LICENSE_PATH, DEFAULT_LICENSE_PATH);
        if (TextUtils.isEmpty(fileName)) {
            Log.e(TAG, "The system property for the license file is empty.");
            showErrorAndFinish();
            return;
        }
        InputStreamReader inputReader = null;
        StringBuilder data = null;
        try {
            data = new StringBuilder(2048);
            char tmp[] = new char[2048];
            int numRead;
            if (fileName.endsWith(".gz")) {
                inputReader = new InputStreamReader(
                    new GZIPInputStream(new FileInputStream(fileName)));
            } else {
                inputReader = new FileReader(fileName);
            }
            while ((numRead = inputReader.read(tmp)) >= 0) {
                data.append(tmp, 0, numRead);
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "License HTML file not found at " + fileName, e);
            showErrorAndFinish();
            return;
        } catch (IOException e) {
            Log.e(TAG, "Error reading license HTML file at " + fileName, e);
            showErrorAndFinish();
            return;
        } finally {
            try {
                if (inputReader != null) {
                    inputReader.close();
                }
            } catch (IOException e) {
            }
        }
        if (TextUtils.isEmpty(data)) {
            Log.e(TAG, "License HTML is empty (from " + fileName + ")");
            showErrorAndFinish();
            return;
        }
        WebView webView = new WebView(this);
        webView.loadDataWithBaseURL(null, data.toString(), "text/html", "utf-8", null);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                mAlert.setTitle(getString(R.string.settings_license_activity_title));
            }
        });
        final AlertController.AlertParams p = mAlertParams;
        p.mTitle = getString(R.string.settings_license_activity_loading);
        p.mView = webView;
        p.mForceInverseBackground = true;
        setupAlert();
    }
    private void showErrorAndFinish() {
        Toast.makeText(this, R.string.settings_license_activity_unavailable, Toast.LENGTH_LONG)
                .show();
        finish();
    }
}
