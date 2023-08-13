public class FileCopyHelper {
    private Context mContext;
    private ArrayList<String> mFilesList;
    public FileCopyHelper(Context context) {
        mContext = context;
        mFilesList = new ArrayList<String>();
    }
    public String copy(int resId, String fileName) {
        InputStream source = null;
        OutputStream target = null;
        try {
            source = mContext.getResources().openRawResource(resId);
            target = mContext.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
            byte[] buffer = new byte[1024];
            for (int len = source.read(buffer); len > 0; len = source.read(buffer)) {
                target.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (source != null) {
                    source.close();
                }
                if (target != null) {
                    target.close();
                }
            } catch (IOException e) {
            }
        }
        mFilesList.add(fileName);
        return mContext.getFileStreamPath(fileName).getAbsolutePath();
    }
    public void clear(){
        for (String path : mFilesList) {
            mContext.deleteFile(path);
        }
    }
}
