public class LowStorageTest extends Activity {
    static final String TAG = "DiskFullTest";
    static final long WAIT_FOR_FINISH = 5 * 60 * 60;
    static final int NO_OF_BLOCKS_TO_FILL = 1000;
    static final int BYTE_SIZE = 1024;
    static final int WAIT_FOR_SYSTEM_UPDATE = 10000;
    private int mBlockSize = 0;
    private final Object fillUpDone = new Object();
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        int totalBlocks = stat.getBlockCount();
        mBlockSize = (int) (stat.getBlockSize());
        TextView startSizeTextView = (TextView) findViewById(R.id.totalsize);
        startSizeTextView.setText(Long.toString((totalBlocks * mBlockSize) / BYTE_SIZE));
        Button button = (Button) findViewById(R.id.button_run);
        button.setOnClickListener(mStartListener);
    }
    View.OnClickListener mStartListener = new OnClickListener() {
        public void onClick(View v) {
            fillDataAndUpdateInfo();
        }
    };
    public void fillDataAndUpdateInfo() {
        updateInfo(this);
    }
    public void fillupdisk(Context context) {
        final Context contextfill = context;
        new Thread() {
            @Override
            public void run() {
                try {
                    File path = Environment.getDataDirectory();
                    StatFs stat = new StatFs(path.getPath());
                    int totalBlocks = stat.getBlockCount();
                    int noOfBlockToFill = stat.getAvailableBlocks();
                    FileOutputStream fs =
                            contextfill.openFileOutput("testdata", Context.MODE_APPEND);
                    for (int i = 0; i < (noOfBlockToFill / NO_OF_BLOCKS_TO_FILL); i++) {
                        byte buf[] = new byte[mBlockSize * NO_OF_BLOCKS_TO_FILL];
                        fs.write(buf);
                        fs.flush();
                    }
                    byte buf[] = new byte[(noOfBlockToFill % NO_OF_BLOCKS_TO_FILL) * mBlockSize];
                    fs.write(buf);
                    fs.flush();
                    fs.close();
                    synchronized (fillUpDone) {
                        fillUpDone.notify();
                    }
                } catch (Exception e) {
                    Log.v(TAG, e.toString());
                }
            }
        }.start();
    }
    public void updateInfo(Context context) {
        fillupdisk(this);
        synchronized (fillUpDone) {
            try {
                fillUpDone.wait(WAIT_FOR_FINISH);
            } catch (Exception e) {
                Log.v(TAG, "wait was interrupted.");
            }
        }
        try {
            Thread.sleep(WAIT_FOR_SYSTEM_UPDATE);
            File path = Environment.getDataDirectory();
            StatFs stat = new StatFs(path.getPath());
            long availableBlocks = stat.getAvailableBlocks();
            TextView freeSizeTextView = (TextView) findViewById(R.id.freesize);
            freeSizeTextView.setText(Long.toString((availableBlocks * mBlockSize) / BYTE_SIZE));
            TextView statusTextView = (TextView) findViewById(R.id.status);
            statusTextView.setText("Finished. You can start the test now.");
        } catch (Exception e) {
            Log.v(TAG, e.toString());
        }
    }
}
