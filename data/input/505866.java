public class LauncherShortcuts extends Activity {
    private static final String EXTRA_KEY = "com.example.android.apis.app.LauncherShortcuts";
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
            setupShortcut();
            finish();
            return;
        }
        setContentView(R.layout.launcher_shortcuts);
        TextView intentInfo = (TextView) findViewById(R.id.txt_shortcut_intent);
        String info = intent.toString();
        String extra = intent.getStringExtra(EXTRA_KEY);
        if (extra != null) {
            info = info + " " + extra;
        }
        intentInfo.setText(info);
    }
    private void setupShortcut() {
        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(this, this.getClass().getName());
        shortcutIntent.putExtra(EXTRA_KEY, "ApiDemos Provided This Shortcut");
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.shortcut_name));
        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(
                this,  R.drawable.app_sample_code);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
        setResult(RESULT_OK, intent);
    }
}
