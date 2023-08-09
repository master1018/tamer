public class ListScrollListener extends ListActivity implements AbsListView.OnScrollListener {
    Handler mHandler = new Handler();
    TextView mText;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.list_scroll_listener);
        String values[] = new String[1000];
        int i=0;
        for(i=0; i<1000; i++) {
            values[i] = ((Integer)i).toString();
        }
        mText = (TextView) findViewById(R.id.text);
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values));
        getListView().setOnScrollListener(this);
    }
    public void onItemSelected(AdapterView parent, View v, int position, long id) {
        mText.setText("Position " + position);
    }
    public void onNothingSelected(AdapterView parent) {
        mText.setText("Nothing");
    }
    public void onScroll(AbsListView view, int firstCell, int cellCount, int itemCount) {
        int last = firstCell + cellCount - 1;
        mText.setText("Showing " + firstCell + "-" + last + "/" + itemCount);
    }
    public void onScrollStateChanged(AbsListView view, int scrollState) {        
    }
}
