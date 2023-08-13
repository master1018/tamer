public class BrowserDownloadPage extends ExpandableListActivity {
    private ExpandableListView      mListView;
    private Cursor                  mDownloadCursor;
    private BrowserDownloadAdapter  mDownloadAdapter;
    private int                     mStatusColumnId;
    private int                     mIdColumnId;
    private int                     mTitleColumnId;
    private long                    mContextMenuPosition;
    private ContentObserver         mContentObserver;
    private View                    mSelectedView;
    private final static String LOGTAG = "BrowserDownloadPage";
    @Override 
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.browser_downloads_page);
        setTitle(getText(R.string.download_title));
        mListView = (ExpandableListView) findViewById(android.R.id.list);
        mListView.setEmptyView(findViewById(R.id.empty));
        mDownloadCursor = managedQuery(Downloads.Impl.CONTENT_URI,
                new String [] {Downloads.Impl._ID, Downloads.Impl.COLUMN_TITLE,
                Downloads.Impl.COLUMN_STATUS, Downloads.Impl.COLUMN_TOTAL_BYTES,
                Downloads.Impl.COLUMN_CURRENT_BYTES,
                Downloads.Impl.COLUMN_DESCRIPTION,
                Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE,
                Downloads.Impl.COLUMN_LAST_MODIFICATION,
                Downloads.Impl.COLUMN_VISIBILITY,
                Downloads.Impl._DATA,
                Downloads.Impl.COLUMN_MIME_TYPE},
                null, Downloads.Impl.COLUMN_LAST_MODIFICATION + " DESC");
        if (mDownloadCursor != null) {
            mStatusColumnId = 
                    mDownloadCursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_STATUS);
            mIdColumnId =
                    mDownloadCursor.getColumnIndexOrThrow(Downloads.Impl._ID);
            mTitleColumnId = 
                    mDownloadCursor.getColumnIndexOrThrow(Downloads.Impl.COLUMN_TITLE);
            mDownloadAdapter = new BrowserDownloadAdapter(this, 
                    mDownloadCursor, mDownloadCursor.getColumnIndexOrThrow(
                    Downloads.Impl.COLUMN_LAST_MODIFICATION));
            setListAdapter(mDownloadAdapter);
            mListView.setOnCreateContextMenuListener(this);
            Intent intent = getIntent();
            final int groupToShow = intent == null || intent.getData() == null
                    ? 0 : checkStatus(ContentUris.parseId(intent.getData()));
            if (mDownloadAdapter.getGroupCount() > groupToShow) {
                mListView.post(new Runnable() {
                    public void run() {
                        if (mDownloadAdapter.getGroupCount() > groupToShow) {
                            mListView.expandGroup(groupToShow);
                        }
                    }
                });
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (mDownloadCursor != null) {
            String where = null;
            for (mDownloadCursor.moveToFirst(); !mDownloadCursor.isAfterLast();
                    mDownloadCursor.moveToNext()) {
                if (!Downloads.Impl.isStatusCompleted(
                        mDownloadCursor.getInt(mStatusColumnId))) {
                    continue;
                }
                int filenameColumnId = mDownloadCursor.getColumnIndexOrThrow(
                        Downloads.Impl._DATA);
                String filename = mDownloadCursor.getString(filenameColumnId);
                if (filename != null) {
                    File file = new File(filename);
                    if (!file.exists()) {
                        long id = mDownloadCursor.getLong(mIdColumnId);
                        if (where == null) {
                            where = Downloads.Impl._ID + " = '" + id + "'";
                        } else {
                            where += " OR " + Downloads.Impl._ID + " = '" + id
                                    + "'";
                        }
                    }
                }
            }
            if (where != null) {
                getContentResolver().delete(Downloads.Impl.CONTENT_URI, where,
                        null);
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mDownloadCursor != null) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.downloadhistory, menu);
        }
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean showCancel = getCancelableCount() > 0;
        menu.findItem(R.id.download_menu_cancel_all).setEnabled(showCancel);
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.download_menu_cancel_all:
                promptCancelAll();
                return true;
        }
        return false;
    }
    private void clearFromDownloads(long id) {
        getContentResolver().delete(ContentUris.withAppendedId(
                Downloads.Impl.CONTENT_URI, id), null, null);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (!mDownloadAdapter.moveCursorToPackedChildPosition(
                mContextMenuPosition)) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.download_menu_open:
                hideCompletedDownload();
                openOrDeleteCurrentDownload(false);
                return true;
            case R.id.download_menu_delete:
                new AlertDialog.Builder(this)
                        .setTitle(R.string.download_delete_file)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setMessage(mDownloadCursor.getString(mTitleColumnId))
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int whichButton) {
                                        openOrDeleteCurrentDownload(true);
                                    }
                                })
                        .show();
                break;
            case R.id.download_menu_clear:
            case R.id.download_menu_cancel:
                clearFromDownloads(mDownloadCursor.getLong(mIdColumnId));
                return true;
        }
        return false;
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (mContentObserver != null) {
            getContentResolver().unregisterContentObserver(mContentObserver);
        }
    }
    private class ChangeObserver extends ContentObserver {
        private final Uri mTrack;
        public ChangeObserver(Uri track) {
            super(new Handler());
            mTrack = track;
        }
        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }
        @Override
        public void onChange(boolean selfChange) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(mTrack,
                        new String[] { Downloads.Impl.COLUMN_STATUS }, null, null,
                        null);
                if (cursor.moveToFirst() && Downloads.Impl.isStatusSuccess(
                        cursor.getInt(0))) {
                    getContentResolver().unregisterContentObserver(
                            mContentObserver);
                    mListView.post(new Runnable() {
                        public void run() {
                            closeContextMenu();
                            openContextMenu(mSelectedView);
                        }
                    });
                }
            } catch (IllegalStateException e) {
                Log.e(LOGTAG, "onChange", e);
            } finally {
                if (cursor != null) cursor.close();
            }
        }
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        if (mDownloadCursor != null) {
            ExpandableListView.ExpandableListContextMenuInfo info
                    = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
            long packedPosition = info.packedPosition;
            if (!mDownloadAdapter.moveCursorToPackedChildPosition(
                    packedPosition)) {
                return;
            }
            mContextMenuPosition = packedPosition;
            menu.setHeaderTitle(mDownloadCursor.getString(mTitleColumnId));
            MenuInflater inflater = getMenuInflater();
            int status = mDownloadCursor.getInt(mStatusColumnId);
            if (Downloads.Impl.isStatusSuccess(status)) {
                inflater.inflate(R.menu.downloadhistorycontextfinished, menu);
            } else if (Downloads.Impl.isStatusError(status)) {
                inflater.inflate(R.menu.downloadhistorycontextfailed, menu);
            } else {
                Uri track = ContentUris.withAppendedId(
                        Downloads.Impl.CONTENT_URI,
                        mDownloadCursor.getLong(mIdColumnId));
                if (mContentObserver != null) {
                    getContentResolver().unregisterContentObserver(
                            mContentObserver);
                }
                mContentObserver = new ChangeObserver(track);
                mSelectedView = v;
                getContentResolver().registerContentObserver(track, false,
                        mContentObserver);
                inflater.inflate(R.menu.downloadhistorycontextrunning, menu);
            }
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }
    private int checkStatus(final long id) {
        int groupToShow = mDownloadAdapter.groupFromChildId(id);
        if (-1 == groupToShow) return 0;
        int status = mDownloadCursor.getInt(mStatusColumnId);
        if (!Downloads.Impl.isStatusError(status)) {
            return groupToShow;
        }
        if (status == Downloads.Impl.STATUS_FILE_ERROR) {
            String title = mDownloadCursor.getString(mTitleColumnId);
            if (title == null || title.length() == 0) {
                title = getString(R.string.download_unknown_filename);
            }
            String msg = getString(R.string.download_file_error_dlg_msg, title);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.download_file_error_dlg_title)
                    .setIcon(android.R.drawable.ic_popup_disk_full)
                    .setMessage(msg)
                    .setPositiveButton(R.string.ok, null)
                    .setNegativeButton(R.string.retry,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int whichButton) {
                                    resumeDownload(id);
                                }
                            })
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.download_failed_generic_dlg_title)
                    .setIcon(R.drawable.ssl_icon)
                    .setMessage(BrowserDownloadAdapter.getErrorText(status))
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }
        return groupToShow;
    }
    private void resumeDownload(final long id) {
    }
    private int getCancelableCount() {
        int count = 0;
        if (mDownloadCursor != null) {
            for (mDownloadCursor.moveToFirst(); !mDownloadCursor.isAfterLast();
                    mDownloadCursor.moveToNext()) {
                int status = mDownloadCursor.getInt(mStatusColumnId);
                if (!Downloads.Impl.isStatusCompleted(status)) {
                    count++;
                }
            }
        }
        return count;
    }
    private void promptCancelAll() {
        int count = getCancelableCount();
        if (count == 0) {
            return;
        }
        if (count == 1) {
            cancelAllDownloads();
            return;
        }
        String msg = 
            getString(R.string.download_cancel_dlg_msg, count);
        new AlertDialog.Builder(this)
                .setTitle(R.string.download_cancel_dlg_title)
                .setIcon(R.drawable.ssl_icon)
                .setMessage(msg)
                .setPositiveButton(R.string.ok, 
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, 
                                    int whichButton) {
                                cancelAllDownloads();
                            }
                        })
                 .setNegativeButton(R.string.cancel, null)
                 .show();
    }
    private void cancelAllDownloads() {
        if (mDownloadCursor.moveToFirst()) {
            StringBuilder where = new StringBuilder();
            boolean firstTime = true;
            while (!mDownloadCursor.isAfterLast()) {
                int status = mDownloadCursor.getInt(mStatusColumnId);
                if (!Downloads.Impl.isStatusCompleted(status)) {
                    if (firstTime) {
                        firstTime = false;
                    } else {
                        where.append(" OR ");
                    }
                    where.append("( ");
                    where.append(Downloads.Impl._ID);
                    where.append(" = '");
                    where.append(mDownloadCursor.getLong(mIdColumnId));
                    where.append("' )");
                }
                mDownloadCursor.moveToNext();
            }
            if (!firstTime) {
                getContentResolver().delete(Downloads.Impl.CONTENT_URI,
                        where.toString(), null);
            }
        }
    }
    private int getClearableCount() {
        int count = 0;
        if (mDownloadCursor.moveToFirst()) {
            while (!mDownloadCursor.isAfterLast()) {
                int status = mDownloadCursor.getInt(mStatusColumnId);
                if (Downloads.Impl.isStatusCompleted(status)) {
                    count++;
                }
                mDownloadCursor.moveToNext();
            }
        }
        return count;
    }
    private void openOrDeleteCurrentDownload(boolean delete) {
        int packageColumnId = mDownloadCursor.getColumnIndexOrThrow(
                Downloads.Impl.COLUMN_NOTIFICATION_PACKAGE);
        String packageName = mDownloadCursor.getString(packageColumnId);
        Intent intent = new Intent(delete ? Intent.ACTION_DELETE
                : Downloads.Impl.ACTION_NOTIFICATION_CLICKED);
        Uri contentUri = ContentUris.withAppendedId(
                Downloads.Impl.CONTENT_URI,
                mDownloadCursor.getLong(mIdColumnId));
        intent.setData(contentUri);
        intent.setPackage(packageName);
        sendBroadcast(intent);
    }
    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
            int groupPosition, int childPosition, long id) {
        mDownloadAdapter.moveCursorToChildPosition(groupPosition,
                childPosition);
        hideCompletedDownload();
        int status = mDownloadCursor.getInt(mStatusColumnId);
        if (Downloads.Impl.isStatusSuccess(status)) {
            openOrDeleteCurrentDownload(false);
        } else {
            checkStatus(id);
        }
        return true;
    }
    private void hideCompletedDownload() {
        int status = mDownloadCursor.getInt(mStatusColumnId);
        int visibilityColumn = mDownloadCursor.getColumnIndexOrThrow(
                Downloads.Impl.COLUMN_VISIBILITY);
        int visibility = mDownloadCursor.getInt(visibilityColumn);
        if (Downloads.Impl.isStatusCompleted(status) &&
                visibility == Downloads.Impl.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) {
            ContentValues values = new ContentValues();
            values.put(Downloads.Impl.COLUMN_VISIBILITY, Downloads.Impl.VISIBILITY_VISIBLE);
            getContentResolver().update(
                    ContentUris.withAppendedId(Downloads.Impl.CONTENT_URI,
                    mDownloadCursor.getLong(mIdColumnId)), values, null, null);
        }
    }
}
