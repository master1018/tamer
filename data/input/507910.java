public class Plugin {
    public interface PreferencesClickHandler {
        public void handleClickEvent(Context context);
    }
    private String mName;
    private String mPath;
    private String mFileName;
    private String mDescription;
    private PreferencesClickHandler mHandler;
    @Deprecated
    public Plugin(String name,
                  String path,
                  String fileName,
                  String description) {
        mName = name;
        mPath = path;
        mFileName = fileName;
        mDescription = description;
        mHandler = new DefaultClickHandler();
    }
    @Deprecated
    public String toString() {
        return mName;
    }
    @Deprecated
    public String getName() {
        return mName;
    }
    @Deprecated
    public String getPath() {
        return mPath;
    }
    @Deprecated
    public String getFileName() {
        return mFileName;
    }
    @Deprecated
    public String getDescription() {
        return mDescription;
    }
    @Deprecated
    public void setName(String name) {
        mName = name;
    }
    @Deprecated
    public void setPath(String path) {
        mPath = path;
    }
    @Deprecated
    public void setFileName(String fileName) {
        mFileName = fileName;
    }
    @Deprecated
    public void setDescription(String description) {
        mDescription = description;
    }
    @Deprecated
    public void setClickHandler(PreferencesClickHandler handler) {
        mHandler = handler;
    }
    @Deprecated
    public void dispatchClickEvent(Context context) {
        if (mHandler != null) {
            mHandler.handleClickEvent(context);
        }
    }
    @Deprecated
    private class DefaultClickHandler implements PreferencesClickHandler,
                                                 DialogInterface.OnClickListener {
        private AlertDialog mDialog;
        @Deprecated
        public void handleClickEvent(Context context) {
            if (mDialog == null) {
                mDialog = new AlertDialog.Builder(context)
                        .setTitle(mName)
                        .setMessage(mDescription)
                        .setPositiveButton(R.string.ok, this)
                        .setCancelable(false)
                        .show();
            }
        }
        @Deprecated
        public void onClick(DialogInterface dialog, int which) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
