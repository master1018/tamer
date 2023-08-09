public class NotesLiveFolder extends Activity {
    public static final Uri CONTENT_URI = Uri.parse("content:
            + NotePad.AUTHORITY + "/live_folders/notes");
    public static final Uri NOTE_URI = Uri.parse("content:
            + NotePad.AUTHORITY + "/notes/#");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final String action = intent.getAction();
        if (LiveFolders.ACTION_CREATE_LIVE_FOLDER.equals(action)) {
            final Intent liveFolderIntent = new Intent();
            liveFolderIntent.setData(CONTENT_URI);
            liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME,
                    getString(R.string.live_folder_name));
            liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON,
                    Intent.ShortcutIconResource.fromContext(this,
                            R.drawable.live_folder_notes));
            liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE,
                    LiveFolders.DISPLAY_MODE_LIST);
            liveFolderIntent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_BASE_INTENT,
                    new Intent(Intent.ACTION_EDIT, NOTE_URI));
            setResult(RESULT_OK, liveFolderIntent);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }
}
