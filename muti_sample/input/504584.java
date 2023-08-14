public class BrowserHistoryPage extends ExpandableListActivity {
    private HistoryAdapter          mAdapter;
    private boolean                 mDisableNewWindow;
    private HistoryItem             mContextHeader;
    private final static String LOGTAG = "browser";
    private class IconReceiver implements IconListener {
        public void onReceivedIcon(String url, Bitmap icon) {
            setListAdapter(mAdapter);
        }
    }
    private final IconReceiver mIconReceiver = new IconReceiver();
    private void loadUrl(String url, boolean newWindow) {
        Intent intent = new Intent().setAction(url);
        if (newWindow) {
            Bundle b = new Bundle();
            b.putBoolean("new_window", true);
            intent.putExtras(b);
        }
        setResultToParent(RESULT_OK, intent);
        finish();
    }
    private void copy(CharSequence text) {
        try {
            IClipboard clip = IClipboard.Stub.asInterface(ServiceManager.getService("clipboard"));
            if (clip != null) {
                clip.setClipboardText(text);
            }
        } catch (android.os.RemoteException e) {
            Log.e(LOGTAG, "Copy failed", e);
        }
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setTitle(R.string.browser_history);
        final String whereClause = Browser.BookmarkColumns.VISITS + " > 0"
                + " AND " + Browser.BookmarkColumns.DATE + " > 0";
        final String orderBy = Browser.BookmarkColumns.DATE + " DESC";
        Cursor cursor = managedQuery(
                Browser.BOOKMARKS_URI,
                Browser.HISTORY_PROJECTION,
                whereClause, null, orderBy);
        mAdapter = new HistoryAdapter(this, cursor,
                Browser.HISTORY_PROJECTION_DATE_INDEX);
        setListAdapter(mAdapter);
        final ExpandableListView list = getExpandableListView();
        list.setOnCreateContextMenuListener(this);
        View v = new ViewStub(this, R.layout.empty_history);
        addContentView(v, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
        list.setEmptyView(v);
        if (list.getExpandableListAdapter().getGroupCount() > 0) {
            list.post(new Runnable() {
                public void run() {
                    if (list.getExpandableListAdapter().getGroupCount() > 0) {
                        list.expandGroup(0);
                    }
                }
            });
        }
        mDisableNewWindow = getIntent().getBooleanExtra("disable_new_window",
                false);
        CombinedBookmarkHistoryActivity.getIconListenerSet()
                .addListener(mIconReceiver);
        Activity parent = getParent();
        if (null == parent
                || !(parent instanceof CombinedBookmarkHistoryActivity)) {
            throw new AssertionError("history page can only be viewed as a tab"
                    + "in CombinedBookmarkHistoryActivity");
        }
        setResultToParent(RESULT_CANCELED, null);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        CombinedBookmarkHistoryActivity.getIconListenerSet()
                .removeListener(mIconReceiver);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.history, menu);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.clear_history_menu_id).setVisible(Browser.canClearHistory(this.getContentResolver()));
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.clear_history_menu_id:
                Browser.clearHistory(getContentResolver());
                ((CombinedBookmarkHistoryActivity) getParent())
                        .removeParentChildRelationShips();
                mAdapter.refreshData();
                return true;
            default:
                break;
        }  
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        ExpandableListContextMenuInfo i = 
            (ExpandableListContextMenuInfo) menuInfo;
        if (!(i.targetView instanceof HistoryItem)) {
            return;
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.historycontext, menu);
        HistoryItem historyItem = (HistoryItem) i.targetView;
        if (mContextHeader == null) {
            mContextHeader = new HistoryItem(this);
        } else if (mContextHeader.getParent() != null) {
            ((ViewGroup) mContextHeader.getParent()).removeView(mContextHeader);
        }
        historyItem.copyTo(mContextHeader);
        menu.setHeaderView(mContextHeader);
        if (mDisableNewWindow) {
            menu.findItem(R.id.new_window_context_menu_id).setVisible(false);
        }
        if (historyItem.isBookmark()) {
            MenuItem item = menu.findItem(R.id.save_to_bookmarks_menu_id);
            item.setTitle(R.string.remove_from_bookmarks);
        }
        PackageManager pm = getPackageManager();
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        ResolveInfo ri = pm.resolveActivity(send, PackageManager.MATCH_DEFAULT_ONLY);
        menu.findItem(R.id.share_link_context_menu_id).setVisible(ri != null);
        super.onCreateContextMenu(menu, v, menuInfo);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        ExpandableListContextMenuInfo i = 
            (ExpandableListContextMenuInfo) item.getMenuInfo();
        HistoryItem historyItem = (HistoryItem) i.targetView;
        String url = historyItem.getUrl();
        String title = historyItem.getName();
        switch (item.getItemId()) {
            case R.id.open_context_menu_id:
                loadUrl(url, false);
                return true;
            case R.id.new_window_context_menu_id:
                loadUrl(url, true);
                return true;
            case R.id.save_to_bookmarks_menu_id:
                if (historyItem.isBookmark()) {
                    Bookmarks.removeFromBookmarks(this, getContentResolver(),
                            url, title);
                } else {
                    Browser.saveBookmark(this, title, url);
                }
                return true;
            case R.id.share_link_context_menu_id:
                Browser.sendString(this, url,
                        getText(R.string.choosertitle_sharevia).toString());
                return true;
            case R.id.copy_url_context_menu_id:
                copy(url);
                return true;
            case R.id.delete_context_menu_id:
                Browser.deleteFromHistory(getContentResolver(), url);
                mAdapter.refreshData();
                return true;
            case R.id.homepage_context_menu_id:
                BrowserSettings.getInstance().setHomePage(this, url);
                Toast.makeText(this, R.string.homepage_set,
                    Toast.LENGTH_LONG).show();
                return true;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }
    @Override
    public boolean onChildClick(ExpandableListView parent, View v,
            int groupPosition, int childPosition, long id) {
        if (v instanceof HistoryItem) {
            loadUrl(((HistoryItem) v).getUrl(), false);
            return true;
        }
        return false;
    }
    private void setResultToParent(int resultCode, Intent data) {
        ((CombinedBookmarkHistoryActivity) getParent()).setResultFromChild(
                resultCode, data);
    }
    private class HistoryAdapter extends DateSortedExpandableListAdapter {
        HistoryAdapter(Context context, Cursor cursor, int index) {
            super(context, cursor, index);
        }
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            HistoryItem item;
            if (null == convertView || !(convertView instanceof HistoryItem)) {
                item = new HistoryItem(BrowserHistoryPage.this);
                item.setPadding(item.getPaddingLeft() + 10,
                        item.getPaddingTop(),
                        item.getPaddingRight(),
                        item.getPaddingBottom());
            } else {
                item = (HistoryItem) convertView;
            }
            if (!moveCursorToChildPosition(groupPosition, childPosition)) {
                return item;
            }
            item.setName(getString(Browser.HISTORY_PROJECTION_TITLE_INDEX));
            String url = getString(Browser.HISTORY_PROJECTION_URL_INDEX);
            item.setUrl(url);
            byte[] data = getBlob(Browser.HISTORY_PROJECTION_FAVICON_INDEX);
            if (data != null) {
                item.setFavicon(BitmapFactory.decodeByteArray(data, 0,
                        data.length));
            } else {
                item.setFavicon(CombinedBookmarkHistoryActivity
                        .getIconListenerSet().getFavicon(url));
            }
            item.setIsBookmark(1 ==
                    getInt(Browser.HISTORY_PROJECTION_BOOKMARK_INDEX));
            return item;
        }
    }
}
