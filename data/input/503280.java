public class MyRssReader5 extends Activity implements OnItemSelectedListener {
    private ListView mRssList;
    private Cursor mCur;
    private RssCursorAdapter mAdap;
    private WebView mWebView;
    private static final int ADD_ELEMENT_REQUEST = 1;
    private Logger mLogger = Logger.getLogger(this.getPackageName());
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen2);
        mRssList = (ListView)findViewById(R.id.rssListView);
        mRssList.setOnItemSelectedListener(this);
        mWebView = (WebView)findViewById(R.id.rssWebView);
        mCur = managedQuery(RssContentProvider.CONTENT_URI, 
                       null, 
                       null, 
                       RssContentProvider.DEFAULT_SORT_ORDER);
        mAdap = new RssCursorAdapter(
                this,
                R.layout.list_element,                  
                mCur, 
                new String[]{RssContentProvider.TITLE}, 
                new int[]{R.id.list_item});             
        mRssList.setAdapter(mAdap);                    
        if(savedInstanceState != null && savedInstanceState.containsKey("lastIndexItem")){
            mRssList.setSelection(savedInstanceState.getInteger("lastIndexItem"));
        }
    }
    public void onItemSelected(AdapterView parent, View v, int position, long id){
        String content = "";
        try{
            content = mCur.getString(mCur.getColumnIndex(RssContentProvider.CONTENT));
            mLogger.info("MyRssReader5 content string:" + content);
        }
        catch (Exception e){
            mLogger.warning("MyRssReader5.onItemSelected() couldn't get the content" +
                            "from the cursor " + e.getMessage()); 
        }
        mWebView.loadData(content, "text/html", "utf-8");
    }
    public void onNothingSelected(AdapterView parent){
        mWebView.loadData("<html><body><p>No selection chosen</p></body></html>", "text/html", "utf-8");   
    }
    @Override
    protected void onSaveInstanceState(Bundle outState){
        int index = mRssList.getSelectedItemIndex();
        if(index > -1){
            outState.putInteger("lastIndexItem", index);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, R.string.menu_option_start, null);
        menu.add(0, 1, R.string.menu_option_stop, null);
        menu.add(0, 2, R.string.menu_option_add, null);
        menu.add(0, 3, R.string.menu_option_delete, null);
        menu.add(0, 4, R.string.menu_option_update, null);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(Menu.Item item){
        super.onOptionsItemSelected(item);
        switch (item.getId()){
            case 0:     
                Intent basicStartIntent = new Intent(RssService.class);
                startService(basicStartIntent);
                break;
            case 1:    
                Intent stopIntent = new Intent(RssService.class);
                stopService(stopIntent);
                break;
            case 2:     
                Intent addIntent = new Intent(AddRssItem.class);
                startActivityForResult(addIntent, ADD_ELEMENT_REQUEST); 
                break;                       
            case 3:     
                if(mRssList.hasFocus()){
                    int currentSelectionIndex = mRssList.getSelectedItemIndex();
                    mLogger.info("MyRssReader5.onOptionsItemSelected(): Deleting list member:" + 
                            mRssList.getSelectedItemIndex());
                    Long itemID = mAdap.getItemId(currentSelectionIndex);
                    getContentResolver().delete(RssContentProvider.CONTENT_URI.addId(itemID), null);
                }
                break;
            case 4:     
                Bundle startupVals = new Bundle(1);
                startupVals.putBoolean(RssService.REQUERY_KEY, true);
                Intent requeryIntent = new Intent(RssService.class);
                startService(requeryIntent, startupVals);
                break;
            default:
                showAlert(null, "I have no idea what you clicked!", "ok", null, false, null);
                break;
        }
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case ADD_ELEMENT_REQUEST:
                      ContentValues vals = new ContentValues(5);
                      vals.put(RssContentProvider.TITLE, data.getStringExtra(RssContentProvider.TITLE));
                      vals.put(RssContentProvider.URL, data.getStringExtra(RssContentProvider.URL));
                      vals.put(RssContentProvider.CONTENT, data.getStringExtra(RssContentProvider.CONTENT));
                      vals.put(RssContentProvider.LAST_UPDATED, data.getIntExtra(RssContentProvider.LAST_UPDATED, 0));
                      Uri uri = getContentResolver().insert(
                              RssContentProvider.CONTENT_URI, 
                              vals);
                      if(uri != null){
                          Bundle startupVals = new Bundle(1);
                          startupVals.putString(RssService.RSS_URL, data.getStringExtra("URL"));
                          Intent startIntent = new Intent(RssService.class);
                          startIntent.putExtras(startupVals);
                          startService(startIntent);
                          mRssList.setSelection(mRssList.getCount() - 1);
                      }
                    break;
                default:
                    break;
            }
        }
    }
    private class RssCursorAdapter extends SimpleCursorAdapter {
        public RssCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            TextView view = (TextView)super.getView(position, convertView, parent);
            if(view != null){
                int hasBeenReadColumnIndex = getCursor().getColumnIndex(RssContentProvider.HAS_BEEN_READ);
                boolean hasBeenRead = (getCursor().getInt(hasBeenReadColumnIndex) == 1 ? true : false);
                if(! hasBeenRead){
                    Typeface type = view.getTypeface();
                    view.setTypeface(Typeface.create(type, Typeface.BOLD_ITALIC));
                }
            }
            return view;
        } 
    }
}
