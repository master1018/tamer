public class BluetoothOppTransferHistory extends Activity implements
        View.OnCreateContextMenuListener, OnItemClickListener {
    private static final String TAG = "BluetoothOppTransferHistory";
    private static final boolean D = Constants.DEBUG;
    private static final boolean V = Constants.VERBOSE;
    private ListView mListView;
    private Cursor mTransferCursor;
    private BluetoothOppTransferAdapter mTransferAdapter;
    private int mIdColumnId;
    private int mContextMenuPosition;
    private BluetoothOppNotification mNotifier;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.bluetooth_transfers_page);
        mListView = (ListView)findViewById(R.id.list);
        mListView.setEmptyView(findViewById(R.id.empty));
        String direction;
        int dir = getIntent().getIntExtra("direction", 0);
        if (dir == BluetoothShare.DIRECTION_OUTBOUND) {
            setTitle(getText(R.string.outbound_history_title));
            direction = "(" + BluetoothShare.DIRECTION + " == " + BluetoothShare.DIRECTION_OUTBOUND
                    + ")";
        } else {
            setTitle(getText(R.string.inbound_history_title));
            direction = "(" + BluetoothShare.DIRECTION + " == " + BluetoothShare.DIRECTION_INBOUND
                    + ")";
        }
        final String selection = BluetoothShare.STATUS + " >= '200' AND " + "("
                + BluetoothShare.VISIBILITY + " IS NULL OR " + BluetoothShare.VISIBILITY + " == '"
                + BluetoothShare.VISIBILITY_VISIBLE + "'" + ")" + " AND " + direction;
        final String sortOrder = BluetoothShare.TIMESTAMP + " DESC";
        mTransferCursor = managedQuery(BluetoothShare.CONTENT_URI, new String[] {
                "_id", BluetoothShare.FILENAME_HINT, BluetoothShare.STATUS,
                BluetoothShare.TOTAL_BYTES, BluetoothShare._DATA, BluetoothShare.TIMESTAMP,
                BluetoothShare.VISIBILITY, BluetoothShare.DESTINATION, BluetoothShare.DIRECTION
        }, selection, sortOrder);
        if (mTransferCursor != null) {
            mIdColumnId = mTransferCursor.getColumnIndexOrThrow(BluetoothShare._ID);
            mTransferAdapter = new BluetoothOppTransferAdapter(this,
                    R.layout.bluetooth_transfer_item, mTransferCursor);
            mListView.setAdapter(mTransferAdapter);
            mListView.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
            mListView.setOnCreateContextMenuListener(this);
            mListView.setOnItemClickListener(this);
        }
        mNotifier = new BluetoothOppNotification(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mTransferCursor != null) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.transferhistory, menu);
        }
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean showClear = getClearableCount() > 0;
        menu.findItem(R.id.transfer_menu_clear_all).setEnabled(showClear);
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.transfer_menu_clear_all:
                promptClearList();
                return true;
        }
        return false;
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        mTransferCursor.moveToPosition(mContextMenuPosition);
        switch (item.getItemId()) {
            case R.id.transfer_menu_open:
                openCompleteTransfer();
                updateNotificationWhenBtDisabled();
                return true;
            case R.id.transfer_menu_clear:
                int sessionId = mTransferCursor.getInt(mIdColumnId);
                Uri contentUri = Uri.parse(BluetoothShare.CONTENT_URI + "/" + sessionId);
                BluetoothOppUtility.updateVisibilityToHidden(this, contentUri);
                updateNotificationWhenBtDisabled();
                return true;
        }
        return false;
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (mTransferCursor != null) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            mTransferCursor.moveToPosition(info.position);
            mContextMenuPosition = info.position;
            String fileName = mTransferCursor.getString(mTransferCursor
                    .getColumnIndexOrThrow(BluetoothShare.FILENAME_HINT));
            if (fileName == null) {
                fileName = this.getString(R.string.unknown_file);
            }
            menu.setHeaderTitle(fileName);
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.transferhistorycontextfinished, menu);
        }
    }
    private void promptClearList() {
        new AlertDialog.Builder(this).setTitle(R.string.transfer_clear_dlg_title).setMessage(
                R.string.transfer_clear_dlg_msg).setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        clearAllDownloads();
                    }
                }).setNegativeButton(android.R.string.cancel, null).show();
    }
    private int getClearableCount() {
        int count = 0;
        if (mTransferCursor.moveToFirst()) {
            while (!mTransferCursor.isAfterLast()) {
                int statusColumnId = mTransferCursor.getColumnIndexOrThrow(BluetoothShare.STATUS);
                int status = mTransferCursor.getInt(statusColumnId);
                if (BluetoothShare.isStatusCompleted(status)) {
                    count++;
                }
                mTransferCursor.moveToNext();
            }
        }
        return count;
    }
    private void clearAllDownloads() {
        if (mTransferCursor.moveToFirst()) {
            while (!mTransferCursor.isAfterLast()) {
                int sessionId = mTransferCursor.getInt(mIdColumnId);
                Uri contentUri = Uri.parse(BluetoothShare.CONTENT_URI + "/" + sessionId);
                BluetoothOppUtility.updateVisibilityToHidden(this, contentUri);
                mTransferCursor.moveToNext();
            }
            updateNotificationWhenBtDisabled();
        }
    }
    public void onItemClick(AdapterView parent, View view, int position, long id) {
        mTransferCursor.moveToPosition(position);
        openCompleteTransfer();
        updateNotificationWhenBtDisabled();
    }
    private void openCompleteTransfer() {
        int sessionId = mTransferCursor.getInt(mIdColumnId);
        Uri contentUri = Uri.parse(BluetoothShare.CONTENT_URI + "/" + sessionId);
        BluetoothOppTransferInfo transInfo = new BluetoothOppTransferInfo();
        transInfo = BluetoothOppUtility.queryRecord(this, contentUri);
        if (transInfo == null) {
            Log.e(TAG, "Error: Can not get data from db");
            return;
        }
        if (transInfo.mDirection == BluetoothShare.DIRECTION_INBOUND
                && BluetoothShare.isStatusSuccess(transInfo.mStatus)) {
            BluetoothOppUtility.updateVisibilityToHidden(this, contentUri);
            BluetoothOppUtility.openReceivedFile(this, transInfo.mFileName, transInfo.mFileType,
                    transInfo.mTimeStamp, contentUri);
        } else {
            Intent in = new Intent(this, BluetoothOppTransferActivity.class);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            in.setData(contentUri);
            this.startActivity(in);
        }
    }
    private void updateNotificationWhenBtDisabled() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isEnabled()) {
            if (V) Log.v(TAG, "Bluetooth is not enabled, update notification manually.");
            mNotifier.updateNotification();
            mNotifier.finishNotification();
        }
    }
}
