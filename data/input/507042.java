public class BluetoothOppLiveFolder extends Activity {
    public static final Uri CONTENT_URI = Uri
            .parse("content:
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (LiveFolders.ACTION_CREATE_LIVE_FOLDER.equals(action)) {
            setResult(RESULT_OK, createLiveFolder(this, CONTENT_URI, this
                    .getString(R.string.btopp_live_folder), R.drawable.bt_share));
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }
    private static Intent createLiveFolder(Context context, Uri uri, String name, int icon) {
        final Intent intent = new Intent();
        intent.setData(uri);
        intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_BASE_INTENT, new Intent(
                Constants.ACTION_OPEN, BluetoothShare.CONTENT_URI));
        intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME, name);
        intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON, Intent.ShortcutIconResource
                .fromContext(context, icon));
        intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE, LiveFolders.DISPLAY_MODE_LIST);
        return intent;
    }
}
