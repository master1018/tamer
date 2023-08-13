public class MyRssReader2 extends Activity{
    private ArrayList<RssItem> mFeeds = null;
    ListView mRssList = null;
    private Logger mLogger = Logger.getLogger("com.example.codelab.rssexample");
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen2);
        mFeeds = initializeList();
        mLogger.info("MyRssReader.onCreate-1  mFeeds value:" + mFeeds.size());
        mRssList = (ListView)findViewById(R.id.rssListView);
        if(mRssList == null){
            mLogger.warning("MyRssReader.onCreate-2 -- Couldn't instantiate a ListView!");
        }
        RssDataAdapter<RssItem> adap = new RssDataAdapter<RssItem>(this, R.layout.add_item, mFeeds);
        if(adap == null){
            mLogger.warning("MyRssReader.onCreate-3 -- Couldn't instantiate RssDataAdapter!");
        }
        if(mFeeds == null){
            mLogger.warning("MyRssReader.onCreate-4 -- Couldn't instantiate a ListView!");
        }
        mRssList.setAdapter(adap);   
        mLogger.info("MyRssReader.onCreate-5 -- Loading preferences.");
        if(savedInstanceState != null && savedInstanceState.containsKey("lastIndexItem"))
        {
            Integer selectedItem = savedInstanceState.getInteger("lastIndexItem");
            if(selectedItem >= 0 && selectedItem < mRssList.getChildCount()){
                mRssList.setSelection(savedInstanceState.getInteger("lastIndexItem"));
            }
            mLogger.info("MyRssReader.onCreate-6 -- Last selected item:" + selectedItem);
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
        menu.add(0, 0, "Start RSS Service", null);
        menu.add(0, 1, "Stop RSS Service", null);
        menu.add(0, 2, "Add New Feed", null);
        menu.add(0, 3, "Delete Feed", null);
        menu.add(0, 4, "Update All Feeds", null);
        return true;
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(Menu.Item item){
        switch (item.getId()){
            case 0:
              showAlert(null, "You clicked 'start'!", "ok", null, false, null);
              break;
            case 1:
              showAlert(null, "You clicked stop!", "ok", null, false, null);
              break;
            case 2:
                showAlert(null, "You clicked 'Add'!", "ok", null, false, null);
                break;
            case 3:
                showAlert(null, "You clicked 'Delete'!", "ok", null, false, null);
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
    private class RssDataAdapter<T> extends ArrayAdapter<T> {
        public RssDataAdapter(Context context, int resource, List objects) {
            super(context, resource, objects);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            TextView view = null;
            RssItem item = (RssItem)this.getItem(position);
            if(item != null)
            {
                view = new TextView(parent.getContext());
                view.setText(item.toString());
                if(! item.hasBeenRead){
                    Typeface type = view.getTypeface();
                    view.setTypeface(Typeface.create(type, Typeface.BOLD_ITALIC));
                }
            }
            return view;    
        }
     }
    private ArrayList<RssItem> initializeList(){
        ArrayList<RssItem> list = new ArrayList<RssItem>();
        list.add(new RssItem("http:
        list.add(new RssItem("http:
        list.add(new RssItem("http:
        list.add(new RssItem("http:
        return list;
    }
}
