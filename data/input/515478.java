public class ExternalStorage extends Activity {
    ViewGroup mLayout;
    static class Item {
        View mRoot;
        Button mCreate;
        Button mDelete;
    }
    Item mExternalStoragePublicPicture;
    Item mExternalStoragePrivatePicture;
    Item mExternalStoragePrivateFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.external_storage);
        mLayout = (ViewGroup)findViewById(R.id.layout);
        mExternalStoragePublicPicture = createStorageControls(
                "Picture: getExternalStoragePublicDirectory",
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES),
                new View.OnClickListener() {
                    public void onClick(View v) {
                        createExternalStoragePublicPicture();
                        updateExternalStorageState();
                    }
                },
                new View.OnClickListener() {
                    public void onClick(View v) {
                        deleteExternalStoragePublicPicture();
                        updateExternalStorageState();
                    }
                });
        mLayout.addView(mExternalStoragePublicPicture.mRoot);
        mExternalStoragePrivatePicture = createStorageControls(
                "Picture getExternalFilesDir",
                getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                new View.OnClickListener() {
                    public void onClick(View v) {
                        createExternalStoragePrivatePicture();
                        updateExternalStorageState();
                    }
                },
                new View.OnClickListener() {
                    public void onClick(View v) {
                        deleteExternalStoragePrivatePicture();
                        updateExternalStorageState();
                    }
                });
        mLayout.addView(mExternalStoragePrivatePicture.mRoot);
        mExternalStoragePrivateFile = createStorageControls(
                "File getExternalFilesDir",
                getExternalFilesDir(null),
                new View.OnClickListener() {
                    public void onClick(View v) {
                        createExternalStoragePrivateFile();
                        updateExternalStorageState();
                    }
                },
                new View.OnClickListener() {
                    public void onClick(View v) {
                        deleteExternalStoragePrivateFile();
                        updateExternalStorageState();
                    }
                });
        mLayout.addView(mExternalStoragePrivateFile.mRoot);
        startWatchingExternalStorage();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopWatchingExternalStorage();
    }
    void handleExternalStorageState(boolean available, boolean writeable) {
        boolean has = hasExternalStoragePublicPicture();
        mExternalStoragePublicPicture.mCreate.setEnabled(writeable && !has);
        mExternalStoragePublicPicture.mDelete.setEnabled(writeable && has);
        has = hasExternalStoragePrivatePicture();
        mExternalStoragePrivatePicture.mCreate.setEnabled(writeable && !has);
        mExternalStoragePrivatePicture.mDelete.setEnabled(writeable && has);
        has = hasExternalStoragePrivateFile();
        mExternalStoragePrivateFile.mCreate.setEnabled(writeable && !has);
        mExternalStoragePrivateFile.mDelete.setEnabled(writeable && has);
    }
    BroadcastReceiver mExternalStorageReceiver;
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;
    void updateExternalStorageState() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
        handleExternalStorageState(mExternalStorageAvailable,
                mExternalStorageWriteable);
    }
    void startWatchingExternalStorage() {
        mExternalStorageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("test", "Storage: " + intent.getData());
                updateExternalStorageState();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        registerReceiver(mExternalStorageReceiver, filter);
        updateExternalStorageState();
    }
    void stopWatchingExternalStorage() {
        unregisterReceiver(mExternalStorageReceiver);
    }
    void createExternalStoragePublicPicture() {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File file = new File(path, "DemoPicture.jpg");
        try {
            path.mkdirs();
            InputStream is = getResources().openRawResource(R.drawable.balloons);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
            MediaScannerConnection.scanFile(this,
                    new String[] { file.toString() }, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                }
            });
        } catch (IOException e) {
            Log.w("ExternalStorage", "Error writing " + file, e);
        }
    }
    void deleteExternalStoragePublicPicture() {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File file = new File(path, "DemoPicture.jpg");
        file.delete();
    }
    boolean hasExternalStoragePublicPicture() {
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File file = new File(path, "DemoPicture.jpg");
        return file.exists();
    }
    void createExternalStoragePrivatePicture() {
        File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(path, "DemoPicture.jpg");
        try {
            InputStream is = getResources().openRawResource(R.drawable.balloons);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data);
            os.write(data);
            is.close();
            os.close();
            MediaScannerConnection.scanFile(this,
                    new String[] { file.toString() }, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    Log.i("ExternalStorage", "Scanned " + path + ":");
                    Log.i("ExternalStorage", "-> uri=" + uri);
                }
            });
        } catch (IOException e) {
            Log.w("ExternalStorage", "Error writing " + file, e);
        }
    }
    void deleteExternalStoragePrivatePicture() {
        File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (path != null) {
            File file = new File(path, "DemoPicture.jpg");
            file.delete();
        }
    }
    boolean hasExternalStoragePrivatePicture() {
        File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (path != null) {
            File file = new File(path, "DemoPicture.jpg");
            return file.exists();
        }
        return false;
    }
     void createExternalStoragePrivateFile() {
         File file = new File(getExternalFilesDir(null), "DemoFile.jpg");
         try {
             InputStream is = getResources().openRawResource(R.drawable.balloons);
             OutputStream os = new FileOutputStream(file);
             byte[] data = new byte[is.available()];
             is.read(data);
             os.write(data);
             is.close();
             os.close();
         } catch (IOException e) {
             Log.w("ExternalStorage", "Error writing " + file, e);
         }
     }
     void deleteExternalStoragePrivateFile() {
         File file = new File(getExternalFilesDir(null), "DemoFile.jpg");
         if (file != null) {
             file.delete();
         }
     }
     boolean hasExternalStoragePrivateFile() {
         File file = new File(getExternalFilesDir(null), "DemoFile.jpg");
         if (file != null) {
             return file.exists();
         }
         return false;
     }
    Item createStorageControls(CharSequence label, File path,
            View.OnClickListener createClick,
            View.OnClickListener deleteClick) {
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        Item item = new Item();
        item.mRoot = inflater.inflate(R.layout.external_storage_item, null);
        TextView tv = (TextView)item.mRoot.findViewById(R.id.label);
        tv.setText(label);
        if (path != null) {
            tv = (TextView)item.mRoot.findViewById(R.id.path);
            tv.setText(path.toString());
        }
        item.mCreate = (Button)item.mRoot.findViewById(R.id.create);
        item.mCreate.setOnClickListener(createClick);
        item.mDelete = (Button)item.mRoot.findViewById(R.id.delete);
        item.mDelete.setOnClickListener(deleteClick);
        return item;
    }
}
