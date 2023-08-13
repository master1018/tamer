public class MyRssReader4 extends Activity {
    ListView mRssList;
    Cursor mCur;
    RssCursorAdapter mAdap;
    private static final int ADD_ELEMENT_REQUEST = 1;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen2);
        mRssList = (ListView)findViewById(R.id.rssListView);
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
                showAlert(null, "You clicked 'start'!", "ok", null, false, null);
                break;
            case 1:    
              showAlert(null, "You clicked stop!", "ok", null, false, null);
              break;                    
            case 2:     
                Intent addIntent = new Intent(AddRssItem.class);
                startActivityForResult(addIntent, ADD_ELEMENT_REQUEST); 
                break; 
            case 3:     
                if(mRssList.hasFocus()){
                  int currentSelectionIndex = mRssList.getSelectedItemIndex();
                  Long itemID = mAdap.getItemId(currentSelectionIndex);
                  getContentResolver().delete(RssContentProvider.CONTENT_URI.addId(itemID), null);
                }
                break;
            case 4:    
                showAlert(null, "You clicked 'Update'!", "ok", null, false, null);
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
                    ContentValues vals = new ContentValues(4);
                    vals.put(RssContentProvider.TITLE, data.getStringExtra(RssContentProvider.TITLE));
                    vals.put(RssContentProvider.URL, data.getStringExtra(RssContentProvider.URL));
                    vals.put(RssContentProvider.CONTENT, data.getStringExtra(RssContentProvider.CONTENT));
                    vals.put(RssContentProvider.LAST_UPDATED, data.getIntExtra(RssContentProvider.LAST_UPDATED, 0));
                    Uri uri = getContentResolver().insert(
                            RssContentProvider.CONTENT_URI, 
                            vals);
                        if(uri != null){
                            mRssList.setSelection(mRssList.getCount() - 1);
                        }
                    break;
                default:
                    break;
            }
        }
    }
    private class RssCursorAdapter extends SimpleCursorAdapter {
        public RssCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to){
            super(context, layout, c, from, to);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            TextView view = (TextView)super.getView(position, convertView, parent);
            if(view != null){
                int hasBeenReadColumnIndex = getCursor().getColumnIndex(
                        RssContentProvider.HAS_BEEN_READ);
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
