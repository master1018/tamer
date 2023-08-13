public class MyRssReader3 extends Activity{
    private ArrayList<RssItem> mFeeds;
    ListView mRssList;
    ArrayAdapter mAdap;
    private static final int ADD_ELEMENT_REQUEST = 1;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen2);
        mFeeds = initializeList();
        mRssList = (ListView)findViewById(R.id.rssListView);
        mAdap = new RssDataAdapter<RssItem>(this, R.layout.list_element, mFeeds);
        mRssList.setAdapter(mAdap);   
        if(savedInstanceState != null && savedInstanceState.containsKey("lastIndexItem"))
            mRssList.setSelection(savedInstanceState.getInteger("lastIndexItem"));
    }
    @Override
    protected void onSaveInstanceState(Bundle outState){
        int index = mRssList.getSelectedItemIndex();
        if(index > -1){
            outState.putInteger("lastIndexItem", index);     
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
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
                    Object selectedItem = mRssList.getSelectedItem();
                    mAdap.removeObject(mRssList.getSelectedItem());
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
                    RssItem newIt = new RssItem(
                            data.getStringExtra("url").toString(), 
                            data.getStringExtra("title").toString());
                    mAdap.addObject(newIt);
                    mRssList.setSelection(mRssList.getCount() - 1);
                break;
                default:
                    break;
            }
        }
    }
    private class RssDataAdapter<T> extends ArrayAdapter<T> {
        public RssDataAdapter(Context context, int resource, List objects) {
            super(context, resource, objects);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            RssItem item = (RssItem)this.getItem(position);
            TextView view = new TextView(parent.getContext());
            view.setText(item.toString());
            if(! item.hasBeenRead){
                Typeface type = view.getTypeface();
                view.setTypeface(Typeface.create(type, Typeface.BOLD_ITALIC));
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
